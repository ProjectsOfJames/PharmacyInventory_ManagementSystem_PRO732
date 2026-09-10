package org.example;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Point of sales ui test");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1050, 650);
//            frame.setContentPane(new PointOfSales());
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
    }
}