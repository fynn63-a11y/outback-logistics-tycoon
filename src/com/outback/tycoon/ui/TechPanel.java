package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.Tech;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechPanel extends JPanel {
    private final Game game;
    private final TreeCanvas canvas = new TreeCanvas();
    private final JLabel rpLabel = new JLabel();
    private final JLabel devLabel = new JLabel();
    private final Map<Tech, Point> nodePos = new HashMap<>();
    private Tech hover = null;

    public TechPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(rpLabel);
        top.add(devLabel);
        JButton hire = new JButton("Entwickler einstellen (90.000 AUD/Jahr)");
        hire.addActionListener(e -> {
            game.hireDeveloper();
            refresh();
        });
        JButton fire = new JButton("Entwickler entlassen");
        fire.addActionListener(e -> {
            game.fireDeveloper();
            refresh();
        });
        top.add(hire);
        top.add(fire);
        JButton legend = new JButton("? Legende");
        legend.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Spalten = Epochen (Modem 1994 / Breitband 2000 / Cloud 2010 / KI 2020).\n"
                        + "Farben = Kategorie: Blau=Shop/Backend, Orange=Inbound, Gruen=Stow, Tuerkis=Pick,\n"
                        + "Gelb=Pack, Lila=Sort, Braun=Outbound, Rosa=Last Mile, Grau=IoT/Infrastruktur, Dunkel=Basis.\n"
                        + "Klick auf einen Knoten erforscht ihn (RP + Epoche muessen reichen). Grün = erforscht, Grau = gesperrt.",
                "Techbaum (HoI4-Stil)", JOptionPane.INFORMATION_MESSAGE));
        top.add(legend);
        add(top, BorderLayout.NORTH);
        canvas.setPreferredSize(new Dimension(1700, 1000));
        add(new JScrollPane(canvas), BorderLayout.CENTER);

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Tech h = techAt(e.getX(), e.getY());
                if (h != hover) {
                    hover = h;
                    canvas.repaint();
                }
            }
        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Tech t = techAt(e.getX(), e.getY());
                if (t != null) {
                    game.researchTech(t);
                    refresh();
                }
            }
        });
    }

    private Tech techAt(int x, int y) {
        for (Map.Entry<Tech, Point> e : nodePos.entrySet()) {
            if (Math.hypot(e.getValue().x - x, e.getValue().y - y) < 26) return e.getKey();
        }
        return null;
    }

    private static Color groupColor(Tech t) {
        String n = t.name();
        if (n.startsWith("EC_")) return new Color(70, 130, 230);
        if (n.startsWith("IN_")) return new Color(230, 140, 50);
        if (n.startsWith("ST_")) return new Color(90, 190, 90);
        if (n.startsWith("PK_")) return new Color(60, 200, 200);
        if (n.startsWith("PA_")) return new Color(230, 210, 80);
        if (n.startsWith("SO_")) return new Color(170, 110, 230);
        if (n.startsWith("OB_")) return new Color(190, 110, 70);
        if (n.startsWith("LM_")) return new Color(240, 130, 170);
        if (n.startsWith("IO_")) return new Color(150, 160, 175);
        return new Color(90, 95, 110);
    }

    public void refresh() {
        rpLabel.setText(String.format("  Forschungspunkte (RP): %.0f", game.researchPoints));
        devLabel.setText(String.format("  Entwickler: %d (+2 RP/Tag)  |  Epoche: %s", game.developers, game.eraName()));
        canvas.repaint();
    }

    private class TreeCanvas extends JPanel {
        TreeCanvas() {
            setBackground(new Color(16, 20, 30));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            nodePos.clear();

            Tech[] techs = Tech.values();
            int colW = 300;
            int x0 = 60;
            int y0 = 90;
            int rowH = 74;
            String[] eraNames = {"Modem-Ära (1994+)", "Breitband-Ära (2000+)", "Cloud-Ära (2010+)", "KI-Ära (2020+)"};

            for (int era = 0; era <= 3; era++) {
                int cx = x0 + era * colW;
                g.setColor(new Color(255, 255, 255));
                g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
                g.drawString(eraNames[era], cx - 40, 40);
                g.setColor(new Color(60, 70, 95));
                g.drawLine(cx - 40, 50, cx - 40, 980);
            }

            Map<Integer, Integer> rowCount = new HashMap<>();
            for (Tech t : techs) {
                int col = Math.min(3, Math.max(0, t.minEra));
                int row = rowCount.merge(col, 1, Integer::sum) - 1;
                int nx = x0 + col * colW;
                int ny = y0 + row * rowH;
                nodePos.put(t, new Point(nx, ny));
            }

            for (Tech t : techs) {
                Point p = nodePos.get(t);
                if (t.minEra > 0) {
                    g.setColor(new Color(70, 80, 105));
                    g.setStroke(new BasicStroke(2f));
                    g.drawLine(p.x - 26, p.y, p.x - colW + 26, p.y);
                }
            }

            for (Tech t : techs) {
                Point p = nodePos.get(t);
                boolean done = game.researched.contains(t);
                boolean eraOk = game.eraIndex() >= t.minEra;
                boolean affordable = game.researchPoints >= t.rpCost;
                Color c = groupColor(t);
                if (done) {
                    g.setColor(new Color(60, 200, 110));
                    g.fillOval(p.x - 26, p.y - 26, 52, 52);
                    g.setColor(Color.WHITE);
                    g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
                    g.drawString("✓", p.x - 8, p.y + 8);
                } else {
                    g.setColor(eraOk ? c : new Color(70, 74, 88));
                    g.fillOval(p.x - 26, p.y - 26, 52, 52);
                    g.setColor(done || eraOk ? Color.WHITE : new Color(140, 145, 160));
                    g.setStroke(new BasicStroke(2f));
                    g.drawOval(p.x - 26, p.y - 26, 52, 52);
                    g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
                    String shortName = t.label.length() > 16 ? t.label.substring(0, 15) + "…" : t.label;
                    drawWrapped(g, shortName, p.x, p.y - 32, 150);
                }
                if (t == hover) {
                    g.setColor(new Color(255, 235, 130));
                    g.setStroke(new BasicStroke(3f));
                    g.drawOval(p.x - 30, p.y - 30, 60, 60);
                    String tip = t.label + " | " + (int) t.rpCost + " RP" + (t.minEra > 0 ? " | ab " + eraNames[t.minEra] : "")
                            + " | " + t.description
                            + (t.bonus != null ? " | Effekt: " + (t.bonus == Tech.Bonus.SERVER ? "+" + (int) t.bonusValue + " Server" : "+" + (int) (t.bonusValue * 100) + "% " + t.bonus.name()) : "")
                            + (done ? " | ERFORSCHT" : !eraOk ? " | GESPERRT (Epoche)" : !affordable ? " | zu wenig RP" : " | klick = erforschen");
                    g.setFont(g.getFont().deriveFont(12f));
                    int tw = g.getFontMetrics().stringWidth(tip);
                    g.setColor(new Color(12, 16, 26, 230));
                    g.fillRoundRect(p.x - tw / 2 - 8, p.y + 34, tw + 16, 24, 8, 8);
                    g.setColor(new Color(255, 240, 180));
                    g.drawString(tip, p.x - tw / 2, p.y + 51);
                }
            }
        }

        private void drawWrapped(Graphics2D g, String text, int cx, int y, int maxW) {
            FontMetrics fm = g.getFontMetrics();
            if (fm.stringWidth(text) <= maxW) {
                g.drawString(text, cx - fm.stringWidth(text) / 2, y);
                return;
            }
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            int ly = y;
            for (String w : words) {
                if (fm.stringWidth(line + " " + w) > maxW && line.length() > 0) {
                    g.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, ly);
                    ly += 13;
                    line = new StringBuilder();
                }
                line.append(w).append(" ");
            }
            g.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, ly);
        }
    }
}
