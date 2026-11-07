package com.outback.tycoon.model;

public enum Difficulty {
    EASY("Leicht", 800_000, 0.6, 0.6),
    NORMAL("Normal", 500_000, 1.0, 1.0),
    HARD("Schwer", 300_000, 1.6, 1.8);

    public final String label;
    public final double startCash;
    public final double disasterMult;
    public final double rivalMult;

    Difficulty(String label, double startCash, double disasterMult, double rivalMult) {
        this.label = label;
        this.startCash = startCash;
        this.disasterMult = disasterMult;
        this.rivalMult = rivalMult;
    }
}
