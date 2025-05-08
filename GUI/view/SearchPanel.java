package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton historyButton;
    private JLabel loadingLabel;

    public SearchPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        add(searchField, BorderLayout.CENTER);

        // 創建按鈕面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        searchButton = new JButton("搜尋");
        searchButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        buttonPanel.add(searchButton);

        historyButton = new JButton("歷史");
        historyButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        buttonPanel.add(historyButton);

        add(buttonPanel, BorderLayout.EAST);

        // 加載中標籤
        loadingLabel = new JLabel("搜尋中，請稍等...", SwingConstants.CENTER);
        loadingLabel.setVisible(false);
        add(loadingLabel, BorderLayout.SOUTH);
    }

    public void setSearchAction(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void showLoading(boolean show) {
        loadingLabel.setVisible(show);
    }

    public String getSearchText() {return searchField.getText().trim();}

    public void setHistoryAction(ActionListener listener) {historyButton.addActionListener(listener);}

}
