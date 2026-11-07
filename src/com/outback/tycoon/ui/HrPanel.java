package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;

import javax.swing.*;
import java.awt.*;

public class HrPanel extends JPanel {
    private final Game game;
    private final JPanel root = new JPanel();
    private final JProgressBar moraleBar = new JProgressBar(0, 100);
    private final JLabel staffLabel = new JLabel();
    private final JCheckBox tempsBox = new JCheckBox("Saisonale Aushilfen im Dezember (Leiharbeiter-Agentur)", true);
    private final JCheckBox insBox = new JCheckBox("Versicherung gegen Katastrophen (8.000 AUD/Monat, zahlt 50.000 AUD Schadenersatz)", false);

    public HrPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        add(new JScrollPane(root), BorderLayout.CENTER);
        tempsBox.addActionListener(e -> game.tempsDecember = tempsBox.isSelected());
        insBox.addActionListener(e -> game.insured = insBox.isSelected());
    }

    public void refresh() {
        root.removeAll();
        JLabel head = new JLabel("Personal, Moral & Risiken");
        head.setFont(head.getFont().deriveFont(Font.BOLD, 13f));
        root.add(head);

        root.add(new JLabel("Moral: " + (int) game.morale + "/100 (unter 30 besteht Streik-Gefahr)"));
        moraleBar.setValue((int) game.morale);
        moraleBar.setForeground(game.morale < 30 ? Color.RED : new Color(80, 180, 80));
        root.add(moraleBar);

        staffLabel.setText("Festangestellte: " + game.permanentStaff);
        root.add(staffLabel);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton hire = new JButton("Mitarbeiter einstellen");
        hire.addActionListener(e -> {
            game.hireStaff();
            refresh();
        });
        JButton fire = new JButton("Mitarbeiter entlassen (Moral sinkt)");
        fire.addActionListener(e -> {
            game.fireStaff();
            game.morale = Math.max(0, game.morale - 3);
            refresh();
        });
        row.add(hire);
        row.add(fire);
        root.add(row);
        root.add(tempsBox);
        root.add(insBox);
        JButton union = new JButton("Gewerkschaft verhandeln (30.000 AUD, Moral +15)");
        union.addActionListener(e -> {
            game.negotiateUnion();
            refresh();
        });
        root.add(union);

        if (game.strikeDaysLeft > 0) {
            JLabel strike = new JLabel("WILDER STREIK! Noch " + game.strikeDaysLeft + " Tage Arbeitsniederlegung.");
            strike.setForeground(Color.RED);
            root.add(strike);
        }
        if (!game.researched.contains(com.outback.tycoon.model.Tech.PATENT_PROTECTION)) {
            JLabel esp = new JLabel("Hinweis: Ohne Patentschutz droht Industriespionage durch KI-Konkurrenten.");
            esp.setForeground(Color.ORANGE);
            root.add(esp);
        }
        root.revalidate();
        root.repaint();
    }
}
