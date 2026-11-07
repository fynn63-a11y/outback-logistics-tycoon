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
    DOCK("Lade- / Export-Rampe (Versand)", 6_000, 4, null, Stage.SHIP),
    AGV_BOT("AGV-Entladeroboter (Tech: AGV-Lkw-Entladung)", 25_000, 6, Tech.IN_AGV, Stage.INBOUND),
    RFID_TUNNEL("RFID-Tunnel (Tech: RFID-Tunnel-Scanner)", 30_000, 6, Tech.IN_RFID, Stage.INBOUND),
    POD_STATION("Regal-Pod-Station (Tech: Mobile Regal-Pods)", 22_000, 5, Tech.ST_PODS, Stage.STORAGE),
    ASRS_CRANE("AS/RS-Hochregal-Kran (Tech: AS/RS)", 60_000, 9, Tech.ST_ASRS, Stage.STORAGE),
    SPARROW_ARM("Sparrow-KI-Greifarm (Tech: Sparrow)", 70_000, 9, Tech.PK_SPARROW, Stage.PICK),
    PACKSIZE("Packsize-Kartonzuschnitt (Tech: Packsize)", 28_000, 6, Tech.PA_PACKSIZE, Stage.PACK),
    PEGASUS("Pegasus-Sortierroboter (Tech: Pegasus)", 55_000, 8, Tech.SO_PEGASUS, Stage.SORT),
    DRONE_PAD("Lieferdrohnen-Landeplatz (Tech: Lieferdrohnen)", 45_000, 7, Tech.LM_DRONE, Stage.SHIP),
    RMS_DESK("RMS-Leitstand (Tech: Robotics Management)", 35_000, 7, Tech.IO_RMS, Stage.MOVE);

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
        return this == ROBOT || this == PARCEL_SORTER || this == SORT_CENTER || this == PALLETIZER || this == WRAPPER
                || this == AGV_BOT || this == RFID_TUNNEL || this == POD_STATION || this == ASRS_CRANE
                || this == SPARROW_ARM || this == PEGASUS || this == DRONE_PAD;
    }
}
