package com.outback.tycoon.model;

public enum WeightClass {
    LIGHT("Klein", 0.8),
    MEDIUM("Mittel", 1.5),
    HEAVY("Schwer", 3.0),
    BULKY("Sperrig", 6.0),
    EXPRESS("Express", 12.0);

    public final String label;
    public final double baseRate;

    WeightClass(String label, double baseRate) {
        this.label = label;
        this.baseRate = baseRate;
    }
}
