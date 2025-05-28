package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton historyButton;
    private JLabel loadingLabel;
    private boolean isDarkMode;

    public SearchPanel(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);

        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(isDarkMode ? Color.WHITE : Color.GRAY);
                    g2.drawString("輸入關鍵詞搜尋梗圖...", 5, getHeight() / 2 + 5);
                }
                g2.dispose();
            }
        };
        searchField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        searchField.setBackground(isDarkMode ? Color.GRAY : Color.WHITE);
        searchField.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(searchField, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);

        searchButton = new JButton("搜尋");
        searchButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        searchButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        searchButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        buttonPanel.add(searchButton);

        historyButton = new JButton("歷史");
        historyButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        historyButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        historyButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        buttonPanel.add(historyButton);

        add(buttonPanel, BorderLayout.EAST);

        // Loading label
        loadingLabel = new JLabel("搜尋中，請稍等...", SwingConstants.CENTER);
        loadingLabel.setVisible(false);
        loadingLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(loadingLabel, BorderLayout.SOUTH);

        // Add Enter key listener to searchField
        searchField.addActionListener(e -> searchButton.doClick());
    }

    public void setSearchAction(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void setSearchField(String txt) {
        searchField.setText(txt);
    }

    public void showLoading(boolean show) {
        loadingLabel.setVisible(show);
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public void setHistoryAction(ActionListener listener) {
        historyButton.addActionListener(listener);
    }
}