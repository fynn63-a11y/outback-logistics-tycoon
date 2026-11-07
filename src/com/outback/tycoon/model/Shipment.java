package com.outback.tycoon.model;

import java.util.HashMap;
import java.util.Map;

public class Shipment implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final int TRANSIT_DAYS = 21;

    public final int id;
    public final String origin;
    public final String destCity;
    public int daysLeft;
    public final Map<String, Integer> goods = new HashMap<>();
    public final double cost;

    public Shipment(int id, String origin, String destCity, Map<String, Integer> goods, double cost, int extraDays) {
        this.id = id;
        this.origin = origin;
        this.destCity = destCity;
        this.goods.putAll(goods);
        this.cost = cost;
        this.daysLeft = TRANSIT_DAYS + extraDays;
    }

    public int totalUnits() {
        int u = 0;
        for (int v : goods.values()) u += v;
        return u;
    }
}
