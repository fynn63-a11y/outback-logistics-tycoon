package com.outback.tycoon.ui;

import javax.swing.*;
import java.awt.Color;

public class Theme {
    public static final Color BG = new Color(22, 26, 36);
    public static final Color PANEL = new Color(32, 38, 52);
    public static final Color PANEL_LIGHT = new Color(42, 49, 66);
    public static final Color ACCENT = new Color(255, 170, 60);
    public static final Color FG = new Color(226, 232, 242);
    public static final Color MUTED = new Color(150, 160, 180);

    public static void apply() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", PANEL);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", FG);
        UIManager.put("Label.foreground", FG);
        UIManager.put("Button.background", PANEL_LIGHT);
        UIManager.put("Button.foreground", FG);
        UIManager.put("Button.focus", ACCENT);
        UIManager.put("ToggleButton.background", PANEL_LIGHT);
        UIManager.put("ToggleButton.foreground", FG);
        UIManager.put("CheckBox.background", PANEL);
        UIManager.put("CheckBox.foreground", FG);
        UIManager.put("RadioButton.background", PANEL);
        UIManager.put("RadioButton.foreground", FG);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.selected", PANEL_LIGHT);
        UIManager.put("TabbedPane.contentAreaBackground", PANEL);
        UIManager.put("TabbedPane.foreground", FG);
        UIManager.put("TextField.background", PANEL_LIGHT);
        UIManager.put("TextField.foreground", FG);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextArea.background", PANEL_LIGHT);
        UIManager.put("TextArea.foreground", FG);
        UIManager.put("ComboBox.background", PANEL_LIGHT);
        UIManager.put("ComboBox.foreground", FG);
        UIManager.put("List.background", PANEL_LIGHT);
        UIManager.put("List.foreground", FG);
        UIManager.put("List.selectionBackground", ACCENT);
        UIManager.put("List.selectionForeground", Color.BLACK);
        UIManager.put("ScrollPane.background", PANEL);
        UIManager.put("Viewport.background", PANEL);
        UIManager.put("ProgressBar.background", PANEL_LIGHT);
        UIManager.put("ProgressBar.foreground", ACCENT);
        UIManager.put("TitledBorder.titleColor", MUTED);
        UIManager.put("Table.background", PANEL_LIGHT);
        UIManager.put("Table.foreground", FG);
        UIManager.put("Table.gridColor", PANEL);
        UIManager.put("ToolTip.background", PANEL_LIGHT);
        UIManager.put("ToolTip.foreground", FG);
        UIManager.put("Menu.background", PANEL_LIGHT);
        UIManager.put("Menu.foreground", FG);
        UIManager.put("MenuBar.background", BG);
        UIManager.put("MenuItem.background", PANEL_LIGHT);
        UIManager.put("MenuItem.foreground", FG);
        UIManager.put("InternalFrame.background", PANEL);
        UIManager.put("Dialog.background", PANEL);
    }
}
