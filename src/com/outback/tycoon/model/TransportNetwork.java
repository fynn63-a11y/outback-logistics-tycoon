package com.outback.tycoon.model;

public class TransportNetwork {
    public static final String[][] ROADS = {
            new String[]{"Brisbane", "Sydney"}, new String[]{"Sydney", "Melbourne"}, new String[]{"Sydney", "Newcastle"},
            new String[]{"Melbourne", "Adelaide"}, new String[]{"Adelaide", "Perth"}, new String[]{"Adelaide", "Alice Springs"},
            new String[]{"Alice Springs", "Darwin"}, new String[]{"Darwin", "Cairns"}, new String[]{"Brisbane", "Cairns"},
            new String[]{"Perth", "Kalgoorlie"}, new String[]{"Sydney", "Canberra"}, new String[]{"Brisbane", "Gold Coast"},
            new String[]{"Melbourne", "Geelong"}, new String[]{"Adelaide", "Whyalla"}, new String[]{"Cairns", "Townsville"},
    };
    public static final String[][] RAILS = {
            new String[]{"Sydney", "Melbourne"}, new String[]{"Melbourne", "Adelaide"}, new String[]{"Adelaide", "Alice Springs"},
            new String[]{"Alice Springs", "Darwin"}, new String[]{"Sydney", "Brisbane"}, new String[]{"Brisbane", "Cairns"},
            new String[]{"Perth", "Kalgoorlie"},
    };
    public static final String[][] AIR = {
            new String[]{"Sydney", "Melbourne"}, new String[]{"Sydney", "Brisbane"}, new String[]{"Sydney", "Perth"},
            new String[]{"Sydney", "Adelaide"}, new String[]{"Melbourne", "Perth"}, new String[]{"Brisbane", "Cairns"},
            new String[]{"Perth", "Darwin"}, new String[]{"Melbourne", "Adelaide"},
    };

    public enum Mode { ROAD, RAIL, AIR }

    public static class Route {
        public final String a, b;
        public final Mode mode;
        public final double capacity;
        public double load = 0;

        public Route(String a, String b, Mode mode, double capacity) {
            this.a = a;
            this.b = b;
            this.mode = mode;
            this.capacity = capacity;
        }

        public boolean connects(String x, String y) {
            return (a.equals(x) && b.equals(y)) || (a.equals(y) && b.equals(x));
        }

        public boolean congested() {
            return load > capacity * 0.8;
        }
    }
}
