package org.example;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIStyle {

    public static final Color PRIMARY_DARK = new Color(89, 19, 163);
    public static final Color PRIMARY = new Color(74, 42, 186);
    public static final Color ACCENT = new Color(105, 168, 25);
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
        JTableHeader header =  table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public void setOpaque(boolean isOpaque) {
                super.setOpaque(true);
            }

            @Override
            public void setForeground(Color c) {
                super.setForeground(Color.WHITE);
            }

            @Override
            public void setBackground(Color c) {
                super.setBackground(PRIMARY);
            }

            @Override
            public void setFont(Font font) {
                super.setFont(FONT_TABLE_HEADER);
            }
        });

        table.setFont(FONT_TABLE);
        table.setRowHeight(26);
        table.setSelectionBackground(new Color(97, 177, 255));
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