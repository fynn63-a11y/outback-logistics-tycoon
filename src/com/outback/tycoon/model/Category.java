package com.outback.tycoon.model;

public enum Category {
    ELECTRONICS("Consumer Electronics", 0.45, 0.06, 1.0, 1.0, WeightClass.MEDIUM),
    APPAREL("Apparel & Textil", 0.30, 0.25, 1.2, 0.8, WeightClass.LIGHT),
    FMCG("FMCG", 0.12, 0.10, 2.2, 0.6, WeightClass.LIGHT),
    BULKY("Bulky Goods / Haushaltswaren", 0.35, 0.08, 0.4, 3.0, WeightClass.BULKY);

    public final String label;
    public final double marginHint;
    public final double returnRate;
    public final double volumeFactor;
    public final double spaceFactor;
    public final WeightClass weightClass;

    Category(String label, double marginHint, double returnRate, double volumeFactor,
             double spaceFactor, WeightClass weightClass) {
        this.label = label;
        this.marginHint = marginHint;
        this.returnRate = returnRate;
        this.volumeFactor = volumeFactor;
        this.spaceFactor = spaceFactor;
        this.weightClass = weightClass;
    }
}
