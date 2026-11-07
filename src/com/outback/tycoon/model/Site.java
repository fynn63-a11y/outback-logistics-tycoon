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
    public int buildDaysLeft = 0;
    public int blockedDays = 0;
    public String blockedReason = "";
    public ServiceLevel serviceLevel = ServiceLevel.STANDARD;
    public final Map<String, Integer> stock = new HashMap<>();
    public final MachineType[][] grid = new MachineType[GRID][GRID];
    public final int[][] upgrades = new int[GRID][GRID];

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

    public double serviceUpgradeCost() {
        return 100_000.0 * (serviceLevel.ordinal() + 1);
    }

    public int machineScore() {
        int s = 0;
        for (int x = 0; x < GRID; x++)
            for (int y = 0; y < GRID; y++)
                if (grid[x][y] != null) s += grid[x][y].score * (1 + upgrades[x][y] / 2);
        return s;
    }

    public int stageScore(MachineType.Stage st) {
        int s = 0;
        for (int x = 0; x < GRID; x++)
            for (int y = 0; y < GRID; y++)
                if (grid[x][y] != null && grid[x][y].stage == st) s += grid[x][y].score * (1 + upgrades[x][y] / 2);
        return s;
    }

    public double gridEfficiency(Game g) {
        int required = 25 * stars;
        double eff = required <= 0 ? 1.0 : Math.min(1.0, machineScore() / (double) required);
        if (g.researched.contains(Tech.SMART_SLOTTING)) eff = Math.min(1.0, eff * 1.25);
        return eff;
    }

    private static final MachineType.Stage[] CHAIN = {
            MachineType.Stage.DELIVERY, MachineType.Stage.UNLOAD, MachineType.Stage.BOOKING,
            MachineType.Stage.INBOUND, MachineType.Stage.STOW, MachineType.Stage.PICK,
            MachineType.Stage.PACK, MachineType.Stage.LOAD, MachineType.Stage.SHIP
    };

    public boolean chainComplete() {
        for (MachineType.Stage st : CHAIN) if (stageScore(st) <= 0) return false;
        return true;
    }

    public MachineType.Stage weakestStage() {
        MachineType.Stage weakest = CHAIN[0];
        int best = Integer.MAX_VALUE;
        for (MachineType.Stage st : CHAIN) {
            int sc = stageScore(st);
            if (sc < best) {
                best = sc;
                weakest = st;
            }
        }
        return weakest;
    }

    public double chainBottleneck(int starsRef) {
        if (!chainComplete()) return 0.3;
        int req = 10 * Math.max(1, starsRef);
        int min = Integer.MAX_VALUE;
        for (MachineType.Stage st : CHAIN) min = Math.min(min, stageScore(st));
        return Math.min(1.0, min / (double) req);
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

    public boolean isOperational() {
        return buildDaysLeft <= 0 && blockedDays <= 0;
    }

    public double throughput(Game g) {
        if (!isOperational()) return 0;
        double t = type.baseThroughput * level * stars * (0.3 + 0.7 * gridEfficiency(g));
        if (chainComplete()) {
            t *= 1.5;
            t *= 0.6 + 0.4 * chainBottleneck(stars);
        }
        if (g.researched.contains(Tech.KIVA_ROBOTS)) t *= 1.5;
        if (g.researched.contains(Tech.HIGH_SPEED_CONVEYORS)) t *= 1.3;
        if (g.tempsDecember && g.month() == 12 && g.strikeDaysLeft <= 0) t *= 1.5;
        t *= 1.0 + 0.05 * (g.staffSkill - 1);
        t *= 1.0 + g.techBonus(Tech.Bonus.THROUGHPUT);
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
