package com.outback.tycoon.model;

public enum SiteType {
    WAREHOUSE("Lager", 250_000, 200, 100, 10),
    FC_HUB("Fulfillment-Center (FC Hub)", 600_000, 500, 250, 20),
    DEPOT("Zwischenlager", 120_000, 150, 80, 6),
    VERTEILZENTRUM("Verteilzentrum", 1_200_000, 800, 400, 25),
    UMSCHLAGZENTRUM("Umschlagzentrum", 400_000, 250, 150, 12),
    CONTAINER_TERMINAL("Container-Terminal", 1_500_000, 600, 300, 30),
    DATA_CENTER("Rechenzentrum (WebServices)", 900_000, 0, 0, 18),
    AIR_HUB("Air-Hub (Flughafen)", 700_000, 300, 200, 16),
    HIGHLAGER("Hochregallager", 800_000, 1200, 150, 22),
    OUTBACK_GO("Outback-Go-Store (kassenlos)", 300_000, 0, 60, 8);

    public final String label;
    public final double baseCost;
    public final int palletSlots;
    public final double baseThroughput;
    public final int buildDays;

    SiteType(String label, double baseCost, int palletSlots, double baseThroughput, int buildDays) {
        this.label = label;
        this.baseCost = baseCost;
        this.palletSlots = palletSlots;
        this.baseThroughput = baseThroughput;
        this.buildDays = buildDays;
    }
}
