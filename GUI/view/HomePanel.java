package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import imagehistoryAndStore.ImageHistory;

public class HomePanel extends JFrame {
    private JTextField searchField;
    private boolean isDarkMode = false;
    private JLabel versionLabel;
    private JLabel titleLabel;
    private JPanel contentPanel;
    private JPanel topPanel;
    private static Image backgroundImage; // Made static to share across instances
    private JButton historyButton;
    private JButton themeButton;
    private JLabel clockLabel;

    public HomePanel() {
        super("梗圖蒐尋器 - 首頁");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());

        // Set content pane background
        getContentPane().setBackground(Color.WHITE);

        // Load background image from user directory or default resource
        loadBackgroundImage();

        // Top panel for history, theme controls, and clock
        topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(true);
        topPanel.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);

        // History button with hover animation
        historyButton = new JButton("歷史");
        historyButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        historyButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        historyButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        historyButton.addActionListener(e -> showHistory());
        historyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                historyButton.setBackground(isDarkMode ? new Color(150, 150, 150) : new Color(230, 230, 250));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                historyButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
            }
        });
        topPanel.add(historyButton, BorderLayout.EAST);

        // Theme toggle button
        themeButton = new JButton("深色模式");
        themeButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        themeButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        themeButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        themeButton.addActionListener(e -> toggleDarkMode());
        topPanel.add(themeButton, BorderLayout.WEST);


        // Clock label
        clockLabel = new JLabel("", SwingConstants.CENTER);
        clockLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        clockLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        topPanel.add(clockLabel, BorderLayout.CENTER);

        // Update clock every second with Chinese format
        SimpleDateFormat sdf = new SimpleDateFormat("a HH:mm yyyy/MM/dd", Locale.CHINA);
        Timer clockTimer = new Timer(1000, e -> {
            clockLabel.setText(sdf.format(new Date()));
        });
        clockTimer.start();

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
                    g2.setColor(isDarkMode ? new Color(0, 0, 0, 0.7f) : new Color(0, 0, 0, 0.5f));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
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
                g2.setColor(Color.BLACK);
                String text = getText();
                FontMetrics fm = getFontMetrics(getFont());
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, x + 2, y + 2);
                g2.setColor(Color.WHITE);
                g2.drawString(text, x, y);
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
                    g2.setColor(isDarkMode ? Color.WHITE : Color.GRAY);
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
        versionLabel.setForeground(Color.BLACK);
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

    private void loadBackgroundImage() {
        // Try to load from user home directory first
        String userBackgroundPath = System.getProperty("user.home") + "/.memeSearchApp/background.png";
        File userBackgroundFile = new File(userBackgroundPath);
        if (userBackgroundFile.exists()) {
            try {
                backgroundImage = ImageIO.read(userBackgroundFile);
                System.out.println("加載自訂背景圖片成功: " + userBackgroundPath);
            } catch (IOException e) {
                System.out.println("加載自訂背景圖片失敗: " + e.getMessage());
                e.printStackTrace();
                loadDefaultBackground();
            }
        } else {
            loadDefaultBackground();
        }
    }

    private void loadDefaultBackground() {
        try {
            String imgPath = "/GUI/resources/background.png";
            java.net.URL imageUrl = getClass().getResource(imgPath);
            if (imageUrl != null) {
                backgroundImage = new ImageIcon(imageUrl).getImage();
                System.out.println("加載預設背景圖片成功，URL: " + imageUrl);
            } else {
                System.out.println("預設背景圖片路徑未找到，請確認 /GUI/resources/background.png 是否存在並標記為 Resources Root");
                backgroundImage = null;
            }
        } catch (Exception e) {
            System.out.println("預設背景圖片加載失敗: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = null;
        }
    }

    public static void setBackgroundImage(Image newBackground) {
        backgroundImage = newBackground;
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
            historyButton.setBackground(Color.GRAY);
            historyButton.setForeground(Color.WHITE);
            themeButton.setBackground(Color.GRAY);
            themeButton.setForeground(Color.WHITE);
            clockLabel.setForeground(Color.WHITE);
        } else {
            getContentPane().setBackground(Color.WHITE);
            contentPanel.setBackground(Color.WHITE);
            topPanel.setBackground(Color.WHITE);
            versionLabel.setForeground(Color.BLACK);
            titleLabel.setForeground(Color.WHITE);
            searchField.setBackground(Color.WHITE);
            searchField.setForeground(Color.BLACK);
            historyButton.setBackground(UIManager.getColor("Button.background"));
            historyButton.setForeground(Color.BLACK);
            themeButton.setBackground(UIManager.getColor("Button.background"));
            themeButton.setForeground(Color.BLACK);
            clockLabel.setForeground(Color.BLACK);
        }
        repaint();
    }

    private void showHistory() {
        ImageHistory historyFrame = new ImageHistory();
        historyFrame.setVisible(true);
    }

    private void openMemeSearchFrame(String query) {
        MemeSearchFrame frame = new MemeSearchFrame(query, isDarkMode);
        frame.setVisible(true);
        dispose();
    }
}