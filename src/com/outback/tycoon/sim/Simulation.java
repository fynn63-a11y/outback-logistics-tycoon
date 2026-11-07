package com.outback.tycoon.sim;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Simulation {
    public static final double[] SEASONALITY = {
            0.90, 0.85, 0.95, 1.00, 1.00, 1.05, 1.05, 1.00, 0.95, 1.00, 1.30, 2.00
    };
    private static final double[] DEMAND_PER_MILLION = {8, 25, 60, 3};
    private static final double FULFILL_COST_PER_UNIT = 1.20;
    private static final double FBA_FEE_PER_ORDER = 3.50;
    private static final double WAGE_PER_PERSON_PER_DAY = 90_000.0 / 365.0;
    private static final double TEMP_WAGE_DECEMBER = 200.0;
    private static final int TEMP_COUNT_DECEMBER = 40;

    private final Random rnd = new Random();

    public void dailyTick(Game g) {
        double revenue = 0;
        double costs = 0;

        if (g.primeDayLeft > 0) {
            g.primeDayLeft--;
        } else {
            g.primeDayIn--;
            if (g.primeDayIn <= 0) {
                g.primeDayLeft = 3;
                g.primeDayIn = 90;
                g.alert("PRIME DAY! Traffic x3.5 fuer 3 Tage.");
            }
        }

        boolean strike = g.strikeDaysLeft > 0;
        if (strike) g.strikeDaysLeft--;

        double totalDemand = 0;
        for (City c : g.cities)
            for (SKU sku : g.catalog) totalDemand += demand(g, c, sku);

        boolean serverCrash = g.isPrimeDay() && totalDemand > g.serverCapacity();
        if (serverCrash) {
            g.alert("SERVER-CRASH beim Prime Day! AWS-Kapazitaet ueberschritten, Bestellungen fallen aus.");
            g.reputation = Math.max(0, g.reputation - 3);
        }

        for (City c : g.cities) {
            for (SKU sku : g.catalog) {
                double demand = demand(g, c, sku);
                if (demand <= 0) continue;
                if (serverCrash) demand *= 0.05;
                if (strike) demand *= 0.10;

                int fulfilled = fulfill(g, c, sku, demand);
                revenue += fulfilled * sku.price;
                costs += fulfilled * (shippingCost(g, sku) + FULFILL_COST_PER_UNIT);
                costs += fulfilled * sku.category.returnRate * sku.baseCost * 0.5;
            }
            double fba = fbaOrders(g, c);
            revenue += fba * FBA_FEE_PER_ORDER;
            costs += fba * 0.40;
        }

        for (Site s : g.sites) {
            costs += s.type.baseCost * 0.002 * s.level;
            if (s.type == SiteType.DATA_CENTER) costs += 200;
        }
        costs += (g.permanentStaff + g.developers) * WAGE_PER_PERSON_PER_DAY;
        if (g.tempsDecember && g.month() == 12 && !strike) costs += TEMP_COUNT_DECEMBER * TEMP_WAGE_DECEMBER;
        for (Carrier c : g.carriers) costs += c.fixedFeePerMonth / 30.0;
        if (g.insured && !g.sandboxMode) costs += 8_000.0 / 30.0;
        if (g.cash < 0 && !g.sandboxMode) costs += -g.cash * 0.12 / 365.0;

        g.cash += revenue - costs;
        g.lastDailyRevenue = revenue;
        g.lastDailyCosts = costs;

        arriveShipments(g);
        if (g.autopilotSourcing) autopilotSourcing(g);
        if (g.autopilotRebalance) autopilotRebalance(g);
        if (g.autopilotPricing) autopilotPricing(g);

        g.researchPoints += g.developers * 2.0;
        g.morale += (70 - g.morale) * 0.01;
        updateRivals(g);
        randomEvents(g);
        disasters(g);
        java.util.Iterator<java.util.Map.Entry<String, Integer>> bit = g.blockedCities.entrySet().iterator();
        while (bit.hasNext()) {
            java.util.Map.Entry<String, Integer> e = bit.next();
            int left = e.getValue() - 1;
            if (left <= 0) bit.remove(); else e.setValue(left);
        }

        if (!g.sandboxMode && g.cash < -g.debtLimit) {
            g.gameOver = true;
            g.gameOverReason = "Bankrott! Kapital unter der Schulden-Grenze von -" +
                    String.format("%,.0f", g.debtLimit) + " AUD und keine Liquiditaet mehr. Raus aus dem Markt.";
            g.alert("GAME OVER: " + g.gameOverReason);
        }
    }

    public double demand(Game g, City c, SKU sku) {
        double base = DEMAND_PER_MILLION[sku.category.ordinal()] * c.popMillions();
        double primeBoost = 1.0 + c.primeQuote * 0.5;
        double season = g.seasonality();
        double primeDay = g.isPrimeDay() ? 3.5 : 1.0;
        double repFactor = 0.5 + g.reputation / 100.0;
        double brandFactor = sku.brand != null ? sku.brand.demandBoost() : 1.0;
        double rivalFactor = Math.max(0.35, 1.0 - g.totalRivalShare());
        if (g.blockedCities.containsKey(c.name)) return 0;
        return base * primeBoost * season * primeDay * repFactor * brandFactor * rivalFactor * sku.category.volumeFactor;
    }

    private void updateRivals(Game g) {
        if (g.rivals.size() < 5 && g.dayIndex > 0 && g.dayIndex % 730 == 0) {
            String[] names = {"Kimberley Cargo", "Red Centre Express", "Tasman Logistics"};
            RivalCompany r = new RivalCompany(names[(int) (g.dayIndex / 730 - 1) % names.length], 0.05,
                    new java.awt.Color(180, 100, 220));
            g.rivals.add(r);
            g.alert("Neuer Wettbewerber am Markt: " + r.name + "! (insgesamt " + g.rivals.size() + " Konkurrenten)");
        }
        double playerMomentum = Math.min(0.02, g.lastDailyRevenue / 500_000.0);
        for (RivalCompany r : g.rivals) {
            r.marketShare += (rnd.nextDouble() - 0.45) * 0.002 + 0.0005;
            r.marketShare -= playerMomentum;
            r.marketShare = Math.max(0.02, Math.min(0.35, r.marketShare));
        }
        double top = 0;
        RivalCompany leader = null;
        for (RivalCompany r : g.rivals) if (r.marketShare > top) { top = r.marketShare; leader = r; }
        if (leader != null && top > 0.25 && rnd.nextDouble() < 0.02) {
            g.alert("Preiskampf! " + leader.name + " unterbietet die Marktpreise (Marktanteil " + (int) (top * 100) + "%).");
        }
    }

    private int fulfill(Game g, City c, SKU sku, double demand) {
        List<Site> sites = g.sitesIn(c.name);
        sites.sort((a, b) -> Double.compare(b.throughput(g), a.throughput(g)));
        double remaining = demand;
        int fulfilled = 0;
        for (Site s : sites) {
            if (remaining <= 0) break;
            int have = s.stockOf(sku.id);
            if (have <= 0) continue;
            double cap = s.throughput(g) / Math.max(1, g.catalog.size());
            int take = (int) Math.min(Math.min(have, remaining), cap);
            if (take > 0) {
                s.removeStock(sku.id, take);
                fulfilled += take;
                remaining -= take;
            }
        }
        return fulfilled;
    }

    private double shippingCost(Game g, SKU sku) {
        Carrier main = g.carriers.get(0);
        double normal = main.rate(sku.category.weightClass);
        double express = main.rate(WeightClass.EXPRESS);
        double blended = g.isPrimeDay() ? 0.7 * normal + 0.3 * express : normal;
        if (g.researched.contains(Tech.OWN_FLEET)) blended *= 0.6;
        return blended;
    }

    private double fbaOrders(Game g, City c) {
        double localThroughput = 0;
        for (Site s : g.sitesIn(c.name)) localThroughput += s.throughput(g);
        return localThroughput * 0.02 * (0.5 + g.reputation / 100.0);
    }

    private void arriveShipments(Game g) {
        List<Shipment> delayed = new java.util.ArrayList<>();
        java.util.Iterator<Shipment> it = g.shipments.iterator();
        while (it.hasNext()) {
            Shipment sh = it.next();
            sh.daysLeft--;
            if (sh.daysLeft > 0) continue;
            it.remove();
            List<Site> candidates = new java.util.ArrayList<>();
            for (Site s : g.sitesIn(sh.destCity))
                if (s.type != SiteType.DATA_CENTER && s.freePallets() > 0) candidates.add(s);
            candidates.sort((a, b) -> Integer.compare(b.freePallets(), a.freePallets()));
            if (candidates.isEmpty()) {
                g.alert("Kein Paletten-Slot frei in " + sh.destCity + " - Container liegt im Hafen (Congestion)!");
                sh.daysLeft = 1;
                delayed.add(sh);
                continue;
            }
            Site dest = candidates.get(0);
            for (Map.Entry<String, Integer> e : sh.goods.entrySet()) dest.addStock(e.getKey(), e.getValue());
            g.alert("Seefracht aus " + sh.origin + " in " + sh.destCity + " eingetroffen (" + sh.totalUnits() + " Einheiten).");
        }
        g.shipments.addAll(delayed);
    }

    private void autopilotSourcing(Game g) {
        for (String port : new String[]{"Sydney", "Melbourne"}) {
            List<Site> portSites = g.sitesIn(port);
            if (portSites.isEmpty()) continue;
            Site hub = portSites.get(0);
            for (SKU sku : g.catalog) {
                boolean alreadyOrdered = false;
                for (Shipment sh : g.shipments)
                    if (sh.goods.containsKey(sku.id)) { alreadyOrdered = true; break; }
                if (alreadyOrdered) continue;
                City c = g.cityByName(port);
                double daily = demand(g, c, sku);
                if (hub.stockOf(sku.id) < daily * 14 && hub.freePallets() > 2) {
                    Map<String, Integer> goods = new HashMap<>();
                    goods.put(sku.id, (int) Math.ceil(daily * 30));
                    double cost = 9_000 + daily * 30 * sku.baseCost * 0.95;
                    g.orderContainer(port, goods, cost);
                    g.alert("Autopilot: China-Import bestellt fuer " + sku.name + " -> " + port + ".");
                }
            }
        }
    }

    private void autopilotRebalance(Game g) {
        int moves = 0;
        for (SKU sku : g.catalog) {
            for (Site a : g.sites) {
                if (a.type == SiteType.DATA_CENTER) continue;
                for (Site b : g.sites) {
                    if (a == b || a.type == SiteType.DATA_CENTER || b.type == SiteType.DATA_CENTER) continue;
                    if (moves >= 10) return;
                    int sa = a.stockOf(sku.id);
                    int sb = b.stockOf(sku.id);
                    if (sa - sb > 50 && b.freePallets() > 0) {
                        int move = (sa - sb) / 4;
                        a.removeStock(sku.id, move);
                        b.addStock(sku.id, move);
                        g.cash -= move * 0.5;
                        moves++;
                    }
                }
            }
        }
    }

    private void autopilotPricing(Game g) {
        for (SKU sku : g.catalog) {
            double m = sku.margin();
            if (m < 0.15 && sku.price < sku.baseCost * 1.6) sku.price *= 1.02;
            else if (m > 0.45 && sku.price > sku.baseCost * 1.15) sku.price *= 0.995;
            sku.price = Math.round(sku.price * 100.0) / 100.0;
        }
    }

    private void disasters(Game g) {
        if (rnd.nextDouble() > 0.012) return;
        List<City> open = new java.util.ArrayList<>();
        for (City c : g.cities) if (g.isUnlocked(c) && !g.blockedCities.containsKey(c.name)) open.add(c);
        if (open.isEmpty()) return;
        City c = open.get(rnd.nextInt(open.size()));
        int days = 3 + rnd.nextInt(5);
        boolean bushfire = c.region.equals("Australien") && g.month() >= 11 || g.month() <= 2;
        String type = bushfire ? "Buschfeuer" : (rnd.nextBoolean() ? "Zyklon" : "Hochwasser");
        g.blockedCities.put(c.name, days);
        g.alert("KATASTROPHE: " + type + " in " + c.name + "! Stadt " + days + " Tage gesperrt.");
        if (g.insured && !g.sandboxMode) {
            g.cash += 50_000;
            g.alert("Versicherung zahlt 50.000 AUD Schadenersatz fuer " + c.name + ".");
        }
    }

    private void randomEvents(Game g) {
        if (g.morale < 30 && rnd.nextDouble() < 0.08 && g.strikeDaysLeft == 0) {
            g.strikeDaysLeft = 2 + rnd.nextInt(4);
            g.reputation = Math.max(0, g.reputation - 4);
            g.alert("WILDER STREIK! Arbeitsniederlegung fuer " + g.strikeDaysLeft + " Tage. PR-Krise in den Medien!");
        }
        if (!g.researched.contains(Tech.PATENT_PROTECTION) && rnd.nextDouble() < 0.05) {
            double stolen = Math.min(g.researchPoints, 20);
            g.researchPoints -= stolen;
            g.alert("INDUSTRIESPIONAGE! KI-Konkurrent hat " + (int) stolen + " Forschungspunkte gestohlen.");
        }
        if (rnd.nextDouble() < 0.01) {
            g.reputation = Math.min(100, g.reputation + 2);
            g.alert("Positive Presse: Effiziente Logistik in " + g.cities.get(rnd.nextInt(g.cities.size())).name + ".");
        }
    }
}
