package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.PaymentRefund;
import com.dineswift.restaurant_service.payment.payload.response.PaymentRefundDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final ModelMapper modelMapper;

    public PaymentRefundDto toDto(PaymentRefund paymentRefund){
        return modelMapper.map(paymentRefund, PaymentRefundDto.class);
    }
}
