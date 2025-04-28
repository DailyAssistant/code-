package view;

import javax.swing.*;
import java.awt.*;

public class ResultsPanel extends JScrollPane {
    private JPanel contentPanel;

    public ResultsPanel() {
        contentPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        setViewportView(contentPanel);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }
}