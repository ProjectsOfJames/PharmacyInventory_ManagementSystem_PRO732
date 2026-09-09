package org.example;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIStyle {

    public static final Color PRIMARY_DARK = new Color(89, 19, 163);
    public static final Color PRIMARY = new Color(74, 42, 186);
    public static final Color ACCENT = new Color(140, 226, 34);
    public static final Color BG_LIGHT = new Color(244, 247, 250);
    public static final Color SUCCESS = new Color(42, 179, 74);
    public static final Color DANGER = new Color(151, 24, 24);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 13);

    public static JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JLabel headerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(PRIMARY_DARK);
        return label;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(26);
        table.getTableHeader().setFont(FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 224, 246));
        table.setGridColor(new Color(224, 224, 224));
    }

    public static JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(FONT_LABEL);
        field.setFont(FONT_FIELD);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}