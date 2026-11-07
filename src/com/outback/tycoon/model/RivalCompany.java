package com.outback.tycoon.model;

import java.awt.Color;

public class RivalCompany {
    public final String name;
    public double marketShare;
    public final Color color;

    public RivalCompany(String name, double marketShare, Color color) {
        this.name = name;
        this.marketShare = marketShare;
        this.color = color;
    }
}
