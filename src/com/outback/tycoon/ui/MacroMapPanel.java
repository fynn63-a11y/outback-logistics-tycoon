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
import java.util.ArrayList;
import java.util.List;

public class MacroMapPanel extends JPanel {
    static final int VW = 2000;
    static final int VH = 1000;

    private static final double[][] AUSTRALIEN = {
            {113.5, -22.0}, {115.0, -20.5}, {122.0, -17.5}, {129.5, -14.5}, {130.5, -12.3},
            {136.0, -12.0}, {142.5, -10.7}, {142.7, -16.0}, {146.0, -19.0}, {149.5, -21.5},
            {152.8, -26.5}, {153.6, -30.0}, {150.5, -34.5}, {146.0, -39.0}, {141.0, -38.3},
            {139.5, -37.5}, {138.6, -35.1}, {136.0, -35.0}, {132.0, -32.0}, {129.0, -31.7},
            {124.0, -33.0}, {120.0, -33.9}, {115.5, -33.5}, {113.8, -28.5}, {113.5, -25.5}
    };
    private static final double SHENZHEN_X = (114.0 + 180) / 360.0;
    private static final double SHENZHEN_Y = (90 - 22.5) / 180.0;
    private static final String[] CAPITALS = {"Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide"};
    private static final double[][] REGION_BBOX = {
            {110, -44, 156, -10},
            {-12, 34, 50, 72},
            {-170, 23, -50, 74}
    };

    private final Game game;
    private final MainWindow window;
    private BufferedImage baseCache;
    private int mouseX = -1;
    private int mouseY = -1;
    private String regionView = "Australien";
    private final JComboBox<String> regionBox;

    public MacroMapPanel(Game game, MainWindow window) {
        this.game = game;
        this.window = window;
        setLayout(new BorderLayout());
        setBackground(new Color(8, 24, 48));
        regionBox = new JComboBox<>(new String[]{"Australien"});
        regionBox.addActionListener(e -> {
            regionView = (String) regionBox.getSelectedItem();
            repaint();
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Ansicht:"));
        top.add(regionBox);
        add(top, BorderLayout.NORTH);

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int[] v = toVirtual(e.getX(), e.getY());
                City nearest = null;
                double best = 42 * viewScale();
                for (City c : game.cities) {
                    double dx = c.mapX * VW - v[0];
                    double dy = c.mapY * VH - v[1];
                    double d = Math.hypot(dx, dy);
                    if (d < best) {
                        best = d;
                        nearest = c;
                    }
                }
                for (City c : game.cities) {
                    java.util.List<Site> cs = game.sitesIn(c.name);
                    if (cs.isEmpty()) continue;
                    double d = Math.hypot(c.mapX * VW - v[0], c.mapY * VH - v[1]);
                    if (d < 24 * viewScale()) {
                        game.selectedCity = c.name;
                        game.selectedSite = cs.get(0);
                        window.openMicro();
                        return;
                    }
                }
                if (game.pendingBuildType != null && nearest != null) {
                    game.selectedCity = nearest.name;
                    Site s = game.buildSite(game.pendingBuildType);
                    if (s != null) game.selectedSite = s;
                    game.pendingBuildType = null;
                    window.openMeso();
                    return;
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
        return (int) ((lon + 180) / 360.0 * VW);
    }

    private int py(double lat) {
        return (int) ((90 - lat) / 180.0 * VH);
    }

    private Path2D.Double continent(double[][] pts) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(px(pts[0][0]), py(pts[0][1]));
        for (int i = 1; i < pts.length; i++) p.lineTo(px(pts[i][0]), py(pts[i][1]));
        p.closePath();
        return p;
    }

    private double viewScale() {
        return (double) getWidth() / (px(156) - px(110));
    }

    private int[] viewOffset() {
        double s = viewScale();
        int oy = (int) ((getHeight() - VH * s) / 2);
        int ox = (int) ((getWidth() - (px(156) - px(110)) * s) / 2 - px(110) * s);
        return new int[]{ox, oy};
    }

    private int[] toVirtual(int mx, int my) {
        int[] o = viewOffset();
        double s = viewScale();
        return new int[]{(int) ((mx - o[0]) / s), (int) ((my - o[1]) / s)};
    }

    private static Color terrainColor(double h) {
        if (h < 0.42) return new Color(206, 188, 138);
        if (h < 0.52) return new Color(122, 168, 84);
        if (h < 0.66) return new Color(140, 152, 92);
        if (h < 0.82) return new Color(134, 104, 70);
        return new Color(238, 240, 245);
    }

    private void renderBase() {
        baseCache = new BufferedImage(VW, VH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = baseCache.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int y = 0; y < VH; y += 2) {
            for (int x = 0; x < VW; x += 2) {
                double depth = Textures.fbm(x * 0.004, y * 0.004, 8, 7, 3);
                int r = (int) (10 + depth * 30);
                int gg = (int) (28 + depth * 50);
                int b = (int) (58 + depth * 60);
                g.setColor(new Color(Math.min(60, r), Math.min(90, gg), Math.min(130, b)));
                g.fillRect(x, y, 2, 2);
            }
        }

        Path2D.Double au = continent(AUSTRALIEN);
        renderTerrain(g, au, 37);
        provinces(g, au);

        g.setColor(new Color(200, 225, 255, 70));
        g.setStroke(new BasicStroke(7f));
        g.draw(au);
        g.setColor(new Color(20, 16, 10));
        g.setStroke(new BasicStroke(3f));
        g.draw(au);

        g.setColor(new Color(225, 232, 242));
        g.setFont(g.getFont().deriveFont(Font.BOLD, 30f));
        g.drawString("AUSTRALIEN", px(122), py(-24));
        g.dispose();
    }

    private void renderTerrain(Graphics2D g, Path2D.Double land, long seed) {
        int step = 2;
        double[][] height = new double[VH / step + 2][VW / step + 2];
        for (int y = 0; y < height.length; y++)
            for (int x = 0; x < height[0].length; x++)
                height[y][x] = Textures.fbm(x * 0.045, y * 0.045, 8, seed, 4);
        BufferedImage img = new BufferedImage(VW, VH, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < VH; y++) {
            for (int x = 0; x < VW; x++) {
                if (!land.contains(x, y)) continue;
                double h = height[y / step][x / step];
                double hill = height[y / step][Math.min(height[0].length - 1, x / step + 1)] - height[y / step][Math.max(0, x / step - 1)];
                Color c = terrainColor(h);
                double light = 1.0 + hill * 6;
                img.setRGB(x, y, new Color(
                        (int) Math.max(0, Math.min(255, c.getRed() * light)),
                        (int) Math.max(0, Math.min(255, c.getGreen() * light)),
                        (int) Math.max(0, Math.min(255, c.getBlue() * light))).getRGB());
            }
        }
        g.drawImage(img, 0, 0, null);
    }

    private void provinces(Graphics2D g, Path2D.Double land) {
        int cell = 52;
        java.util.Random r = new java.util.Random(42);
        for (int y = cell / 2; y < VH; y += cell) {
            for (int x = cell / 2; x < VW; x += cell) {
                if (!land.contains(x, y)) continue;
                int v = r.nextInt(20) - 10;
                g.setColor(new Color(255, 250, 230, 5 + Math.abs(v) / 5));
                g.fillRect(x - cell / 2, y - cell / 2, cell, cell);
                g.setColor(new Color(30, 24, 14, 30));
                g.drawRect(x - cell / 2, y - cell / 2, cell, cell);
            }
        }
    }

    private Color regionColor(City c) {
        switch (c.region) {
            case "Europa": return new Color(120, 160, 255);
            case "Nordamerika": return new Color(130, 230, 140);
            default: return new Color(255, 170, 70);
        }
    }

    private boolean isCapital(String name) {
        for (String c : CAPITALS) if (c.equals(name)) return true;
        return false;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (baseCache == null) renderBase();
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] o = viewOffset();
        double s = viewScale();
        g.translate(o[0], o[1]);
        g.scale(s, s);
        g.drawImage(baseCache, 0, 0, null);

        drawTopBar(g);

        for (Shipment sh : game.shipments) {
            City c = game.cityByName(sh.destCity);
            double frac = Math.max(0, Math.min(1, 1.0 - sh.daysLeft / (double) Shipment.TRANSIT_DAYS));
            int sx = (int) (SHENZHEN_X * VW);
            int sy = (int) (SHENZHEN_Y * VH);
            int x = (int) (sx + (c.mapX * VW - sx) * frac);
            int y = (int) (sy + (c.mapY * VH - sy) * frac);
            g.setColor(new Color(255, 220, 120, 90));
            g.drawLine(sx, sy, x, y);
            g.setColor(new Color(255, 210, 80));
            g.fillOval(x - 6, y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.drawOval(x - 6, y - 6, 12, 12);
        }

        for (RivalCompany r : game.rivals) {
            for (int i = 0; i < r.siteCities.size(); i++) {
                City rc = game.cityByName(r.siteCities.get(i));
                g.setColor(r.color);
                g.fillRect((int) (rc.mapX * VW) - 12 - (i % 5) * 8, (int) (rc.mapY * VH) - 12 - (i % 5) * 5, 8, 8);
            }
        }

        for (City c : game.cities) {
            int x = (int) (c.mapX * VW);
            int y = (int) (c.mapY * VH);
            int r = (int) Math.max(6, Math.min(22, 7 + c.popMillions() * 2.5));
            boolean unlocked = game.isUnlocked(c);
            boolean selected = c.name.equals(game.selectedCity);
            boolean blocked = game.blockedCities.containsKey(c.name);
            List<Site> citySites = game.sitesIn(c.name);
            if (selected) {
                g.setColor(new Color(255, 235, 120));
                g.setStroke(new BasicStroke(4f));
                g.drawOval(x - r - 6, y - r - 6, (r + 6) * 2, (r + 6) * 2);
            }
            g.setColor(unlocked ? regionColor(c) : new Color(110, 110, 120));
            g.fillOval(x - r, y - r, r * 2, r * 2);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x - r, y - r, r * 2, r * 2);
            if (isCapital(c.name)) {
                g.setColor(new Color(255, 240, 150));
                drawStar(g, x, y, r + 9, 5);
            }
            if (blocked) {
                g.setColor(new Color(255, 70, 30));
                g.setFont(g.getFont().deriveFont(Font.BOLD, 26f));
                g.drawString("!", x - 7, y + 10);
            }
            for (int i = 0; i < citySites.size(); i++) {
                int sz = 7 + citySites.get(i).level * 2;
                g.setColor(new Color(90, 230, 140));
                g.fillRect(x - r - 5 - (sz + 3) * i - sz, y + r + 6, sz, sz);
                g.setColor(new Color(20, 60, 35));
                g.drawRect(x - r - 5 - (sz + 3) * i - sz, y + r + 6, sz, sz);
            }
            if (c.popMillions() >= 1.0 || selected || !citySites.isEmpty()) {
                String label = c.name + (unlocked ? "" : " (gesperrt)");
                g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
                int tw = g.getFontMetrics().stringWidth(label);
                g.setColor(new Color(10, 14, 22, 210));
                g.fillRoundRect(x + r + 6, y - 28, tw + 18, 46, 10, 10);
                g.setColor(Color.WHITE);
                g.drawString(label, x + r + 15, y - 12);
                g.setFont(g.getFont().deriveFont(18f));
                g.setColor(new Color(200, 210, 225));
                g.drawString(String.format("%,d Einw. | Standorte: %d", c.population, citySites.size()), x + r + 15, y + 10);
            }
        }

        if (game.pendingBuildType != null && mouseX >= 0) {
            int[] v = toVirtual(mouseX, mouseY);
            g.setColor(new Color(120, 255, 150, 130));
            g.fillRect(v[0] - 14, v[1] - 14, 28, 28);
            g.setColor(Color.WHITE);
            g.drawRect(v[0] - 14, v[1] - 14, 28, 28);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 20f));
            g.drawString("Platzieren: " + game.pendingBuildType.label + " (klicken)", v[0] + 20, v[1] - 16);
        }

        drawAnimations(g);

        if (game.isPrimoDay()) {
            g.setColor(new Color(255, 60, 40));
            g.setFont(g.getFont().deriveFont(Font.BOLD, 44f));
            g.drawString("PRIMO DAY! Traffic x3.5", VW / 2 - 260, 110);
        }
    }

    private void drawTopBar(Graphics2D g) {
        g.setColor(new Color(12, 16, 26, 225));
        g.fillRect(0, 0, VW, 64);
        g.setColor(new Color(70, 90, 130));
        g.drawLine(0, 64, VW, 64);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 26f));
        g.drawString(game.companyName + "  |  " + game.date() + " (Tag " + game.dayIndex + ")  |  "
                + String.format("Kasse: %,.0f AUD", game.cash) + "  |  " + game.eraName() + "  |  Ansicht: " + regionView, 26, 42);
        g.setFont(g.getFont().deriveFont(20f));
        g.setColor(new Color(255, 200, 90));
        g.drawString(game.isPrimoDay() ? "PRIMO DAY aktiv!" : "Naechster Primo Day in " + game.primeDayIn + " Tagen",
                VW - 640, 42);
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
            int cx = (int) (c.mapX * VW);
            int cy = (int) (c.mapY * VH);
            double orbit = t / 700.0 + i * 2.1;
            int dx = (int) (Math.cos(orbit) * 30);
            int dy = (int) (Math.sin(orbit) * 15);
            g.setColor(new Color(245, 248, 255));
            g.fillOval(cx + dx - 4, cy + dy - 4, 8, 8);
            g.setColor(new Color(120, 160, 220));
            g.drawOval(cx + dx - 4, cy + dy - 4, 8, 8);
        }

        List<String> truckCities = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : game.trucks.entrySet())
            if (e.getValue() > 0 && !game.fleetDown.containsKey(e.getKey())) truckCities.add(e.getKey());
        for (int i = 0; i < Math.min(5, truckCities.size()); i++) {
            City c = game.cityByName(truckCities.get(i));
            int cx = (int) (c.mapX * VW);
            int cy = (int) (c.mapY * VH);
            double p = ((t / 1100.0) + i * 0.37) % 1.0;
            int tx = cx + (int) (Math.sin(p * Math.PI * 2 + i) * 50);
            int ty = cy + 30 + (int) (Math.cos(p * Math.PI * 2 + i) * 13);
            g.setColor(new Color(255, 215, 80));
            g.fillRect(tx - 11, ty - 6, 22, 13);
            g.setColor(new Color(60, 40, 10));
            g.fillRect(tx + 8, ty - 6, 5, 13);
            g.setColor(Color.BLACK);
            g.fillOval(tx - 8, ty + 7, 6, 6);
            g.fillOval(tx + 6, ty + 7, 6, 6);
        }

        drawPlane(g, game.cityByName("Sydney"), game.cityByName("Perth"), t, 0.0006);
        drawPlane(g, game.cityByName("Sydney"),
                new City("Shenzhen", "China", "Asien", 17_000_000, 0.5, SHENZHEN_X, SHENZHEN_Y), t, 0.00055);
    }

    private void drawPlane(Graphics2D g, City a, City b, long t, double speed) {
        if (b == null) return;
        double p = (t * speed) % 1.0;
        int x1 = (int) (a.mapX * VW);
        int y1 = (int) (a.mapY * VH);
        int x2 = (int) (b.mapX * VW);
        int y2 = (int) (b.mapY * VH);
        int px = (int) (x1 + (x2 - x1) * p);
        int py = (int) (y1 + (y2 - y1) * p - Math.sin(p * Math.PI) * 80);
        double ang = Math.atan2(y2 - y1, x2 - x1);
        g.setColor(new Color(235, 240, 250));
        g.fillPolygon(new int[]{px + (int) (18 * Math.cos(ang)), px + (int) (16 * Math.cos(ang + 2.5)), px + (int) (16 * Math.cos(ang - 2.5))},
                new int[]{py + (int) (18 * Math.sin(ang)), py + (int) (16 * Math.sin(ang + 2.5)), py + (int) (16 * Math.sin(ang - 2.5))}, 3);
    }
}
