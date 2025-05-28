package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import controller.ImageEditor;
import model.TransferableImage;
import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import javax.swing.SwingUtilities;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.HashSet;
import service.ImageSearchService;
import service.ImageWithUrl;

public class ResultsPanel extends JScrollPane {
    private JPanel contentPanel;
    private JPanel wrapperPanel;
    private String Query = "";
    private boolean isLoading = false;
    private boolean isFirstSearch = true;
    private final HashSet<String> loadedImageUrls = new HashSet<>();//記錄所有圖片url,set可以避免重複
    private final Object lock = new Object();//用在synchronized
    private final JLabel loadingMsg;
    private int currentPage = 0;
    private final int imagesPerPage = 9;
    private boolean isDarkMode;

    public ResultsPanel(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        wrapperPanel = new JPanel(new BorderLayout());//最外層panel，為了顯示底下的"加載中"而新增的
        wrapperPanel.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);
        contentPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);
        loadingMsg = new JLabel("搜尋中，請稍等", SwingConstants.CENTER);
        loadingMsg.setPreferredSize(new Dimension(contentPanel.getWidth(), 30));
        loadingMsg.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        wrapperPanel.add(contentPanel, BorderLayout.CENTER);
        wrapperPanel.add(loadingMsg, BorderLayout.SOUTH);

        JScrollBar sideBar = getVerticalScrollBar();//右邊的滾動條
        sideBar.setUnitIncrement(25);// scrollpane靈敏度
        sideBar.addAdjustmentListener(e -> {
            if (isFirstSearch || isLoading) {
                isFirstSearch = false; // Reset only for scroll events
                return;
            }
            int extent = sideBar.getModel().getExtent();
            int maximum = sideBar.getMaximum();
            int value = sideBar.getValue();

            if (!isLoading && value + extent >= maximum - 10) {
                isLoading = true;
                System.out.println("已經划到底部，觸發搜尋");
                loadMoreImages();
            }
        });

        setViewportView(wrapperPanel);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void setQuery(String query) {//輸入關鍵字並加載圖片
        synchronized (lock) {//確保只讓一個thread來使用資源
            this.Query = query;
            this.isLoading = true;
            this.isFirstSearch = true; // Ensure it's true for the first search
            this.currentPage = 0;
            loadedImageUrls.clear();
            contentPanel.removeAll();
            revalidate();
            repaint();

            // Hide loading message for the first search
            loadingMsg.setVisible(false);

            SwingUtilities.invokeLater(() -> loadMoreImages());
        }
    }

    public void displayImages(List<ImageWithUrl> images) {
        for (ImageWithUrl imgWithUrl : images) {
            Image img = imgWithUrl.image;
            String url = imgWithUrl.url;
            if (loadedImageUrls.contains(imgWithUrl.url)) {// 跳過已經抓到的圖
                continue;
            }
            loadedImageUrls.add(imgWithUrl.url);

            BufferedImage originalBuffered = toBufferedImage(img);
            Image scaledImg = originalBuffered.getScaledInstance(250, 200, Image.SCALE_SMOOTH);
            BufferedImage scaledBuffered = new BufferedImage(250, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledBuffered.createGraphics();
            g2d.drawImage(scaledImg, 0, 0, null);
            g2d.dispose();
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
            imageLabel.setBorder(BorderFactory.createLineBorder(isDarkMode ? Color.GRAY : Color.LIGHT_GRAY, 1));

            setupDragSource(imageLabel, scaledBuffered);
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ImageEditor.openEditor((JFrame) SwingUtilities.getWindowAncestor(ResultsPanel.this),
                            toBufferedImage(img),
                            url);
                }
            });
            contentPanel.add(imageLabel);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void loadMoreImages() {
        synchronized (lock) {//確保只讓一個thread來使用資源
            new Thread(() -> {
                try {
                    int offset = currentPage * imagesPerPage;
                    List<ImageWithUrl> newImages = ImageSearchService.searchImages(Query, offset, imagesPerPage);
                    currentPage++;

                    SwingUtilities.invokeLater(() -> {
                        if (newImages.isEmpty()) {
                            loadingMsg.setText("已載入所有圖片");
                            loadingMsg.setVisible(true);
                            return;
                        }
                        displayImages(newImages);
                        if (isFirstSearch) {
                            isFirstSearch = false; // Reset only after the first search completes
                        }
                        loadingMsg.setVisible(false);
                        isLoading = false;
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        isLoading = false;
                        loadingMsg.setText("載入圖片時發生錯誤");
                        loadingMsg.setVisible(true);
                    });
                }
            }).start();

            // Show the loading message only if this is not the first search
            if (!isFirstSearch) {
                loadingMsg.setText("搜尋中，請稍等");
                loadingMsg.setVisible(true);
            }
        }
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    private void setupDragSource(JLabel label, BufferedImage image) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(
                label,
                DnDConstants.ACTION_COPY,
                new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        try {
                            Transferable transferable = new TransferableImage(image);
                            Image dragImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                                    dragImage, new Point(0, 0), "drag"
                            );
                            dge.startDrag(cursor, transferable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}