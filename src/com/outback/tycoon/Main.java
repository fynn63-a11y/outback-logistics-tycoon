package com.outback.tycoon;

import com.outback.tycoon.core.Game;
import com.outback.tycoon.ui.MainWindow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            com.outback.tycoon.ui.Theme.apply();
            Game game = new Game();
            game.start();
            new MainWindow(game).setVisible(true);
        });
    }
}
