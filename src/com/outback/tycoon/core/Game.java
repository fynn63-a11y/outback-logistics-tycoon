package com.outback.tycoon.core;

import com.outback.tycoon.model.*;
import com.outback.tycoon.sim.Simulation;

import javax.swing.Timer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Game {
    public static final LocalDate START_DATE = LocalDate.of(1994, 1, 1);
    public static final long DAY_MILLIS = 5_000;
    public static final double DEBT_LIMIT = 1_000_000;
    public static final double IPO_THRESHOLD = 50_000_000;

    public long dayIndex = 0;
    public double cash = 500_000;
    public double debtLimit = DEBT_LIMIT;
    public double loanTaken = 0;
    public boolean insured = false;
    public final java.util.Map<String, Integer> blockedCities = new java.util.HashMap<>();
    public double reputation = 50;
    public boolean gameOver = false;
    public String gameOverReason = "";

    public final List<City> cities = new ArrayList<>();
    public final List<Site> sites = new ArrayList<>();
    public final List<Shipment> shipments = new ArrayList<>();
    public final List<SKU> catalog = new ArrayList<>();
    public final List<Brand> brands = new ArrayList<>();
    public final List<Carrier> carriers = new ArrayList<>();

    public final Set<Tech> researched = EnumSet.noneOf(Tech.class);
    public double researchPoints = 0;
    public int developers = 1;

    public double morale = 70;
    public int permanentStaff = 10;
    public boolean tempsDecember = true;
    public int strikeDaysLeft = 0;
    public int primeDayIn = 60;
    public int primeDayLeft = 0;

    public boolean autopilotRebalance = false;
    public boolean autopilotPricing = false;
    public boolean autopilotSourcing = false;

    public boolean sandboxMode = false;
    public final List<RivalCompany> rivals = new ArrayList<>();

    public boolean ipoDone = false;
    public double lastDailyRevenue = 0;
    public double lastDailyCosts = 0;

    public final List<String> alerts = new ArrayList<>();
    public String selectedCity = "Sydney";
    public Site selectedSite = null;

    public int speed = 1;
    private int tickAcc = 0;
    private Timer timer;
    private int nextSiteId = 1;
    private int nextShipmentId = 1;
    private final Simulation sim = new Simulation();

    public Game() {
        seedData();
    }

    private void seedData() {
        cities.add(new City("Sydney", "New South Wales", "Australien", 5_300_000, 0.32, 0.920, 0.688));
        cities.add(new City("Melbourne", "Victoria", "Australien", 5_200_000, 0.28, 0.903, 0.710));
        cities.add(new City("Brisbane", "Queensland", "Australien", 2_600_000, 0.22, 0.925, 0.653));
        cities.add(new City("Perth", "Western Australia", "Australien", 2_100_000, 0.18, 0.822, 0.678));
        cities.add(new City("Adelaide", "South Australia", "Australien", 1_400_000, 0.20, 0.885, 0.694));
        cities.add(new City("London", "England", "Europa", 9_000_000, 0.40, 0.500, 0.214));
        cities.add(new City("Berlin", "Deutschland", "Europa", 6_000_000, 0.30, 0.537, 0.208));
        cities.add(new City("Rotterdam", "Niederlande", "Europa", 660_000, 0.45, 0.513, 0.212));
        cities.add(new City("Los Angeles", "Kalifornien", "Nordamerika", 4_000_000, 0.38, 0.172, 0.311));
        cities.add(new City("New York", "New York", "Nordamerika", 8_500_000, 0.42, 0.294, 0.274));
        cities.add(new City("Toronto", "Ontario", "Nordamerika", 3_000_000, 0.33, 0.279, 0.257));
        cities.add(new City("Vancouver", "British Columbia", "Nordamerika", 700_000, 0.35, 0.158, 0.226));

        addDataCities("Australien", CityData.AUSTRALIEN, 0.875, 0.700, 0.100, 0.090);
        addDataCities("Europa", CityData.EUROPA, 0.525, 0.215, 0.055, 0.055);
        addDataCities("Nordamerika", CityData.USA, 0.220, 0.295, 0.130, 0.065);
        addDataCities("Nordamerika", CityData.KANADA, 0.260, 0.245, 0.080, 0.030);

        carriers.add(new Carrier("Outback Freight Co.", 4_000));
        carriers.add(new Carrier("Southern Cross Logistics", 6_500));
        carriers.add(new Carrier("Pacific Parcel Express", 9_000));

        Brand novaTech = new Brand("NovaTech", false);
        Brand apexHome = new Brand("Apex Home", false);
        brands.add(novaTech);
        brands.add(apexHome);

        rivals.add(new RivalCompany("Apex Logistics", 0.12, new java.awt.Color(220, 90, 90)));
        rivals.add(new RivalCompany("Southern Star Freight", 0.09, new java.awt.Color(230, 160, 60)));
        rivals.add(new RivalCompany("Pacific Rim Fulfilment", 0.07, new java.awt.Color(120, 140, 230)));

        catalog.add(new SKU("el-1", "NovaPhone X", Category.ELECTRONICS, 320, novaTech));
        catalog.add(new SKU("el-2", "SoundBuds Pro", Category.ELECTRONICS, 45, null));
        catalog.add(new SKU("ap-1", "Basic Tee", Category.APPAREL, 6, null));
        catalog.add(new SKU("ap-2", "Denim Jeans", Category.APPAREL, 18, null));
        catalog.add(new SKU("fm-1", "Outback Muesli 500g", Category.FMCG, 2.2, null));
        catalog.add(new SKU("fm-2", "Shampoo 300ml", Category.FMCG, 1.8, null));
        catalog.add(new SKU("bu-1", "Loungesessel", Category.BULKY, 140, apexHome));
        catalog.add(new SKU("bu-2", "Billy Regal", Category.BULKY, 60, null));
    }

    private void addDataCities(String region, String[] data, double cx, double cy, double w, double h) {
        for (int i = 0; i < data.length; i++) {
            String[] parts = data[i].split("\\|");
            String name = parts[0];
            double popM = Double.parseDouble(parts[1]);
            boolean exists = false;
            for (City c : cities) if (c.name.equals(name)) { exists = true; break; }
            if (exists) continue;
            double ang = i * 2.399963;
            double rad = Math.sqrt((i + 0.5) / data.length);
            double x = Math.max(0.02, Math.min(0.98, cx + Math.cos(ang) * rad * w));
            double y = Math.max(0.02, Math.min(0.98, cy + Math.sin(ang) * rad * h));
            double prime = Math.max(0.15, Math.min(0.45, 0.15 + popM * 0.05));
            cities.add(new City(name, region, region, (long) (popM * 1_000_000), prime, x, y));
        }
    }

    public LocalDate date() {
        return START_DATE.plusDays(dayIndex);
    }

    public int month() {
        return date().getMonthValue();
    }

    public double seasonality() {
        return Simulation.SEASONALITY[month() - 1];
    }

    public boolean isPrimeDay() {
        return primeDayLeft > 0;
    }

    public void start() {
        timer = new Timer(100, e -> onTick());
        timer.start();
    }

    private void onTick() {
        if (gameOver || speed == 0) return;
        tickAcc += 100 * speed;
        while (tickAcc >= (int) DAY_MILLIS) {
            tickAcc -= (int) DAY_MILLIS;
            dayIndex++;
            sim.dailyTick(this);
        }
    }

    public void alert(String msg) {
        alerts.add(0, date() + " (Tag " + dayIndex + "): " + msg);
        if (alerts.size() > 80) alerts.remove(alerts.size() - 1);
    }

    public double serverCapacity() {
        double cap = 0;
        for (Site s : sites)
            if (s.type == SiteType.DATA_CENTER) cap += 5_000.0 * s.level;
        if (researched.contains(Tech.AWS_CLOUD)) cap += 200_000;
        return cap;
    }

    public double networkThroughput() {
        double t = 0;
        for (Site s : sites) t += s.throughput(this);
        return t;
    }

    public double marketCapEstimate() {
        return Math.max(0, lastDailyRevenue * 365.0) * 8.0;
    }

    public boolean ipoAvailable() {
        return !ipoDone && marketCapEstimate() >= IPO_THRESHOLD;
    }

    public void doIpo() {
        if (!ipoAvailable()) return;
        ipoDone = true;
        cash += 25_000_000;
        alert("IPO an der ASX Sydney! 25 Mio. AUD Kapital aufgenommen.");
    }

    public boolean charge(double amount) {
        if (sandboxMode) return true;
        if (cash < amount) return false;
        cash -= amount;
        return true;
    }

    public boolean isUnlocked(City c) {
        if (c.region.equals("Australien")) return true;
        if (c.region.equals("Europa")) return ipoDone;
        return marketCapEstimate() >= 200_000_000;
    }

    public String unlockHint(City c) {
        if (c.region.equals("Europa")) return "Europa schaltest du mit dem IPO an der ASX frei.";
        if (c.region.equals("Nordamerika")) return "Nordamerika (USA/Kanada) ab 200 Mio. AUD Marktkapitalisierung.";
        return "";
    }

    public Site buildSite(SiteType type) {
        City c = cityByName(selectedCity);
        if (!isUnlocked(c)) {
            alert(c.name + " ist noch gesperrt. " + unlockHint(c));
            return null;
        }
        if (!charge(type.baseCost)) {
            alert("Nicht genug Kapital fuer " + type.label + " in " + selectedCity + ".");
            return null;
        }
        Site s = new Site(nextSiteId++, type, selectedCity);
        sites.add(s);
        alert(type.label + " in " + selectedCity + " gebaut.");
        return s;
    }

    public void upgradeSite(Site s) {
        if (!charge(s.upgradeCost())) {
            alert("Nicht genug Kapital fuer Ausbau von " + s.describe() + ".");
            return;
        }
        s.level++;
        alert(s.describe() + " auf Stufe " + s.level + " ausgebaut.");
    }

    public void upgradeStars(Site s) {
        if (s.stars >= 5) return;
        if (!charge(s.starUpgradeCost())) {
            alert("Nicht genug Kapital fuer Automatisierungs-Upgrade.");
            return;
        }
        s.stars++;
        alert(s.describe() + " jetzt " + s.stars + " Sterne.");
    }

    public boolean hasContainerTerminal(String cityName) {
        for (Site s : sitesIn(cityName))
            if (s.type == SiteType.CONTAINER_TERMINAL) return true;
        return false;
    }

    public Shipment orderContainer(String destCity, java.util.Map<String, Integer> goods, double cost) {
        long active = shipments.stream().filter(s -> s.destCity.equals(destCity)).count();
        int limit = hasContainerTerminal(destCity) ? 7 : 4;
        int congestion = (int) Math.max(0, active - limit) * 2;
        if (hasContainerTerminal(destCity)) congestion = Math.max(0, congestion - 2);
        if (blockedCities.containsKey(destCity)) congestion += 3;
        if (congestion > 0) alert("Hafen-Stau (Port Congestion) in " + destCity + ": +" + congestion + " Tage Seefracht.");
        if (!charge(cost)) return null;
        Shipment sh = new Shipment(nextShipmentId++, "Shenzhen/Shanghai", destCity, goods, cost, congestion);
        shipments.add(sh);
        return sh;
    }

    public Brand foundBrand(String name) {
        if (!charge(250_000)) {
            alert("Markengruendung kostet 250.000 AUD.");
            return null;
        }
        Brand b = new Brand(name, true);
        brands.add(b);
        alert("Neue Eigenmarke gegruendet: " + name + "!");
        return b;
    }

    public void launchCampaign(Brand b) {
        if (!b.playerOwned || !charge(50_000)) return;
        b.marketingLevel++;
        alert("Werbekampagne fuer " + b.name + " (Stufe " + b.marketingLevel + ").");
    }

    public void researchTech(Tech t) {
        if (researched.contains(t) || researchPoints < t.rpCost) return;
        researchPoints -= t.rpCost;
        researched.add(t);
        alert("Technologie erforscht: " + t.label + " - " + t.description);
    }

    public void hireDeveloper() {
        developers++;
    }

    public void fireDeveloper() {
        if (developers > 0) developers--;
    }

    public void hireStaff() {
        permanentStaff++;
    }

    public void fireStaff() {
        if (permanentStaff > 1) permanentStaff--;
    }

    public void negotiateCarrier(Carrier c) {
        if (!c.canNegotiate() || !charge(c.negotiateCost())) return;
        c.contractLevel++;
        alert("Vertrag mit " + c.name + " ausgehandelt (Stufe " + c.contractLevel + ", -" + (8 * c.contractLevel) + "% Tarife).");
    }

    public double totalRivalShare() {
        double sum = 0;
        for (RivalCompany r : rivals) sum += r.marketShare;
        return sum;
    }

    public double rivalValue(RivalCompany r) {
        return 10_000_000 + r.marketShare * 100_000_000;
    }

    public void takeLoan() {
        cash += 500_000;
        debtLimit += 500_000;
        loanTaken += 500_000;
        alert("Kredit aufgenommen: +500.000 AUD. Schulden-Grenze jetzt " + String.format("%,.0f", -debtLimit) + " AUD (Zins 12% p.a. auf Negativsaldo).");
    }

    public void negotiateUnion() {
        if (!charge(30_000)) {
            alert("Gewerkschaftsverhandlung kostet 30.000 AUD.");
            return;
        }
        morale = Math.min(100, morale + 15);
        alert("Gewerkschaftsverhandlung erfolgreich: Lohnerhoehung, Moral +15. Streikrisiko sinkt.");
    }

    public SKU developProduct(Brand b) {
        if (b == null || !b.playerOwned) return null;
        if (researchPoints < 30 || !charge(150_000)) {
            alert("Produktentwicklung braucht 30 Forschungspunkte und 150.000 AUD.");
            return null;
        }
        researchPoints -= 30;
        Category[] cats = Category.values();
        Category cat = cats[(int) (Math.random() * cats.length)];
        String[] names = {"Outback Classic", "Red Dust Edition", "Coastal Pro", "Bush Ranger", "Sunset Series", "Blue Ocean"};
        String name = names[(int) (Math.random() * names.length)];
        SKU sku = new SKU("pl-" + (catalog.size() + 1), name, cat, 5 + Math.random() * 200, b);
        sku.price = Math.round(sku.baseCost * 1.8 * 100.0) / 100.0;
        catalog.add(sku);
        alert("PRODUKT-LAUNCH: '" + name + "' (" + cat.label + ") der Eigenmarke " + b.name + " - hohe Marge!");
        return sku;
    }

    public void acquireRival(RivalCompany r) {
        double value = rivalValue(r);
        if (!charge(value)) {
            alert("Uebernahme von " + r.name + " kostet " + String.format("%,.0f", value) + " AUD - nicht genug Kapital.");
            return;
        }
        rivals.remove(r);
        reputation = Math.min(100, reputation + 5);
        alert("UEBERNAHME: " + r.name + " (Marktanteil " + (int) (r.marketShare * 100) + "%) ist jetzt Teil deines Konzerns!");
    }

    public City cityByName(String name) {
        for (City c : cities) if (c.name.equals(name)) return c;
        return cities.get(0);
    }

    public List<Site> sitesIn(String cityName) {
        List<Site> out = new ArrayList<>();
        for (Site s : sites) if (s.city.equals(cityName)) out.add(s);
        return out;
    }
}
