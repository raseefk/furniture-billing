package com.furnitureshop.model;

public enum InvoiceStatus {
    QUOTATION("Quotation", "bg-yellow-100 text-yellow-800"),
    FINAL_BILL("Final Bill", "bg-green-100 text-green-800"),
    CANCELLED("Cancelled", "bg-red-100 text-red-800");

    private final String displayName;
    private final String badgeClass;

    InvoiceStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
