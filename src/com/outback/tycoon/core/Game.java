package com.outback.tycoon.core;

import com.outback.tycoon.model.*;
import com.outback.tycoon.sim.Simulation;

import javax.swing.Timer;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final LocalDate START_DATE = LocalDate.of(1994, 1, 1);
    public static final long DAY_MILLIS = 5_000;
    public static final double DEBT_LIMIT = 1_000_000;
    public static final double IPO_THRESHOLD = 50_000_000;
    public static final int SHARES = 1_000_000;

    public long dayIndex = 0;
    public double cash = 500_000;
    public double debtLimit = DEBT_LIMIT;
    public double loanTaken = 0;
    public boolean insured = false;
    public final Map<String, Integer> blockedCities = new HashMap<>();
    public double reputation = 50;
    public boolean gameOver = false;
    public String gameOverReason = "";

    public Scenario scenario = Scenario.ENDLESS;
    public boolean won = false;
    public String winMessage = "";
    public boolean primeDaySurvived = false;

    public final List<City> cities = new ArrayList<>();
    public final List<Site> sites = new ArrayList<>();
    public final List<Shipment> shipments = new ArrayList<>();
    public final List<SKU> catalog = new ArrayList<>();
    public final List<Brand> brands = new ArrayList<>();
    public final List<Carrier> carriers = new ArrayList<>();
    public final List<RivalCompany> rivals = new ArrayList<>();

    public final Set<Tech> researched = EnumSet.noneOf(Tech.class);
    public double researchPoints = 0;
    public int developers = 1;

    public double morale = 70;
    public int permanentStaff = 10;
    public int staffSkill = 1;
    public boolean tempsDecember = true;
    public int strikeDaysLeft = 0;
    public int primeDayIn = 60;
    public int primeDayLeft = 0;

    public boolean autopilotRebalance = false;
    public boolean autopilotPricing = false;
    public boolean autopilotSourcing = false;
    public boolean autopilotZones = false;
    public final Map<String, Integer> zoneAssignment = new HashMap<>();
    public Difficulty difficulty = Difficulty.NORMAL;
    public final Map<String, Integer> fleetDown = new HashMap<>();
    public final Map<String, Double> cityRating = new HashMap<>();
    public int flashSaleDays = 0;
    public Category flashSaleCategory = null;
    public final List<String> subsidiaries = new ArrayList<>();
    public final java.util.Set<String> achievements = new java.util.HashSet<>();
    public double lastFillRate = 1.0;

    public boolean sandboxMode = false;

    public String companyName = "Outback Logistics";
    public double fxCny = 0.21;
    public double fxUsd = 0.65;
    public final List<DealerContract> offers = new ArrayList<>();
    public final List<DealerContract> contracts = new ArrayList<>();
    public double quarterRevenue = 0;
    public double quarterCosts = 0;
    public int quarterDay = 0;
    public double lastB2B = 0;
    public boolean tutorialOn = true;
    public int tutorialStep = 0;
    public boolean settingFullscreen = true;

    public boolean ipoDone = false;
    public double sharePrice = 0;
    public boolean ownLastMile = false;
    public final Map<String, Integer> trucks = new HashMap<>();
    public final Map<String, Integer> vans = new HashMap<>();

    public double lastDailyRevenue = 0;
    public double lastDailyCosts = 0;
    public int lastDailyReturns = 0;
    public final List<Double> histCash = new ArrayList<>();
    public final List<Double> histRevenue = new ArrayList<>();
    public final List<Double> histRivalShare = new ArrayList<>();

    public final List<String> alerts = new ArrayList<>();
    public String selectedCity = "Sydney";
    public Site selectedSite = null;

    public int speed = 1;
    private int tickAcc = 0;
    private transient Timer timer;
    private int nextSiteId = 1;
    private int nextShipmentId = 1;
    private int lastEra = -1;
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

        catalog.add(new SKU("el-1", "NovaPhone X", Category.ELECTRONICS, 320, novaTech));
        catalog.add(new SKU("el-2", "SoundBuds Pro", Category.ELECTRONICS, 45, null));
        catalog.add(new SKU("el-3", "UltraBook 15 Zoll", Category.ELECTRONICS, 900, null));
        catalog.add(new SKU("el-4", "GameKonsole Z", Category.ELECTRONICS, 280, null));
        catalog.add(new SKU("tx-1", "Basic Tee", Category.APPAREL_BASIC, 6, null));
        catalog.add(new SKU("tx-2", "Socken 3er-Pack", Category.APPAREL_BASIC, 4, null));
        catalog.add(new SKU("tx-3", "Designer Shirt", Category.APPAREL_LUXURY, 60, null));
        catalog.add(new SKU("tx-4", "Massanzug", Category.APPAREL_LUXURY, 350, null));
        catalog.add(new SKU("sn-1", "Runner Pro", Category.SNEAKERS, 80, null));
        catalog.add(new SKU("sn-2", "Sneaker Classic", Category.SNEAKERS, 45, null));
        catalog.add(new SKU("au-1", "Bremsbelag-Set", Category.CAR_PARTS, 35, null));
        catalog.add(new SKU("au-2", "Motoröl 5L", Category.CAR_PARTS, 25, null));
        catalog.add(new SKU("fm-1", "Outback Muesli 500g", Category.FMCG, 2.2, null));
        catalog.add(new SKU("fm-2", "Shampoo 300ml", Category.FMCG, 1.8, null));
        catalog.add(new SKU("cc-1", "Bio-Milch 1L", Category.COLD_CHAIN, 1.5, null));
        catalog.add(new SKU("cc-2", "Rinderfilet 500g", Category.COLD_CHAIN, 12, null));
        catalog.add(new SKU("hz-1", "WC-Reiniger", Category.HAZMAT, 2.5, null));
        catalog.add(new SKU("hz-2", "Deo-Spray 150ml", Category.HAZMAT, 3, null));
        catalog.add(new SKU("bu-1", "Loungesessel", Category.BULKY, 140, apexHome));
        catalog.add(new SKU("bu-2", "Billy Regal", Category.BULKY, 60, null));
        catalog.add(new SKU("se-1", "Adventskalender", Category.FMCG, 8, null, new Integer[]{11, 12}));
        catalog.add(new SKU("se-2", "Lebkuchen 500g", Category.FMCG, 4, null, new Integer[]{12}));
        catalog.add(new SKU("se-3", "Sonnenbrille Summer", Category.APPAREL_LUXURY, 25, null, new Integer[]{12, 1, 2}));
        catalog.add(new SKU("se-4", "Sonnencreme LSF50", Category.FMCG, 6, null, new Integer[]{12, 1, 2}));

        rivals.add(new RivalCompany("Apex Logistics", 0.12, new java.awt.Color(220, 90, 90)));
        rivals.add(new RivalCompany("Southern Star Freight", 0.09, new java.awt.Color(230, 160, 60)));
        rivals.add(new RivalCompany("Pacific Rim Fulfilment", 0.07, new java.awt.Color(120, 140, 230)));
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

    public int eraIndex() {
        int y = date().getYear();
        if (y < 2000) return 0;
        if (y < 2010) return 1;
        if (y < 2020) return 2;
        return 3;
    }

    public String eraName() {
        switch (eraIndex()) {
            case 0: return "Modem-Ära (1994-1999)";
            case 1: return "Breitband-Ära (2000-2009)";
            case 2: return "Cloud-Ära (2010-2019)";
            default: return "KI-Ära (ab 2020)";
        }
    }

    public int lastEraValue() {
        return lastEra;
    }

    public void setLastEra(int e) {
        lastEra = e;
    }

    public double eraDemandMultiplier() {
        return 1.0 + eraIndex() * 0.35;
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
        com.outback.tycoon.audio.Sfx.alert();
    }

    public double serverCapacity() {
        double cap = 0;
        for (Site s : sites)
            if (s.type == SiteType.DATA_CENTER) cap += 5_000.0 * s.level;
        if (researched.contains(Tech.AWS_CLOUD)) cap += 200_000;
        return cap;
    }

    public int effectiveTrucks(String city) {
        return fleetDown.containsKey(city) ? 0 : trucks.getOrDefault(city, 0);
    }

    public int effectiveVans(String city) {
        return fleetDown.containsKey(city) ? 0 : vans.getOrDefault(city, 0);
    }

    public double cityRating(String cityName) {
        return cityRating.getOrDefault(cityName, 3.0);
    }

    public void startFlashSale(Category cat) {
        if (!charge(20_000)) {
            alert("Blitzangebot kostet 20.000 AUD Marketing.");
            return;
        }
        flashSaleDays = 3;
        flashSaleCategory = cat;
        alert("BLITZANGEBOT gestartet! 3 Tage lang -20% Preis, x1.6 Nachfrage"
                + (cat != null ? " fuer " + cat.label : " fuer ALLES") + ".");
    }

    public void foundSubsidiary(String name) {
        if (!charge(500_000)) {
            alert("Tochterfirma kostet 500.000 AUD.");
            return;
        }
        subsidiaries.add(name);
        alert("Tochterfirma '" + name + "' gegruendet (+3.000 AUD/Tag passiv).");
    }

    public double siteThroughput(Site s) {
        double bonus = 1.0 + 0.05 * effectiveTrucks(s.city) + 0.02 * effectiveVans(s.city);
        return s.throughput(this) * bonus;
    }

    public double networkThroughput() {
        double t = 0;
        for (Site s : sites) t += siteThroughput(s);
        return t;
    }

    public double marketCapEstimate() {
        if (ipoDone) return sharePrice * SHARES;
        return Math.max(0, lastDailyRevenue * 365.0) * 8.0;
    }

    public boolean ipoAvailable() {
        return !ipoDone && marketCapEstimate() >= IPO_THRESHOLD;
    }

    public void doIpo() {
        if (!ipoAvailable()) return;
        ipoDone = true;
        sharePrice = 25.0;
        cash += 25_000_000;
        com.outback.tycoon.audio.Sfx.coin();
        alert("IPO an der ASX Sydney! 25 Mio. AUD Kapital aufgenommen, Aktienkurs 25,00 AUD.");
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

    public boolean charge(double amount) {
        if (sandboxMode) return true;
        if (cash < amount) return false;
        cash -= amount;
        return true;
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
        s.buildDaysLeft = type.buildDays;
        sites.add(s);
        alert(type.label + " in " + selectedCity + " gebaut - Baustelle, Bauzeit " + type.buildDays + " Tage.");
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

    public void upgradeService(Site s) {
        if (s.serviceLevel == ServiceLevel.SAME_DAY) return;
        if (!charge(s.serviceUpgradeCost())) {
            alert("Nicht genug Kapital fuer Service-Upgrade.");
            return;
        }
        s.serviceLevel = ServiceLevel.values()[s.serviceLevel.ordinal() + 1];
        alert(s.describe() + " bietet jetzt " + s.serviceLevel.label + " (Nachfrage x" + s.serviceLevel.demandFactor + ").");
    }

    public double distanceKm(City a, City b) {
        return Math.hypot(a.mapX - b.mapX, a.mapY - b.mapY) * 25_000;
    }

    public Site servingSite(String cityName) {
        Integer id = zoneAssignment.get(cityName);
        if (id != null)
            for (Site s : sites)
                if (s.id == id && s.isOperational()) return s;
        for (Site s : sitesIn(cityName))
            if (s.isOperational()) return s;
        return null;
    }

    public void assignZone(String cityName, int siteId) {
        zoneAssignment.put(cityName, siteId);
    }

    public void autoAssignZones() {
        for (City c : cities) {
            if (!isUnlocked(c)) continue;
            Site best = null;
            double bestScore = -1e18;
            for (Site s : sites) {
                if (!s.isOperational() || s.type == SiteType.DATA_CENTER) continue;
                double d = distanceKm(c, cityByName(s.city));
                double score = s.throughput(this) - d / 10.0 + (s.city.equals(c.name) ? 500 : 0);
                if (score > bestScore) {
                    bestScore = score;
                    best = s;
                }
            }
            if (best != null) zoneAssignment.put(c.name, best.id);
        }
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

    public Shipment orderContainer(String destCity, Map<String, Integer> goods, double cost) {
        long active = shipments.stream().filter(s -> s.destCity.equals(destCity)).count();
        int limit = hasContainerTerminal(destCity) ? 7 : 4;
        int congestion = (int) Math.max(0, active - limit) * 2;
        if (hasContainerTerminal(destCity)) congestion = Math.max(0, congestion - 2);
        if (blockedCities.containsKey(destCity)) congestion += 3;
        if (congestion > 0) alert("Hafen-Stau (Port Congestion) in " + destCity + ": +" + congestion + " Tage Seefracht.");
        double audCost = cost * (fxCny / 0.21);
        if (!charge(audCost)) return null;
        Shipment sh = new Shipment(nextShipmentId++, "Shenzhen/Shanghai", destCity, goods, audCost, congestion);
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

    public void researchTech(Tech t) {
        if (researched.contains(t) || researchPoints < t.rpCost || eraIndex() < t.minEra) return;
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

    public void trainStaff() {
        if (staffSkill >= 5) return;
        double cost = 20_000.0 * staffSkill;
        if (!charge(cost)) {
            alert("Schulung kostet " + String.format("%,.0f", cost) + " AUD.");
            return;
        }
        staffSkill++;
        morale = Math.min(100, morale + 2);
        alert("Mitarbeiter geschult: Skill Stufe " + staffSkill + " (+5% Durchsatz pro Stufe).");
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

    public void buyTruck() {
        if (!charge(80_000)) {
            alert("LKW kostet 80.000 AUD.");
            return;
        }
        trucks.merge(selectedCity, 1, Integer::sum);
        alert("LKW gekauft fuer Strecke " + selectedCity + " (+5% Durchsatz, -5% Fracht).");
    }

    public void buyVan() {
        if (!charge(30_000)) {
            alert("Transporter kostet 30.000 AUD.");
            return;
        }
        vans.merge(selectedCity, 1, Integer::sum);
        alert("Transporter gekauft fuer " + selectedCity + " (+2% Durchsatz, +3% Nachfrage).");
    }

    public void offerContract(DealerContract c) {
        if (offers.size() >= 3) return;
        offers.add(c);
        alert("WETTBEWERBS-EVENT: Ausschreibung von " + c.retailer + " - " + c.unitsPerDay + " Einheiten/Tag ("
                + c.category.label + ") zum Preis " + String.format("%.2f", c.pricePerUnit) + " AUD. Annahme im Tab 'Firma & Finanzen'.");
    }

    public void acceptOffer(DealerContract c) {
        offers.remove(c);
        contracts.add(c);
        alert("Vertrag mit " + c.retailer + " unterschrieben (" + c.daysLeft + " Tage).");
    }

    public void declineOffer(DealerContract c) {
        offers.remove(c);
        alert("Ausschreibung von " + c.retailer + " abgelehnt.");
    }

    public String tutorialHint() {
        if (!tutorialOn) return "";
        switch (tutorialStep) {
            case 0: return "TUTORIAL 1/5: Baue einen Standort in Sydney (Tab 'Stadt & Bau (Meso)').";
            case 1: return "TUTORIAL 2/5: Öffne das Mikro-Grid und platziere mind. 5 Maschinen (Packbau oben).";
            case 2: return "TUTORIAL 3/5: Bestelle eine China-Seefracht (Autopilot-Sourcing aktivieren oder manuell).";
            case 3: return "TUTORIAL 4/5: Erforsche eine Technologie im Tab 'Technik & F&E' (brauchst 40 RP).";
            case 4: return "TUTORIAL 5/5: Erreiche 100.000 AUD Tagesumsatz. Dann ist das Tutorial beendet.";
            default: return "";
        }
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

    public void acquireRival(RivalCompany r) {
        double value = rivalValue(r);
        if (!charge(value)) {
            alert("Uebernahme von " + r.name + " kostet " + String.format("%,.0f", value) + " AUD - nicht genug Kapital.");
            return;
        }
        rivals.remove(r);
        reputation = Math.min(100, reputation + 5);
        com.outback.tycoon.audio.Sfx.coin();
        alert("UEBERNAHME: " + r.name + " (Marktanteil " + (int) (r.marketShare * 100) + "%) ist jetzt Teil deines Konzerns!");
    }

    public City cityByName(String name) {
        for (City c : cities) if (c.name.equals(name)) return c;
        return cities.get(0);
    }

    public SKU skuById(String id) {
        for (SKU s : catalog) if (s.id.equals(id)) return s;
        return null;
    }

    public List<Site> sitesIn(String cityName) {
        List<Site> out = new ArrayList<>();
        for (Site s : sites) if (s.city.equals(cityName)) out.add(s);
        return out;
    }

    public void save(File f) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        }
    }

    public static Game load(File f) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Game) ois.readObject();
        }
    }

    public void copyFrom(Game o) {
        dayIndex = o.dayIndex;
        cash = o.cash;
        debtLimit = o.debtLimit;
        loanTaken = o.loanTaken;
        insured = o.insured;
        blockedCities.clear();
        blockedCities.putAll(o.blockedCities);
        reputation = o.reputation;
        gameOver = o.gameOver;
        gameOverReason = o.gameOverReason;
        scenario = o.scenario;
        won = o.won;
        winMessage = o.winMessage;
        primeDaySurvived = o.primeDaySurvived;
        cities.clear(); cities.addAll(o.cities);
        sites.clear(); sites.addAll(o.sites);
        shipments.clear(); shipments.addAll(o.shipments);
        catalog.clear(); catalog.addAll(o.catalog);
        brands.clear(); brands.addAll(o.brands);
        carriers.clear(); carriers.addAll(o.carriers);
        rivals.clear(); rivals.addAll(o.rivals);
        researched.clear(); researched.addAll(o.researched);
        researchPoints = o.researchPoints;
        developers = o.developers;
        morale = o.morale;
        permanentStaff = o.permanentStaff;
        staffSkill = o.staffSkill;
        tempsDecember = o.tempsDecember;
        strikeDaysLeft = o.strikeDaysLeft;
        primeDayIn = o.primeDayIn;
        primeDayLeft = o.primeDayLeft;
        autopilotRebalance = o.autopilotRebalance;
        autopilotPricing = o.autopilotPricing;
        autopilotSourcing = o.autopilotSourcing;
        autopilotZones = o.autopilotZones;
        zoneAssignment.clear(); zoneAssignment.putAll(o.zoneAssignment);
        sandboxMode = o.sandboxMode;
        ipoDone = o.ipoDone;
        sharePrice = o.sharePrice;
        ownLastMile = o.ownLastMile;
        trucks.clear(); trucks.putAll(o.trucks);
        vans.clear(); vans.putAll(o.vans);
        lastDailyRevenue = o.lastDailyRevenue;
        lastDailyCosts = o.lastDailyCosts;
        lastDailyReturns = o.lastDailyReturns;
        histCash.clear(); histCash.addAll(o.histCash);
        histRevenue.clear(); histRevenue.addAll(o.histRevenue);
        histRivalShare.clear(); histRivalShare.addAll(o.histRivalShare);
        alerts.clear(); alerts.addAll(o.alerts);
        selectedCity = o.selectedCity;
        selectedSite = o.selectedSite;
        nextSiteId = o.nextSiteId;
        nextShipmentId = o.nextShipmentId;
        lastEra = o.lastEra;
        companyName = o.companyName;
        fxCny = o.fxCny;
        fxUsd = o.fxUsd;
        offers.clear(); offers.addAll(o.offers);
        contracts.clear(); contracts.addAll(o.contracts);
        quarterRevenue = o.quarterRevenue;
        quarterCosts = o.quarterCosts;
        quarterDay = o.quarterDay;
        lastB2B = o.lastB2B;
        tutorialOn = o.tutorialOn;
        tutorialStep = o.tutorialStep;
        settingFullscreen = o.settingFullscreen;
        difficulty = o.difficulty;
        fleetDown.clear(); fleetDown.putAll(o.fleetDown);
        cityRating.clear(); cityRating.putAll(o.cityRating);
        flashSaleDays = o.flashSaleDays;
        flashSaleCategory = o.flashSaleCategory;
        subsidiaries.clear(); subsidiaries.addAll(o.subsidiaries);
        achievements.clear(); achievements.addAll(o.achievements);
        lastFillRate = o.lastFillRate;
    }
}
