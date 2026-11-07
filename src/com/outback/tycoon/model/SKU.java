package com.outback.tycoon.model;

public class SKU implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public final String id;
    public final String name;
    public final Category category;
    public final double baseCost;
    public double price;
    public Brand brand;

    public SKU(String id, String name, Category category, double cost, Brand brand) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.baseCost = cost;
        this.price = Math.round(cost * (1.0 + category.marginHint) * 100.0) / 100.0;
        this.brand = brand;
    }

    public double margin() {
        if (price <= 0) return 0;
        return (price - baseCost) / price;
    }
}
