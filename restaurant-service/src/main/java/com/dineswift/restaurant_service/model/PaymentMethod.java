package com.dineswift.restaurant_service.model;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    UNKNOWN("Unknown"),
    CARD("Card"),
    CASH("Cash"),
    DIGITAL_WALLET("Digital Wallet"),
    BANK_TRANSFER("Bank Transfer"),
    GIFT_CARD("Gift Card"),
    LOYALTY_POINTS("Loyalty Points"),
    UPI("Upi"),
    NET_BANKING("NetBanking");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod getPaymentMethodByName(String name) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.getDisplayName().equalsIgnoreCase(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("No PaymentMethod with name " + name + " found.");
    }
}
