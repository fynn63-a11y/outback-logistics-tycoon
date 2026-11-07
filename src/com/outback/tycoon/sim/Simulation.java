package com.outback.tycoon.sim;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Simulation implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final double[] SEASONALITY = {
            0.90, 0.85, 0.95, 1.00, 1.00, 1.05, 1.05, 1.00, 0.95, 1.00, 1.30, 2.00
    };
    private static final double[] DEMAND_PER_MILLION = {8, 25, 6, 10, 2, 60, 20, 4, 3};
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
                costs += fulfilled * (shippingCost(g, sku, c) + FULFILL_COST_PER_UNIT);
                double returns = fulfilled * sku.category.returnRate;
                g.lastDailyReturns += (int) returns;
                costs += returns * sku.baseCost * 0.3;
                if (sku.category.coldChain) costs += fulfilled * 0.8;
                if (sku.category.hazardous) costs += fulfilled * 0.5;
            }
            double fba = fbaOrders(g, c);
            if (g.researched.contains(Tech.AWS_CLOUD)) fba *= 1.2;
            revenue += fba * FBA_FEE_PER_ORDER;
            costs += fba * 0.40;
        }

        if (g.researched.contains(Tech.AWS_CLOUD)) {
            double web = 5_000 * (1 + g.networkThroughput() / 10_000.0);
            revenue += web;
        }

        double b2b = 0;
        for (Site s : g.sites) {
            if (s.isOperational() && s.type != SiteType.DATA_CENTER) b2b += s.freePallets() * 1.5;
        }
        if (!g.sites.isEmpty()) b2b += 500;
        g.lastB2B = b2b;
        revenue += b2b;

        for (DealerContract ct : new java.util.ArrayList<>(g.contracts)) {
            int stock = 0;
            for (Site s : g.sites)
                for (java.util.Map.Entry<String, Integer> e : s.stock.entrySet()) {
                    SKU sk = g.skuById(e.getKey());
                    if (sk != null && sk.category == ct.category) stock += e.getValue();
                }
            double rate = Math.min(1.0, stock / (double) Math.max(1, ct.unitsPerDay * 7));
            double sold = ct.unitsPerDay * rate * (g.isPrimeDay() ? 2.0 : 1.0) * g.seasonality();
            revenue += sold * ct.pricePerUnit;
            if (rate < 0.8) {
                double penalty = (0.8 - rate) * ct.unitsPerDay * ct.penaltyPerMissedUnit;
                costs += penalty;
            }
            ct.daysLeft--;
            if (ct.daysLeft <= 0) {
                g.contracts.remove(ct);
                g.alert("Vertrag mit " + ct.retailer + " ausgelaufen.");
            }
        }

        g.fxCny *= 1.0 + (rnd.nextDouble() - 0.5) * 0.02;
        g.fxUsd *= 1.0 + (rnd.nextDouble() - 0.5) * 0.02;
        g.fxCny = Math.max(0.15, Math.min(0.30, g.fxCny));
        g.fxUsd = Math.max(0.50, Math.min(0.85, g.fxUsd));

        g.quarterRevenue += revenue;
        g.quarterCosts += costs;
        g.quarterDay++;
        if (g.quarterDay >= 90) {
            double profit = g.quarterRevenue - g.quarterCosts;
            double tax = Math.max(0, profit) * 0.30;
            costs += tax;
            g.cash -= tax;
            double eps = profit / Game.SHARES;
            g.alert(String.format("QUARTALSBERICHT: Umsatz %,.0f AUD | Kosten %,.0f AUD | Gewinn %,.0f AUD | Steuer (30%%) %,.0f AUD | EPS %.2f AUD",
                    g.quarterRevenue, g.quarterCosts, profit, tax, eps));
            if (profit < 0) g.reputation = Math.max(0, g.reputation - 3);
            g.quarterRevenue = 0;
            g.quarterCosts = 0;
            g.quarterDay = 0;
        }

        for (Site s : g.sites) {
            if (s.buildDaysLeft > 0) {
                s.buildDaysLeft--;
                if (s.buildDaysLeft == 0) g.alert(s.describe() + " fertiggestellt und betriebsbereit!");
            }
            if (s.blockedDays > 0) s.blockedDays--;
        }

        for (Site s : g.sites) {
            double rent = s.type.baseCost * 0.002 * s.level;
            if (g.cityByName(s.city).region.equals("Nordamerika")) rent *= g.fxUsd / 0.65;
            costs += rent;
            if (s.type == SiteType.DATA_CENTER) costs += 200;
            boolean cold = false;
            boolean haz = false;
            for (String skuId : s.stock.keySet()) {
                SKU sk = g.skuById(skuId);
                if (sk != null && s.stock.get(skuId) > 0) {
                    if (sk.category.coldChain) cold = true;
                    if (sk.category.hazardous) haz = true;
                }
            }
            if (cold) costs += 80;
            if (haz) costs += 60;
        }
        costs += (g.permanentStaff + g.developers) * WAGE_PER_PERSON_PER_DAY;
        if (g.tempsDecember && g.month() == 12 && !strike) costs += TEMP_COUNT_DECEMBER * TEMP_WAGE_DECEMBER;
        for (Carrier c : g.carriers) costs += c.fixedFeePerMonth / 30.0;
        if (g.insured && !g.sandboxMode) costs += 8_000.0 / 30.0;
        if (g.cash < 0 && !g.sandboxMode) costs += -g.cash * 0.12 / 365.0;

        g.cash += revenue - costs;
        g.lastDailyRevenue = revenue;
        g.lastDailyCosts = costs;

        if (g.isPrimeDay() && !serverCrash) g.primeDaySurvived = true;

        if (g.ipoDone && !g.sandboxMode) {
            double profitFactor = Math.max(-0.05, Math.min(0.05, (revenue - costs) / 1_000_000.0));
            g.sharePrice *= 1.0 + profitFactor + (g.reputation - 50) / 2000.0 + (rnd.nextDouble() - 0.5) * 0.01;
            g.sharePrice = Math.max(1.0, g.sharePrice);
        }
        if (g.ownLastMile) g.reputation = Math.min(100, g.reputation + 0.05);

        int era = g.eraIndex();
        if (era != g.lastEraValue()) {
            if (g.lastEraValue() >= 0) g.alert("NEUE EPOCHE: " + g.eraName() + "! E-Commerce-Nachfrage steigt.");
            g.setLastEra(era);
        }
        if (!g.won && g.scenario.isWon(g)) {
            g.won = true;
            g.winMessage = "SIEG im Szenario '" + g.scenario.label + "'!";
            g.alert("SIEG! " + g.winMessage);
            com.outback.tycoon.audio.Sfx.coin();
        }
        g.histCash.add(g.cash);
        g.histRevenue.add(revenue);
        g.histRivalShare.add(g.totalRivalShare());
        if (g.histCash.size() > 500) {
            g.histCash.remove(0);
            g.histRevenue.remove(0);
            g.histRivalShare.remove(0);
        }

        arriveShipments(g);
        if (g.autopilotSourcing) autopilotSourcing(g);
        if (g.autopilotRebalance) autopilotRebalance(g);
        if (g.autopilotPricing) autopilotPricing(g);
        if (g.autopilotZones) g.autoAssignZones();

        g.researchPoints += g.developers * 2.0;
        g.morale += (70 - g.morale) * 0.01;
        updateRivals(g);
        randomEvents(g);
        disasters(g);
        harborStrike(g);
        siteCrises(g);
        tenderEvents(g);
        tutorialCheck(g);
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
        if (!sku.availableIn(g.month())) return 0;
        double base = DEMAND_PER_MILLION[sku.category.ordinal()] * c.popMillions();
        double primeBoost = 1.0 + c.primeQuote * 0.5;
        double season = g.seasonality();
        double primeDay = g.isPrimeDay() ? 3.5 : 1.0;
        double repFactor = 0.5 + g.reputation / 100.0;
        double brandFactor = sku.brand != null ? sku.brand.demandBoost() : 1.0;
        double rivalFactor = Math.max(0.35, 1.0 - g.totalRivalShare());
        if (g.blockedCities.containsKey(c.name)) return 0;
        double eraFactor = g.eraDemandMultiplier();
        double webFactor = g.researched.contains(Tech.AWS_CLOUD) ? 1.15 : 1.0;
        double lastMileFactor = g.ownLastMile ? 1.10 : 1.0;
        double vanFactor = 1.0 + 0.03 * g.vans.getOrDefault(c.name, 0);
        Site server = g.servingSite(c.name);
        double serviceFactor = server != null ? server.serviceLevel.demandFactor : 0.4;
        return base * primeBoost * season * primeDay * repFactor * brandFactor * rivalFactor
                * sku.category.volumeFactor * eraFactor * webFactor * lastMileFactor * vanFactor * serviceFactor;
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
        List<Site> sites = new java.util.ArrayList<>(g.sitesIn(c.name));
        Site server = g.servingSite(c.name);
        if (server != null && sites.contains(server)) {
            sites.remove(server);
            sites.add(0, server);
        }
        sites.sort((a, b) -> Double.compare(g.siteThroughput(b), g.siteThroughput(a)));
        if (server != null && sites.contains(server) && !sites.get(0).equals(server)) {
            sites.remove(server);
            sites.add(0, server);
        }
        double remaining = demand;
        int fulfilled = 0;
        for (Site s : sites) {
            if (remaining <= 0) break;
            int have = s.stockOf(sku.id);
            if (have <= 0) continue;
            double cap = g.siteThroughput(s) / Math.max(1, g.catalog.size());
            int take = (int) Math.min(Math.min(have, remaining), cap);
            if (take > 0) {
                s.removeStock(sku.id, take);
                fulfilled += take;
                remaining -= take;
            }
        }
        return fulfilled;
    }

    private double shippingCost(Game g, SKU sku, City c) {
        Carrier main = g.carriers.get(0);
        double normal = main.rate(sku.category.weightClass);
        double express = main.rate(WeightClass.EXPRESS);
        double blended = g.isPrimeDay() ? 0.7 * normal + 0.3 * express : normal;
        double truckDiscount = Math.min(0.30, 0.05 * g.trucks.getOrDefault(c.name, 0));
        blended *= (1.0 - truckDiscount);
        Site server = g.servingSite(c.name);
        double dist = server != null ? g.distanceKm(g.cityByName(server.city), c) : 1_500;
        blended *= 1.0 + dist / 5_000.0;
        if (server != null && server.serviceLevel == com.outback.tycoon.model.ServiceLevel.SAME_DAY)
            blended += com.outback.tycoon.model.ServiceLevel.SAME_DAY.surchargePerParcel;
        blended *= 1.0 + 0.2 * sku.category.spaceFactor;
        if (sku.category.hazardous) blended += 0.5;
        if (g.ownLastMile) blended *= 1.25;
        if (g.researched.contains(Tech.OWN_FLEET)) blended *= 0.6;
        return blended;
    }

    private double fbaOrders(Game g, City c) {
        double localThroughput = 0;
        for (Site s : g.sitesIn(c.name)) localThroughput += g.siteThroughput(s);
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

    private void harborStrike(Game g) {
        if (rnd.nextDouble() > 0.005) return;
        String port = rnd.nextBoolean() ? "Sydney" : "Melbourne";
        int days = 3 + rnd.nextInt(3);
        g.blockedCities.put(port, days);
        for (Shipment sh : g.shipments)
            if (sh.destCity.equals(port)) sh.daysLeft += 5;
        g.alert("HAFEN-STREIK in " + port + "! Stadt " + days + " Tage gesperrt, alle Seefrachten +5 Tage.");
        if (g.insured && !g.sandboxMode) {
            g.cash += 40_000;
            g.alert("Versicherung zahlt 40.000 AUD Betriebsunterbrechung fuer " + port + ".");
        }
    }

    private void siteCrises(Game g) {
        if (rnd.nextDouble() > 0.008) return;
        List<Site> candidates = new java.util.ArrayList<>();
        for (Site s : g.sites) if (s.isOperational() && s.machineScore() > 0) candidates.add(s);
        if (candidates.isEmpty()) return;
        Site s = candidates.get(rnd.nextInt(candidates.size()));
        s.blockedDays = 2 + rnd.nextInt(4);
        String type = rnd.nextBoolean() ? "LAGERBRAND" : (rnd.nextBoolean() ? "DIEBSTAHL" : "IT-AUSFALL");
        s.blockedReason = type;
        g.alert("STANDORT-KRISE: " + type + " bei " + s.describe() + "! " + s.blockedDays + " Tage außer Betrieb.");
        if (g.insured && !g.sandboxMode) {
            g.cash += 30_000;
            g.alert("Versicherung zahlt 30.000 AUD fuer den Schaden bei " + s.city + ".");
        }
    }

    private void tenderEvents(Game g) {
        if (rnd.nextDouble() < 0.04) {
            String[] retailers = {"GreenMart", "FreshWay", "VoltCity", "MotorHub", "HomeStyle", "SportZone"};
            Category[] cats = Category.values();
            Category cat = cats[rnd.nextInt(cats.length)];
            g.offerContract(new DealerContract(retailers[rnd.nextInt(retailers.length)], cat,
                    50 + rnd.nextInt(200), 2 + rnd.nextDouble() * 20));
        }
    }

    private void tutorialCheck(Game g) {
        if (!g.tutorialOn || g.tutorialStep > 4) return;
        boolean done = false;
        switch (g.tutorialStep) {
            case 0: done = !g.sites.isEmpty(); break;
            case 1: done = g.sites.stream().anyMatch(s -> s.machineScore() >= 10); break;
            case 2: done = !g.shipments.isEmpty() || g.autopilotSourcing; break;
            case 3: done = !g.researched.isEmpty(); break;
            case 4: done = g.lastDailyRevenue >= 100_000; break;
        }
        if (done) {
            g.tutorialStep++;
            if (g.tutorialStep > 4) g.alert("TUTORIAL abgeschlossen! Viel Erfolg mit " + g.companyName + ".");
            else g.alert("Tutorial-Schritt " + g.tutorialStep + " erledigt!");
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
