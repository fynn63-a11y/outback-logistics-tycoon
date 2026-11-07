package com.outback.tycoon.model;

import java.util.EnumMap;
import java.util.Map;

public class Carrier implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public final String name;
    public final Map<WeightClass, Double> baseRates = new EnumMap<>(WeightClass.class);
    public final double fixedFeePerMonth;
    public int contractLevel = 0;

    public Carrier(String name, double fixedFeePerMonth) {
        this.name = name;
        this.fixedFeePerMonth = fixedFeePerMonth;
        for (WeightClass wc : WeightClass.values()) baseRates.put(wc, wc.baseRate);
    }

    public double rate(WeightClass wc) {
        double discount = 0.08 * contractLevel;
        return baseRates.get(wc) * (1.0 - discount);
    }

    public double negotiateCost() {
        return 20_000.0 * (contractLevel + 1);
    }

    public boolean canNegotiate() {
        return contractLevel < 3;
    }
}
