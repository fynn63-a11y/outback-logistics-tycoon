package com.outback.tycoon.ui;

import com.outback.tycoon.audio.Music;
import com.outback.tycoon.core.Game;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class HudPanel extends JPanel {
    private final Game game;
    private final Music music;
    private final JLabel cashLabel = new JLabel();
    private final JLabel dateLabel = new JLabel();
    private final JLabel pnlLabel = new JLabel();
    private final JLabel repLabel = new JLabel();
    private final JLabel primeLabel = new JLabel();
    private final JCheckBox chkRebalance = new JCheckBox("Autonomes Rebalancing");
    private final JCheckBox chkPricing = new JCheckBox("Autonomes Pricing & Sourcing");
    private final JCheckBox chkSourcing = new JCheckBox("China-Import (Sourcing)");
    private final JCheckBox chkZones = new JCheckBox("Autonome Lieferzonen");
    private final JList<String> alertList;
    private final DefaultListModel<String> alertModel = new DefaultListModel<>();
    private final JButton musicBtn = new JButton();
    private final JLabel tutorialLabel = new JLabel(" ");
    private MainWindow window;

    public HudPanel(Game game, Music music) {
        this.game = game;
        this.music = music;
        tutorialLabel.setForeground(new Color(120, 200, 255));
        setPreferredSize(new Dimension(300, 800));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(section("Status"));
        add(cashLabel);
        add(dateLabel);
        add(pnlLabel);
        add(repLabel);
        add(primeLabel);
        add(Box.createVerticalStrut(8));

        add(section("Geschwindigkeit (1 Tag = 5 Sek.)"));
        JPanel speedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        int[] speeds = {0, 1, 2, 4};
        String[] labels = {"II", "▶", "▶▶", "▶▶▶"};
        for (int i = 0; i < speeds.length; i++) {
            final int s = speeds[i];
            JButton b = new JButton(labels[i]);
            b.addActionListener(e -> game.speed = s);
            speedRow.add(b);
        }
        add(speedRow);
        add(Box.createVerticalStrut(8));

        add(section("Sandbox-Modus"));
        JCheckBox chkSandbox = new JCheckBox("Sandbox: unbegrenztes Kapital, kein Bankrott");
        chkSandbox.addActionListener(e -> game.sandboxMode = chkSandbox.isSelected());
        add(chkSandbox);
        JLabel sandboxHint = new JLabel("<html><div style='width:260px'>Im Sandbox-Modus kosten Bau, Maschinen, Importe und Uebernahmen kein Geld und eine Insolvenz ist ausgeschlossen - ideal, um alle Systeme risikofrei auszuprobieren.</div></html>");
        sandboxHint.setFont(sandboxHint.getFont().deriveFont(10f));
        add(sandboxHint);
        add(Box.createVerticalStrut(8));

        add(section("Autopilot (vollautonomer Modus)"));
        chkRebalance.setSelected(false);
        chkRebalance.addActionListener(e -> game.autopilotRebalance = chkRebalance.isSelected());
        chkPricing.addActionListener(e -> game.autopilotPricing = chkPricing.isSelected());
        chkSourcing.addActionListener(e -> game.autopilotSourcing = chkSourcing.isSelected());
        chkZones.addActionListener(e -> game.autopilotZones = chkZones.isSelected());
        add(chkRebalance);
        add(chkPricing);
        add(chkSourcing);
        add(chkZones);
        add(Box.createVerticalStrut(8));

        add(section("Musik & Sound"));
        musicBtn.addActionListener(e -> {
            music.toggle();
            musicBtn.setText(music.isEnabled() ? "Musik: an" : "Musik: aus");
        });
        musicBtn.setText("Musik: an");
        add(musicBtn);
        JButton sfxBtn = new JButton("Soundeffekte: an");
        sfxBtn.addActionListener(e -> {
            com.outback.tycoon.audio.Sfx.setEnabled(!com.outback.tycoon.audio.Sfx.isEnabled());
            sfxBtn.setText(com.outback.tycoon.audio.Sfx.isEnabled() ? "Soundeffekte: an" : "Soundeffekte: aus");
        });
        add(sfxBtn);
        add(Box.createVerticalStrut(8));

        add(section("Speichern / Laden"));
        JButton saveBtn = new JButton("Spiel speichern...");
        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    game.save(fc.getSelectedFile());
                    game.alert("Spiel gespeichert: " + fc.getSelectedFile().getName());
                } catch (Exception ex) {
                    game.alert("Speichern fehlgeschlagen: " + ex.getMessage());
                }
            }
        });
        JButton loadBtn = new JButton("Spiel laden...");
        loadBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Game loaded = Game.load(fc.getSelectedFile());
                    game.copyFrom(loaded);
                    game.alert("Spiel geladen: " + fc.getSelectedFile().getName());
                } catch (Exception ex) {
                    game.alert("Laden fehlgeschlagen: " + ex.getMessage());
                }
            }
        });
        add(saveBtn);
        add(loadBtn);
        add(Box.createVerticalStrut(8));

        add(section("Tutorial"));
        add(tutorialLabel);
        JButton settingsBtn = new JButton("Einstellungen...");
        settingsBtn.addActionListener(e -> {
            if (window != null) window.openSettings();
        });
        add(settingsBtn);
        add(Box.createVerticalStrut(8));

        add(section("Meldungen"));
        alertList = new JList<>(alertModel);
        add(new JScrollPane(alertList));
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public void refresh() {
        cashLabel.setText(String.format("Kasse: %,.0f AUD (Grenze %,.0f)", game.cash, -game.debtLimit));
        cashLabel.setForeground(game.cash < 0 ? Color.RED : Color.GREEN.darker());
        dateLabel.setText("Datum: " + game.date() + " (Tag " + game.dayIndex + ", Jahr " + game.date().getYear() + ")");
        pnlLabel.setText(String.format("Gestern: +%,.0f / -%,.0f AUD", game.lastDailyRevenue, game.lastDailyCosts));
        repLabel.setText("Reputation: " + (int) game.reputation + "/100");
        primeLabel.setText(game.isPrimoDay()
                ? "PRIMO DAY aktiv! x3.5 (" + game.primeDayLeft + " Tage)"
                : "Naechster Primo Day in " + game.primeDayIn + " Tagen");
        primeLabel.setForeground(game.isPrimoDay() ? Color.RED : Color.getHSBColor(0.33f, 0.7f, 0.6f));
        String hint = game.tutorialHint();
        tutorialLabel.setText(hint.isEmpty() ? " " : "<html><div style='width:260px'>" + hint + "</div></html>");
        Vector<String> v = new Vector<>(game.alerts);
        alertModel.clear();
        for (String s : v) alertModel.addElement(s);
    }

    public void setWindow(MainWindow w) {
        this.window = w;
    }
}
