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
        root.add(label(String.format("Marktkapitalisierung: %,.0f AUD%s", game.marketCapEstimate(),
                game.ipoDone ? String.format(" (Aktienkurs %.2f AUD x %d Mio. Aktien)", game.sharePrice, Game.SHARES / 1_000_000) : " (Schätzung)")));
        root.add(label("Epoche: " + game.eraName() + " | Szenario: " + game.scenario.label
                + (game.won ? " | SIEG!" : "")));
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

        root.add(section("Flotte & Letzte Meile (Stadt: " + game.selectedCity + ")"));
        JPanel fleetRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton truck = new JButton("LKW kaufen (80.000 AUD)");
        truck.addActionListener(e -> {
            game.buyTruck();
            refresh();
        });
        JButton van = new JButton("Transporter kaufen (30.000 AUD)");
        van.addActionListener(e -> {
            game.buyVan();
            refresh();
        });
        JCheckBox lastMile = new JCheckBox("Eigener Zustelldienst (Letzte Meile)", game.ownLastMile);
        lastMile.addActionListener(e -> game.ownLastMile = lastMile.isSelected());
        fleetRow.add(truck);
        fleetRow.add(van);
        fleetRow.add(lastMile);
        root.add(fleetRow);
        root.add(label("LKW in " + game.selectedCity + ": " + game.trucks.getOrDefault(game.selectedCity, 0)
                + " | Transporter: " + game.vans.getOrDefault(game.selectedCity, 0)));
        root.add(label(game.researched.contains(com.outback.tycoon.model.Tech.OWS_CLOUD)
                ? game.webServiceName + " WebServices aktiv: passive Einnahmen + Webshop-Bonus x1.15 + MF +20%"
                : "WebServices noch nicht erforscht (Techbaum, Cloud-Ära). Dein Service heißt: " + game.webServiceName));
        root.add(Box.createVerticalStrut(10));

        root.add(section("Vertragshändler & Ausschreibungen (Wettbewerb-Events)"));
        if (game.offers.isEmpty() && game.contracts.isEmpty()) {
            root.add(label("Aktuell keine Ausschreibungen. Neue Events erscheinen periodisch."));
        }
        for (com.outback.tycoon.model.DealerContract o : new java.util.ArrayList<>(game.offers)) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(label("ANGEBOT: " + o.retailer + " | " + o.unitsPerDay + "/Tag (" + o.category.label + ") à "
                    + String.format("%.2f", o.pricePerUnit) + " AUD | Laufzeit " + o.daysLeft + " Tage"));
            JButton yes = new JButton("Annehmen");
            yes.addActionListener(e -> {
                game.acceptOffer(o);
                refresh();
            });
            JButton no = new JButton("Ablehnen");
            no.addActionListener(e -> {
                game.declineOffer(o);
                refresh();
            });
            row.add(yes);
            row.add(no);
            root.add(row);
        }
        for (com.outback.tycoon.model.DealerContract c : game.contracts) {
            root.add(label("AKTIV: " + c.retailer + " | " + c.unitsPerDay + "/Tag (" + c.category.label + ") | noch " + c.daysLeft + " Tage | Strafe bei Fehlmenge: "
                    + String.format("%.0f", c.penaltyPerMissedUnit) + " AUD/Einheit"));
        }
        root.add(Box.createVerticalStrut(10));

        root.add(section("Ökosystem: Primo & Co. (Kundenbindung)"));
        root.add(label(String.format("Primo-Abonnenten: %,d | Primo-Quote global: %.0f%% (wirkt auf Nachfrage aller Städte)",
                game.primeSubscribers, game.primeShare() * 100)));
        JPanel ecoRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton prime = new JButton("Primo-Werbekampagne (100.000 AUD, +Abonnenten)");
        prime.addActionListener(e -> {
            game.campaignPrimo();
            refresh();
        });
        JButton video = new JButton(game.primeVideo ? "Primo View: aktiv" : "Primo View freischalten (200.000 AUD)");
        video.setEnabled(!game.primeVideo);
        video.addActionListener(e -> {
            game.unlockEcosystem(true, false, false);
            refresh();
        });
        JButton music = new JButton(game.primeMusic ? "Primo Tunes: aktiv" : "Primo Tunes freischalten (200.000 AUD)");
        music.setEnabled(!game.primeMusic);
        music.addActionListener(e -> {
            game.unlockEcosystem(false, true, false);
            refresh();
        });
        JButton fresh = new JButton(game.primeFresh ? "Primo Fresh (Lebensmittel): aktiv" : "Primo Fresh freischalten (150.000 AUD, +20% FMCG/Kühlkette)");
        fresh.setEnabled(!game.primeFresh);
        fresh.addActionListener(e -> {
            game.unlockEcosystem(false, false, true);
            refresh();
        });
        ecoRow.add(prime);
        ecoRow.add(video);
        ecoRow.add(music);
        ecoRow.add(fresh);
        root.add(ecoRow);
        root.add(Box.createVerticalStrut(10));

        root.add(section("Blitzangebote & Tochterfirmen"));
        JPanel promoRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton flashAll = new JButton("Blitzangebot ALLE Kategorien (20.000 AUD, 3 Tage: -20% Preis, x1.6 Nachfrage)");
        flashAll.addActionListener(e -> {
            game.startFlashSale(null);
            refresh();
        });
        promoRow.add(flashAll);
        if (game.flashSaleDays > 0) root.add(label("BLITZANGEBOT AKTIV noch " + game.flashSaleDays + " Tage!"));
        JButton sub = new JButton("Tochterfirma gruenden (500.000 AUD, +3.000 AUD/Tag)");
        sub.addActionListener(e -> {
            game.foundSubsidiary("Tochter " + (game.subsidiaries.size() + 1));
            refresh();
        });
        promoRow.add(sub);
        root.add(promoRow);
        for (String s : game.subsidiaries) root.add(label("Tochterfirma: " + s));
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

        root.add(section("GuV - Einnahmen & Ausgaben (letzter Tag)"));
        double revSum = 0;
        for (java.util.Map.Entry<String, Double> e : game.revBy.entrySet()) {
            root.add(label(String.format("  + %s: %,.0f AUD", e.getKey(), e.getValue())));
            revSum += e.getValue();
        }
        root.add(label(String.format("  = Einnahmen gesamt: %,.0f AUD", revSum)));
        double costSum = 0;
        for (java.util.Map.Entry<String, Double> e : game.costBy.entrySet()) {
            root.add(label(String.format("  - %s: %,.0f AUD", e.getKey(), e.getValue())));
            costSum += e.getValue();
        }
        root.add(label(String.format("  = Ausgaben gesamt: %,.0f AUD | Saldo: %,.0f AUD", costSum, revSum - costSum)));
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
