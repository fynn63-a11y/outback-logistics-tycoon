package com.outback.tycoon.model;

import com.outback.tycoon.core.Game;

public enum Scenario {
    ENDLESS("Endlos-Sandbox", "Freies Spiel ab 1994 ohne festes Ziel.") {
        @Override
        public boolean isWon(Game g) {
            return false;
        }
    },
    IPO_RACE("IPO-Rennen", "Schaffe den Börsengang an der ASX Sydney - der IPO ist der Sieg.") {
        @Override
        public boolean isWon(Game g) {
            return g.ipoDone;
        }
    },
    PRIME_SURVIVAL("Prime-Day-Überleben", "Überlebe deinen ersten Prime Day ohne Server-Crash.") {
        @Override
        public boolean isWon(Game g) {
            return g.primeDaySurvived;
        }
    },
    MONOPOLY("Monopol", "Übernehme alle Konkurrenten - null verbleibende Rivals = Sieg.") {
        @Override
        public boolean isWon(Game g) {
            return g.rivals.isEmpty() && g.dayIndex > 30;
        }
    };

    public final String label;
    public final String description;

    Scenario(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public abstract boolean isWon(Game g);
}
