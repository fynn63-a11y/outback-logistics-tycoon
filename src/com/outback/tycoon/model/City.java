package com.outback.tycoon.model;

public class City {
    public final String name;
    public final String state;
    public final String region;
    public final long population;
    public final double primeQuote;
    public final double mapX;
    public final double mapY;

    public City(String name, String state, String region, long population, double primeQuote, double mapX, double mapY) {
        this.name = name;
        this.state = state;
        this.region = region;
        this.population = population;
        this.primeQuote = primeQuote;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public double popMillions() {
        return population / 1_000_000.0;
    }
}
