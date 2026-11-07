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
        java.util.List<String> catList = new java.util.ArrayList<>();
        catList.add("Alle");
        for (MachineType.Stage st : MachineType.Stage.values()) catList.add(st.label);
        JComboBox<String> catBox = new JComboBox<>(catList.toArray(new String[0]));
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
                    if (s.grid[gx][gy] != null) {
                        s.grid[gx][gy] = null;
                        s.upgrades[gx][gy] = 0;
                    }
                } else {
                    if (selectedMachine.requiredTech != null && !game.researched.contains(selectedMachine.requiredTech)) {
                        game.alert(selectedMachine.label + " erfordert Tech: " + selectedMachine.requiredTech.label + ".");
                        return;
                    }
                    if (s.grid[gx][gy] == selectedMachine) {
                        double upCost = selectedMachine.cost * 0.5 * (s.upgrades[gx][gy] + 1);
                        if (!game.charge(upCost)) {
                            game.alert("Modul-Upgrade kostet " + String.format("%,.0f", upCost) + " AUD.");
                            return;
                        }
                        if (s.upgrades[gx][gy] < 4) s.upgrades[gx][gy]++;
                        game.alert("Modul '" + selectedMachine.label + "' auf Stufe +" + s.upgrades[gx][gy] + " geupgradet (+50% Score/Stufe, mehr Durchsatz).");
                    } else {
                        if (!game.charge(selectedMachine.cost)) {
                            game.alert("Nicht genug Kapital fuer " + selectedMachine.label + ".");
                            return;
                        }
                        s.grid[gx][gy] = selectedMachine;
                        s.upgrades[gx][gy] = 0;
                    }
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
        JButton bpSave = new JButton("Als Blaupause speichern");
        bpSave.addActionListener(e -> {
            if (game.selectedSite == null) return;
            String name = JOptionPane.showInputDialog(this, "Name der Blaupause:", "Blaupause speichern", JOptionPane.QUESTION_MESSAGE);
            if (name != null && !name.trim().isEmpty()) game.saveBlueprint(name.trim(), game.selectedSite);
        });
        JButton bpApply = new JButton("Blaupause anwenden...");
        bpApply.addActionListener(e -> {
            if (game.selectedSite == null || game.blueprints.isEmpty()) return;
            String[] names = game.blueprints.keySet().toArray(new String[0]);
            Object sel = JOptionPane.showInputDialog(this, "Blaupause wählen:", "Blaupause anwenden", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if (sel != null) game.applyBlueprint((String) sel, game.selectedSite);
            repaint();
        });
        palette.add(bpSave);
        palette.add(bpApply);
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
                    "%s (%dx%d) | Modus: %s | Score %d | Grid-Effizienz %.0f%% | Kette %s | Engpass: %s | Durchsatz %.0f/Tag | Lager: %d Einheiten, Paletten %d/%d (max. Kapazitaet)",
                    s.describe(), s.w, s.h, eraseMode ? "Loeschen" : selectedMachine.label,
                    s.machineScore(), s.gridEfficiency(game) * 100,
                    s.chainComplete() ? "komplett (x1.5)" : "unvollstaendig",
                    s.chainComplete() ? s.weakestStage().label : "-",
                    s.throughput(game),
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
            return Math.max(6, Math.min(getWidth() / gw(), (getHeight() * 2) / gh()));
        }

        private int tileH() {
            return tileW() / 2;
        }

        private int originX() {
            return (getWidth() - gw() * tileW()) / 2 + gw() * tileW() / 2;
        }

        private int originY() {
            return (getHeight() - gh() * tileH()) / 2 + 10;
        }

        int[] cellAt(int mx, int my) {
            for (int x = 0; x < gw(); x++) {
                for (int y = 0; y < gh(); y++) {
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
            for (int x = 0; x < gw(); x++) {
                for (int y = 0; y < gh(); y++) {
                    int cx = originX() + (x - y) * tw / 2;
                    int cy = originY() + (x + y) * th / 2;
                    Paint tilePaint = ((x + y) % 2 == 0) ? Textures.paint("fA", Textures.generate(64, new Color(78, 88, 108), new Color(64, 73, 92), 8, 3, 21)) : Textures.paint("fB", Textures.generate(64, new Color(68, 77, 96), new Color(56, 64, 82), 8, 3, 33));
                    diamond(g, cx, cy, tw, th, tilePaint);
                    g.setColor(new Color(72, 80, 98));
                    g.drawPolygon(new int[]{cx, cx + tw / 2, cx, cx - tw / 2}, new int[]{cy - th / 2, cy, cy + th / 2, cy}, 4);

                    MachineType m = s.grid[x][y];
                    if (m != null) drawMachine(g, m, cx, cy, tw, th, t, s.upgrades[x][y]);
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
            int c10x = originX() + gw() * tw / 2;
            int c10y = originY() + gh() * th / 2;
            int c01x = originX() - gh() * tw / 2;
            int c01y = originY() + gh() * th / 2;
            int c11x = originX();
            int c11y = originY() + gh() * th;
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
            for (int x = 0; x < s.w; x++)
                for (int y = 0; y < s.h; y++)
                    if (s.grid[x][y] == MachineType.ROBOT) robots++;
            for (int i = 0; i < Math.min(3, robots); i++) {
                int row = Math.min(4 + i * 6, s.h - 1);
                double p = ((t / 1600.0) + i * 0.37) % 2.0;
                if (p > 1.0) p = 2.0 - p;
                int bx = originX() + (int) ((p - 0.5) * 2 * s.w * tw / 2);
                int by = originY() + (row + row) * th / 2;
                g.setColor(new Color(255, 150, 40));
                g.fillRoundRect(bx - 5, by - 4, 10, 8, 3, 3);
                g.setColor(Color.BLACK);
                g.drawRoundRect(bx - 5, by - 4, 10, 8, 3, 3);
                g.setColor(new Color(255, 255, 255));
                g.fillRect(bx - 2, by - 2, 4, 3);
            }
            for (int x = 0; x < gw(); x++) {
                for (int y = 0; y < gh(); y++) {
                    if (s.grid[x][y] == null) continue;
                    int cx = originX() + (x - y) * tw / 2;
                    int cy = originY() + (x + y) * th / 2 - th;
                    double blink = 0.25 + 0.75 * Math.abs(Math.sin(t / 350.0 + x * 1.7 + y * 2.3));
                    g.setColor(new Color(120, 255, 140, (int) (blink * 220)));
                    g.fillOval(cx + tw / 4 - 2, cy - 3, 4, 4);
                }
            }
        }

        private void drawMachine(Graphics2D g, MachineType m, int cx, int cy, int tw, int th, long t, int up) {
            int lift = th + up * th / 2;
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
            drawModuleFx(g, m, cx, cy, lift, w, h, t, up);
            if (up > 0) {
                g.setColor(new Color(255, 215, 80));
                g.setFont(g.getFont().deriveFont(Font.BOLD, 10f));
                g.drawString("+" + up, cx - 6, cy - lift - h / 2 - 4);
            }
        }

        private void drawModuleFx(Graphics2D g, MachineType m, int cx, int cy, int lift, int w, int h, long t, int up) {
            switch (m) {
                case RFID_TUNNEL: {
                    double pulse = 0.3 + 0.7 * Math.abs(Math.sin(t / 400.0 + cx));
                    g.setColor(new Color(200, 140, 255, (int) (pulse * 200)));
                    g.drawArc(cx - w / 2, cy - lift - h, w, h * 2, 0, 180);
                    break;
                }
                case PARCEL_SORTER:
                case PEGASUS:
                case SORT_CENTER: {
                    double p = (t / 300.0 + cx) % 1.0;
                    g.setColor(new Color(255, 210, 90));
                    g.fillOval(cx - w / 2 + (int) (p * w) - 2, cy - lift - 2, 5, 5);
                    break;
                }
                case PALLETIZER: {
                    int blocks = 1 + (int) ((t / 700.0 + cx) % 3);
                    g.setColor(new Color(160, 110, 60));
                    for (int i = 0; i < blocks; i++) g.fillRect(cx - 4, cy - lift - 4 - i * 5, 8, 4);
                    break;
                }
                case DOCK:
                case LOAD_DOCK: {
                    double door = 0.5 + 0.5 * Math.sin(t / 900.0 + cx);
                    g.setColor(new Color(30, 22, 12, (int) (120 + door * 100)));
                    g.fillRect(cx - w / 4, cy - lift - (int) (h * (0.3 + 0.5 * door)), w / 2, (int) (h * (0.3 + 0.5 * door)));
                    break;
                }
                case DRONE_PAD: {
                    double blink = 0.3 + 0.7 * Math.abs(Math.sin(t / 500.0 + cx));
                    g.setColor(new Color(120, 255, 140, (int) (blink * 220)));
                    g.drawOval(cx - w / 3, cy - lift - h / 3, w * 2 / 3, h * 2 / 3);
                    g.setColor(Color.WHITE);
                    g.fillOval(cx - 2 + (int) (Math.sin(t / 300.0) * 4), cy - lift - 2 + (int) (Math.cos(t / 300.0) * 3), 4, 4);
                    break;
                }
                case AGV_BOT: {
                    double p = ((t / 500.0) + cx) % 1.0;
                    g.setColor(new Color(255, 150, 40));
                    g.fillRoundRect(cx - w / 2 + (int) (p * w) - 3, cy - lift - 3, 6, 6, 2, 2);
                    break;
                }
                case ASRS_CRANE: {
                    double arm = Math.sin(t / 600.0 + cx) * w / 4;
                    g.setColor(new Color(255, 255, 255));
                    g.drawLine(cx, cy - lift - h, cx + (int) arm, cy - lift - h);
                    g.fillRect(cx + (int) arm - 2, cy - lift - h, 4, 6);
                    break;
                }
                default:
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
                case AGV_BOT: return new Color(255, 140, 60);
                case RFID_TUNNEL: return new Color(160, 120, 255);
                case POD_STATION: return new Color(255, 220, 90);
                case ASRS_CRANE: return new Color(120, 90, 60);
                case SPARROW_ARM: return new Color(255, 100, 140);
                case PACKSIZE: return new Color(100, 200, 160);
                case PEGASUS: return new Color(90, 200, 255);
                case DRONE_PAD: return new Color(220, 220, 230);
                case RMS_DESK: return new Color(140, 160, 180);
                default: return Color.WHITE;
            }
        }
    }
}
