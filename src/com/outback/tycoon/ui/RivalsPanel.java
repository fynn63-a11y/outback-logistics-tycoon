package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.RivalCompany;

import javax.swing.*;
import java.awt.*;

public class RivalsPanel extends JPanel {
    private final Game game;
    private final JPanel root = new JPanel();

    public RivalsPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        add(new JScrollPane(root), BorderLayout.CENTER);
    }

    public void refresh() {
        root.removeAll();
        JLabel head = new JLabel("KI-Konkurrenten am australischen Logistikmarkt (" + game.rivals.size() + "/5)");
        head.setFont(head.getFont().deriveFont(Font.BOLD, 13f));
        head.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        root.add(head);
        JLabel hint = new JLabel("<html><div style='width:520px'>Konkurrenten nehmen dir Marktanteil am Nachfrage-Volumen weg. "
                + "Gute Performance (Umsatz, Reputation) draengt sie zurueck. Ab genug Kapital kannst du sie uebernehmen "
                + "(bis zu 5 Konkurrenten erscheinen im Laufe der Zeit).</div></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 6));
        root.add(hint);

        for (RivalCompany r : game.rivals) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(r.color), BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            JProgressBar share = new JProgressBar(0, 35);
            share.setValue((int) (r.marketShare * 100));
            share.setStringPainted(true);
            share.setString("Marktanteil " + (int) (r.marketShare * 100) + "%");
            share.setPreferredSize(new Dimension(220, 18));
            row.add(new JLabel(r.name + (r.sites > 0 ? " | eigene Standorte: " + r.sites + " in " + String.join(", ", r.siteCities) : "")));
            row.add(share);
            double value = game.rivalValue(r);
            JButton buy = new JButton(String.format("Uebernehmen (%,.0f AUD)", value));
            buy.addActionListener(e -> {
                game.acquireRival(r);
                refresh();
            });
            row.add(buy);
            root.add(row);
        }
        JLabel total = new JLabel("Gesamt-Marktanteil der Konkurrenz: " + (int) (game.totalRivalShare() * 100)
                + "% (dein Marktanteil: " + (100 - (int) (game.totalRivalShare() * 100)) + "%)");
        total.setBorder(BorderFactory.createEmptyBorder(8, 6, 4, 6));
        root.add(total);
        root.revalidate();
        root.repaint();
    }
}
