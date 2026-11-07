package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.Site;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final Game game;
    private final JPanel root = new JPanel();
    private final JProgressBar autoBar = new JProgressBar(0, 100);

    public DashboardPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        add(new JScrollPane(root), BorderLayout.CENTER);
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 6, 2, 6));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel row(String text) {
        JLabel l = new JLabel(text);
        l.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public void refresh() {
        root.removeAll();
        root.add(section("Dashboard - Übersicht"));
        root.add(row(String.format("Datum: %s | Kasse: %,.0f AUD | Reputation: %d/100",
                game.date(), game.cash, (int) game.reputation)));
        root.add(row(String.format("Letzter Tag: Umsatz %,.0f AUD | Kosten %,.0f AUD | Saldo %,.0f AUD",
                game.lastDailyRevenue, game.lastDailyCosts, game.lastDailyRevenue - game.lastDailyCosts)));
        root.add(row(String.format("Standorte gesamt: %d | Netzwerk-Durchsatz: %.0f Einheiten/Tag | Serverkapazität: %.0f",
                game.sites.size(), game.networkThroughput(), game.serverCapacity())));
        root.add(row(String.format("Aktive Seefrachten: %d | Gesperrte Städte (Katastrophe): %d | Prime Day: %s",
                game.shipments.size(), game.blockedCities.size(), game.isPrimeDay() ? "AKTIV x3.5" : "in " + game.primeDayIn + " Tagen")));
        root.add(row(String.format("Konkurrenten: %d | deren Marktanteil gesamt: %d%%",
                game.rivals.size(), (int) (game.totalRivalShare() * 100))));
        root.add(row(String.format("Wechselkurse: 1 CNY = %.3f AUD | 1 USD = %.3f AUD | B2B-Lagerhotel gestern: %,.0f AUD",
                game.fxCny, game.fxUsd, game.lastB2B)));
        double assets = game.cash;
        for (Site s : game.sites) assets += s.type.baseCost * s.level;
        root.add(row(String.format("Bilanz: Aktiva %,.0f AUD (Kasse + Standorte) | Passiva %,.0f AUD (Kredite) | Eigenkapital %,.0f AUD | næchster Quartalsbericht in %d Tagen",
                assets, game.loanTaken, assets - game.loanTaken, 90 - game.quarterDay)));
        root.add(row(String.format("Forschungspunkte: %.0f | Entwickler: %d | Moral: %d/100 | Streik: %s",
                game.researchPoints, game.developers, (int) game.morale, game.strikeDaysLeft > 0 ? game.strikeDaysLeft + " Tage" : "nein")));

        double auto = globalAutomation();
        root.add(section("Automatisierung (Menschen vs. Roboter)"));
        root.add(row(String.format("Automatisierungsgrad gesamt: %.0f%%  (Roboter %.0f%% / Menschen %.0f%%)",
                auto, auto, 100 - auto)));
        autoBar.setValue((int) auto);
        autoBar.setStringPainted(true);
        autoBar.setString("Roboter-Anteil " + (int) auto + "%");
        autoBar.setForeground(auto > 50 ? new Color(80, 200, 120) : new Color(230, 170, 60));
        autoBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoBar.setMaximumSize(new Dimension(600, 20));
        root.add(autoBar);
        root.add(row("Vollständige Prozesskette (Eingang→Lager→Pick→Pack→Versand) im Grid: x1.5 Durchsatz"));

        root.add(section("Standorte im Detail"));
        if (game.sites.isEmpty()) {
            root.add(row("Noch keine Standorte gebaut."));
        }
        for (Site s : game.sites) {
            int units = 0;
            for (int v : s.stock.values()) units += v;
            root.add(row(String.format("%s | %s, %s | Stufe %d, %d★ | Durchsatz %.0f/Tag | Automation %.0f%% | Kette %s | Paletten %d/%d | Bestand %d",
                    s.type.label, s.city, s.type == com.outback.tycoon.model.SiteType.DATA_CENTER ? "-" : "",
                    s.level, s.stars, s.throughput(game), s.automationPercent(),
                    s.chainComplete() ? "komplett" : "unvollständig", s.palletUsed(), s.palletSlots(), units)));
        }
        root.revalidate();
        root.repaint();
    }

    private double globalAutomation() {
        double autoScore = 0;
        double totalScore = 0;
        for (Site s : game.sites) {
            for (int x = 0; x < Site.GRID; x++)
                for (int y = 0; y < Site.GRID; y++) {
                    com.outback.tycoon.model.MachineType m = s.grid[x][y];
                    if (m == null) continue;
                    totalScore += m.score;
                    if (m.isAutomated()) autoScore += m.score;
                }
        }
        return totalScore == 0 ? 0 : autoScore * 100.0 / totalScore;
    }
}
