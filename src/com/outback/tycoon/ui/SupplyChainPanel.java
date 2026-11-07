package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.Shipment;
import com.outback.tycoon.model.Site;

import javax.swing.*;
import java.awt.*;

public class SupplyChainPanel extends JPanel {
    private final Game game;

    public SupplyChainPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(700, 420));
        setBackground(new Color(18, 22, 32));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        g.drawString("Lieferkette: China → Seehäfen → deine Hubs → Kunden", 16, 24);

        int inTransitUnits = 0;
        for (Shipment s : game.shipments) inTransitUnits += s.totalUnits();
        int stockTotal = 0;
        int hubs = 0;
        for (Site s : game.sites) {
            if (s.isOperational() && s.type != com.outback.tycoon.model.SiteType.DATA_CENTER) {
                hubs++;
                for (int v : s.stock.values()) stockTotal += v;
            }
        }
        int blockedHubs = 0;
        for (Site s : game.sites) if (!s.isOperational() && s.type != com.outback.tycoon.model.SiteType.DATA_CENTER) blockedHubs++;

        box(g, 30, 80, 150, 70, new Color(200, 80, 60), "Shenzhen / Shanghai",
                "China-Importe", "FX: 1 CNY = " + String.format("%.3f", game.fxCny) + " AUD");
        box(g, 260, 80, 150, 70, new Color(80, 140, 220), "Seehäfen (Sydney/Melbourne)",
                "In Transit: " + game.shipments.size() + " Container", inTransitUnits + " Einheiten unterwegs");
        box(g, 490, 80, 170, 70, new Color(80, 190, 110), "Deine Hubs (" + hubs + " aktiv, " + blockedHubs + " außer Betrieb)",
                "Lagerbestand gesamt: " + stockTotal, "Durchsatz: " + String.format("%.0f", game.networkThroughput()) + "/Tag");
        box(g, 260, 220, 150, 70, new Color(230, 180, 70), "Kunden (Marktplatz + Eigenvertrieb)",
                String.format("Umsatz gestern: %,.0f AUD", game.lastDailyRevenue), "Retouren: " + game.lastDailyReturns + " Einheiten");

        arrow(g, 180, 115, 260, 115);
        arrow(g, 410, 115, 490, 115);
        arrow(g, 565, 150, 410, 220);
        arrow(g, 260, 255, 180, 150);
    }

    private void box(Graphics2D g, int x, int y, int w, int h, Color c, String title, String l1, String l2) {
        g.setColor(c);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
        g.drawString(title, x + 8, y + 18);
        g.setFont(g.getFont().deriveFont(10f));
        g.drawString(l1, x + 8, y + 38);
        g.drawString(l2, x + 8, y + 54);
    }

    private void arrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x1, y1, x2, y2);
        double ang = Math.atan2(y2 - y1, x2 - x1);
        g.fillPolygon(new int[]{x2, x2 - (int) (10 * Math.cos(ang - 0.4)), x2 - (int) (10 * Math.cos(ang + 0.4))},
                new int[]{y2, y2 - (int) (10 * Math.sin(ang - 0.4)), y2 - (int) (10 * Math.sin(ang + 0.4))}, 3);
    }
}
