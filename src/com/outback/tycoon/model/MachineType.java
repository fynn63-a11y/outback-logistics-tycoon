package com.outback.tycoon.model;

public enum MachineType {
    DOCK("Anlieferungs-Rampe", 6_000, 4, null, Stage.DELIVERY),
    AGV_BOT("AGV-Entladeroboter", 25_000, 6, Tech.IN_AGV, Stage.UNLOAD),
    RFID_TUNNEL("RFID-Tunnel (Buchen/ASN)", 30_000, 6, Tech.IN_RFID, Stage.BOOKING),
    BOOKING_DESK("Buchungs-Terminal (ASN)", 1_500, 2, null, Stage.BOOKING),
    SPAWNER("Wareneingang (Spawner)", 2_000, 1, null, Stage.INBOUND),
    CONVEYOR("Förderband / Rollbahn", 500, 2, null, Stage.MOVE),
    TROLLEY("Rollwagen", 300, 1, null, Stage.MOVE),
    RMS_DESK("RMS-Leitstand", 35_000, 7, Tech.IO_RMS, Stage.MOVE),
    RACK("Regal / Rack (Einlagern)", 800, 1, null, Stage.STOW),
    POD_STATION("Regal-Pod-Station", 22_000, 5, Tech.ST_PODS, Stage.STOW),
    ASRS_CRANE("AS/RS-Hochregal-Kran", 60_000, 9, Tech.ST_ASRS, Stage.STOW),
    PICK("Pick-Station (Kommissionierung)", 4_000, 3, null, Stage.PICK),
    ROBOT("Roboter (Kiva)", 15_000, 5, Tech.KIVA_ROBOTS, Stage.PICK),
    SPARROW_ARM("Sparrow-KI-Greifarm", 70_000, 9, Tech.PK_SPARROW, Stage.PICK),
    PACK_TABLE("Packtisch (manuell)", 1_200, 2, null, Stage.PACK),
    ERECTOR("Kartonaufrichter", 3_000, 2, null, Stage.PACK),
    SEALER("Klebebandmaschine (Sealer)", 2_500, 2, null, Stage.PACK),
    WRAPPER("Wickelmaschine (Stretch-Wrapper)", 4_500, 3, Tech.WRAPPING, Stage.PACK),
    STRAPPER("Umreifungsmaschine (Strapper)", 3_500, 2, null, Stage.PACK),
    PACKSIZE("Packsize-Kartonzuschnitt", 28_000, 6, Tech.PA_PACKSIZE, Stage.PACK),
    PARCEL_SORTER("Paketsortierer", 12_000, 5, Tech.SORTATION_AI, Stage.SORT),
    SORT_CENTER("Sortierzentrum", 20_000, 6, Tech.SORTATION_AI, Stage.SORT),
    PEGASUS("Pegasus-Sortierroboter", 55_000, 8, Tech.SO_PEGASUS, Stage.SORT),
    LOAD_DOCK("Verladetor (LKW)", 6_000, 4, null, Stage.LOAD),
    PALLETIZER("Paketstapler (Palette/Container)", 8_000, 4, Tech.PALLETIZING, Stage.LOAD),
    DRONE_PAD("Lieferdrohnen-Landeplatz (Versand)", 45_000, 7, Tech.LM_DRONE, Stage.SHIP),
    ESTOP("E-Stop-Sicherheitszone", 1_000, 1, null, Stage.MOVE),
    MEZZANINE("Mezzanin-Ebene (Zwischengeschoss, +50% Paletten)", 40_000, 8, null, Stage.STOW),
    DIVERT("Förderband-Weiche (Routing)", 2_500, 2, null, Stage.MOVE);

    public enum Stage {
        DELIVERY("Anlieferung"), UNLOAD("Entladen"), BOOKING("Buchen (ASN)"), INBOUND("Wareneingang"),
        MOVE("Transport"), STOW("Einlagern"), PICK("Kommissionieren"), PACK("Verpacken"),
        SORT("Sortierung"), LOAD("Verladen (LKW)"), SHIP("Versand");

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
