package com.outback.tycoon.model;

public enum SiteType {
    WAREHOUSE("Lager", 250_000, 200, 100),
    FC_HUB("Fulfillment-Center (FC Hub)", 600_000, 500, 250),
    DEPOT("Zwischenlager", 120_000, 150, 80),
    DATA_CENTER("Rechenzentrum (AWS)", 900_000, 0, 0);

    public final String label;
    public final double baseCost;
    public final int palletSlots;
    public final double baseThroughput;

    SiteType(String label, double baseCost, int palletSlots, double baseThroughput) {
        this.label = label;
        this.baseCost = baseCost;
        this.palletSlots = palletSlots;
        this.baseThroughput = baseThroughput;
    }
}
