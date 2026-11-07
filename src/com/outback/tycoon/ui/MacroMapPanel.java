package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.City;
import com.outback.tycoon.model.Shipment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

public class MacroMapPanel extends JPanel {
    private static final double[][] NORDAMERIKA = {
            {-168, 66}, {-140, 70}, {-125, 72}, {-110, 73}, {-95, 72}, {-85, 70}, {-80, 73},
            {-70, 62}, {-60, 52}, {-65, 45}, {-70, 42}, {-75, 35}, {-80, 30}, {-81, 25},
            {-90, 29}, {-97, 26}, {-105, 22}, {-110, 23}, {-115, 30}, {-125, 40}, {-130, 55},
            {-150, 60}, {-165, 60}
    };
    private static final double[][] EUROPA = {
            {-10, 36}, {-2, 37}, {5, 38}, {12, 38}, {20, 35}, {28, 36}, {30, 42}, {40, 45},
            {48, 46}, {40, 52}, {30, 55}, {38, 60}, {30, 70}, {18, 70}, {12, 65}, {5, 62},
            {0, 58}, {-5, 58}, {-10, 52}, {-5, 48}, {-2, 44}
    };
    private static final double[][] AUSTRALIEN = {
            {113.5, -22.0}, {115.0, -20.5}, {122.0, -17.5}, {129.5, -14.5}, {130.5, -12.3},
            {136.0, -12.0}, {142.5, -10.7}, {142.7, -16.0}, {146.0, -19.0}, {149.5, -21.5},
            {152.8, -26.5}, {153.6, -30.0}, {150.5, -34.5}, {146.0, -39.0}, {141.0, -38.3},
            {139.5, -37.5}, {138.6, -35.1}, {136.0, -35.0}, {132.0, -32.0}, {129.0, -31.7},
            {124.0, -33.0}, {120.0, -33.9}, {115.5, -33.5}, {113.8, -28.5}, {113.5, -25.5}
    };
    private static final double SHENZHEN_X = (114.0 + 180) / 360.0;
    private static final double SHENZHEN_Y = (90 - 22.5) / 180.0;

    private final Game game;
    private final MainWindow window;

    public MacroMapPanel(Game game, MainWindow window) {
        this.game = game;
        this.window = window;
        setBackground(new Color(8, 24, 48));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                City nearest = null;
                double best = 42;
                for (City c : game.cities) {
                    double dx = c.mapX * getWidth() - e.getX();
                    double dy = c.mapY * getHeight() - e.getY();
                    double d = Math.hypot(dx, dy);
                    if (d < best) {
                        best = d;
                        nearest = c;
                    }
                }
                if (nearest != null) {
                    game.selectedCity = nearest.name;
                    window.openMeso();
                }
            }
        });
    }

    private int px(double lon) {
        return (int) ((lon + 180) / 360.0 * getWidth());
    }

    private int py(double lat) {
        return (int) ((90 - lat) / 180.0 * getHeight());
    }

    private Path2D.Double continent(double[][] pts) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(px(pts[0][0]), py(pts[0][1]));
        for (int i = 1; i < pts.length; i++) p.lineTo(px(pts[i][0]), py(pts[i][1]));
        p.closePath();
        return p;
    }

    private Color regionColor(City c) {
        switch (c.region) {
            case "Europa": return new Color(90, 140, 230);
            case "Nordamerika": return new Color(110, 210, 130);
            default: return new Color(230, 150, 60);
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint ocean = new GradientPaint(0, 0, new Color(16, 48, 88), 0, getHeight(), new Color(6, 20, 44));
        g.setPaint(ocean);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setPaint(Textures.waterPaint());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setPaint(Textures.landPaint());
        g.fill(continent(NORDAMERIKA));
        g.fill(continent(EUROPA));
        g.fill(continent(AUSTRALIEN));
        g.setColor(new Color(110, 84, 46));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(continent(NORDAMERIKA));
        g.draw(continent(EUROPA));
        g.draw(continent(AUSTRALIEN));

        g.setColor(new Color(200, 215, 235));
        g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
        g.drawString("NORDAMERIKA", px(-125), py(58));
        g.drawString("EUROPA", px(8), py(56));
        g.drawString("AUSTRALIEN", px(126), py(-25));

        g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
        g.setColor(Color.WHITE);
        g.drawString("Weltkarte - Makro-Ebene (" + game.date().getYear() + ")", 16, 24);
        g.setFont(g.getFont().deriveFont(11f));
        g.drawString("Klicke eine freigeschaltete Stadt. Europa nach dem IPO, Nordamerika ab 200 Mio. AUD Marktkapitalisierung.", 16, 42);

        for (Shipment sh : game.shipments) {
            City c = game.cityByName(sh.destCity);
            double frac = 1.0 - sh.daysLeft / (double) Shipment.TRANSIT_DAYS;
            frac = Math.max(0, Math.min(1, frac));
            int sx = (int) (SHENZHEN_X * getWidth());
            int sy = (int) (SHENZHEN_Y * getHeight());
            int x = (int) (sx + (c.mapX * getWidth() - sx) * frac);
            int y = (int) (sy + (c.mapY * getHeight() - sy) * frac);
            g.setColor(new Color(255, 210, 80));
            g.fillOval(x - 3, y - 3, 6, 6);
        }

        for (City c : game.cities) {
            int x = (int) (c.mapX * getWidth());
            int y = (int) (c.mapY * getHeight());
            int r = (int) Math.max(3, Math.min(12, 3 + c.popMillions() * 1.5));
            boolean unlocked = game.isUnlocked(c);
            boolean selected = c.name.equals(game.selectedCity);
            boolean blocked = game.blockedCities.containsKey(c.name);
            java.util.List<com.outback.tycoon.model.Site> citySites = game.sitesIn(c.name);
            g.setColor(unlocked ? regionColor(c) : new Color(90, 90, 100));
            if (selected) {
                g.setColor(Color.YELLOW);
                g.fillOval(x - r - 3, y - r - 3, (r + 3) * 2, (r + 3) * 2);
                g.setColor(unlocked ? regionColor(c) : new Color(90, 90, 100));
            }
            g.fillOval(x - r, y - r, r * 2, r * 2);
            g.setColor(Color.WHITE);
            g.drawOval(x - r, y - r, r * 2, r * 2);
            if (blocked) {
                g.setColor(new Color(255, 80, 40));
                g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
                g.drawString("!", x - 3, y + 5);
            }
            for (int i = 0; i < citySites.size(); i++) {
                int sz = 3 + citySites.get(i).level;
                g.setColor(new Color(80, 220, 130));
                g.fillRect(x - r - 2 - (sz + 2) * i - sz, y + r + 3, sz, sz);
            }
            if (c.popMillions() >= 1.0 || selected || !citySites.isEmpty()) {
                g.setFont(g.getFont().deriveFont(10f));
                g.setColor(Color.WHITE);
                String label = c.name + (unlocked ? "" : " (gesperrt)");
                g.drawString(label, x + r + 4, y - 2);
                g.drawString(String.format("%,d Einw. | Prime %.0f%% | Standorte: %d",
                        c.population, c.primeQuote * 100, citySites.size()), x + r + 4, y + 11);
            }
        }

        if (game.isPrimeDay()) {
            g.setColor(Color.RED);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
            g.drawString("PRIME DAY! Traffic x3.5", getWidth() / 2 - 120, 30);
        }

        drawAnimations(g);
    }

    private void drawAnimations(Graphics2D g) {
        long t = System.currentTimeMillis();
        java.util.List<com.outback.tycoon.model.Site> ops = new java.util.ArrayList<>();
        for (com.outback.tycoon.model.Site s : game.sites) if (s.isOperational()) ops.add(s);

        for (int i = 0; i < ops.size(); i++) {
            com.outback.tycoon.model.Site s = ops.get(i);
            City c = game.cityByName(s.city);
            int cx = (int) (c.mapX * getWidth());
            int cy = (int) (c.mapY * getHeight());
            double orbit = t / 900.0 + i * 2.1;
            int dx = (int) (Math.cos(orbit) * 14);
            int dy = (int) (Math.sin(orbit) * 7);
            g.setColor(new Color(240, 240, 250));
            g.fillOval(cx + dx - 2, cy + dy - 2, 4, 4);
            g.setColor(new Color(200, 200, 220));
            g.drawOval(cx + dx - 2, cy + dy - 2, 4, 4);
        }

        java.util.List<String> truckCities = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : game.trucks.entrySet())
            if (e.getValue() > 0 && !game.fleetDown.containsKey(e.getKey())) truckCities.add(e.getKey());
        for (int i = 0; i < Math.min(4, truckCities.size()); i++) {
            City c = game.cityByName(truckCities.get(i));
            int cx = (int) (c.mapX * getWidth());
            int cy = (int) (c.mapY * getHeight());
            double p = ((t / 1400.0) + i * 0.33) % 1.0;
            int tx = cx + (int) (Math.sin(p * Math.PI * 2 + i) * 22);
            int ty = cy + 14 + (int) (Math.cos(p * Math.PI * 2 + i) * 6);
            g.setColor(new Color(255, 220, 90));
            g.fillRect(tx - 4, ty - 2, 8, 5);
            g.setColor(Color.BLACK);
            g.fillRect(tx + 2, ty - 2, 2, 5);
        }

        drawPlane(g, game.cityByName("Sydney"), game.cityByName("London"), t, 0.0009);
        drawPlane(g, game.cityByName("Sydney"), game.cityByName("Los Angeles"), t, 0.0007);
        drawPlane(g, game.cityByName("Sydney"),
                new City("Shenzhen", "China", "Asien", 17_000_000, 0.5, SHENZHEN_X, SHENZHEN_Y), t, 0.0011);
    }

    private void drawPlane(Graphics2D g, City a, City b, long t, double speed) {
        if (b == null) return;
        double p = (t * speed) % 1.0;
        int x1 = (int) (a.mapX * getWidth());
        int y1 = (int) (a.mapY * getHeight());
        int x2 = (int) (b.mapX * getWidth());
        int y2 = (int) (b.mapY * getHeight());
        int px = (int) (x1 + (x2 - x1) * p);
        int py = (int) (y1 + (y2 - y1) * p - Math.sin(p * Math.PI) * 30);
        double ang = Math.atan2(y2 - y1, x2 - x1);
        g.setColor(new Color(230, 235, 245));
        g.fillPolygon(new int[]{px + (int) (8 * Math.cos(ang)), px + (int) (7 * Math.cos(ang + 2.5)), px + (int) (7 * Math.cos(ang - 2.5))},
                new int[]{py + (int) (8 * Math.sin(ang)), py + (int) (7 * Math.sin(ang + 2.5)), py + (int) (7 * Math.sin(ang - 2.5))}, 3);
    }
}
