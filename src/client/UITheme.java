package client;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class UITheme {
    public static final Color PRIMARY = new Color(45, 118, 232);
    public static final Color ACCENT = new Color(0, 150, 136);
    public static final Color BG = new Color(245, 247, 250);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color HEADER_FG = new Color(34, 40, 49);
    public static final Color MUTED = new Color(120, 130, 140);

    public static void applyTheme() {
        try {
            UIManager.put("control", BG);
            UIManager.put("info", PANEL_BG);
            UIManager.put("nimbusBase", PRIMARY.darker());
            UIManager.put("nimbusBlueGrey", BG);
            UIManager.put("nimbusFocus", PRIMARY);
            UIManager.put("TextField.background", PANEL_BG);
            UIManager.put("PasswordField.background", PANEL_BG);
            UIManager.put("Button.background", PRIMARY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
            UIManager.put("Table.rowHeight", 26);
        } catch (Exception e) {
            // ignore
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        stylePrimaryButton(b);
        return b;
    }

    public static void stylePrimaryButton(AbstractButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
    }

    public static JButton createAccentButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        return b;
    }

    public static JButton createGhostButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(PRIMARY.darker());
        b.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 222)));
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        return b;
    }

    public static JLabel iconLabel(String emoji, String text) {
        JLabel l = new JLabel(emoji + "  " + text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    public static void styleInput(JTextComponent component) {
        component.setFont(new Font("SansSerif", Font.PLAIN, 15));
        component.setOpaque(true);
        component.setBackground(PANEL_BG);
        component.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(210, 214, 222)));
        component.setMargin(new Insets(4, 6, 4, 6));
    }
}
