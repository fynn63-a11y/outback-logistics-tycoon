package com.outback.tycoon.model;

import com.outback.tycoon.core.Game;

import java.util.HashMap;
import java.util.Map;

public class Site implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAX_GRID = 40;
    public static final int GRID = MAX_GRID;
    public int w = 25;
    public int h = 25;
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
    public final MachineType[][] grid = new MachineType[MAX_GRID][MAX_GRID];
    public final int[][] upgrades = new int[MAX_GRID][MAX_GRID];
    public final MachineType[][] grid2 = new MachineType[MAX_GRID][MAX_GRID];
    public final int[][] upgrades2 = new int[MAX_GRID][MAX_GRID];
    public boolean level2 = false;

    public Site(int id, SiteType type, String city) {
        this.id = id;
        this.type = type;
        this.city = city;
    }

    public int palletSlots() {
        double mezz = 1.0;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (grid[x][y] == MachineType.MEZZANINE) { mezz = 1.5; break; }
        return (int) (type.palletSlots * level * mezz);
    }

    public boolean hasMezzanine() {
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (grid[x][y] == MachineType.MEZZANINE) { level2 = true; return true; }
        return false;
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

    public double wear = 0;

    private double cellFactor(int x, int y) {
        MachineType m = grid[x][y];
        if (m == null) return 0;
        double f = 1.0;
        if (m == MachineType.CONVEYOR) {
            boolean linked = false;
            int[][] nb = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
            for (int[] n : nb)
                if (n[0] >= 0 && n[0] < w && n[1] >= 0 && n[1] < h && grid[n[0]][n[1]] == MachineType.CONVEYOR) linked = true;
            if (!linked) f *= 0.5;
        } else {
            int[][] nb = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
            for (int[] n : nb)
                if (n[0] >= 0 && n[0] < w && n[1] >= 0 && n[1] < h && grid[n[0]][n[1]] == m) { f *= 1.2; break; }
        }
        return f;
    }

    public int machineScore() {
        double s = 0;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (grid[x][y] != null) s += grid[x][y].score * (1 + upgrades[x][y] / 2) * cellFactor(x, y);
                if (level2 && grid2[x][y] != null) s += grid2[x][y].score * (1 + upgrades2[x][y] / 2);
        return (int) s;
    }

    public int stageScore(MachineType.Stage st) {
        double s = 0;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (grid[x][y] != null && grid[x][y].stage == st) s += grid[x][y].score * (1 + upgrades[x][y] / 2) * cellFactor(x, y);
                if (level2 && grid2[x][y] != null && grid2[x][y].stage == st) s += grid2[x][y].score * (1 + upgrades2[x][y] / 2);
        return (int) s;
    }

    public boolean hasEstop() {
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (grid[x][y] == MachineType.ESTOP) return true;
        return false;
    }

    public double flowPenalty() {
        java.util.Map<MachineType.Stage, Double> avgX = new java.util.HashMap<>();
        java.util.Map<MachineType.Stage, Integer> cnt = new java.util.HashMap<>();
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (grid[x][y] != null) {
                    avgX.merge(grid[x][y].stage, (double) x, Double::sum);
                    cnt.merge(grid[x][y].stage, 1, Integer::sum);
                }
        java.util.List<MachineType.Stage> order = java.util.Arrays.asList(CHAIN);
        int inversions = 0;
        for (int i = 0; i < order.size(); i++) {
            for (int j = i + 1; j < order.size(); j++) {
                MachineType.Stage a = order.get(i);
                MachineType.Stage b = order.get(j);
                if (avgX.containsKey(a) && avgX.containsKey(b)
                        && avgX.get(a) / cnt.get(a) > avgX.get(b) / cnt.get(b)) inversions++;
            }
        }
        return 1.0 - Math.min(0.5, inversions * 0.1);
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
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++) {
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
        t *= flowPenalty();
        t *= 1.0 - 0.3 * wear;
        if (g.powerShortage()) t *= 0.5;
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
