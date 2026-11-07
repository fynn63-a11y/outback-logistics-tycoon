package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.City;
import com.outback.tycoon.model.Site;
import com.outback.tycoon.model.SiteType;

import javax.swing.*;
import java.awt.*;

public class MesoPanel extends JPanel {
    private final Game game;
    private final MainWindow window;
    private final JPanel siteList = new JPanel();

    public MesoPanel(Game game, MainWindow window) {
        this.game = game;
        this.window = window;
        setLayout(new BorderLayout());
        siteList.setLayout(new BoxLayout(siteList, BoxLayout.Y_AXIS));
        add(new JScrollPane(siteList), BorderLayout.CENTER);

        JPanel buildBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        buildBar.setBackground(new Color(24, 28, 38));
        buildBar.setBorder(BorderFactory.createTitledBorder("BAUMENÜ - Standort wählen und bauen"));
        for (SiteType t : SiteType.values()) {
            JButton b = new JButton(t.label + "  |  " + String.format("%,.0f", t.baseCost) + " AUD  |  Bauzeit " + t.buildDays + "d");
            b.setBackground(typeColor(t));
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            b.setBorderPainted(true);
            b.setFocusPainted(false);
            b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
            b.addActionListener(e -> {
                Site s = game.buildSite(t);
                if (s != null) game.selectedSite = s;
                refresh();
            });
            buildBar.add(b);
        }
        add(buildBar, BorderLayout.SOUTH);
    }

    public void refresh() {
        siteList.removeAll();
        City c = game.cityByName(game.selectedCity);
        JLabel head = new JLabel(String.format(
                "%s, %s - %,d Einwohner, Primo-Quote %.0f%%, Saisonfaktor x%.2f",
                c.name, c.state, c.population, c.primeQuote * 100, game.seasonality()));
        head.setFont(head.getFont().deriveFont(Font.BOLD, 14f));
        head.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        siteList.add(head);

        if (!game.isUnlocked(c)) {
            JLabel locked = new JLabel("Region gesperrt: " + game.unlockHint(c));
            locked.setForeground(Color.RED);
            locked.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            siteList.add(locked);
        }
        java.util.List<Site> sites = game.sitesIn(c.name);
        if (sites.isEmpty() && game.isUnlocked(c)) {
            JLabel empty = new JLabel("Noch keine Standorte hier. Baue unten dein erstes Lager / FC Hub.");
            empty.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            siteList.add(empty);
        }
        for (Site s : sites) {
            siteList.add(siteRow(s));
        }
        siteList.revalidate();
        siteList.repaint();
    }

    private Color typeColor(SiteType t) {
        switch (t) {
            case WAREHOUSE: return new Color(96, 125, 139);
            case FC_HUB: return new Color(46, 125, 50);
            case DEPOT: return new Color(141, 110, 99);
            case VERTEILZENTRUM: return new Color(25, 118, 210);
            case UMSCHLAGZENTRUM: return new Color(0, 151, 167);
            case CONTAINER_TERMINAL: return new Color(69, 90, 160);
            case AIR_HUB: return new Color(100, 181, 246);
            case HIGHLAGER: return new Color(121, 85, 72);
            case DATA_CENTER: return new Color(103, 58, 183);
            case OUTBACK_GO: return new Color(230, 126, 34);
            default: return new Color(60, 66, 80);
        }
    }

    private JPanel siteRow(Site s) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setBackground(new Color(30, 35, 48));
        row.setOpaque(true);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(s == game.selectedSite ? new Color(255, 213, 79) : typeColor(s.type), 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        String stars = "★".repeat(s.stars) + "☆".repeat(5 - s.stars);
        String state = s.buildDaysLeft > 0 ? "IM BAU (" + s.buildDaysLeft + " Tage)"
                : s.blockedDays > 0 ? "KRISE: " + s.blockedReason + " (" + s.blockedDays + " Tage)" : "betriebsbereit";
        JLabel info = new JLabel(String.format(
                "%s | Stufe %d | %s | %s | Paletten %d/%d | Durchsatz %.0f/Tag",
                s.describe(), s.level, stars, state, s.palletUsed(), s.palletSlots(),
                s.throughput(game)));
        row.add(info);

        JButton select = new JButton("Auswaehlen");
        select.addActionListener(e -> {
            game.selectedSite = s;
            refresh();
        });
        JButton gridBtn = new JButton("Mikro-Grid oeffnen");
        gridBtn.addActionListener(e -> {
            game.selectedSite = s;
            window.openMicro();
        });
        JButton upgrade = new JButton(String.format("Ausbau (%,.0f)", s.upgradeCost()));
        upgrade.addActionListener(e -> {
            game.upgradeSite(s);
            refresh();
        });
        JButton star = new JButton(String.format("+Stern (%,.0f)", s.starUpgradeCost()));
        star.setEnabled(s.stars < 5);
        star.addActionListener(e -> {
            game.upgradeStars(s);
            refresh();
        });
        JButton service = new JButton(s.serviceLevel == com.outback.tycoon.model.ServiceLevel.SAME_DAY
                ? "Same-Day aktiv"
                : String.format("Service -> %s (%,.0f)", com.outback.tycoon.model.ServiceLevel.values()[s.serviceLevel.ordinal() + 1].label, s.serviceUpgradeCost()));
        service.setEnabled(s.serviceLevel != com.outback.tycoon.model.ServiceLevel.SAME_DAY);
        service.addActionListener(e -> {
            game.upgradeService(s);
            refresh();
        });
        row.add(select);
        row.add(gridBtn);
        row.add(upgrade);
        row.add(star);
        row.add(service);
        return row;
    }
}
