package com.outback.tycoon.model;

public enum ServiceLevel {
    STANDARD("Standard (2-3 Tage)", 1.0, 0.0),
    NEXT_DAY("Next-Day", 1.15, 1.5),
    SAME_DAY("Same-Day garantiert", 1.35, 3.0);

    public final String label;
    public final double demandFactor;
    public final double surchargePerParcel;

    ServiceLevel(String label, double demandFactor, double surchargePerParcel) {
        this.label = label;
        this.demandFactor = demandFactor;
        this.surchargePerParcel = surchargePerParcel;
    }
}
