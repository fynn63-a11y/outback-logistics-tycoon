package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChartsPanel extends JPanel {
    private final Game game;

    public ChartsPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(700, 500));
        setBackground(new Color(18, 22, 32));
        setLayout(new BorderLayout());
        JButton csv = new JButton("Statistik als CSV exportieren...");
        csv.addActionListener(e -> exportCsv());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(csv);
        add(south, BorderLayout.SOUTH);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter w = new java.io.PrintWriter(fc.getSelectedFile(), "UTF-8")) {
            w.println("tag,kasse_aud,tagesumsatz_aud,konkurrenz_marktanteil");
            int n = game.histCash.size();
            for (int i = 0; i < n; i++) {
                w.printf(java.util.Locale.US, "%d,%.2f,%.2f,%.4f%n", i,
                        game.histCash.get(i), game.histRevenue.get(i), game.histRivalShare.get(i));
            }
            game.alert("Statistik exportiert: " + fc.getSelectedFile().getName());
        } catch (Exception ex) {
            game.alert("CSV-Export fehlgeschlagen: " + ex.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        g.drawString("Statistik & Verlauf (letzte " + game.histCash.size() + " Tage) - Epoche: " + game.eraName(), 16, 22);
        drawChart(g, game.histCash, new Color(90, 220, 120), "Kasse (AUD)", 40, 40, getWidth() - 40, 200);
        drawChart(g, game.histRevenue, new Color(240, 170, 70), "Tagesumsatz (AUD)", 40, 260, getWidth() - 40, 120);
        drawChart(g, game.histRivalShare, new Color(220, 90, 90), "Marktanteil Konkurrenz (0-1)", 40, 400, getWidth() - 40, 80);
        if (!game.histShare.isEmpty())
            drawChart(g, game.histShare, new Color(140, 200, 255), "Aktienkurs ASX (AUD)", 40, 500, getWidth() - 40, 80);
    }

    private void drawChart(Graphics2D g, List<Double> data, Color color, String title, int x, int y, int w, int h) {
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(g.getFont().deriveFont(11f));
        g.drawString(title, x, y - 6);
        g.setColor(new Color(60, 66, 82));
        g.drawRect(x, y, w, h);
        if (data.size() < 2) return;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double v : data) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (max - min < 1e-9) { max = min + 1; }
        g.setColor(color);
        int prevX = x;
        int prevY = y + h;
        for (int i = 0; i < data.size(); i++) {
            int px = x + (int) (i * w / (double) (data.size() - 1));
            int py = y + h - (int) ((data.get(i) - min) / (max - min) * (h - 4)) - 2;
            g.drawLine(prevX, prevY, px, py);
            prevX = px;
            prevY = py;
        }
        g.setColor(Color.GRAY);
        g.drawString(String.format("min %,.0f", min), x + 4, y + h - 4);
        g.drawString(String.format("max %,.0f", max), x + w - 90, y + 12);
    }
}
