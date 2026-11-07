package com.outback.tycoon.model;

public enum Tech {
    HIGH_SPEED_CONVEYORS("High-Speed-Rollbahnen", 40, "+30% Durchsatz aller Standorte"),
    KIVA_ROBOTS("Kiva-Roboter", 80, "+50% Durchsatz aller Standorte"),
    SMART_SLOTTING("Smart Slotting AI", 60, "+25% Grid-Effizienz (Laufweg-Optimierung)"),
    AWS_CLOUD("AWS Cloud", 100, "+200.000 Serverkapazitaet gegen Prime-Day-Crashes"),
    OWN_FLEET("Eigene Flotte", 120, "-40% Frachtkosten"),
    AUTOMATION_PROTOCOLS("Automatisierungs-Protokolle", 50, "Schaltet Autopilot-Features frei"),
    PATENT_PROTECTION("Patentschutz", 70, "Schuetzt F&E vor Industriespionage"),
    SORTATION_AI("Sortier-KI", 90, "Schaltet Paketsortierer & Sortierzentrum im Mikro-Grid frei"),
    PALLETIZING("Paketstapler-Roboter", 70, "Schaltet Paketstapler (Palletizer) frei"),
    WRAPPING("Stretch-Wickel-Automatisierung", 50, "Schaltet Wickelmaschine frei");

    public final String label;
    public final double rpCost;
    public final String description;

    Tech(String label, double rpCost, String description) {
        this.label = label;
        this.rpCost = rpCost;
        this.description = description;
    }
}
