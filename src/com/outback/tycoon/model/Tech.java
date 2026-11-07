package com.outback.tycoon.model;

public enum Tech {
    HIGH_SPEED_CONVEYORS("High-Speed-Rollbahnen", 40, "+30% Durchsatz aller Standorte", 0),
    KIVA_ROBOTS("Kiva-Roboter", 80, "+50% Durchsatz, schaltet Kiva-Roboter im Grid frei (ab Cloud-Ära)", 2),
    SMART_SLOTTING("Smart Slotting AI", 60, "+25% Grid-Effizienz (Laufweg-Optimierung)", 1),
    OWS_CLOUD("OWS Cloud / WebServices", 100, "+200.000 Serverkapazität, Webshop x1.15, WebServices-Einnahmen (ab Cloud-Ära)", 2),
    OWN_FLEET("Eigene Flotte", 120, "-40% Frachtkosten", 1),
    AUTOMATION_PROTOCOLS("Automatisierungs-Protokolle", 50, "Schaltet Autopilot-Features frei (ab Breitband-Ära)", 1),
    PATENT_PROTECTION("Patentschutz", 70, "Schützt F&E vor Industriespionage", 1),
    SORTATION_AI("Sortier-KI", 90, "Schaltet Paketsortierer & Sortierzentrum im Mikro-Grid frei", 2),
    PALLETIZING("Paketstapler-Roboter", 70, "Schaltet Paketstapler (Palletizer) frei", 2),
    WRAPPING("Stretch-Wickel-Automatisierung", 50, "Schaltet Wickelmaschine frei", 1);

    public final String label;
    public final double rpCost;
    public final String description;
    public final int minEra;

    Tech(String label, double rpCost, String description, int minEra) {
        this.label = label;
        this.rpCost = rpCost;
        this.description = description;
        this.minEra = minEra;
    }
}
