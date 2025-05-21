package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import imagehistoryAndStore.ImageHistory;

public class HomePanel extends JFrame {
    private JTextField searchField;
    private boolean isDarkMode = false;
    private JLabel versionLabel;
    private JLabel titleLabel;
    private JPanel contentPanel;
    private JPanel topPanel;
    private Image backgroundImage;

    public HomePanel() {
        super("梗圖蒐尋器 - 首頁");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());

        // Set content pane background
        getContentPane().setBackground(Color.WHITE);

        // Load background image
        try {
            java.net.URL imageUrl = getClass().getResource("/GUI/resources/background.png");
            if (imageUrl != null) {
                backgroundImage = new ImageIcon(imageUrl).getImage();
                System.out.println("背景圖片加載成功，URL: " + imageUrl);
            } else {
                System.out.println("背景圖片路徑未找到，請確認 GUI/resources/background.png 是否存在並標記為 Resources Root");
                backgroundImage = null;
            }
        } catch (Exception e) {
            System.out.println("背景圖片加載失敗: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = null;
        }

        // Top panel for history and theme controls
        topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // History button with hover animation
        JButton historyButton = new JButton("歷史");
        historyButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        historyButton.addActionListener(e -> showHistory());
        historyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                historyButton.setBackground(new Color(230, 230, 250));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                historyButton.setBackground(null);
            }
        });
        topPanel.add(historyButton, BorderLayout.EAST);

        // Theme toggle button
        JButton themeButton = new JButton("深色模式");
        themeButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        themeButton.addActionListener(e -> toggleDarkMode());
        topPanel.add(themeButton, BorderLayout.WEST);

        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Main content panel with GridBagLayout and background image
        contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background image
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    // Adjust overlay based on mode
                    g2.setColor(isDarkMode ? new Color(0, 0, 0, 0.7f) : new Color(0, 0, 0, 0.5f));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Fallback background
                    g2.setColor(isDarkMode ? new Color(50, 50, 50, 255) : new Color(240, 248, 255, 50));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        // Top padding
        gbc.weighty = 0.5;
        contentPanel.add(Box.createVerticalGlue(), gbc);

        // Title with shadow effect
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 20, 0);
        titleLabel = new JLabel("梗圖蒐尋器", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 72));
                g2.setColor(Color.BLACK); // Shadow color
                String text = getText();
                FontMetrics fm = getFontMetrics(getFont());
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, x + 2, y + 2); // Shadow
                g2.setColor(Color.WHITE); // Foreground text
                g2.drawString(text, x, y); // Main text
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 72));
        titleLabel.setPreferredSize(new Dimension(400, 100));
        contentPanel.add(titleLabel, gbc);

        // Search field
        gbc.insets = new Insets(0, 0, 10, 0);
        searchField = new JTextField(40) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(Color.WHITE); // Placeholder text color
                    g2.drawString("輸入關鍵詞搜尋梗圖...", 5, getHeight() / 2 + 5);
                }
                g2.dispose();
            }
        };
        searchField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        searchField.setMaximumSize(new Dimension(500, 60));
        searchField.setPreferredSize(new Dimension(500, 60));
        searchField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { repaint(); }
            @Override
            public void focusLost(FocusEvent e) { repaint(); }
        });
        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                searchField.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                searchField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }
        });
        contentPanel.add(searchField, gbc);

        // Shortcut hint
        JLabel shortcutHint = new JLabel("按Enter搜尋", SwingConstants.CENTER);
        shortcutHint.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        shortcutHint.setForeground(Color.WHITE);
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(shortcutHint, gbc);

        // Bottom padding
        gbc.weighty = 0.5;
        contentPanel.add(Box.createVerticalGlue(), gbc);

        // Version footer
        versionLabel = new JLabel("版本 1.0", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        versionLabel.setForeground(Color.BLACK); // Black for light mode
        add(versionLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Search action on Enter key
        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                openMemeSearchFrame(query);
            } else {
                JOptionPane.showMessageDialog(this, "請輸入搜尋關鍵詞", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        setLocationRelativeTo(null);
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            getContentPane().setBackground(Color.DARK_GRAY);
            contentPanel.setBackground(Color.DARK_GRAY);
            topPanel.setBackground(Color.DARK_GRAY);
            versionLabel.setForeground(Color.WHITE);
            titleLabel.setForeground(Color.WHITE);
            searchField.setBackground(Color.GRAY);
            searchField.setForeground(Color.WHITE);
        } else {
            getContentPane().setBackground(Color.WHITE);
            contentPanel.setBackground(Color.WHITE);
            topPanel.setBackground(Color.WHITE);
            versionLabel.setForeground(Color.BLACK);
            titleLabel.setForeground(Color.WHITE); // Keep white for shadow effect
            searchField.setBackground(Color.WHITE);
            searchField.setForeground(Color.BLACK);
        }
        // Reload background image to ensure consistency
        try {
            java.net.URL imageUrl = getClass().getResource("/GUI/resources/background.png");
            if (imageUrl != null) {
                backgroundImage = new ImageIcon(imageUrl).getImage();
                System.out.println("背景圖片加載成功，URL: " + imageUrl);
            } else {
                System.out.println("背景圖片路徑未找到，請確認 GUI/resources/background.png 是否存在並標記為 Resources Root");
                backgroundImage = null;
            }
        } catch (Exception e) {
            System.out.println("背景圖片加載失敗: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = null;
        }
        repaint();
    }

    private void showHistory() {
        ImageHistory historyFrame = new ImageHistory();
        historyFrame.setVisible(true);
    }

    private void openMemeSearchFrame(String query) {
        MemeSearchFrame frame = new MemeSearchFrame(query);
        frame.setVisible(true);
        dispose();
    }
}