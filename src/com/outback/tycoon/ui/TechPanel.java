package com.outback.tycoon.ui;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.Tech;

import javax.swing.*;
import java.awt.*;

public class TechPanel extends JPanel {
    private final Game game;
    private final JPanel root = new JPanel();
    private final JLabel rpLabel = new JLabel();
    private final JLabel devLabel = new JLabel();

    private static String eraName(int era) {
        switch (era) {
            case 1: return "Breitband-Ära (2000+)";
            case 2: return "Cloud-Ära (2010+)";
            default: return "Modem-Ära";
        }
    }

    public TechPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        add(new JScrollPane(root), BorderLayout.CENTER);
    }

    public void refresh() {
        root.removeAll();
        rpLabel.setText(String.format("Forschungspunkte (RP): %.0f", game.researchPoints));
        devLabel.setText("Software-Entwickler: " + game.developers + " (+2 RP/Tag pro Entwickler)");
        root.add(rpLabel);
        root.add(devLabel);
        JPanel devRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
        devRow.add(hire);
        devRow.add(fire);
        root.add(devRow);
        root.add(Box.createVerticalStrut(8));

        for (Tech t : Tech.values()) {
            boolean done = game.researched.contains(t);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            boolean eraOk = game.eraIndex() >= t.minEra;
            JLabel l = new JLabel((done ? "[ERFORSCHT] " : "") + t.label + " - " + t.description
                    + (done ? "" : " (" + (int) t.rpCost + " RP" + (t.minEra > 0 ? ", ab " + eraName(t.minEra) : "") + ")"));
            JButton b = new JButton(done ? "Erforscht" : (eraOk ? "Forschen (" + (int) t.rpCost + " RP)" : "Gesperrt bis " + eraName(t.minEra)));
            b.setEnabled(!done && eraOk && game.researchPoints >= t.rpCost);
            b.addActionListener(e -> {
                game.researchTech(t);
                refresh();
            });
            row.add(l);
            row.add(b);
            root.add(row);
        }
        boolean auto = game.researched.contains(Tech.AUTOMATION_PROTOCOLS);
        JLabel hint = new JLabel(auto
                ? "Automatisierungs-Protokolle erforscht: Autopilot-Schalter im HUD voll wirksam."
                : "Tipp: Erforsche 'Automatisierungs-Protokolle', um den Autopiloten freizuschalten.");
        hint.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        root.add(hint);
        root.revalidate();
        root.repaint();
    }
}
