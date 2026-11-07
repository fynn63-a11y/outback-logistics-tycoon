package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.MachineType;
import com.outback.tycoon.model.Site;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

public class MicroPanel extends JPanel {
    private final Game game;
    private final GridCanvas canvas = new GridCanvas();
    private final JPanel palette = new JPanel();
    private final JLabel status = new JLabel();
    private MachineType selectedMachine = MachineType.CONVEYOR;
    private boolean eraseMode = false;
    private int[] hover = null;
    private boolean roofOn = true;
    private Timer animTimer;

    public MicroPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());

        JPanel north = new JPanel(new BorderLayout());
        String[] cats = {"Alle", "Wareneingang", "Transport", "Lagerung", "Kommissionierung", "Verpackung", "Sortierung", "Versand"};
        JComboBox<String> catBox = new JComboBox<>(cats);
        catBox.addActionListener(e -> rebuildPalette(palette, (String) catBox.getSelectedItem()));
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterRow.add(new JLabel("Packbau-Kategorie:"));
        filterRow.add(catBox);
        north.add(filterRow, BorderLayout.NORTH);
        palette.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rebuildPalette(palette, "Alle");
        north.add(palette, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        canvas.setPreferredSize(new Dimension(760, 560));
        canvas.setBackground(new Color(24, 28, 38));
        add(canvas, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(status);
        add(bottom, BorderLayout.SOUTH);

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hover = canvas.cellAt(e.getX(), e.getY());
                canvas.repaint();
            }
        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Site s = game.selectedSite;
                if (s == null) return;
                int[] cell = canvas.cellAt(e.getX(), e.getY());
                if (cell == null) return;
                int gx = cell[0];
                int gy = cell[1];
                if (eraseMode) {
                    if (s.grid[gx][gy] != null) s.grid[gx][gy] = null;
                } else {
                    if (selectedMachine.requiredTech != null && !game.researched.contains(selectedMachine.requiredTech)) {
                        game.alert(selectedMachine.label + " erfordert Tech: " + selectedMachine.requiredTech.label + ".");
                        return;
                    }
                    if (!game.charge(selectedMachine.cost)) {
                        game.alert("Nicht genug Kapital fuer " + selectedMachine.label + ".");
                        return;
                    }
                    s.grid[gx][gy] = selectedMachine;
                }
                refreshStatus();
                repaint();
            }
        });

        animTimer = new Timer(90, e -> canvas.repaint());
        animTimer.start();
    }

    private void rebuildPalette(JPanel palette, String category) {
        palette.removeAll();
        for (MachineType m : MachineType.values()) {
            if (!category.equals("Alle") && !m.stage.label.equals(category)) continue;
            boolean unlocked = m.requiredTech == null || game.researched.contains(m.requiredTech);
            JButton b = new JButton(m.label + " (" + String.format("%,.0f", m.cost) + ")"
                    + (unlocked ? "" : " [Tech: " + m.requiredTech.label + "]"));
            b.setEnabled(unlocked);
            b.addActionListener(e -> {
                selectedMachine = m;
                eraseMode = false;
                refreshStatus();
            });
            palette.add(b);
        }
        JButton roof = new JButton("Dach an/aus");
        roof.addActionListener(e -> {
            roofOn = !roofOn;
            canvas.repaint();
        });
        palette.add(roof);
        JButton erase = new JButton("Loeschen-Modus");
        erase.addActionListener(e -> {
            eraseMode = true;
            refreshStatus();
        });
        palette.add(erase);
        palette.revalidate();
        palette.repaint();
    }

    private void refreshStatus() {
        if (game == null) return;
        Site s = game.selectedSite;
        if (s == null) {
            status.setText("Kein Standort ausgewaehlt - waehle zuerst in 'Stadt & Bau (Meso)' einen Standort.");
        } else {
            int units = 0;
            for (int v : s.stock.values()) units += v;
            status.setText(String.format(
                    "%s | Modus: %s | Score %d | Grid-Effizienz %.0f%% | Durchsatz %.0f/Tag | Lager: %d Einheiten, Paletten %d/%d (max. Kapazitaet)",
                    s.describe(), eraseMode ? "Loeschen" : selectedMachine.label,
                    s.machineScore(), s.gridEfficiency(game) * 100, s.throughput(game),
                    units, s.palletUsed(), s.palletSlots()));
        }
    }

    @Override
    public void repaint() {
        super.repaint();
        if (game != null) refreshStatus();
    }

    private class GridCanvas extends JPanel {
        private int tileW() {
            return Math.max(14, Math.min(getWidth() / Site.GRID, (getHeight() * 2) / Site.GRID));
        }

        private int tileH() {
            return tileW() / 2;
        }

        private int originX() {
            return (getWidth() - Site.GRID * tileW()) / 2 + Site.GRID * tileW() / 2;
        }

        private int originY() {
            return (getHeight() - Site.GRID * tileH()) / 2 + 10;
        }

        int[] cellAt(int mx, int my) {
            for (int x = 0; x < Site.GRID; x++) {
                for (int y = 0; y < Site.GRID; y++) {
                    int sx = originX() + (x - y) * tileW() / 2;
                    int sy = originY() + (x + y) * tileH() / 2;
                    if (mx >= sx - tileW() / 2 && mx <= sx + tileW() / 2 && my >= sy - tileH() / 2 && my <= sy + tileH() / 2)
                        return new int[]{x, y};
                }
            }
            return null;
        }

        private void diamond(Graphics2D g, int cx, int cy, int w, int h, Paint paint) {
            int[] xs = {cx, cx + w / 2, cx, cx - w / 2};
            int[] ys = {cy - h / 2, cy, cy + h / 2, cy};
            g.setPaint(paint);
            g.fillPolygon(xs, ys, 4);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Site s = game.selectedSite;
            if (s == null) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawString("Kein Standort ausgewaehlt.", 20, 30);
                return;
            }
            int tw = tileW();
            int th = tileH();
            long t = System.currentTimeMillis();
            for (int x = 0; x < Site.GRID; x++) {
                for (int y = 0; y < Site.GRID; y++) {
                    int cx = originX() + (x - y) * tw / 2;
                    int cy = originY() + (x + y) * th / 2;
                    Paint tilePaint = ((x + y) % 2 == 0) ? Textures.paint("fA", Textures.generate(64, new Color(78, 88, 108), new Color(64, 73, 92), 8, 3, 21)) : Textures.paint("fB", Textures.generate(64, new Color(68, 77, 96), new Color(56, 64, 82), 8, 3, 33));
                    diamond(g, cx, cy, tw, th, tilePaint);
                    g.setColor(new Color(72, 80, 98));
                    g.drawPolygon(new int[]{cx, cx + tw / 2, cx, cx - tw / 2}, new int[]{cy - th / 2, cy, cy + th / 2, cy}, 4);

                    MachineType m = s.grid[x][y];
                    if (m != null) drawMachine(g, m, cx, cy, tw, th, t);
                    if (m == MachineType.CONVEYOR || m == MachineType.SPAWNER) {
                        double phase = ((t / 120.0) + x * 3 + y * 5) % 1.0;
                        int px = cx + (int) ((phase - 0.5) * tw * 0.6);
                        int py = cy + (int) ((phase - 0.5) * th * 0.3);
                        g.setColor(new Color(255, 200, 90));
                        g.fillRect(px - 2, py - 2, 5, 5);
                    }
                }
            }
            drawWallsAndRoof(g, s, tw, th);
            drawWarehouseFx(g, s, t, tw, th);
            if (hover != null && !eraseMode && s.grid[hover[0]][hover[1]] == null) {
                int cx = originX() + (hover[0] - hover[1]) * tw / 2;
                int cy = originY() + (hover[0] + hover[1]) * th / 2;
                Color c = machineColor(selectedMachine);
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 110));
                diamond(g, cx, cy - th, tw * 2 / 3, th * 2 / 3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 110));
                g.setColor(new Color(255, 255, 255, 160));
                g.drawPolygon(new int[]{cx, cx + tw / 3, cx, cx - tw / 3},
                        new int[]{cy - th - th / 3, cy - th, cy - th + th / 3, cy - th}, 4);
            }
        }

        private void drawWallsAndRoof(Graphics2D g, Site s, int tw, int th) {
            int wallH = th * 8;
            int c00x = originX();
            int c00y = originY();
            int c10x = originX() + Site.GRID * tw / 2;
            int c10y = originY() + Site.GRID * th / 2;
            int c01x = originX() - Site.GRID * tw / 2;
            int c01y = originY() + Site.GRID * th / 2;
            int c11x = originX();
            int c11y = originY() + Site.GRID * th;
            g.setColor(new Color(105, 92, 78));
            g.fillPolygon(new int[]{c00x, c01x, c01x, c00x}, new int[]{c00y, c01y, c01y - wallH, c00y - wallH}, 4);
            g.setColor(new Color(88, 76, 62));
            g.fillPolygon(new int[]{c00x, c10x, c10x, c00x}, new int[]{c00y, c10y, c10y - wallH, c00y - wallH}, 4);
            g.setColor(new Color(50, 42, 34));
            g.drawPolygon(new int[]{c00x, c01x, c01x, c00x}, new int[]{c00y, c01y, c01y - wallH, c00y - wallH}, 4);
            g.drawPolygon(new int[]{c00x, c10x, c10x, c00x}, new int[]{c00y, c10y, c10y - wallH, c00y - wallH}, 4);
            if (s.level >= 3 && roofOn) {
                g.setColor(new Color(150, 60, 50, 90));
                g.fillPolygon(new int[]{c00x, c10x, c11x, c01x}, new int[]{c00y - wallH, c10y - wallH, c11y - wallH, c01y - wallH}, 4);
            }
        }

        private void drawWarehouseFx(Graphics2D g, Site s, long t, int tw, int th) {
            int robots = 0;
            for (int x = 0; x < Site.GRID; x++)
                for (int y = 0; y < Site.GRID; y++)
                    if (s.grid[x][y] == MachineType.ROBOT) robots++;
            for (int i = 0; i < Math.min(3, robots); i++) {
                int row = 4 + i * 6;
                double p = ((t / 1600.0) + i * 0.37) % 2.0;
                if (p > 1.0) p = 2.0 - p;
                int bx = originX() + (int) ((p - 0.5) * 2 * Site.GRID * tw / 2);
                int by = originY() + (row + row) * th / 2;
                g.setColor(new Color(255, 150, 40));
                g.fillRoundRect(bx - 5, by - 4, 10, 8, 3, 3);
                g.setColor(Color.BLACK);
                g.drawRoundRect(bx - 5, by - 4, 10, 8, 3, 3);
                g.setColor(new Color(255, 255, 255));
                g.fillRect(bx - 2, by - 2, 4, 3);
            }
            for (int x = 0; x < Site.GRID; x++) {
                for (int y = 0; y < Site.GRID; y++) {
                    if (s.grid[x][y] == null) continue;
                    int cx = originX() + (x - y) * tw / 2;
                    int cy = originY() + (x + y) * th / 2 - th;
                    double blink = 0.25 + 0.75 * Math.abs(Math.sin(t / 350.0 + x * 1.7 + y * 2.3));
                    g.setColor(new Color(120, 255, 140, (int) (blink * 220)));
                    g.fillOval(cx + tw / 4 - 2, cy - 3, 4, 4);
                }
            }
        }

        private void drawMachine(Graphics2D g, MachineType m, int cx, int cy, int tw, int th, long t) {
            int lift = th;
            int w = tw * 2 / 3;
            int h = th * 2 / 3;
            Color c = machineColor(m);
            switch (m) {
                case CONVEYOR: {
                    g.setColor(new Color(70, 74, 86));
                    g.fillRoundRect(cx - w / 2, cy - lift - h / 2, w, h, 4, 4);
                    g.setColor(new Color(170, 176, 192));
                    double off = (t / 90.0) % 6;
                    for (int i = -w / 2; i < w / 2; i += 6) {
                        int lx = cx + i + (int) off;
                        g.drawLine(lx, cy - lift - h / 2 + 2, lx, cy - lift + h / 2 - 2);
                    }
                    g.setColor(Color.BLACK);
                    g.drawRoundRect(cx - w / 2, cy - lift - h / 2, w, h, 4, 4);
                    break;
                }
                case RACK: {
                    g.setColor(new Color(150, 110, 60));
                    g.fillRect(cx - w / 2, cy - lift - h / 2, w, h);
                    g.setColor(new Color(90, 64, 34));
                    for (int i = 1; i <= 3; i++)
                        g.drawLine(cx - w / 2, cy - lift - h / 2 + i * h / 4, cx + w / 2, cy - lift - h / 2 + i * h / 4);
                    g.setColor(Color.BLACK);
                    g.drawRect(cx - w / 2, cy - lift - h / 2, w, h);
                    break;
                }
                case ROBOT: {
                    g.setColor(new Color(255, 150, 40));
                    g.fillRoundRect(cx - w / 2, cy - lift - h / 2, w, h, 5, 5);
                    g.setColor(new Color(60, 40, 20));
                    g.drawRoundRect(cx - w / 2, cy - lift - h / 2, w, h, 5, 5);
                    g.setColor(Color.WHITE);
                    g.fillOval(cx - 3, cy - lift - 2, 6, 4);
                    double blink = 0.3 + 0.7 * Math.abs(Math.sin(t / 300.0 + cx));
                    g.setColor(new Color(120, 255, 140, (int) (blink * 255)));
                    g.fillOval(cx + w / 4 - 2, cy - lift - h / 2 + 2, 4, 4);
                    break;
                }
                case PICK: {
                    g.setColor(new Color(60, 170, 90));
                    g.fillRect(cx - w / 2, cy - lift - h / 2, w, h);
                    g.setColor(new Color(120, 230, 255));
                    g.fillRect(cx - w / 4, cy - lift - h / 2 + 3, w / 2, h / 3);
                    g.setColor(Color.BLACK);
                    g.drawRect(cx - w / 2, cy - lift - h / 2, w, h);
                    break;
                }
                case DOCK: {
                    g.setColor(new Color(90, 60, 30));
                    g.fillRect(cx - w / 2, cy - lift - h / 2, w, h);
                    g.setColor(new Color(40, 28, 14));
                    g.fillRect(cx - w / 4, cy - lift - h / 4, w / 2, h / 2 + 2);
                    g.setColor(new Color(255, 220, 80));
                    g.drawRect(cx - w / 2, cy - lift - h / 2, w, h);
                    break;
                }
                case SPAWNER: {
                    g.setColor(new Color(60, 110, 200));
                    g.fillRect(cx - w / 2, cy - lift - h, w, h * 2);
                    g.setColor(new Color(20, 40, 90));
                    g.fillRect(cx - w / 4, cy - lift - h / 2, w / 2, h);
                    break;
                }
                default: {
                    diamond(g, cx, cy - lift, w, h, c);
                    g.setColor(Color.WHITE);
                    g.drawPolygon(new int[]{cx - w / 2, cx, cx + w / 2, cx},
                            new int[]{cy - lift - h / 2, cy - lift, cy - lift + h / 2, cy - lift}, 4);
                    g.setColor(c.darker());
                    g.fillPolygon(new int[]{cx - w / 2, cx, cx, cx - w / 2},
                            new int[]{cy - lift, cy - lift + h / 2, cy + h / 2, cy}, 4);
                    g.fillPolygon(new int[]{cx + w / 2, cx, cx, cx + w / 2},
                            new int[]{cy - lift, cy - lift + h / 2, cy + h / 2, cy}, 4);
                }
            }
        }

        private Color machineColor(MachineType m) {
            switch (m) {
                case SPAWNER: return new Color(90, 160, 250);
                case CONVEYOR: return new Color(150, 150, 160);
                case ERECTOR: return new Color(250, 180, 80);
                case PICK: return new Color(120, 220, 120);
                case SEALER: return new Color(250, 120, 120);
                case STRAPPER: return new Color(220, 120, 220);
                case RACK: return new Color(160, 120, 70);
                case DOCK: return new Color(80, 220, 220);
                default: return Color.WHITE;
            }
        }
    }
}
