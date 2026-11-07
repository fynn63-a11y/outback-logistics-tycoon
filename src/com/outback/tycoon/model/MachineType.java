package com.outback.tycoon.model;

public enum MachineType {
    SPAWNER("Spawner (Wareneingang)", 2_000, 1, null, Stage.INBOUND),
    CONVEYOR("Förderband / Rollbahn", 500, 2, null, Stage.MOVE),
    TROLLEY("Rollwagen", 300, 1, null, Stage.MOVE),
    RACK("Regal / Rack (Lagerung)", 800, 1, null, Stage.STORAGE),
    PICK("Pick-Station (Kommissionierung)", 4_000, 3, null, Stage.PICK),
    ROBOT("Roboter (Kiva: Regal zum Packer)", 15_000, 5, Tech.KIVA_ROBOTS, Stage.PICK),
    ERECTOR("Kartonaufrichter", 3_000, 2, null, Stage.PACK),
    PACK_TABLE("Packtisch (manuell)", 1_200, 2, null, Stage.PACK),
    SEALER("Klebebandmaschine (Sealer)", 2_500, 2, null, Stage.PACK),
    WRAPPER("Wickelmaschine (Stretch-Wrapper)", 4_500, 3, Tech.WRAPPING, Stage.PACK),
    STRAPPER("Umreifungsmaschine (Strapper)", 3_500, 2, null, Stage.PACK),
    PALLETIZER("Paketstapler (Palette/Container)", 8_000, 4, Tech.PALLETIZING, Stage.SHIP),
    PARCEL_SORTER("Paketsortierer", 12_000, 5, Tech.SORTATION_AI, Stage.SORT),
    SORT_CENTER("Sortierzentrum", 20_000, 6, Tech.SORTATION_AI, Stage.SORT),
    DOCK("Lade- / Export-Rampe (Versand)", 6_000, 4, null, Stage.SHIP);

    public enum Stage {
        INBOUND("Wareneingang"), MOVE("Transport"), STORAGE("Lagerung"), PICK("Kommissionierung"),
        PACK("Verpackung"), SORT("Sortierung"), SHIP("Versand");

        public final String label;

        Stage(String label) {
            this.label = label;
        }
    }

    public final String label;
    public final double cost;
    public final int score;
    public final Tech requiredTech;
    public final Stage stage;

    MachineType(String label, double cost, int score, Tech requiredTech, Stage stage) {
        this.label = label;
        this.cost = cost;
        this.score = score;
        this.requiredTech = requiredTech;
        this.stage = stage;
    }

    public boolean isAutomated() {
        return this == ROBOT || this == PARCEL_SORTER || this == SORT_CENTER || this == PALLETIZER || this == WRAPPER;
    }
}
