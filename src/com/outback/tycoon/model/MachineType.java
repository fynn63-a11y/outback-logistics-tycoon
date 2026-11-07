package com.outback.tycoon.model;

public enum MachineType {
    SPAWNER("Spawner", 2_000, 1),
    CONVEYOR("Rollbahn", 500, 2),
    ERECTOR("Kartonaufrichter", 3_000, 2),
    PICK("Pick-Station", 4_000, 3),
    SEALER("Klebebandmaschine (Sealer)", 2_500, 2),
    STRAPPER("Umreifungsmaschine (Strapper)", 3_500, 2),
    RACK("Regal / Rack", 800, 1),
    DOCK("Export-Rampe", 6_000, 4);

    public final String label;
    public final double cost;
    public final int score;

    MachineType(String label, double cost, int score) {
        this.label = label;
        this.cost = cost;
        this.score = score;
    }
}
