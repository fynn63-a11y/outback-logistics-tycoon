package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.Brand;
import com.outback.tycoon.model.Carrier;
import com.outback.tycoon.model.SKU;
import com.outback.tycoon.model.Shipment;

import javax.swing.*;
import java.awt.*;

public class CompanyPanel extends JPanel {
    private final Game game;
    private final JPanel root = new JPanel();

    public CompanyPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        add(new JScrollPane(root), BorderLayout.CENTER);
    }

    public void refresh() {
        root.removeAll();
        root.add(section("Finanzen"));
        root.add(label(String.format("Kasse: %,.0f AUD (Schulden-Grenze: %,.0f AUD, Kredite aufgenommen: %,.0f AUD)",
                game.cash, -game.debtLimit, game.loanTaken)));
        JButton loan = new JButton("Kredit aufnehmen (+500.000 AUD, erhoeht Schulden-Grenze, 12% Zins p.a. auf Minus)");
        loan.addActionListener(e -> {
            game.takeLoan();
            refresh();
        });
        root.add(loan);
        root.add(label(String.format("Letzter Tag: Umsatz %,.0f AUD, Kosten %,.0f AUD, Saldo %,.0f AUD",
                game.lastDailyRevenue, game.lastDailyCosts, game.lastDailyRevenue - game.lastDailyCosts)));
        root.add(label(String.format("Marktkapitalisierung (Schätzung): %,.0f AUD", game.marketCapEstimate())));
        root.add(label(String.format("Netzwerk-Durchsatz: %.0f Einheiten/Tag | Serverkapazität: %.0f", game.networkThroughput(), game.serverCapacity())));
        root.add(label("Aktive Seefrachten: " + game.shipments.size()));

        JButton ipo = new JButton(game.ipoDone
                ? "IPO bereits vollzogen (ASX Sydney)"
                : String.format("IPO an der ASX Sydney (ab %,.0f AUD Marktkap., +25 Mio. AUD)", Game.IPO_THRESHOLD));
        ipo.setEnabled(game.ipoAvailable());
        ipo.addActionListener(e -> {
            game.doIpo();
            refresh();
        });
        root.add(ipo);
        root.add(Box.createVerticalStrut(10));

        root.add(section("Marken-Inkubator (Private Labeling)"));
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField("Outback Gear", 14);
        JButton found = new JButton("Eigene Marke gruenden (250.000 AUD)");
        found.addActionListener(e -> {
            game.foundBrand(nameField.getText().trim().isEmpty() ? "Outback Gear" : nameField.getText().trim());
            refresh();
        });
        brandRow.add(nameField);
        brandRow.add(found);
        root.add(brandRow);
        for (Brand b : game.brands) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(label((b.playerOwned ? "[EIGEN] " : "[Rivale] ") + b.name
                    + (b.playerOwned ? " | Marketing-Stufe " + b.marketingLevel : "")));
            if (b.playerOwned) {
                JButton camp = new JButton("Werbekampagne (50.000 AUD)");
                camp.addActionListener(e -> {
                    game.launchCampaign(b);
                    refresh();
                });
                JButton dev = new JButton("Produkt entwickeln (30 RP + 150.000 AUD)");
                dev.addActionListener(e -> {
                    game.developProduct(b);
                    refresh();
                });
                row.add(camp);
                row.add(dev);
            }
            root.add(row);
        }
        root.add(Box.createVerticalStrut(10));

        root.add(section("Frachtdienstleister & Vertraege (Make-or-Buy)"));
        for (Carrier c : game.carriers) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(label(c.name + " | Vertragsstufe " + c.contractLevel + "/3 | Fixkosten "
                    + String.format("%,.0f", c.fixedFeePerMonth) + " AUD/Monat"));
            JButton neg = new JButton(String.format("Vertrag aushandeln (%,.0f AUD)", c.negotiateCost()));
            neg.setEnabled(c.canNegotiate());
            neg.addActionListener(e -> {
                game.negotiateCarrier(c);
                refresh();
            });
            row.add(neg);
            root.add(row);
        }
        root.add(Box.createVerticalStrut(10));

        root.add(section("Katalog & Autopilot-Preise"));
        for (SKU sku : game.catalog) {
            root.add(label(String.format("%s [%s] | EK %.2f | VK %.2f | Marge %.0f%% | Marke: %s",
                    sku.name, sku.category.label, sku.baseCost, sku.price, sku.margin() * 100,
                    sku.brand != null ? sku.brand.name : "-")));
        }
        root.add(Box.createVerticalStrut(10));

        root.add(section("Laufende China-Importe (Seefracht)"));
        for (Shipment sh : game.shipments) {
            root.add(label("Container #" + sh.id + " " + sh.origin + " -> " + sh.destCity
                    + " | noch " + sh.daysLeft + " Tage | " + sh.totalUnits() + " Einheiten"));
        }
        root.revalidate();
        root.repaint();
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 4, 2, 4));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
