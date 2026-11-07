package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.City;
import com.outback.tycoon.model.Site;

import javax.swing.*;
import java.awt.*;

public class ZonesPanel extends JPanel {
    private final Game game;
    private final JPanel root = new JPanel();

    public ZonesPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        add(new JScrollPane(root), BorderLayout.CENTER);
    }

    public void refresh() {
        root.removeAll();
        JLabel head = new JLabel("Lieferzonen - welches Zentrum bedient welche Stadt?");
        head.setFont(head.getFont().deriveFont(Font.BOLD, 13f));
        head.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        root.add(head);
        JLabel hint = new JLabel("<html><div style='width:640px'>Jede freigeschaltete Stadt wird einem betriebsbereiten Standort zugewiesen. "
                + "Distanz (km, aus der Karte) erhoeht die Frachtkosten; das Service-Level des Zentrums (Standard / Next-Day / Same-Day) "
                + "erhoeht die Nachfrage in der Zone. Weiter entfernte Zonen haben NICHT weniger Kunden - sie kosten nur mehr. "
                + "'Auto-Zonen' weist jede Stadt automatisch dem besten Zentrum zu (Autopilot).</div></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 6));
        root.add(hint);

        JButton auto = new JButton("Auto-Zonen zuweisen (Autopilot)");
        auto.addActionListener(e -> {
            game.autoAssignZones();
            game.alert("Lieferzonen automatisch zugewiesen.");
            refresh();
        });
        JPanel autoRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        autoRow.add(auto);
        root.add(autoRow);

        for (City c : game.cities) {
            if (!game.isUnlocked(c)) continue;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            Site server = game.servingSite(c.name);
            String distText = server != null
                    ? String.format("Distanz %,.0f km | Service %s (x%.2f) | Bewertung %.1f/5", game.distanceKm(game.cityByName(server.city), c), server.serviceLevel.label, server.serviceLevel.demandFactor, game.cityRating(c.name))
                    : String.format("kein Zentrum zugewiesen (Nachfrage x0.4) | Bewertung %.1f/5", game.cityRating(c.name));
            row.add(new JLabel(c.name + ": " + distText));

            java.util.List<String> options = new java.util.ArrayList<>();
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            options.add("(automatisch)");
            ids.add(-1);
            for (Site s : game.sites) {
                if (s.isOperational() && s.type != com.outback.tycoon.model.SiteType.DATA_CENTER) {
                    options.add("#" + s.id + " " + s.type.label + " (" + s.city + ", " + s.serviceLevel.label + ")");
                    ids.add(s.id);
                }
            }
            JComboBox<String> combo = new JComboBox<>(options.toArray(new String[0]));
            Integer assigned = game.zoneAssignment.get(c.name);
            if (assigned != null) {
                int idx = ids.indexOf(assigned);
                if (idx >= 0) combo.setSelectedIndex(idx);
            }
            final String cityName = c.name;
            combo.addActionListener(e -> {
                int sel = combo.getSelectedIndex();
                if (sel <= 0) game.zoneAssignment.remove(cityName);
                else game.assignZone(cityName, ids.get(sel));
                refresh();
            });
            row.add(combo);
            root.add(row);
        }
        root.revalidate();
        root.repaint();
    }
}
