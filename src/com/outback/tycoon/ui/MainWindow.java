package com.outback.tycoon.ui;

import com.outback.tycoon.audio.Music;
import com.outback.tycoon.core.Game;
import com.outback.tycoon.model.Scenario;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final Game game;
    private final Music music = new Music();
    private final HudPanel hud;
    private final MacroMapPanel macro;
    private final MesoPanel meso;
    private final MicroPanel micro;
    private final CompanyPanel company;
    private final TechPanel tech;
    private final HrPanel hr;
    private final RivalsPanel rivalsPanel;
    private final DashboardPanel dashboard;
    private final ChartsPanel charts;
    private final SupplyChainPanel supplyChain;
    private final ZonesPanel zones;
    private final JTabbedPane tabs = new JTabbedPane();
    private boolean gameOverShown = false;
    private boolean wonShown = false;

    public MainWindow(Game game) {
        super("Outback Logistics Tycoon");
        this.game = game;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        macro = new MacroMapPanel(game, this);
        meso = new MesoPanel(game, this);
        micro = new MicroPanel(game);
        company = new CompanyPanel(game);
        tech = new TechPanel(game);
        hr = new HrPanel(game);
        rivalsPanel = new RivalsPanel(game);
        dashboard = new DashboardPanel(game);
        charts = new ChartsPanel(game);
        supplyChain = new SupplyChainPanel(game);
        zones = new ZonesPanel(game);
        hud = new HudPanel(game, music);
        hud.setWindow(this);

        chooseScenario();
        chooseDifficulty();
        chooseCompanyName();
        installShortcuts();

        tabs.addTab("Karte (Makro)", macro);
        tabs.addTab("Dashboard", dashboard);
        tabs.addTab("Statistik", charts);
        tabs.addTab("Lieferkette", supplyChain);
        tabs.addTab("Lieferzonen", zones);
        tabs.addTab("Stadt & Bau (Meso)", meso);
        tabs.addTab("Lager-Grid (Mikro)", micro);
        tabs.addTab("Firma & Finanzen", company);
        tabs.addTab("Technik & F&E", tech);
        tabs.addTab("Personal & Risiko", hr);
        tabs.addTab("Konkurrenz", rivalsPanel);

        add(tabs, BorderLayout.CENTER);
        add(hud, BorderLayout.EAST);

        music.start();

        Timer uiTimer = new Timer(200, e -> refresh());
        uiTimer.start();

        applyFullscreen();
    }

    public void applyFullscreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (game.settingFullscreen && gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            gd.setFullScreenWindow(null);
            setExtendedState(MAXIMIZED_BOTH);
        }
    }

    private void chooseDifficulty() {
        String[] options = new String[com.outback.tycoon.model.Difficulty.values().length];
        for (int i = 0; i < options.length; i++) options[i] = com.outback.tycoon.model.Difficulty.values()[i].label;
        Object sel = JOptionPane.showInputDialog(this, "Schwierigkeitsgrad wählen:",
                "Schwierigkeit", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (sel != null) {
            for (com.outback.tycoon.model.Difficulty d : com.outback.tycoon.model.Difficulty.values())
                if (d.label.equals(sel)) {
                    game.difficulty = d;
                    game.cash = d.startCash;
                }
        }
    }

    private void chooseCompanyName() {
        String name = JOptionPane.showInputDialog(this, "Wie soll deine Logistik-Firma heißen?",
                "Firmenname", JOptionPane.QUESTION_MESSAGE);
        if (name != null && !name.trim().isEmpty()) game.companyName = name.trim();
        setTitle(game.companyName + " - Outback Logistics Tycoon");
    }

    private boolean wsPromptShown = false;

    private void maybeAskWebServiceName() {
        if (wsPromptShown || game.webServiceNamed) return;
        boolean hasDC = false;
        for (com.outback.tycoon.model.Site s : game.sites)
            if (s.type == com.outback.tycoon.model.SiteType.DATA_CENTER) hasDC = true;
        if (!hasDC && !game.researched.contains(com.outback.tycoon.model.Tech.OWS_CLOUD)) return;
        wsPromptShown = true;
        game.webServiceNamed = true;
        String name = JOptionPane.showInputDialog(this,
                "Du eröffnest jetzt deine Web-Services! Wie sollen sie heißen? (z. B. CloudX, SkyNet, …)",
                "Web-Services eröffnen", JOptionPane.QUESTION_MESSAGE);
        game.webServiceName = (name == null || name.trim().isEmpty()) ? "OWS" : name.trim();
        game.alert("Web-Services '" + game.webServiceName + "' eroeffnet! Rechenzentren bringen jetzt Einnahmen.");
    }

    private void installShortcuts() {
        JRootPane rp = getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(' '), "pause");
        rp.getActionMap().put("pause", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                game.speed = game.speed == 0 ? 1 : 0;
            }
        });
        for (int i = 1; i <= 8; i++) {
            final int idx = i - 1;
            rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char) ('0' + i)), "tab" + i);
            rp.getActionMap().put("tab" + i, new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (idx < tabs.getTabCount()) tabs.setSelectedIndex(idx);
                }
            });
        }
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "faster");
        rp.getActionMap().put("faster", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                game.speed = Math.min(4, game.speed + 1);
            }
        });
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "slower");
        rp.getActionMap().put("slower", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                game.speed = Math.max(0, game.speed - 1);
            }
        });
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('m'), "music");
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "esc");
        rp.getActionMap().put("esc", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openPauseMenu();
            }
        });
        rp.getActionMap().put("music", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                music.toggle();
            }
        });
    }

    public void openPauseMenu() {
        boolean wasRunning = game.speed > 0 && !game.gameOver;
        if (wasRunning) game.speed = 0;
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        JButton cont = new JButton("Weiterspielen");
        JButton save = new JButton("Spiel speichern...");
        JButton load = new JButton("Spiel laden...");
        JButton settings = new JButton("Einstellungen...");
        JButton quit = new JButton("Spiel beenden");
        p.add(cont); p.add(save); p.add(load); p.add(settings); p.add(quit);
        final boolean[] open = {true};
        cont.addActionListener(e -> { if (wasRunning) game.speed = 1; open[0] = false; });
        save.addActionListener(e -> { saveGame(); });
        load.addActionListener(e -> { loadGame(); });
        settings.addActionListener(e -> { openSettings(); });
        quit.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Wirklich beenden? Nicht gespeicherter Fortschritt geht verloren.",
                    "Beenden", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
        });
        JOptionPane pane = new JOptionPane(p, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        JDialog dlg = pane.createDialog(this, "Menü (ESC) - " + game.companyName);
        cont.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    public void saveGame() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                game.save(fc.getSelectedFile());
                game.alert("Spiel gespeichert: " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                game.alert("Speichern fehlgeschlagen: " + ex.getMessage());
            }
        }
    }

    public void loadGame() {
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
    }

    public void openSettings() {
        JPanel p = new JPanel(new GridLayout(0, 1));
        JCheckBox fs = new JCheckBox("Vollbild", game.settingFullscreen);
        JCheckBox mus = new JCheckBox("Musik", music.isEnabled());
        JCheckBox sfx = new JCheckBox("Soundeffekte", com.outback.tycoon.audio.Sfx.isEnabled());
        JTextField name = new JTextField(game.companyName, 16);
        JTextField wsName = new JTextField(game.webServiceName, 16);
        p.add(new JLabel("Einstellungen (Tasten: 1-8 Tabs, Leer = Pause, +/- Tempo, M Musik)"));
        p.add(fs);
        p.add(mus);
        p.add(sfx);
        p.add(new JLabel("Firmenname:"));
        p.add(name);
        p.add(new JLabel("Web-Service-Name:"));
        p.add(wsName);
        int r = JOptionPane.showConfirmDialog(this, p, "Einstellungen", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            game.settingFullscreen = fs.isSelected();
            applyFullscreen();
            if (!fs.isSelected()) {
                setSize(1280, 800);
                setLocationRelativeTo(null);
            }
            if (mus.isSelected() != music.isEnabled()) music.toggle();
            if (sfx.isSelected() != com.outback.tycoon.audio.Sfx.isEnabled())
                com.outback.tycoon.audio.Sfx.setEnabled(sfx.isSelected());
            if (!name.getText().trim().isEmpty()) {
                game.companyName = name.getText().trim();
                setTitle(game.companyName + " - Outback Logistics Tycoon");
            }
            if (!wsName.getText().trim().isEmpty()) game.webServiceName = wsName.getText().trim();
        }
    }

    public void openMeso() {
        setTabByTitle("Stadt & Bau (Meso)");
    }

    public void openMicro() {
        setTabByTitle("Lager-Grid (Mikro)");
    }

    private void setTabByTitle(String title) {
        for (int i = 0; i < tabs.getTabCount(); i++)
            if (tabs.getTitleAt(i).equals(title)) {
                tabs.setSelectedIndex(i);
                return;
            }
    }

    public JTabbedPane tabs() {
        return tabs;
    }

    private void chooseScenario() {
        String[] options = new String[Scenario.values().length];
        for (int i = 0; i < options.length; i++) options[i] = Scenario.values()[i].label;
        Object sel = JOptionPane.showInputDialog(this, "Szenario wählen:\n" + Scenario.IPO_RACE.description,
                "Outback Logistics Tycoon - Szenario", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (sel != null) {
            for (Scenario s : Scenario.values()) if (s.label.equals(sel)) game.scenario = s;
        }
    }

    public void refresh() {
        maybeAskWebServiceName();
        hud.refresh();
        dashboard.refresh();
        charts.repaint();
        supplyChain.repaint();
        zones.refresh();
        macro.repaint();
        meso.refresh();
        micro.repaint();
        company.refresh();
        tech.refresh();
        hr.refresh();
        rivalsPanel.refresh();
        if (game.gameOver && !gameOverShown) {
            gameOverShown = true;
            com.outback.tycoon.audio.Sfx.crash();
            JOptionPane.showMessageDialog(this,
                    "GAME OVER\n\n" + game.gameOverReason,
                    "Bankrott", JOptionPane.ERROR_MESSAGE);
        }
        if (game.won && !wonShown) {
            wonShown = true;
            JOptionPane.showMessageDialog(this,
                    game.winMessage + "\n\nDu kannst weiterspielen (Endlos-Modus).",
                    "Sieg!", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
