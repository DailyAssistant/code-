package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.awt.Image;
import service.ImageSearchService;
import imagehistoryAndStore.ImageHistory;
import service.ImageWithUrl;


public class MemeSearchFrame extends JFrame {
    private SearchPanel searchPanel;
    private ResultsPanel resultsPanel;
    private final JLabel loadingMsg;
    private final JPanel topPanel;

    public MemeSearchFrame() {
        super("梗圖蒐尋器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout(10, 10));

        // 設置 topPanel 使用 BorderLayout，這樣可以將 loading 信息放在底部
        topPanel = new JPanel(new BorderLayout());
        searchPanel = new SearchPanel();
        resultsPanel = new ResultsPanel();
        resultsPanel.setQuery(searchPanel.getSearchText());//將輸入搜尋欄的字串傳給resultpanel
        // loading message
        loadingMsg = new JLabel("搜尋中，請稍等", SwingConstants.CENTER);
        loadingMsg.setPreferredSize(new Dimension(topPanel.getWidth(), 30));
        add(searchPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);

        // 設置搜尋按鈕的動作
        searchPanel.setSearchAction(e -> {
            String query = searchPanel.getSearchText();
            if (!query.isEmpty()) {
                performSearch(query);
            } else {
                JOptionPane.showMessageDialog(this, "請輸入搜尋關鍵詞", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        searchPanel.setHistoryAction(e -> showHistory());
        setLocationRelativeTo(null);
    }

    private void performSearch(String query) {
        searchPanel.showLoading(true);

        new Thread(() -> {
            try {
                List<ImageWithUrl> images = ImageSearchService.searchImages(query);
                SwingUtilities.invokeLater(() -> {
                    resultsPanel.displayImages(images);
                    searchPanel.showLoading(false);
                });
                if (images.size() == 0){//處理當沒抓到圖片的情形
                    JOptionPane.showMessageDialog(this,"沒有找到您要的結果!");
                    searchPanel.setSearchField("");//清空找不到圖片的關鍵字
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "搜尋失敗: " + ex.getMessage(),
                            "錯誤", JOptionPane.ERROR_MESSAGE);
                    searchPanel.showLoading(false);
                });
            }
        }).start();
    }
    private void showHistory(){
        ImageHistory historyFrame = new ImageHistory();
        historyFrame.setVisible(true);
    }

}