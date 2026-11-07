package com.outback.tycoon.model;

public class Brand {
    public final String name;
    public final boolean playerOwned;
    public int marketingLevel = 0;

    public Brand(String name, boolean playerOwned) {
        this.name = name;
        this.playerOwned = playerOwned;
    }

    public double demandBoost() {
        if (!playerOwned) return 0.85;
        return 1.0 + 0.15 * marketingLevel;
    }
}
