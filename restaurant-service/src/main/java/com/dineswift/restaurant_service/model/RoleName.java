package com.dineswift.restaurant_service.model;

public enum RoleName {
    ROLE_ADMIN("Admin"),
    ROLE_MANAGER("Manager"),
    ROLE_STAFF("Staff"),
    ROLE_CHEF("Chef"),
    ROLE_WAITER("Waiter"),
    ROLE_CASHIER("Cashier");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RoleName fromDisplayName(String displayName) {
        for (RoleName role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role display name: " + displayName);
    }

    public static RoleName fromName(String name) {
        try {
            return RoleName.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role name: " + name);
        }
    }
}
