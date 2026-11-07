package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.City;
import com.outback.tycoon.model.RivalCompany;
import com.outback.tycoon.model.Shipment;
import com.outback.tycoon.model.Site;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

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
    private static final String[] CAPITALS = {"Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide",
            "London", "Berlin", "Rotterdam", "Los Angeles", "New York", "Toronto", "Vancouver"};

    private final Game game;
    private final MainWindow window;
    private BufferedImage baseCache;
    private int baseW = -1;
    private int baseH = -1;

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
                    if (Math.hypot(dx, dy) < best) {
                        best = Math.hypot(dx, dy);
                        nearest = c;
                    }
                }
                if (nearest != null) {
                    game.selectedCity = nearest.name;
                    window.openMeso();
                }
            }
        });
        Timer anim = new Timer(80, e -> repaint());
        anim.start();
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

    private void renderBase() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        baseW = w;
        baseH = h;
        baseCache = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = baseCache.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint ocean = new GradientPaint(0, 0, new Color(14, 44, 82), 0, h, new Color(5, 18, 40));
        g.setPaint(ocean);
        g.fillRect(0, 0, w, h);
        g.setPaint(Textures.waterPaint());
        g.fillRect(0, 0, w, h);

        Path2D.Double na = continent(NORDAMERIKA);
        Path2D.Double eu = continent(EUROPA);
        Path2D.Double au = continent(AUSTRALIEN);

        g.setPaint(Textures.landPaint());
        g.fill(na);
        g.fill(eu);
        g.fill(au);

        hillshade(g, na);
        hillshade(g, eu);
        hillshade(g, au);

        provinces(g, na);
        provinces(g, eu);
        provinces(g, au);

        g.setColor(new Color(30, 22, 12));
        g.setStroke(new BasicStroke(3f));
        g.draw(na);
        g.draw(eu);
        g.draw(au);
        g.setColor(new Color(120, 95, 55));
        g.setStroke(new BasicStroke(1f));
        g.draw(na);
        g.draw(eu);
        g.draw(au);

        g.setColor(new Color(210, 220, 235));
        g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
        g.drawString("NORDAMERIKA", px(-128), py(60));
        g.drawString("EUROPA", px(6), py(58));
        g.drawString("AUSTRALIEN", px(124), py(-24));
        g.dispose();
    }

    private void hillshade(Graphics2D g, Path2D.Double land) {
        int w = baseW;
        int h = baseH;
        int step = 3;
        double[][] height = new double[h / step + 2][w / step + 2];
        for (int y = 0; y < height.length; y++)
            for (int x = 0; x < height[0].length; x++)
                height[y][x] = Textures.fbm(x * 0.08, y * 0.08, 8, 99, 4);
        BufferedImage shade = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!land.contains(x, y)) continue;
                int gx = x / step;
                int gy = y / step;
                double hl = height[gy][Math.max(0, gx - 1)];
                double hr = height[gy][Math.min(height[0].length - 1, gx + 1)];
                double light = 0.82 + 0.36 * (hr - hl) * 8;
                int a = (int) Math.max(0, Math.min(90, (light - 0.82) * 220 + 18));
                shade.setRGB(x, y, new Color(255, 250, 230, a).getRGB());
            }
        }
        g.drawImage(shade, 0, 0, null);
    }

    private void provinces(Graphics2D g, Path2D.Double land) {
        int cell = 26;
        java.util.Random r = new java.util.Random(42);
        for (int y = cell / 2; y < baseH; y += cell) {
            for (int x = cell / 2; x < baseW; x += cell) {
                if (!land.contains(x, y)) continue;
                int v = r.nextInt(26) - 13;
                g.setColor(new Color(255, 245, 220, 10 + Math.abs(v) / 2));
                g.fillRect(x - cell / 2, y - cell / 2, cell, cell);
                g.setColor(new Color(60, 45, 25, 60));
                g.drawRect(x - cell / 2, y - cell / 2, cell, cell);
            }
        }
    }

    private Color regionColor(City c) {
        switch (c.region) {
            case "Europa": return new Color(90, 140, 230);
            case "Nordamerika": return new Color(110, 210, 130);
            default: return new Color(230, 150, 60);
        }
    }

    private boolean isCapital(String name) {
        for (String c : CAPITALS) if (c.equals(name)) return true;
        return false;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (baseCache == null || baseW != getWidth() || baseH != getHeight()) renderBase();
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(baseCache, 0, 0, null);

        drawTopBar(g);

        for (Shipment sh : game.shipments) {
            City c = game.cityByName(sh.destCity);
            double frac = Math.max(0, Math.min(1, 1.0 - sh.daysLeft / (double) Shipment.TRANSIT_DAYS));
            int sx = (int) (SHENZHEN_X * getWidth());
            int sy = (int) (SHENZHEN_Y * getHeight());
            int x = (int) (sx + (c.mapX * getWidth() - sx) * frac);
            int y = (int) (sy + (c.mapY * getHeight() - sy) * frac);
            g.setColor(new Color(255, 220, 120, 90));
            g.drawLine(sx, sy, x, y);
            g.setColor(new Color(255, 210, 80));
            g.fillOval(x - 4, y - 4, 8, 8);
            g.setColor(Color.WHITE);
            g.drawOval(x - 4, y - 4, 8, 8);
        }

        for (RivalCompany r : game.rivals) {
            for (int i = 0; i < r.siteCities.size(); i++) {
                City rc = game.cityByName(r.siteCities.get(i));
                g.setColor(r.color);
                g.fillRect((int) (rc.mapX * getWidth()) - 9 - (i % 5) * 5, (int) (rc.mapY * getHeight()) - 9 - (i % 5) * 3, 5, 5);
            }
        }

        for (City c : game.cities) {
            int x = (int) (c.mapX * getWidth());
            int y = (int) (c.mapY * getHeight());
            int r = (int) Math.max(4, Math.min(13, 4 + c.popMillions() * 1.5));
            boolean unlocked = game.isUnlocked(c);
            boolean selected = c.name.equals(game.selectedCity);
            boolean blocked = game.blockedCities.containsKey(c.name);
            List<Site> citySites = game.sitesIn(c.name);
            if (selected) {
                g.setColor(new Color(255, 235, 120));
                g.setStroke(new BasicStroke(2.5f));
                g.drawOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
            }
            g.setColor(unlocked ? regionColor(c) : new Color(90, 90, 100));
            g.fillOval(x - r, y - r, r * 2, r * 2);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(x - r, y - r, r * 2, r * 2);
            if (isCapital(c.name)) {
                g.setColor(new Color(255, 240, 150));
                drawStar(g, x, y, r + 5, 5);
            }
            if (blocked) {
                g.setColor(new Color(255, 70, 30));
                g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
                g.drawString("!", x - 4, y + 6);
            }
            for (int i = 0; i < citySites.size(); i++) {
                int sz = 4 + citySites.get(i).level;
                g.setColor(new Color(90, 230, 140));
                g.fillRect(x - r - 3 - (sz + 2) * i - sz, y + r + 4, sz, sz);
                g.setColor(new Color(20, 60, 35));
                g.drawRect(x - r - 3 - (sz + 2) * i - sz, y + r + 4, sz, sz);
            }
            if (c.popMillions() >= 1.0 || selected || !citySites.isEmpty()) {
                String label = c.name + (unlocked ? "" : " (gesperrt)");
                g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
                int tw = g.getFontMetrics().stringWidth(label);
                g.setColor(new Color(10, 14, 22, 200));
                g.fillRoundRect(x + r + 3, y - 16, tw + 10, 26, 6, 6);
                g.setColor(Color.WHITE);
                g.drawString(label, x + r + 8, y - 8);
                g.setFont(g.getFont().deriveFont(10f));
                g.setColor(new Color(200, 210, 225));
                g.drawString(String.format("%,d Einw. | Standorte: %d", c.population, citySites.size()), x + r + 8, y + 4);
            }
        }

        drawAnimations(g);

        if (game.isPrimoDay()) {
            g.setColor(new Color(255, 60, 40));
            g.setFont(g.getFont().deriveFont(Font.BOLD, 24f));
            g.drawString("PRIMO DAY! Traffic x3.5", getWidth() / 2 - 130, 56);
        }
    }

    private void drawTopBar(Graphics2D g) {
        g.setColor(new Color(12, 16, 26, 220));
        g.fillRect(0, 0, getWidth(), 34);
        g.setColor(new Color(70, 90, 130));
        g.drawLine(0, 34, getWidth(), 34);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        g.drawString(game.companyName + "  |  " + game.date() + " (Tag " + game.dayIndex + ")  |  "
                + String.format("Kasse: %,.0f AUD", game.cash) + "  |  " + game.eraName(), 14, 22);
        g.setFont(g.getFont().deriveFont(11f));
        g.setColor(new Color(255, 200, 90));
        g.drawString(game.isPrimoDay() ? "PRIMO DAY aktiv!" : "Naechster Primo Day in " + game.primeDayIn + " Tagen",
                getWidth() - 320, 22);
    }

    private void drawStar(Graphics2D g, int cx, int cy, int r, int points) {
        int[] xs = new int[points * 2];
        int[] ys = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double ang = Math.PI / 2 + i * Math.PI / points;
            double rad = (i % 2 == 0) ? r : r * 0.45;
            xs[i] = cx + (int) (Math.cos(ang) * rad);
            ys[i] = cy - (int) (Math.sin(ang) * rad);
        }
        g.fillPolygon(xs, ys, points * 2);
    }

    private void drawAnimations(Graphics2D g) {
        long t = System.currentTimeMillis();
        List<Site> ops = new ArrayList<>();
        for (Site s : game.sites) if (s.isOperational()) ops.add(s);

        for (int i = 0; i < ops.size(); i++) {
            Site s = ops.get(i);
            City c = game.cityByName(s.city);
            int cx = (int) (c.mapX * getWidth());
            int cy = (int) (c.mapY * getHeight());
            double orbit = t / 700.0 + i * 2.1;
            int dx = (int) (Math.cos(orbit) * 16);
            int dy = (int) (Math.sin(orbit) * 8);
            g.setColor(new Color(245, 248, 255));
            g.fillOval(cx + dx - 2, cy + dy - 2, 5, 5);
            g.setColor(new Color(120, 160, 220));
            g.drawOval(cx + dx - 2, cy + dy - 2, 5, 5);
        }

        List<String> truckCities = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : game.trucks.entrySet())
            if (e.getValue() > 0 && !game.fleetDown.containsKey(e.getKey())) truckCities.add(e.getKey());
        for (int i = 0; i < Math.min(5, truckCities.size()); i++) {
            City c = game.cityByName(truckCities.get(i));
            int cx = (int) (c.mapX * getWidth());
            int cy = (int) (c.mapY * getHeight());
            double p = ((t / 1100.0) + i * 0.37) % 1.0;
            int tx = cx + (int) (Math.sin(p * Math.PI * 2 + i) * 26);
            int ty = cy + 16 + (int) (Math.cos(p * Math.PI * 2 + i) * 7);
            g.setColor(new Color(255, 215, 80));
            g.fillRect(tx - 6, ty - 3, 12, 7);
            g.setColor(new Color(60, 40, 10));
            g.fillRect(tx + 4, ty - 3, 3, 7);
            g.setColor(Color.BLACK);
            g.fillOval(tx - 4, ty + 4, 3, 3);
            g.fillOval(tx + 3, ty + 4, 3, 3);
        }

        drawPlane(g, game.cityByName("Sydney"), game.cityByName("London"), t, 0.00045);
        drawPlane(g, game.cityByName("Sydney"), game.cityByName("Los Angeles"), t, 0.00035);
        drawPlane(g, game.cityByName("Sydney"),
                new City("Shenzhen", "China", "Asien", 17_000_000, 0.5, SHENZHEN_X, SHENZHEN_Y), t, 0.00055);
    }

    private void drawPlane(Graphics2D g, City a, City b, long t, double speed) {
        if (b == null) return;
        double p = (t * speed) % 1.0;
        int x1 = (int) (a.mapX * getWidth());
        int y1 = (int) (a.mapY * getHeight());
        int x2 = (int) (b.mapX * getWidth());
        int y2 = (int) (b.mapY * getHeight());
        int px = (int) (x1 + (x2 - x1) * p);
        int py = (int) (y1 + (y2 - y1) * p - Math.sin(p * Math.PI) * 40);
        double ang = Math.atan2(y2 - y1, x2 - x1);
        g.setColor(new Color(235, 240, 250));
        g.fillPolygon(new int[]{px + (int) (10 * Math.cos(ang)), px + (int) (9 * Math.cos(ang + 2.5)), px + (int) (9 * Math.cos(ang - 2.5))},
                new int[]{py + (int) (10 * Math.sin(ang)), py + (int) (9 * Math.sin(ang + 2.5)), py + (int) (9 * Math.sin(ang - 2.5))}, 3);
    }
}
