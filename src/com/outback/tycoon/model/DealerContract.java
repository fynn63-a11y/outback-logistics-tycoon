package com.outback.tycoon.model;

public class DealerContract implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public final String retailer;
    public final Category category;
    public final int unitsPerDay;
    public final double pricePerUnit;
    public final double penaltyPerMissedUnit;
    public int daysLeft;

    public DealerContract(String retailer, Category category, int unitsPerDay, double pricePerUnit) {
        this.retailer = retailer;
        this.category = category;
        this.unitsPerDay = unitsPerDay;
        this.pricePerUnit = pricePerUnit;
        this.penaltyPerMissedUnit = 5.0;
        this.daysLeft = 90;
    }
}
