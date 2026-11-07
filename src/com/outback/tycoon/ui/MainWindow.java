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
        hud = new HudPanel(game, music);

        chooseScenario();

        tabs.addTab("Dashboard", dashboard);
        tabs.addTab("Statistik", charts);
        tabs.addTab("Karte (Makro)", macro);
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
        hud.refresh();
        dashboard.refresh();
        charts.repaint();
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
