package com.outback.tycoon.model;

import java.awt.Color;

public class RivalCompany implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public final String name;
    public double marketShare;
    public final Color color;
    public int sites = 0;
    public final java.util.List<String> siteCities = new java.util.ArrayList<>();
    public double aggression = 1.0;
    public boolean insolvent = false;
    public final java.util.Set<Category> categories = java.util.EnumSet.noneOf(Category.class);

    public RivalCompany(String name, double marketShare, Color color) {
        this.name = name;
        this.marketShare = marketShare;
        this.color = color;
    }
}
