package com.outback.tycoon.model;

public enum Category {
    ELECTRONICS("Elektronik (Handys, Kopfhörer, PCs)", 0.45, 0.06, 1.0, 1.0, WeightClass.MEDIUM, false, false),
    APPAREL_BASIC("Textilien günstig", 0.30, 0.25, 1.2, 0.8, WeightClass.LIGHT, false, false),
    APPAREL_LUXURY("Textilien Premium", 0.55, 0.12, 0.4, 1.0, WeightClass.LIGHT, false, false),
    SNEAKERS("Sportschuhe", 0.40, 0.18, 0.8, 0.9, WeightClass.MEDIUM, false, false),
    CAR_PARTS("Autoteile", 0.35, 0.04, 0.3, 2.5, WeightClass.HEAVY, false, false),
    FMCG("FMCG", 0.12, 0.10, 2.2, 0.6, WeightClass.LIGHT, false, false),
    COLD_CHAIN("Kühlkette (Lebensmittel)", 0.25, 0.15, 1.5, 0.8, WeightClass.LIGHT, true, false),
    HAZMAT("Gefahrgut / Chemie", 0.40, 0.08, 0.5, 0.8, WeightClass.MEDIUM, false, true),
    BULKY("Bulky / Haushaltswaren", 0.35, 0.08, 0.4, 3.0, WeightClass.BULKY, false, false);

    public final String label;
    public final double marginHint;
    public final double returnRate;
    public final double volumeFactor;
    public final double spaceFactor;
    public final WeightClass weightClass;
    public final boolean coldChain;
    public final boolean hazardous;

    Category(String label, double marginHint, double returnRate, double volumeFactor,
             double spaceFactor, WeightClass weightClass, boolean coldChain, boolean hazardous) {
        this.label = label;
        this.marginHint = marginHint;
        this.returnRate = returnRate;
        this.volumeFactor = volumeFactor;
        this.spaceFactor = spaceFactor;
        this.weightClass = weightClass;
        this.coldChain = coldChain;
        this.hazardous = hazardous;
    }
}
