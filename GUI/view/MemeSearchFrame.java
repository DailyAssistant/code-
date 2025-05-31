package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import service.ImageSearchService;
import imagehistoryAndStore.ImageHistory;
import service.ImageWithUrl;

public class MemeSearchFrame extends JFrame {//搜尋頁面
    private SearchPanel searchPanel;
    private ResultsPanel resultsPanel;//顯示抓到的圖的panel
    private final JLabel loadingMsg;//顯示搜尋中的label
    private final JPanel topPanel;
    private JButton backButton;
    private boolean isDarkMode;

    public MemeSearchFrame(String query, boolean isDarkMode) {
        super("梗圖蒐尋器");
        this.isDarkMode = isDarkMode;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout(10, 10));

        // Apply dark mode to frame
        getContentPane().setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);

        // Top panel with back button and search panel
        topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(true);
        topPanel.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);
        searchPanel = new SearchPanel(isDarkMode);
        resultsPanel = new ResultsPanel(isDarkMode);

        // Back button
        backButton = new JButton("返回首頁");
        backButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        backButton.setPreferredSize(new Dimension(100, 40));
        backButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        backButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        backButton.addActionListener(e -> returnToHome());

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // Loading message
        loadingMsg = new JLabel("搜尋中，請稍等", SwingConstants.CENTER);
        loadingMsg.setPreferredSize(new Dimension(topPanel.getWidth(), 30));
        loadingMsg.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        topPanel.add(loadingMsg, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);

        // Set search query and perform search if provided
        searchPanel.setSearchField(query);
        resultsPanel.setQuery(query);
        if (!query.isEmpty()) {
            performSearch(query);
        }

        // Set search button action
        searchPanel.setSearchAction(e -> {
            String searchQuery = searchPanel.getSearchText();
            if (!searchQuery.isEmpty()) {
                resultsPanel.setQuery(searchQuery);
                performSearch(searchQuery);
            } else {
                JOptionPane.showMessageDialog(this, "請輸入搜尋關鍵詞", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Set history button action
        searchPanel.setHistoryAction(e -> showHistory());
        setLocationRelativeTo(null);
    }

    private void performSearch(String query) {
        loadingMsg.setVisible(true);

        new Thread(() -> {
            try {
                List<ImageWithUrl> images = ImageSearchService.searchImages(query);
                SwingUtilities.invokeLater(() -> {
                    resultsPanel.displayImages(images);
                    loadingMsg.setVisible(false);
                });
                if (images.size() == 0) {
                    JOptionPane.showMessageDialog(this, "沒有找到您要的結果!");
                    searchPanel.setSearchField("");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "搜尋失敗: " + ex.getMessage(),
                            "錯誤", JOptionPane.ERROR_MESSAGE);
                    loadingMsg.setVisible(false);
                });
            }
        }).start();
    }

    private void showHistory() {
        ImageHistory historyFrame = new ImageHistory();
        historyFrame.setVisible(true);
    }

    private void returnToHome() {
        HomePanel home = new HomePanel();
        home.setVisible(true);
        dispose();
    }

}