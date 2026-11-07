package com.outback.tycoon.ui;

import com.outback.tycoon.audio.Music;
import com.outback.tycoon.core.Game;

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
    private final JTabbedPane tabs = new JTabbedPane();
    private boolean gameOverShown = false;

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
        hud = new HudPanel(game, music);

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
        tabs.setSelectedIndex(1);
    }

    public JTabbedPane tabs() {
        return tabs;
    }

    public void refresh() {
        hud.refresh();
        macro.repaint();
        meso.refresh();
        micro.repaint();
        company.refresh();
        tech.refresh();
        hr.refresh();
        rivalsPanel.refresh();
        if (game.gameOver && !gameOverShown) {
            gameOverShown = true;
            JOptionPane.showMessageDialog(this,
                    "GAME OVER\n\n" + game.gameOverReason,
                    "Bankrott", JOptionPane.ERROR_MESSAGE);
        }
    }
}
