package com.outback.tycoon.model;

import com.outback.tycoon.core.Game;

import java.util.HashMap;
import java.util.Map;

public class Site implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final int GRID = 20;
    private static final int UNITS_PER_PALLET = 100;

    public final int id;
    public final SiteType type;
    public final String city;
    public int level = 1;
    public int stars = 1;
    public final Map<String, Integer> stock = new HashMap<>();
    public final MachineType[][] grid = new MachineType[GRID][GRID];

    public Site(int id, SiteType type, String city) {
        this.id = id;
        this.type = type;
        this.city = city;
    }

    public int palletSlots() {
        return type.palletSlots * level;
    }

    public int palletUsed() {
        int units = 0;
        for (int v : stock.values()) units += v;
        return (units + UNITS_PER_PALLET - 1) / UNITS_PER_PALLET;
    }

    public int freePallets() {
        return Math.max(0, palletSlots() - palletUsed());
    }

    public double upgradeCost() {
        return type.baseCost * (1.0 + 0.8 * level);
    }

    public double starUpgradeCost() {
        return 50_000.0 * stars * level;
    }

    public int machineScore() {
        int s = 0;
        for (int x = 0; x < GRID; x++)
            for (int y = 0; y < GRID; y++)
                if (grid[x][y] != null) s += grid[x][y].score;
        return s;
    }

    public double gridEfficiency(Game g) {
        int required = 25 * stars;
        double eff = required <= 0 ? 1.0 : Math.min(1.0, machineScore() / (double) required);
        if (g.researched.contains(Tech.SMART_SLOTTING)) eff = Math.min(1.0, eff * 1.25);
        return eff;
    }

    public int stageScore(MachineType.Stage st) {
        int s = 0;
        for (int x = 0; x < GRID; x++)
            for (int y = 0; y < GRID; y++)
                if (grid[x][y] != null && grid[x][y].stage == st) s += grid[x][y].score;
        return s;
    }

    public boolean chainComplete() {
        return stageScore(MachineType.Stage.INBOUND) > 0
                && stageScore(MachineType.Stage.STORAGE) > 0
                && stageScore(MachineType.Stage.PICK) > 0
                && stageScore(MachineType.Stage.PACK) > 0
                && stageScore(MachineType.Stage.SHIP) > 0;
    }

    public double automationPercent() {
        int auto = 0;
        int manual = 0;
        for (int x = 0; x < GRID; x++)
            for (int y = 0; y < GRID; y++) {
                MachineType m = grid[x][y];
                if (m == null) continue;
                if (m.isAutomated()) auto += m.score; else manual += m.score;
            }
        int total = auto + manual;
        return total == 0 ? 0 : auto * 100.0 / total;
    }

    public double throughput(Game g) {
        double t = type.baseThroughput * level * stars * (0.3 + 0.7 * gridEfficiency(g));
        if (chainComplete()) t *= 1.5;
        if (g.researched.contains(Tech.KIVA_ROBOTS)) t *= 1.5;
        if (g.researched.contains(Tech.HIGH_SPEED_CONVEYORS)) t *= 1.3;
        if (g.tempsDecember && g.month() == 12 && g.strikeDaysLeft <= 0) t *= 1.5;
        t *= 1.0 + 0.05 * (g.staffSkill - 1);
        return t;
    }

    public int stockOf(String skuId) {
        return stock.getOrDefault(skuId, 0);
    }

    public void addStock(String skuId, int qty) {
        stock.merge(skuId, qty, Integer::sum);
    }

    public void removeStock(String skuId, int qty) {
        int left = stockOf(skuId) - qty;
        if (left <= 0) stock.remove(skuId); else stock.put(skuId, left);
    }

    public String describe() {
        return type.label + " #" + id + " (" + city + ") " + stars + "★";
    }
}
