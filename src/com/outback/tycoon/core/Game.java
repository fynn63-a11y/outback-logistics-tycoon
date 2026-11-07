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
    public final Map<String, Integer> refurbStock = new HashMap<>();
    public final Map<String, Double> revBy = new java.util.LinkedHashMap<>();
    public final Map<String, Double> costBy = new java.util.LinkedHashMap<>();
    public long primeSubscribers = 0;
    public boolean primeVideo = false;
    public boolean primeMusic = false;
    public boolean primeFresh = false;

    public double primeShare() {
        return Math.min(0.45, primeSubscribers / 5_000_000.0);
    }

    public void campaignPrimo() {
        if (!charge(100_000)) {
            alert("Primo-Werbekampagne kostet 100.000 AUD.");
            return;
        }
        long newSubs = 20_000 + (long) (reputation * 500) + (long) networkThroughput();
        primeSubscribers += newSubs;
        alert("PRIMO-KAMPAGNE: +" + newSubs + " Abonnenten (gesamt " + primeSubscribers + ").");
    }

    public void unlockEcosystem(boolean video, boolean music, boolean fresh) {
        double cost = (video && !primeVideo ? 200_000 : 0) + (music && !primeMusic ? 200_000 : 0) + (fresh && !primeFresh ? 150_000 : 0);
        if (cost > 0 && !charge(cost)) {
            alert("Ökosystem-Dienste kosten " + String.format("%,.0f", cost) + " AUD.");
            return;
        }
        primeVideo = primeVideo || video;
        primeMusic = primeMusic || music;
        primeFresh = primeFresh || fresh;
        alert("Ökosystem erweitert: " + (video ? "Primo View " : "") + (music ? "Primo Tunes " : "") + (fresh ? "Primo Fresh (Lebensmittel)" : ""));
    }

    public void addRev(String key, double v) {
        revBy.merge(key, v, Double::sum);
    }

    public void addCost(String key, double v) {
        costBy.merge(key, v, Double::sum);
    }

    public boolean sandboxMode = false;

    public String companyName = "Outback Logistics";
    public String webServiceName = "";
    public boolean webServiceNamed = false;
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

        addDataCities("Australien", CityData.AUSTRALIEN, 0.875, 0.700, 0.100, 0.090);
        generateMinorCities();

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
        catalog.add(new SKU("el-5", "Tablet 10 Zoll", Category.ELECTRONICS, 220, null));
        catalog.add(new SKU("el-6", "Smartwatch", Category.ELECTRONICS, 90, null));
        catalog.add(new SKU("el-7", "Kamera DSLR", Category.ELECTRONICS, 450, null));
        catalog.add(new SKU("tx-5", "Hoodie", Category.APPAREL_BASIC, 12, null));
        catalog.add(new SKU("tx-6", "Jeans", Category.APPAREL_BASIC, 18, null));
        catalog.add(new SKU("tx-7", "Lederjacke", Category.APPAREL_LUXURY, 180, null));
        catalog.add(new SKU("tx-8", "Seidenkrawatte", Category.APPAREL_LUXURY, 45, null));
        catalog.add(new SKU("sn-3", "Wanderschuhe", Category.SNEAKERS, 95, null));
        catalog.add(new SKU("sn-4", "Sandalen", Category.SNEAKERS, 30, null));
        catalog.add(new SKU("au-3", "Reifen 205/55", Category.CAR_PARTS, 80, null));
        catalog.add(new SKU("au-4", "Scheibenwischer-Set", Category.CAR_PARTS, 18, null));
        catalog.add(new SKU("fm-3", "Kaffee 500g", Category.FMCG, 5.5, null));
        catalog.add(new SKU("fm-4", "Nudeln 500g", Category.FMCG, 1.2, null));
        catalog.add(new SKU("fm-5", "Olivenoel 750ml", Category.FMCG, 6.5, null));
        catalog.add(new SKU("cc-3", "Joghurt 500g", Category.COLD_CHAIN, 2.8, null));
        catalog.add(new SKU("cc-4", "Gouda 400g", Category.COLD_CHAIN, 7.5, null));
        catalog.add(new SKU("hz-3", "Farben Spray", Category.HAZMAT, 4.5, null));
        catalog.add(new SKU("hz-4", "Batterie Li-Ion", Category.HAZMAT, 9, null));
        catalog.add(new SKU("bu-3", "Esstisch", Category.BULKY, 220, null));
        catalog.add(new SKU("bu-4", "Matratze 140x200", Category.BULKY, 180, null));
        catalog.add(new SKU("bu-5", "Schreibtisch", Category.BULKY, 150, null));
        catalog.add(new SKU("sp-1", "Fussball", Category.FMCG, 15, null));
        catalog.add(new SKU("sp-2", "Yogamatte", Category.SNEAKERS, 25, null));
        catalog.add(new SKU("ty-1", "Autoradio", Category.ELECTRONICS, 120, null));
        catalog.add(new SKU("ty-2", "Navi-Handheld", Category.ELECTRONICS, 80, null));
        catalog.add(new SKU("ho-1", "Stehlampe", Category.BULKY, 60, null));
        catalog.add(new SKU("ho-2", "Bettwaesche-Set", Category.APPAREL_BASIC, 25, null));
        catalog.add(new SKU("ho-3", "Handtuch 4er", Category.APPAREL_BASIC, 12, null));
        catalog.add(new SKU("to-1", "Rucksack 30L", Category.SNEAKERS, 40, null));
        catalog.add(new SKU("to-2", "Koffer Hartschale", Category.BULKY, 110, null));
        catalog.add(new SKU("pe-1", "Parfum 50ml", Category.APPAREL_LUXURY, 60, null));
        catalog.add(new SKU("pe-2", "Deo Roll-on", Category.HAZMAT, 3.5, null));
        catalog.add(new SKU("ki-1", "Spielzeug-Roboter", Category.ELECTRONICS, 35, null));
        catalog.add(new SKU("ki-2", "Brettspiel", Category.FMCG, 22, null));
        catalog.add(new SKU("ga-1", "Grill", Category.BULKY, 130, null));
        catalog.add(new SKU("ga-2", "Campingzelt", Category.BULKY, 160, null));
        catalog.add(new SKU("of-1", "Aktenordner 10er", Category.FMCG, 8, null));
        catalog.add(new SKU("of-2", "Druckerpapier A4", Category.FMCG, 6, null));

        RivalCompany apex = new RivalCompany("Apex Logistics", 0.12, new java.awt.Color(220, 90, 90));
        apex.sites = 2;
        apex.siteCities.add("Melbourne");
        apex.siteCities.add("Perth");
        apex.aggression = 1.6;
        apex.categories.add(Category.ELECTRONICS);
        RivalCompany southern = new RivalCompany("Southern Star Freight", 0.09, new java.awt.Color(230, 160, 60));
        southern.sites = 1;
        southern.siteCities.add("Sydney");
        southern.aggression = 0.8;
        southern.categories.add(Category.FMCG);
        RivalCompany pacific = new RivalCompany("Pacific Rim Fulfilment", 0.07, new java.awt.Color(120, 140, 230));
        pacific.sites = 1;
        pacific.siteCities.add("Brisbane");
        pacific.aggression = 1.2;
        pacific.categories.add(Category.BULKY);
        rivals.add(apex);
        rivals.add(southern);
        rivals.add(pacific);
    }

    private void generateMinorCities() {
        java.util.Random r = new java.util.Random(1994);
        String[][] prefixes = {
                {"Port ", "Mount ", "Fort ", "New ", "Lake ", "Cape ", "Gold", "River"},
                {"San ", "Santa ", "Los ", "Las ", "El ", "New ", "Fort ", "Saint "},
                {"Vill", "Neu", "Alt", "Ober", "Unter", "Klein", "Gross", "Bad "},
                {"North ", "South ", "East ", "West ", "Port ", "Fort ", "New "}};
        String[][] suffixes = {
                {"ville", "town", "field", "hurst", "dale", "port", "shire", " Creek"},
                {"burg", "stadt", "dorf", "hausen", "fels", "heim", " bach", "tal"},
                {"dorf", "stadt", "burg", "berg", "feld", "heim", "hausen", "tal"},
                {"ville", "ton", "ville", "field", " Harbour", "ville", "ton", " City"}};
        double[][] regions = {
                {0.875, 0.700, 0.100, 0.090, 900, 0}};
        String[] regionNames = {"Australien"};
        for (double[] reg : regions) {
            int pIdx = (int) reg[5];
            for (int i = 0; i < (int) reg[4]; i++) {
                String name = prefixes[pIdx][r.nextInt(prefixes[pIdx].length)] + suffixes[pIdx][r.nextInt(suffixes[pIdx].length)];
                boolean exists = false;
                for (City c : cities) if (c.name.equals(name)) { exists = true; break; }
                if (exists) continue;
                double x = Math.max(0.02, Math.min(0.98, reg[0] + (r.nextDouble() - 0.5) * 2 * reg[2]));
                double y = Math.max(0.02, Math.min(0.98, reg[1] + (r.nextDouble() - 0.5) * 2 * reg[3]));
                long pop = 5_000 + r.nextInt(495_000);
                double prime = 0.10 + r.nextDouble() * 0.15;
                cities.add(new City(name, regionNames[pIdx], regionNames[pIdx], pop, prime, x, y));
            }
        }
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

    public boolean isPrimoDay() {
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

    public int refurbTotal() {
        int t = 0;
        for (int v : refurbStock.values()) t += v;
        return t;
    }

    public void alert(String msg) {
        alerts.add(0, date() + " (Tag " + dayIndex + "): " + msg);
        if (alerts.size() > 80) alerts.remove(alerts.size() - 1);
        com.outback.tycoon.audio.Sfx.alert();
    }

    public double techBonus(Tech.Bonus b) {
        double sum = 0;
        for (Tech t : researched) if (t.bonus == b) sum += t.bonusValue;
        if (b == Tech.Bonus.COST && heads.contains("Einkauf")) sum += 0.03;
        if (b == Tech.Bonus.DEMAND && heads.contains("Marketing")) sum += 0.03;
        if (b == Tech.Bonus.THROUGHPUT && heads.contains("IT")) sum += 0.03;
        return sum;
    }

    public SiteType pendingBuildType = null;
    public final java.util.Set<String> heads = new java.util.HashSet<>();
    public boolean workersHousing = false;
    public boolean airFreight = false;
    public final java.util.Set<String> railHubs = new java.util.HashSet<>();
    public int priceWarDays = 0;
    public String priceWarRival = "";
    public boolean priceWarCounter = false;
    public final java.util.Set<Integer> missionsDone = new java.util.HashSet<>();
    public final java.util.Set<String> eventFlags = new java.util.HashSet<>();
    public int sabotageCooldown = 0;

    public double serverCapacity() {
        double cap = techBonus(Tech.Bonus.SERVER);
        for (Site s : sites)
            if (s.type == SiteType.DATA_CENTER) cap += 5_000.0 * s.level;
        if (researched.contains(Tech.OWS_CLOUD)) cap += 200_000;
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
        return true;
    }

    public String unlockHint(City c) {
        return "";
    }

    public boolean charge(double amount) {
        if (sandboxMode) return true;
        if (cash < amount) return false;
        cash -= amount;
        return true;
    }

    public Site buildSite(SiteType type) {
        if (type == SiteType.HAZ_STORAGE && !researched.contains(Tech.PA_HAZMAT)) {
            alert("Gefahrgut-Lager braucht die Lizenz-Technologie 'Hazmat-Routing beim Packen'.");
            return null;
        }
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

    public boolean hasAirHub(String cityName) {
        for (Site s : sitesIn(cityName))
            if (s.type == SiteType.AIR_HUB && s.isOperational()) return true;
        return false;
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

    public void hireHead(String dept) {
        if (heads.contains(dept) || !charge(200_000)) return;
        heads.add(dept);
        alert("Neuer Abteilungsleiter eingestellt: " + dept + " (200.000 AUD, Gehalt laufend).");
    }

    public void buildWorkersHousing() {
        if (workersHousing || !charge(150_000)) return;
        workersHousing = true;
        morale = Math.min(100, morale + 10);
        alert("Mitarbeiter-Wohnheim gebaut: Moral-Ziel steigt auf 80.");
    }

    public void buyAirFreightContract() {
        if (airFreight || !charge(150_000)) return;
        airFreight = true;
        alert("Luftfracht-Vertrag: Express-Rate -30% zwischen Air-Hubs.");
    }

    public void buyRailHub() {
        if (railHubs.contains(selectedCity) || !charge(300_000)) return;
        railHubs.add(selectedCity);
        alert("Bahnanschluss fuer " + selectedCity + ": guenstigere Langstrecke (-50% Distanzkosten).");
    }

    public void sabotage(RivalCompany r) {
        if (sabotageCooldown > 0 || !charge(500_000)) {
            alert("Sabotage kostet 500.000 AUD (Cooldown aktiv oder zu wenig Kapital).");
            return;
        }
        sabotageCooldown = 30;
        if (Math.random() < 0.6) {
            r.marketShare *= 0.7;
            if (!r.siteCities.isEmpty()) {
                r.siteCities.remove(r.siteCities.size() - 1);
                r.sites = Math.max(0, r.sites - 1);
            }
            alert("SABOTAGE erfolgreich gegen " + r.name + "! Marktanteil faellt, ein Standort zerstoert.");
        } else {
            reputation = Math.max(0, reputation - 8);
            alert("PR-SKANDAL! Sabotage gegen " + r.name + " aufgeflogen - Reputation -8.");
        }
    }

    public void counterPriceWar() {
        if (priceWarDays > 0) priceWarCounter = true;
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
        double v = 10_000_000 + r.marketShare * 100_000_000;
        return r.insolvent ? v * 0.2 : v;
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
        webServiceName = o.webServiceName;
        webServiceNamed = o.webServiceNamed;
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
        pendingBuildType = o.pendingBuildType;
        heads.clear(); heads.addAll(o.heads);
        workersHousing = o.workersHousing;
        airFreight = o.airFreight;
        railHubs.clear(); railHubs.addAll(o.railHubs);
        priceWarDays = o.priceWarDays;
        priceWarRival = o.priceWarRival;
        priceWarCounter = o.priceWarCounter;
        missionsDone.clear(); missionsDone.addAll(o.missionsDone);
        eventFlags.clear(); eventFlags.addAll(o.eventFlags);
        sabotageCooldown = o.sabotageCooldown;
        difficulty = o.difficulty;
        fleetDown.clear(); fleetDown.putAll(o.fleetDown);
        cityRating.clear(); cityRating.putAll(o.cityRating);
        flashSaleDays = o.flashSaleDays;
        flashSaleCategory = o.flashSaleCategory;
        subsidiaries.clear(); subsidiaries.addAll(o.subsidiaries);
        achievements.clear(); achievements.addAll(o.achievements);
        lastFillRate = o.lastFillRate;
        refurbStock.clear(); refurbStock.putAll(o.refurbStock);
        revBy.clear(); revBy.putAll(o.revBy);
        costBy.clear(); costBy.putAll(o.costBy);
        primeSubscribers = o.primeSubscribers;
        primeVideo = o.primeVideo;
        primeMusic = o.primeMusic;
        primeFresh = o.primeFresh;
    }
}
