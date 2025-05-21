package view;

import javax.swing.*;

public class MainGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomePanel home = new HomePanel();
            home.setVisible(true);
        });
    }
}