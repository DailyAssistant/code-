package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JButton historyButton;
    private List<String> searchHistory = new ArrayList<>();

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
        historyButton.addActionListener(e -> showSearchHistory());
        buttonPanel.add(historyButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    private void showSearchHistory() {
        if (searchHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暫無搜尋歷史", "歷史紀錄", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog historyDialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), "搜尋歷史", true);
        historyDialog.setSize(300, 400);
        historyDialog.setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String query : searchHistory) {
            listModel.addElement(query);
        }

        JList<String> historyList = new JList<>(listModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                searchField.setText(historyList.getSelectedValue());
                historyDialog.dispose();
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyList);
        historyDialog.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("清除歷史");
        clearButton.addActionListener(e -> {
            searchHistory.clear();
            historyDialog.dispose();
            JOptionPane.showMessageDialog(historyDialog, "已清除所有歷史紀錄", "清除完成", JOptionPane.INFORMATION_MESSAGE);
        });

        historyDialog.add(clearButton, BorderLayout.SOUTH);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    public void addToHistory(String query) {
        if (!query.trim().isEmpty() && !searchHistory.contains(query)) {
            searchHistory.add(query);
        }
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getHistoryButton() {
        return historyButton;
    }

    public List<String> getSearchHistory() {
        return searchHistory;
    }
}
