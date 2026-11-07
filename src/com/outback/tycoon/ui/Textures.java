package com.outback.tycoon.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Textures {
    private static final Map<String, TexturePaint> CACHE = new HashMap<>();

    private static double hash(int x, int y, long seed) {
        long h = seed * 0x9E3779B97F4A7C15L;
        h = h * 6364136223846793005L + x * 1442695040888963407L;
        h = h * 6364136223846793005L + y * 1442695040888963407L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return (h & 0xFFFFFFFFL) / 4294967296.0;
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    private static int mod(int a, int n) {
        int r = a % n;
        return r < 0 ? r + n : r;
    }

    public static double valueNoise(double x, double y, int period, long seed) {
        int x0 = mod((int) Math.floor(x), period);
        int y0 = mod((int) Math.floor(y), period);
        int x1 = mod(x0 + 1, period);
        int y1 = mod(y0 + 1, period);
        double fx = smooth(x - Math.floor(x));
        double fy = smooth(y - Math.floor(y));
        double v00 = hash(x0, y0, seed);
        double v10 = hash(x1, y0, seed);
        double v01 = hash(x0, y1, seed);
        double v11 = hash(x1, y1, seed);
        double top = v00 + (v10 - v00) * fx;
        double bottom = v01 + (v11 - v01) * fx;
        return top + (bottom - top) * fy;
    }

    public static double fbm(double x, double y, int basePeriod, long seed, int octaves) {
        double sum = 0;
        double amp = 1.0;
        double freq = 1.0;
        double norm = 0;
        int period = basePeriod;
        for (int o = 0; o < octaves; o++) {
            sum += amp * valueNoise(x * freq, y * freq, period, seed + o * 101);
            norm += amp;
            amp *= 0.5;
            freq *= 2.0;
            period *= 2;
        }
        return sum / norm;
    }

    public static BufferedImage generate(int size, Color c1, Color c2, int baseFreq, int octaves, long seed) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                double n = fbm(px * baseFreq / (double) size, py * baseFreq / (double) size, baseFreq, seed, octaves);
                double grain = hash(px, py, seed + 999) * 0.06;
                double t = Math.max(0, Math.min(1, n + grain - 0.03));
                int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * t);
                int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
                int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
                img.setRGB(px, py, new Color(r, g, b).getRGB());
            }
        }
        return img;
    }

    public static BufferedImage land() {
        return generate(128, new Color(198, 160, 98), new Color(146, 106, 58), 4, 4, 11);
    }

    public static BufferedImage water() {
        return generate(128, new Color(26, 66, 116), new Color(9, 28, 58), 6, 3, 7);
    }

    public static BufferedImage floorA() {
        return generate(64, new Color(56, 62, 76), new Color(46, 52, 64), 8, 3, 21);
    }

    public static BufferedImage floorB() {
        return generate(64, new Color(50, 56, 68), new Color(41, 46, 58), 8, 3, 33);
    }

    public static BufferedImage metal() {
        return generate(64, new Color(150, 155, 165), new Color(110, 115, 128), 16, 2, 47);
    }

    public static TexturePaint paint(String name, BufferedImage img) {
        return CACHE.computeIfAbsent(name, k -> new TexturePaint(img, new Rectangle(0, 0, img.getWidth(), img.getHeight())));
    }

    public static TexturePaint landPaint() {
        return paint("land", land());
    }

    public static TexturePaint waterPaint() {
        return paint("water", water());
    }

    public static TexturePaint floorAPaint() {
        return paint("floorA", floorA());
    }

    public static TexturePaint floorBPaint() {
        return paint("floorB", floorB());
    }

    public static TexturePaint metalPaint() {
        return paint("metal", metal());
    }
}
