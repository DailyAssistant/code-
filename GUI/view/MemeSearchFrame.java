package view;

import javax.swing.*;
import java.awt.*;

public class MemeSearchFrame extends JFrame {
    private SearchPanel searchPanel;

    public MemeSearchFrame() {
        super("梗圖蒐尋器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout(10, 10));

        searchPanel = new SearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        add(new ResultsPanel(), BorderLayout.CENTER);
        add(new ButtonPanel(), BorderLayout.SOUTH);

        // 範例：設置搜尋按鈕的動作
        searchPanel.getSearchButton().addActionListener(e -> {
            String query = searchPanel.getSearchField().getText();
            searchPanel.addToHistory(query); // 加入歷史
            performSearch(query); // 執行搜尋
        });

        setLocationRelativeTo(null);
    }

    private void performSearch(String query) {
        // 實現搜尋邏輯
    }
}