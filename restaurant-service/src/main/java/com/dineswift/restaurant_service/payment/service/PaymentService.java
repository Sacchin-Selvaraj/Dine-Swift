package com.dineswift.restaurant_service.payment.service;

import com.dineswift.restaurant_service.exception.BookingException;
import com.dineswift.restaurant_service.exception.PaymentException;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.payload.response.tableBooking.PaymentCreateResponse;
import com.dineswift.restaurant_service.payment.payload.request.PaymentDetails;
import com.dineswift.restaurant_service.payment.repository.PaymentRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TableBookingRepository tableBookingRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.api.secret}")
    private String secretKey;


    public PaymentCreateResponse initiatePayment(TableBooking newBooking,String paymentName, BigDecimal amount) {

        Payment newPayment = new Payment();
        newPayment.setPaymentName(paymentName);
        newPayment.setAmount(amount);
        newPayment.setPaymentStatus(PaymentStatus.CREATED);
        newPayment.setTableBooking(newBooking);

        log.info("Create orderId using Razorpay");
        String contactEmail = newBooking.getGuestInformation().getContactEmail();
        String createdOrderId = createRazorpayOrder(amount, "INR",contactEmail);

        newPayment.setOrderId(createdOrderId);
        Payment savedPayment = paymentRepository.save(newPayment);

        return PaymentCreateResponse.builder()
                .orderId(savedPayment.getOrderId())
                .amount(savedPayment.getAmount())
                .paymentName(paymentName)
                .description("Payment for table booking ID: " + newBooking.getTableBookingId())
                .email(contactEmail)
                .tableBookingId(newBooking.getTableBookingId())
                .bookingStatus(newBooking.getBookingStatus())
                .currency("INR")
                .build();
    }

    private String createRazorpayOrder(BigDecimal upfrontAmount, String currency, String contactEmail) {

        try {
            JSONObject paymentRequest = new JSONObject();
            int amountInPaise = upfrontAmount.multiply(new BigDecimal(100)).intValue();
            paymentRequest.put("amount", amountInPaise);
            paymentRequest.put("currency", currency);
            paymentRequest.put("receipt", contactEmail);
            paymentRequest.put("payment_capture", 1);

            Order createdOrder = razorpayClient.orders.create(paymentRequest);
            System.out.println(createdOrder.toString());
            String orderId = createdOrder.get("id");
            log.info("Order created in Razorpay with ID: {}", orderId);
            return orderId;
        } catch (RazorpayException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyPayment(PaymentDetails paymentDetails) {
        String generatedSignature = "";
        boolean isSignatureValid;
        com.razorpay.Payment paymentData = null;
        try {
            paymentData = razorpayClient.payments.fetch(paymentDetails.getPaymentId());

            String payload = paymentDetails.getOrderId() + '|' + paymentDetails.getPaymentId();

            String secretKey = getSecretKey();

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] signatureBytes = mac.doFinal(payload.getBytes());
            generatedSignature = bytesToHex(signatureBytes);


            isSignatureValid = constantTimeEquals(generatedSignature, paymentDetails.getSignature());

            if(!isSignatureValid){
                log.warn("Signature mismatch: generatedSignature={}, receivedSignature={}", generatedSignature, paymentDetails.getSignature());
                handleSignatureMismatch(paymentDetails);
                return false;
            }

        } catch (Exception e) {
            log.error("Error generating HMAC SHA256 signature :" + e.getMessage());
            handleSignatureMismatch(paymentDetails);
            return false;
        }
        String paymentStatus = paymentData.get("status");
        if (paymentStatus.equals("captured") || paymentStatus.equals("authorized")) {
            log.info("Payment signature verified successfully for paymentId: {}", paymentDetails.getPaymentId());
            handleSuccessfulPayment(paymentDetails,paymentData);
            return true;
        } else {
            log.warn("Payment signature verification failed for paymentId: {}", paymentDetails.getPaymentId());
            handleFailedPayment(paymentDetails,paymentData);
            return false;
        }

    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String getSecretKey() {

        log.info("Get the secret key from configuration");
        return secretKey;
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }

    private void handleSignatureMismatch(PaymentDetails paymentDetails) {
        log.info("Handling tampered payment for orderId: {}", paymentDetails.getOrderId());
        Payment failerPayment = paymentRepository.findByOrderId(paymentDetails.getOrderId())
                .orElseThrow(()-> new PaymentException("No payment found with orderId: " + paymentDetails.getOrderId()));
        failerPayment.setPaymentStatus(PaymentStatus.TAMPERED);
        failerPayment.setPaymentMethod(PaymentMethod.UNKNOWN);
        failerPayment.setTransactionId(paymentDetails.getPaymentId());

        log.info("Fetching failure reason from Razorpay for paymentId: {}", paymentDetails.getPaymentId());
        String failureReason = "Payment signature mismatch - possible tampering detected";
        failerPayment.setFailureReason(failureReason);

        paymentRepository.save(failerPayment);

    }

    private void handleSuccessfulPayment(PaymentDetails paymentDetails, com.razorpay.Payment paymentData) {
        log.info("Handling successful payment for orderId: {}", paymentDetails.getOrderId());
        Payment payment = paymentRepository.findByOrderId(paymentDetails.getOrderId())
                .orElseThrow(()-> new PaymentException("No payment found with orderId: " + paymentDetails.getOrderId()));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(paymentDetails.getPaymentId());

        log.info("Fetching payment method from paymentId: {}", paymentDetails.getPaymentId());
        PaymentMethod paymentMethod = getPaymentMethodFromId(paymentData);
        payment.setPaymentMethod(paymentMethod);

        TableBooking booking = payment.getTableBooking();
        if (!booking.getIsUpfrontPaid()) {
            booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
            booking.setIsUpfrontPaid(true);
        }
        else
            booking.setBookingStatus(BookingStatus.ORDER_COMPLETED);

        // remove the cart for the user as payment is completed

        paymentRepository.save(payment);

    }

    private void handleFailedPayment(PaymentDetails paymentDetails, com.razorpay.Payment paymentData) {
        log.info("Handling failed payment for orderId: {}", paymentDetails.getOrderId());
        Payment failerPayment = paymentRepository.findByOrderId(paymentDetails.getOrderId())
                .orElseThrow(()-> new PaymentException("No payment found with orderId: " + paymentDetails.getOrderId()));
        failerPayment.setPaymentStatus(PaymentStatus.FAILED);
        failerPayment.setPaymentMethod(getPaymentMethodFromId(paymentData));
        failerPayment.setTransactionId(paymentDetails.getPaymentId());

        log.info("Fetching failure reason from Razorpay for paymentId: {}", paymentDetails.getPaymentId());
        String failureReason = getPaymentFailureReason(paymentData);
        failerPayment.setFailureReason(failureReason);

        TableBooking failedBooking = failerPayment.getTableBooking();
        failedBooking.setBookingStatus(BookingStatus.PAYMENT_PENDING);

        paymentRepository.save(failerPayment);

    }

    private String getPaymentFailureReason(com.razorpay.Payment paymentData) {

        try {
            String failureReason = paymentData.get("error_description");
            return failureReason != null ? failureReason : "Unknown failure reason";
        } catch (IllegalArgumentException e) {
            log.error("Error fetching payment details from Razorpay: " + e.getMessage());
            return "Error fetching failure reason";
        }
    }


    private PaymentMethod getPaymentMethodFromId(com.razorpay.Payment paymentData) {

        try {
            String paymentMethod = paymentData.get("method");
            return PaymentMethod.getPaymentMethodByName(paymentMethod);
        } catch (RuntimeException e) {
            log.error("Error fetching payment details from Razorpay: " + e.getMessage());
            throw new PaymentException("Error fetching payment details from Razorpay");
        }
    }

    public PaymentCreateResponse generatePayNow(UUID tableBookingId) {
        log.info("Generating pay-now link for bookingId: {}", tableBookingId);
        TableBooking existingBooking = tableBookingRepository.findByIdAndIsActive(tableBookingId)
                .orElseThrow(() -> new PaymentException("Invalid table booking ID: " + tableBookingId));

        PaymentCreateResponse paymentResponse=null;
        if (!existingBooking.getIsUpfrontPaid()){
            log.info("Generating upfront payment link for bookingId: {}", tableBookingId);
            paymentResponse = initiatePayment(existingBooking,"Upfront Payment",existingBooking.getUpfrontAmount());
        }else if (!existingBooking.getIsPendingAmountPaid()) {
            log.info("Generating final payment link for bookingId: {}", tableBookingId);
            paymentResponse = initiatePayment(existingBooking,"Pending Payment",existingBooking.getPendingAmount());
        } else {
            throw new BookingException("All payments are already completed for booking ID: " + tableBookingId);
        }

        log.info("Pay-now link generated successfully for bookingId: {}", tableBookingId);
        return paymentResponse;
    }
}
