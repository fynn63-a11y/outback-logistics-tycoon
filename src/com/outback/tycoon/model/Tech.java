package com.outback.tycoon.model;

public enum Tech {
    HIGH_SPEED_CONVEYORS("High-Speed-Rollbahnen", 40, "+30% Durchsatz aller Standorte", 0, null, 0),
    KIVA_ROBOTS("Kiva-Roboter", 80, "+50% Durchsatz, schaltet Kiva-Roboter im Grid frei", 2, null, 0),
    SMART_SLOTTING("Smart Slotting AI", 60, "+25% Grid-Effizienz", 1, Bonus.THROUGHPUT, 0.25),
    OWS_CLOUD("OWS Cloud / WebServices", 100, "+200.000 Serverkapazität, Webshop x1.15, WebServices-Einnahmen", 2, Bonus.SERVER, 200_000),
    OWN_FLEET("Eigene Flotte", 120, "-40% Frachtkosten", 1, Bonus.COST, 0.40),
    AUTOMATION_PROTOCOLS("Automatisierungs-Protokolle", 50, "Schaltet Autopilot-Features frei", 1, null, 0),
    PATENT_PROTECTION("Patentschutz", 70, "Schützt F&E vor Industriespionage", 1, Bonus.SECURITY, 3),
    SORTATION_AI("Sortier-KI", 90, "Schaltet Paketsortierer & Sortierzentrum frei", 2, Bonus.THROUGHPUT, 0.05),
    PALLETIZING("Paketstapler-Roboter", 70, "Schaltet Paketstapler frei", 2, Bonus.THROUGHPUT, 0.03),
    WRAPPING("Stretch-Wickel-Automatisierung", 50, "Schaltet Wickelmaschine frei", 1, null, 0),

    EC_1CLICK("1-Click-Checkout", 60, "Schnellkauf: +3% Nachfrage", 1, Bonus.DEMAND, 0.03),
    EC_ANTICIPATORY("Anticipatory Shipping (KI-Vorhersage)", 120, "+5% Nachfrage, Ware schon vor Bestellung im Regional-Lager", 2, Bonus.DEMAND, 0.05),
    EC_RECO("Recommendation Engine", 80, "+4% Nachfrage", 1, Bonus.DEMAND, 0.04),
    EC_DYNPricing("Dynamische Preisanpassung (Echtzeit)", 90, "+3% Nachfrage", 1, Bonus.DEMAND, 0.03),
    EC_FRAUD("ML-Betrugserkennung", 70, "-Sicherheitsrisiko", 1, Bonus.SECURITY, 2),
    EC_API("API-Gateway (Seller Central)", 100, "+4% Nachfrage (Drittanbieter)", 1, Bonus.DEMAND, 0.04),
    EC_VENDOR("Vendor Central (Direktlieferanten)", 90, "-3% Einkaufskosten", 1, Bonus.COST, 0.03),
    EC_AB("A/B-Testing-Infrastruktur", 60, "+2% Nachfrage", 1, Bonus.DEMAND, 0.02),
    EC_CDN("CDN (Content Delivery)", 110, "+50.000 Serverkapazität", 2, Bonus.SERVER, 50_000),
    EC_SYNC("Echtzeit-Bestandssynchronisation", 80, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    EC_ADDR("Adressvalidierung", 50, "-2% Versandkosten (Fehlerkorrektur)", 0, Bonus.COST, 0.02),
    EC_VOICE("Voice Shopping", 100, "+3% Nachfrage", 2, Bonus.DEMAND, 0.03),
    EC_SUBMGMT("Abo-Verwaltungssystem", 70, "+3% Nachfrage", 1, Bonus.DEMAND, 0.03),
    EC_RMA("Automatisiertes RMA (Retouren)", 80, "-2% Retourenkosten", 1, Bonus.COST, 0.02),
    EC_CHATBOT("KI-Chatbots Kundenservice", 90, "+2% Nachfrage", 2, Bonus.DEMAND, 0.02),
    EC_DB("Verteilte Datenbank (Warenkorb)", 130, "+80.000 Serverkapazität", 2, Bonus.SERVER, 80_000),
    EC_ROUTING("Order-Routing-Algorithmus", 110, "+5% Durchsatz (optimales FC)", 2, Bonus.THROUGHPUT, 0.05),
    EC_CROSSDOCK("Cross-Docking-Logik", 100, "+4% Durchsatz (Umverteilung ohne Einlagerung)", 1, Bonus.THROUGHPUT, 0.04),
    EC_WMS("WMS (Warehouse Management System)", 140, "+6% Durchsatz", 2, Bonus.THROUGHPUT, 0.06),
    EC_TMS("TMS (Transportation Management)", 120, "-5% Frachtkosten", 1, Bonus.COST, 0.05),
    EC_SLA("SLA-Monitoring-Dashboards", 70, "+2% Nachfrage", 1, Bonus.DEMAND, 0.02),
    EC_TWIN("Digital Twin (virtuelle Lager-Simulation)", 160, "+8% Durchsatz", 3, Bonus.THROUGHPUT, 0.08),

    IN_ASN("ASN-Verarbeitung (Advanced Shipping Notice)", 50, "+3% Wareneingangs-Durchsatz", 0, Bonus.THROUGHPUT, 0.03),
    IN_DOCK("Automatisierte Dock-Türen", 70, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    IN_YARD("Yard Management System", 90, "-3% Frachtkosten", 1, Bonus.COST, 0.03),
    IN_AGV("AGV-Lkw-Entladung", 120, "+4% Durchsatz", 2, Bonus.THROUGHPUT, 0.04),
    IN_TELE("Teleskop-Förderbänder", 80, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    IN_PALLET("Palettierer + Depalettierer (Roboterarme)", 140, "+5% Durchsatz", 2, Bonus.THROUGHPUT, 0.05),
    IN_RFID("RFID-Tunnel-Scanner", 110, "+4% Durchsatz (Massenlesung)", 2, Bonus.THROUGHPUT, 0.04),
    IN_CUBI("Cubiscan (3D-Volumen/Gewicht)", 90, "-2% Versandkosten", 1, Bonus.COST, 0.02),
    IN_CAM("KI-Kameras Schadenskontrolle", 100, "-2% Warenverluste", 2, Bonus.COST, 0.02),
    IN_VNA("VNA-Stapler (schienengeführt)", 120, "+3% Durchsatz (Hochregal)", 2, Bonus.THROUGHPUT, 0.03),
    IN_DIVERT("Förderband-Weichen (Divert Gates)", 80, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    IN_SHOE("Schuhsorter (Slide Shoe Sorter)", 150, "+5% Sortier-Durchsatz", 2, Bonus.THROUGHPUT, 0.05),

    ST_RANDOM("Random Stow (chaotische Lagerhaltung)", 60, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    ST_PODS("Mobile Regal-Pods", 100, "+4% Durchsatz", 2, Bonus.THROUGHPUT, 0.04),
    ST_FID("Fiducial-Marker (Boden-Navigation)", 90, "+3% Durchsatz", 2, Bonus.THROUGHPUT, 0.03),
    ST_G2P("Goods-to-Person Workstations", 130, "+4% Durchsatz", 2, Bonus.THROUGHPUT, 0.04),
    ST_PTL("Put-to-Light", 80, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    ST_ASRS("AS/RS (autom. Hochregal-Krane)", 170, "+6% Durchsatz", 3, Bonus.THROUGHPUT, 0.06),
    ST_CAPAI("Kapazitätsoptimierungs-KI", 110, "+4% Durchsatz", 2, Bonus.THROUGHPUT, 0.04),
    ST_AGC("AGC (Kleinteile-Transporter)", 120, "+3% Durchsatz", 2, Bonus.THROUGHPUT, 0.03),
    ST_PREDICT("Predictive Maintenance (Bänder)", 90, "weniger Ausfälle", 1, Bonus.SECURITY, 1),
    ST_DRONE("Inventur-Drohnen im Lager", 140, "Bestandsprüfung per Drohne", 3, Bonus.SECURITY, 1),

    PK_BATCH("Batch-Picking", 70, "+4% Pick-Durchsatz", 1, Bonus.THROUGHPUT, 0.04),
    PK_WAVE("Wave-Picking", 60, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    PK_ZONE("Zone-Picking", 60, "+3% Durchsatz", 1, Bonus.THROUGHPUT, 0.03),
    PK_VOICE("Voice Picking", 80, "+3% Durchsatz", 2, Bonus.THROUGHPUT, 0.03),
    PK_SPARROW("Sparrow (KI-Greifarm Artikel)", 180, "+6% Durchsatz", 3, Bonus.THROUGHPUT, 0.06),
    PK_ROBIN("Robin (KI-Sortierarm)", 160, "+5% Durchsatz", 3, Bonus.THROUGHPUT, 0.05),
    PK_CV("CV-Pickfehler-Erkennung", 130, "Fehler runter", 2, Bonus.SECURITY, 1),
    PK_COBOT("Cobots (schwere Teile)", 150, "+4% Durchsatz", 3, Bonus.THROUGHPUT, 0.04),

    PA_CARTON("Cartonization (passgenaue Kartons)", 90, "-3% Verpackungskosten", 1, Bonus.COST, 0.03),
    PA_PACKSIZE("Packsize (Karton Echtzeit-Zuschnitt)", 120, "-3% Verpackungskosten", 2, Bonus.COST, 0.03),
    PA_SLAM("SLAM-Linie (Scan-Label-Apply-Manifest)", 130, "+4% Pack-Durchsatz", 2, Bonus.THROUGHPUT, 0.04),
    PA_POD("Print-on-Demand (Rechnungen im Packprozess)", 80, "+2% Nachfrage", 1, Bonus.DEMAND, 0.02),
    PA_HAZMAT("Hazmat-Routing beim Packen", 100, "-2% Gefahrgut-Kosten", 1, Bonus.COST, 0.02),

    SO_PEGASUS("Pegasus (Sortier-Roboter)", 160, "+5% Sortier-Durchsatz", 3, Bonus.THROUGHPUT, 0.05),
    SO_CROSSBELT("Cross-Belt Sorter", 180, "+6% Sortier-Durchsatz", 3, Bonus.THROUGHPUT, 0.06),
    SO_TILT("Tilt-Tray-Sortierer", 140, "+4% Durchsatz", 2, Bonus.THROUGHPUT, 0.04),
    SO_VISION("Vision-Tunnel (6-Seiten-Scan)", 120, "+3% Durchsatz", 2, Bonus.THROUGHPUT, 0.03),
    SO_JAM("Jam-Detection (Stauerkennung)", 80, "weniger Stillstände", 1, Bonus.SECURITY, 1),
    SO_SING("Singulator + Gapping", 100, "+3% Durchsatz", 2, Bonus.THROUGHPUT, 0.03),

    OB_DOCKALGO("Dock-Door-Assignment", 90, "-3% Logistikkosten", 1, Bonus.COST, 0.03),
    OB_MIDDLE("Middle-Mile-Routenoptimierung", 130, "-4% Transportkosten", 2, Bonus.COST, 0.04),
    OB_GPS("GPS-Echtzeit-Tracking Flotte", 100, "+2% Nachfrage (Tracking)", 1, Bonus.DEMAND, 0.02),
    OB_AIR("Eigene Air-Flotte", 200, "-4% Express-Fracht", 3, Bonus.COST, 0.04),
    OB_GEO("Geofencing (Auto-Check-in)", 80, "-2% Logistikkosten", 1, Bonus.COST, 0.02),
    OB_WX("Wetter-/Verkehrs-Prädiktions-KI", 120, "-3% Transit-Kosten", 2, Bonus.COST, 0.03),

    LM_STATION("Delivery Stations (letzte Meile)", 110, "+3% Last-Mile-Nachfrage", 1, Bonus.DEMAND, 0.03),
    LM_ROUTE("Routenoptimierungs-KI (sekündlich)", 140, "-4% Zustellkosten", 2, Bonus.COST, 0.04),
    LM_FLEX("Gig-Fahrer-App (Flex)", 90, "-3% Zustellkosten", 1, Bonus.COST, 0.03),
    LM_EDV("Elektro-Transporter (EDV)", 170, "-5% Zustellkosten", 3, Bonus.COST, 0.05),
    LM_LOCKER("Hub-Locker (Packstationen)", 120, "+4% Nachfrage", 2, Bonus.DEMAND, 0.04),
    LM_KEY("In-Home/In-Garage Delivery", 150, "+3% Premium-Nachfrage", 3, Bonus.DEMAND, 0.03),
    LM_DRONE("Lieferdrohnen (30-Min-Zustellung)", 220, "+5% Same-Day-Nachfrage", 3, Bonus.DEMAND, 0.05),
    LM_SCOUT("Gehweg-Roboter (Scout)", 160, "+2% Nachfrage", 3, Bonus.DEMAND, 0.02),
    LM_OTP("OTP + PoD-Kameras (Beweiszustellung)", 110, "Sicherheit hoch", 2, Bonus.SECURITY, 2),

    IO_IOT("IoT Core (Millionen Sensoren)", 150, "+100.000 Serverkapazität", 2, Bonus.SERVER, 100_000),
    IO_RMS("Robotics Management System", 170, "+5% Durchsatz (alle Roboter koordiniert)", 3, Bonus.THROUGHPUT, 0.05),
    IO_EDGE("Edge-Computing im Lager", 140, "+60.000 Serverkapazität", 2, Bonus.SERVER, 60_000),
    IO_PSA("CV-PSA-Erkennung (Helm/Weste)", 100, "Sicherheit hoch", 2, Bonus.SECURITY, 1),
    IO_GRAFANA("Manager-Dashboards (Grafana/Kibana)", 80, "+2% Nachfrage (Transparenz)", 1, Bonus.DEMAND, 0.02),
    IO_UPS("USV/Notstrom für Server", 90, "+40.000 Serverkapazität", 1, Bonus.SERVER, 40_000),
    IO_ML("ML-Nachfrageprognose", 160, "+5% Nachfrage (Prognose)", 3, Bonus.DEMAND, 0.05),
    IO_WFM("Workforce-Management (Schichtplanung)", 100, "+3% Durchsatz", 2, Bonus.THROUGHPUT, 0.03),
    IO_DARK("Dark Stores (Same-Day-Mikro-Lager)", 150, "+3% Same-Day-Nachfrage", 3, Bonus.DEMAND, 0.03),
    IO_TOWER("Autonomous Supply Chain Control Tower", 200, "+6% Gesamtdurchsatz", 3, Bonus.THROUGHPUT, 0.06),
    IO_BUILDROBOT("Bau-Roboter (Auto-Platzierung nach Blaupause, -20% Baukosten)", 150, "Blaupausen automatisch zu 80% Preis anwenden", 3, Bonus.THROUGHPUT, 0.03);

    public enum Bonus { THROUGHPUT, COST, DEMAND, SERVER, SECURITY }

    public final String label;
    public final double rpCost;
    public final String description;
    public final int minEra;
    public final Bonus bonus;
    public final double bonusValue;

    Tech(String label, double rpCost, String description, int minEra, Bonus bonus, double bonusValue) {
        this.label = label;
        this.rpCost = rpCost;
        this.description = description;
        this.minEra = minEra;
        this.bonus = bonus;
        this.bonusValue = bonusValue;
    }
}
