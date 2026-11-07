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
                    if (m != null) {
                        int lift = th;
                        Color c = machineColor(m);
                        diamond(g, cx, cy - lift, tw * 2 / 3, th * 2 / 3, c);
                        g.setColor(Color.WHITE);
                        g.drawPolygon(new int[]{cx - tw / 3, cx, cx + tw / 3, cx},
                                new int[]{cy - lift - th / 3, cy - lift, cy - lift + th / 3, cy - lift}, 4);
                        g.setColor(c.darker());
                        g.fillPolygon(new int[]{cx - tw / 3, cx, cx, cx - tw / 3 + 0},
                                new int[]{cy - lift, cy - lift + th / 3, cy + th / 3, cy}, 4);
                        g.fillPolygon(new int[]{cx + tw / 3, cx, cx, cx + tw / 3},
                                new int[]{cy - lift, cy - lift + th / 3, cy + th / 3, cy}, 4);
                        g.setColor(Color.BLACK);
                        g.drawString(m.name().substring(0, 1), cx - 3, cy - lift + 4);
                    }
                    if (m == MachineType.CONVEYOR || m == MachineType.SPAWNER) {
                        double phase = ((t / 120.0) + x * 3 + y * 5) % 1.0;
                        int px = cx + (int) ((phase - 0.5) * tw * 0.6);
                        int py = cy + (int) ((phase - 0.5) * th * 0.3);
                        g.setColor(new Color(255, 200, 90));
                        g.fillRect(px - 2, py - 2, 5, 5);
                    }
                }
            }
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
