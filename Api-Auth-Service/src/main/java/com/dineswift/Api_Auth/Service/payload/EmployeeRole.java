package com.dineswift.Api_Auth.Service.payload;

public enum EmployeeRole {
    ROLE_ADMIN("Admin"),
    ROLE_MANAGER("Manager"),
    ROLE_STAFF("Staff"),
    ROLE_CHEF("Chef"),
    ROLE_WAITER("Waiter"),
    ROLE_CASHIER("Cashier");

    private final String displayName;

    EmployeeRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EmployeeRole fromDisplayName(String displayName) {
        for (EmployeeRole role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role display name: " + displayName);
    }

    public static EmployeeRole fromName(String name) {
        try {
            return EmployeeRole.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role name: " + name);
        }
    }
}
