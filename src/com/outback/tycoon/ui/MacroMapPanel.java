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
    static final int VW = 3200;
    static final int VH = 1600;

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
    private BufferedImage peaksCache;
    private int mouseX = -1;
    private int mouseY = -1;
    private String regionView = "Australien";
    private boolean view3D = false;
    private int mapStyle = 0;
    private double zoom = 1.0;
    private City hoverCity = null;
    private double panX = 0;
    private double panY = 0;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private boolean panning = false;
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
        JButton styleBtn = new JButton("Stil: Satellit");
        styleBtn.addActionListener(e -> {
            mapStyle = 1 - mapStyle;
            styleBtn.setText(mapStyle == 0 ? "Stil: Satellit" : "Stil: Politisch-flach");
            baseCache = null;
            repaint();
        });
        top.add(styleBtn);
        JButton view3dBtn = new JButton("3D-Ansicht: aus");
        view3dBtn.addActionListener(e -> {
            view3D = !view3D;
            view3dBtn.setText("3D-Ansicht: " + (view3D ? "an" : "aus"));
            repaint();
        });
        top.add(view3dBtn);
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("-");
        zoomIn.addActionListener(e -> { zoom = Math.min(2.5, zoom + 0.25); repaint(); });
        zoomOut.addActionListener(e -> { zoom = Math.max(0.5, zoom - 0.25); repaint(); });
        top.add(new JLabel("Zoom:"));
        top.add(zoomOut);
        top.add(zoomIn);
        add(top, BorderLayout.NORTH);

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                int[] v = toVirtual(mouseX, mouseY);
                hoverCity = null;
                double best = 30 * viewScale();
                for (City c : game.cities) {
                    double d = Math.hypot(c.mapX * VW - v[0], c.mapY * VH - v[1]);
                    if (d < best) {
                        best = d;
                        hoverCity = c;
                    }
                }
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartX = e.getX();
                dragStartY = e.getY();
                panning = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (Math.hypot(e.getX() - dragStartX, e.getY() - dragStartY) > 6) panning = true;
                if (panning) {
                    panX += e.getX() - mouseX;
                    panY += e.getY() - mouseY;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (panning) return;
                mouseClicked(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    panX = 0;
                    panY = 0;
                    zoom = 1.0;
                    repaint();
                }
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
                    final City clicked = nearest;
                    game.selectedCity = clicked.name;
                    boolean terminal = game.hasContainerTerminal(clicked.name);
                    long ships = game.shipments.stream().filter(sh -> sh.destCity.equals(clicked.name)).count();
                    if (terminal || ships > 0) {
                        JOptionPane.showMessageDialog(MacroMapPanel.this,
                                "HAFEN " + clicked.name + ":\nContainer-Terminal: " + (terminal ? "vorhanden (Stau-Schwelle erhöht)" : "nein")
                                        + "\nAktive Seefrachten: " + ships
                                        + "\nGesperrt (Katastrophe): " + (game.blockedCities.containsKey(clicked.name) ? "JA (" + game.blockedCities.get(clicked.name) + " Tage)" : "nein"),
                                "Hafen-Info " + clicked.name, JOptionPane.INFORMATION_MESSAGE);
                    }
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
        double bw = px(156) - px(108);
        double bh = py(-8) - py(-44);
        return Math.min(getWidth() / bw, getHeight() / bh) * 0.96 * zoom;
    }

    private int[] viewOffset() {
        double s = viewScale();
        double bw = (px(156) - px(108)) * s;
        double bh = (py(-8) - py(-44)) * s;
        int ox = (int) ((getWidth() - bw) / 2 - px(108) * s) + (int) panX;
        int oy = (int) ((getHeight() - bh) / 2 - py(-44) * s) + (int) panY;
        return new int[]{ox, oy};
    }

    private java.awt.geom.AffineTransform viewTransform() {
        int[] o = viewOffset();
        double s = viewScale();
        java.awt.geom.AffineTransform t = new java.awt.geom.AffineTransform();
        t.translate(o[0], o[1]);
        if (view3D) {
            t.scale(s, s * 0.60);
            t.shear(0.22, 0);
        } else {
            t.scale(s, s);
        }
        return t;
    }

    private int[] toVirtual(int mx, int my) {
        try {
            java.awt.geom.AffineTransform inv = viewTransform().createInverse();
            java.awt.geom.Point2D p = inv.transform(new java.awt.geom.Point2D.Double(mx, my), null);
            return new int[]{(int) p.getX(), (int) p.getY()};
        } catch (Exception e) {
            return new int[]{mx, my};
        }
    }

    private static Color terrainColor(double h, double lon, double lat) {
        if (h < 0.42) return new Color(216, 196, 148);
        boolean desert = lon > 124 && lon < 141 && lat < -19 && lat > -31;
        boolean tropic = lat > -20;
        boolean tasmania = lat < -40;
        boolean coastForest = h < 0.52 && !desert;
        if (h < 0.52) return tasmania ? new Color(96, 140, 80) : tropic ? new Color(80, 158, 70) : desert ? new Color(198, 128, 66) : coastForest ? new Color(96, 148, 78) : new Color(122, 168, 84);
        if (h < 0.66) return desert ? new Color(186, 110, 56) : tasmania ? new Color(110, 140, 90) : new Color(140, 152, 92);
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
                int r = (int) (16 + depth * 40);
                int gg = (int) (52 + depth * 70);
                int b = (int) (110 + depth * 90);
                g.setColor(new Color(Math.min(70, r), Math.min(130, gg), Math.min(200, b)));
                g.fillRect(x, y, 2, 2);
            }
        }

        Path2D.Double au = continent(AUSTRALIEN);
        AUSTRALIEN_POLY = au;
        if (mapStyle == 1) {
            g.setPaint(new Color(148, 190, 110));
            g.fill(au);
            g.setPaint(Textures.paint("flatNoise", Textures.generate(128, new Color(150, 192, 112), new Color(136, 182, 100), 8, 2, 55)));
            g.fill(au);
        } else {
            renderTerrain(g, au, 37);
        }
        drawRiversAndLakes(g);
        drawNetwork(g, ROADS, new Color(120, 120, 128), 6f, false);
        drawNetwork(g, RAILS, new Color(40, 36, 34), 5f, true);
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

        peaksCache = new BufferedImage(VW, VH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = peaksCache.createGraphics();
        Path2D.Double au2 = continent(AUSTRALIEN);
        int step = 4;
        double[][] height = new double[VH / step + 2][VW / step + 2];
        for (int y = 0; y < height.length; y++)
            for (int x = 0; x < height[0].length; x++)
                height[y][x] = Textures.fbm(x * 0.045, y * 0.045, 8, 37, 4);
        for (int y = 0; y < VH; y++)
            for (int x = 0; x < VW; x++)
                if (au2.contains(x, y) && height[y / step][x / step] > 0.82) {
                    int a = (int) Math.min(255, (height[y / step][x / step] - 0.82) * 2200);
                    pg.setColor(new Color(255, 255, 255, a));
                    pg.fillRect(x, y, 1, 1);
                }
        pg.dispose();
    }

    private static boolean auNear(int x, int y) {
        return AUSTRALIEN_POLY != null && distToPoly(x, y) < 26;
    }

    private static double distToPoly(int x, int y) {
        double best = 1e9;
        for (int i = 0; i < AUSTRALIEN.length; i++) {
            double[] a = AUSTRALIEN[i];
            double[] b = AUSTRALIEN[(i + 1) % AUSTRALIEN.length];
            int ax = pxStatic(a[0]), ay = pyStatic(a[1]);
            int bx = pxStatic(b[0]), by = pyStatic(b[1]);
            double t = Math.max(0, Math.min(1, ((x - ax) * (bx - ax) + (y - ay) * (by - ay)) / (double) ((bx - ax) * (bx - ax) + (by - ay) * (by - ay) + 1)));
            double dx = x - (ax + t * (bx - ax));
            double dy = y - (ay + t * (by - ay));
            best = Math.min(best, Math.hypot(dx, dy));
        }
        return best;
    }

    private static int pxStatic(double lon) {
        return (int) ((lon + 180) / 360.0 * VW);
    }

    private static int pyStatic(double lat) {
        return (int) ((90 - lat) / 180.0 * VH);
    }

    private static Path2D.Double AUSTRALIEN_POLY;

    private static final double[][][] RIVERS = {
            new double[][]{{146.5,-31.5},{147.0,-32.5},{146.0,-34.0},{144.5,-35.5},{143.0,-36.5},{141.0,-37.5},{138.8,-35.1}}, // Murray
            new double[][]{{149.0,-30.5},{148.5,-28.5},{148.0,-26.5},{147.0,-24.0}}, // Darling
            new double[][]{{149.5,-20.0},{148.0,-19.0},{146.5,-17.5}}, // Flinders
            new double[][]{{138.0,-26.0},{136.0,-24.0},{134.5,-22.5}}, // Georgina/Diamantina
            new double[][]{{115.5,-26.0},{117.5,-24.5},{119.5,-23.0}}, // Gascoyne
            new double[][]{{151.0,-33.8},{150.5,-32.0}}, // Hawkesbury
    };
    private static final String[][] ROADS = {
            new String[]{"Brisbane", "Sydney"}, new String[]{"Sydney", "Melbourne"}, new String[]{"Sydney", "Newcastle"},
            new String[]{"Melbourne", "Adelaide"}, new String[]{"Adelaide", "Perth"}, new String[]{"Adelaide", "Alice Springs"},
            new String[]{"Alice Springs", "Darwin"}, new String[]{"Darwin", "Cairns"}, new String[]{"Brisbane", "Cairns"},
            new String[]{"Perth", "Kalgoorlie"}, new String[]{"Sydney", "Canberra"}, new String[]{"Brisbane", "Gold Coast"},
            new String[]{"Melbourne", "Geelong"}, new String[]{"Adelaide", "Whyalla"}, new String[]{"Cairns", "Townsville"},
    };
    private static final String[][] RAILS = {
            new String[]{"Sydney", "Melbourne"}, new String[]{"Melbourne", "Adelaide"}, new String[]{"Adelaide", "Alice Springs"},
            new String[]{"Alice Springs", "Darwin"}, new String[]{"Sydney", "Brisbane"}, new String[]{"Brisbane", "Cairns"},
            new String[]{"Perth", "Kalgoorlie"},
    };
    private static final String[][] AIRROUTES = {
            new String[]{"Sydney", "Melbourne"}, new String[]{"Sydney", "Brisbane"}, new String[]{"Sydney", "Perth"},
            new String[]{"Sydney", "Adelaide"}, new String[]{"Melbourne", "Perth"}, new String[]{"Brisbane", "Cairns"},
            new String[]{"Perth", "Darwin"}, new String[]{"Melbourne", "Adelaide"},
    };

    private static final double[][] LAKES = {
            new double[]{135.9, -28.5}, new double[]{135.0, -30.5}, // Lake Eyre North/South
            new double[]{141.5, -31.5}, // Lake Torrens-Region
            new double[]{146.8, -35.1}, // Lake Alexandrina
    };

    private void drawRiversAndLakes(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(60, 120, 180));
        for (double[][] river : RIVERS) {
            Path2D.Double p = new Path2D.Double();
            p.moveTo(px(river[0][0]), py(river[0][1]));
            for (int i = 1; i < river.length; i++) p.lineTo(px(river[i][0]), py(river[i][1]));
            g.draw(p);
        }
        g.setColor(new Color(70, 135, 195));
        for (double[] lake : LAKES) {
            g.fillOval(px(lake[0]) - 14, py(lake[1]) - 8, 28, 16);
        }
    }

    private void drawNetwork(Graphics2D g, String[][] edges, Color c, float w, boolean rail) {
        g.setColor(c);
        g.setStroke(rail ? new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 8f}, 0f) : new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (String[] e : edges) {
            City a = game.cityByName(e[0]);
            City b = game.cityByName(e[1]);
            int x1 = px((a.mapX * 360 - 180));
            int y1 = py(90 - a.mapY * 180);
            int x2 = px((b.mapX * 360 - 180));
            int y2 = py(90 - b.mapY * 180);
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(new BasicStroke(1f));
    }

    private int[] cityScreen(City c, int[] o, double s) {
        return new int[]{(int) (o[0] + c.mapX * VW * s), (int) (o[1] + c.mapY * VH * s)};
    }

    private void renderTerrain(Graphics2D g, Path2D.Double land, long seed) {
        int step = 3;
        double[][] height = new double[VH / step + 2][VW / step + 2];
        for (int y = 0; y < height.length; y++)
            for (int x = 0; x < height[0].length; x++)
                height[y][x] = Textures.fbm(x * 0.045, y * 0.045, 8, seed, 4);
        BufferedImage img = new BufferedImage(VW, VH, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < VH; y++) {
            for (int x = 0; x < VW; x++) {
                double lon = x / (double) VW * 360 - 180;
                double lat = 90 - y / (double) VH * 180;
                if (!land.contains(x, y)) {
                    if (auNear(x, y)) img.setRGB(x, y, new Color(70, 130, 170).getRGB());
                    continue;
                }
                double h = height[y / step][x / step];
                double hill = height[y / step][Math.min(height[0].length - 1, x / step + 1)] - height[y / step][Math.max(0, x / step - 1)];
                Color c = terrainColor(h, lon, lat);
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
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (baseCache == null) {
            if (getWidth() > 50 && getHeight() > 50) renderBase();
            else {
                g.setColor(Color.WHITE);
                g.drawString("Karte wird geladen...", 40, 60);
                return;
            }
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.transform(viewTransform());
        g.drawImage(baseCache, 0, 0, null);
        if (view3D && mapStyle == 0 && peaksCache != null) {
            g.setColor(new Color(90, 80, 75, 160));
            g.translate(-16, -8);
            g.drawImage(peaksCache, 0, 0, null);
            g.translate(8, 14);
            g.drawImage(peaksCache, 0, 0, null);
            g.translate(8, 16);
            g.setColor(new Color(255, 255, 255, 120));
            g.drawImage(peaksCache, 0, 0, null);
        }
        g.setTransform(new java.awt.geom.AffineTransform());

        int[] o = viewOffset();
        double s = viewScale();
        drawTopBar(g);

        for (Shipment sh : game.shipments) {
            City c = game.cityByName(sh.destCity);
            double frac = Math.max(0, Math.min(1, 1.0 - sh.daysLeft / (double) Shipment.TRANSIT_DAYS));
            int sx = (int) (o[0] + SHENZHEN_X * VW * s);
            int sy = (int) (o[1] + SHENZHEN_Y * VH * s);
            int x = (int) (sx + ((o[0] + c.mapX * VW * s) - sx) * frac);
            int y = (int) (sy + ((o[1] + c.mapY * VH * s) - sy) * frac);
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
                drawBuilding(g, (int) (o[0] + rc.mapX * VW * s) - 7 - (i % 5) * 8, (int) (o[1] + rc.mapY * VH * s) - 6 - (i % 5) * 5, 10, r.color, r.color.darker());
            }
        }

        for (City c : game.cities) {
            int x = (int) (o[0] + c.mapX * VW * s);
            int y = (int) (o[1] + c.mapY * VH * s);
            int r = (int) Math.max(4, Math.min(11, 4 + c.popMillions() * 1.2));
            boolean unlocked = game.isUnlocked(c);
            boolean selected = c.name.equals(game.selectedCity);
            boolean blocked = game.blockedCities.containsKey(c.name);
            List<Site> citySites = game.sitesIn(c.name);
            if (selected) {
                g.setColor(new Color(255, 235, 120));
                g.setStroke(new BasicStroke(2.5f));
                g.drawOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
            }
            g.setColor(unlocked ? regionColor(c) : new Color(110, 110, 120));
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
                int sz = 8 + citySites.get(i).level * 2;
                drawBuilding(g, x - r - 4 - (sz + 3) * i - sz, y + r + 5, sz);
            }
            if (c.popMillions() >= 1.0 || selected || !citySites.isEmpty()) {
                String label = c.name + (unlocked ? "" : " (gesperrt)");
                g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
                int tw = g.getFontMetrics().stringWidth(label);
                g.setColor(new Color(10, 14, 22, 210));
                g.fillRoundRect(x + r + 4, y - 15, tw + 10, 24, 6, 6);
                g.setColor(Color.WHITE);
                g.drawString(label, x + r + 9, y - 7);
                g.setFont(g.getFont().deriveFont(10f));
                g.setColor(new Color(200, 210, 225));
                g.drawString(String.format("%,d Einw. | Standorte: %d", c.population, citySites.size()), x + r + 9, y + 6);
            }
        }

        if (hoverCity != null && game.pendingBuildType == null) {
            City hc = hoverCity;
            Site server = game.servingSite(hc.name);
            String info = String.format("%s | %,d Einw. | Primo %.0f%% | Bewertung %.1f/5 | Standorte: %d%s",
                    hc.name, hc.population, hc.primeQuote * 100, game.cityRating(hc.name), game.sitesIn(hc.name).size(),
                    server != null ? String.format(" | Distanz: %,.0f km", game.distanceKm(game.cityByName(server.city), hc)) : "");
            g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
            int tw = g.getFontMetrics().stringWidth(info);
            int hx = (int) (o[0] + hc.mapX * VW * s);
            int hy = (int) (o[1] + hc.mapY * VH * s);
            g.setColor(new Color(12, 16, 26, 235));
            g.fillRoundRect(hx + 14, hy - 34, tw + 14, 24, 8, 8);
            g.setColor(new Color(255, 235, 150));
            g.drawString(info, hx + 21, hy - 18);
        }

        if (game.pendingBuildType != null && mouseX >= 0) {
            g.setColor(new Color(120, 255, 150, 120));
            g.fillRect(mouseX - 8, mouseY - 8, 16, 16);
            g.setColor(Color.WHITE);
            g.drawRect(mouseX - 8, mouseY - 8, 16, 16);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            g.drawString("Platzieren: " + game.pendingBuildType.label + " (klicken)", mouseX + 12, mouseY - 10);
        }

        drawAnimations(g, o, s);

        if (game.isPrimoDay()) {
            g.setColor(new Color(255, 60, 40));
            g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
            g.drawString("PRIMO DAY! Traffic x3.5", getWidth() / 2 - 120, 80);
        }
    }

    private void drawTopBar(Graphics2D g) {
        g.setColor(new Color(12, 16, 26, 225));
        g.fillRect(0, 0, getWidth(), 34);
        g.setColor(new Color(70, 90, 130));
        g.drawLine(0, 34, getWidth(), 34);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 13f));
        g.drawString(game.companyName + "  |  " + game.date() + " (Tag " + game.dayIndex + ")  |  "
                + String.format("Kasse: %,.0f AUD", game.cash) + "  |  " + game.eraName() + "  |  " + regionView, 14, 22);
        g.setFont(g.getFont().deriveFont(11f));
        g.setColor(new Color(255, 200, 90));
        g.drawString(game.isPrimoDay() ? "PRIMO DAY aktiv!" : "Naechster Primo Day in " + game.primeDayIn + " Tagen",
                getWidth() - 320, 22);
        String wIcon = switch (game.weather) { case "Duerre" -> "[DUERRE]"; case "Zyklon" -> "[ZYKLON!]"; case "Regen" -> "[Regen]"; case "Hitzewelle" -> "[Hitze]"; default -> "[Sonne]"; };
        g.setColor(new Color(140, 200, 255));
        g.drawString("Wetter: " + wIcon + " (noch " + game.weatherDays + "d)", 14, 56);
    }

    private void drawBuilding(Graphics2D g, int bx, int by, int sz) {
        drawBuilding(g, bx, by, sz, new Color(90, 220, 140), new Color(150, 50, 40));
    }

    private void drawBuilding(Graphics2D g, int bx, int by, int sz, Color body, Color roof) {
        g.setColor(body);
        g.fillRect(bx, by, sz, sz);
        g.setColor(roof);
        g.fillPolygon(new int[]{bx - 2, bx + sz / 2, bx + sz + 2}, new int[]{by, by - sz / 2, by}, 3);
        g.setColor(new Color(30, 70, 45));
        g.drawRect(bx, by, sz, sz);
        g.setColor(new Color(255, 255, 180));
        g.fillRect(bx + sz / 4, by + sz / 4, Math.max(1, sz / 5), Math.max(1, sz / 5));
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

    private void drawAnimations(Graphics2D g, int[] o, double s) {
        long t = System.currentTimeMillis();
        List<Site> ops = new ArrayList<>();
        for (Site site : game.sites) if (site.isOperational()) ops.add(site);

        for (int i = 0; i < ops.size(); i++) {
            Site site = ops.get(i);
            City c = game.cityByName(site.city);
            int cx = (int) (o[0] + c.mapX * VW * s);
            int cy = (int) (o[1] + c.mapY * VH * s);
            double orbit = t / 700.0 + i * 2.1;
            int dx = (int) (Math.cos(orbit) * 14);
            int dy = (int) (Math.sin(orbit) * 7);
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
            int cx = (int) (o[0] + c.mapX * VW * s);
            int cy = (int) (o[1] + c.mapY * VH * s);
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

        java.util.Random rr = new java.util.Random(7);
        for (int i = 0; i < 4; i++) {
            String[] e = ROADS[rr.nextInt(ROADS.length)];
            City a = game.cityByName(e[0]);
            City b = game.cityByName(e[1]);
            double p = (t * 0.00006 * (1 + i * 0.3) + i * 0.31) % 1.0;
            int[] pa = cityScreen(a, o, s);
            int[] pb = cityScreen(b, o, s);
            int tx = (int) (pa[0] + (pb[0] - pa[0]) * p);
            int ty = (int) (pa[1] + (pb[1] - pa[1]) * p);
            g.setColor(new Color(255, 215, 80));
            g.fillRect(tx - 5, ty - 3, 10, 6);
            g.setColor(Color.BLACK);
            g.fillOval(tx - 3, ty + 3, 3, 3);
            g.fillOval(tx + 2, ty + 3, 3, 3);
        }
        for (int i = 0; i < 2; i++) {
            String[] e = RAILS[rr.nextInt(RAILS.length)];
            City a = game.cityByName(e[0]);
            City b = game.cityByName(e[1]);
            double p = (t * 0.00004 * (1 + i * 0.5) + i * 0.5) % 1.0;
            int[] pa = cityScreen(a, o, s);
            int[] pb = cityScreen(b, o, s);
            int tx = (int) (pa[0] + (pb[0] - pa[0]) * p);
            int ty = (int) (pa[1] + (pb[1] - pa[1]) * p);
            g.setColor(new Color(200, 60, 50));
            g.fillRect(tx - 8, ty - 3, 16, 6);
            g.setColor(Color.BLACK);
            g.fillRect(tx - 8, ty + 3, 16, 2);
        }
        drawPlane(g, o, s, game.cityByName("Sydney"), game.cityByName("Perth"), t, 0.0006);
        drawPlane(g, o, s, game.cityByName("Sydney"),
                new City("Shenzhen", "China", "Asien", 17_000_000, 0.5, SHENZHEN_X, SHENZHEN_Y), t, 0.00055);
        for (int i = 0; i < AIRROUTES.length; i++) {
            drawPlane(g, o, s, game.cityByName(AIRROUTES[i][0]), game.cityByName(AIRROUTES[i][1]), t, 0.00025 + i * 0.00007);
        }
    }

    private void drawPlane(Graphics2D g, int[] o, double s, City a, City b, long t, double speed) {
        if (b == null) return;
        double p = (t * speed) % 1.0;
        int x1 = (int) (o[0] + a.mapX * VW * s);
        int y1 = (int) (o[1] + a.mapY * VH * s);
        int x2 = (int) (o[0] + b.mapX * VW * s);
        int y2 = (int) (o[1] + b.mapY * VH * s);
        int px = (int) (x1 + (x2 - x1) * p);
        int py = (int) (y1 + (y2 - y1) * p - Math.sin(p * Math.PI) * 40);
        double ang = Math.atan2(y2 - y1, x2 - x1);
        g.setColor(new Color(235, 240, 250));
        g.fillPolygon(new int[]{px + (int) (10 * Math.cos(ang)), px + (int) (9 * Math.cos(ang + 2.5)), px + (int) (9 * Math.cos(ang - 2.5))},
                new int[]{py + (int) (10 * Math.sin(ang)), py + (int) (9 * Math.sin(ang + 2.5)), py + (int) (9 * Math.sin(ang - 2.5))}, 3);
    }
}
