package imagehistoryAndStore;

import javax.imageio.ImageIO;
import javax.swing.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import controller.ImageEditor;
import view.ResultsPanel;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class ImageHistory extends JFrame {
    private final JPanel imagePanel;
    private final File imageFolder = new File("../ImageHistory/images");
    private final File historyFile = new File("history.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<ImageRecord> imageHistory = new ArrayList<>();
    private final JComboBox<String> sortComboBox;

    public ImageHistory() {
        super("圖片歷史紀錄");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        imagePanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        sortComboBox = new JComboBox<>(new String[]{
                "使用次數（高→低）",
                "最近使用（新→舊）",
                "默認（檔案名稱）"
        });
        sortComboBox.addActionListener(e -> refreshImages());

        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> {
            loadHistory();
            loadImages();
        });

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("排序方式："));
        topPanel.add(sortComboBox);
        topPanel.add(refreshButton);
        add(topPanel, BorderLayout.NORTH);

        loadHistory();       // 讀取 JSON
        loadImages();        // 掃描資料夾、更新紀錄
        saveHistory();       // 儲存更新後的紀錄
    }

    // 讀取 JSON 紀錄
    private void loadHistory() {
        imageHistory.clear();
        if (historyFile.exists()) {
            try (Reader reader = new FileReader(historyFile)) {
                java.lang.reflect.Type listType = new TypeToken<List<ImageRecord>>() {}.getType();
                List<ImageRecord> loaded = gson.fromJson(reader, listType);
                if (loaded != null) {
                    imageHistory.addAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 儲存 JSON 紀錄
    private void saveHistory() {
        try (Writer writer = new FileWriter(historyFile)) {
            gson.toJson(imageHistory, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 載入圖片 & 更新 UI
    private void loadImages() {
        imagePanel.removeAll();

        if (!imageFolder.exists() || !imageFolder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "找不到圖片資料夾");
            return;
        }

        File[] files = imageFolder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".gif");
        });

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "該資料夾內沒有圖片");
            return;
        }

        Set<String> existingPaths = new HashSet<>();
        for (ImageRecord record : imageHistory) {
            existingPaths.add(record.getPath());
        }

        for (File file : files) {
            if (!existingPaths.contains(file.getPath())) {
                imageHistory.add(new ImageRecord(file.getName(), file.getPath()));
            }
        }

        saveHistory();
        refreshImages();
    }

    private void refreshImages() {
        imagePanel.removeAll();

        // 依排序條件排序
        String selected = (String) sortComboBox.getSelectedItem();

        switch (selected) {
            case "使用次數（高→低）":
                imageHistory.sort(Comparator.comparingInt(ImageRecord::getUseCount).reversed());
                break;
            case "最近使用（新→舊）":
                imageHistory.sort(Comparator.comparing(ImageRecord::getLastUsed, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "默認（檔案名稱）":
                imageHistory.sort(Comparator.comparing(ImageRecord::getFilename, String.CASE_INSENSITIVE_ORDER));
                break;
        }

        for (ImageRecord record : imageHistory) {
            File file = new File(record.getPath());
            if (!file.exists()) continue;

            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    int targetWidth = 200;
                    int targetHeight = (int)((double) img.getHeight() / img.getWidth() * targetWidth);
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH));

                    JLabel label = new JLabel(icon);
                    label.setText(record.getFilename());
                    label.setHorizontalTextPosition(JLabel.CENTER);
                    label.setVerticalTextPosition(JLabel.BOTTOM);
                    label.setPreferredSize(new Dimension(targetWidth, targetHeight + 30));

                    // 滑鼠右鍵功能
                    label.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {
                            if (e.isPopupTrigger()) showMenu(e, img, file, record);
                        }

                        public void mouseReleased(MouseEvent e) {
                            if (e.isPopupTrigger()) showMenu(e, img, file, record);
                        }

                        public void mouseClicked(MouseEvent e) {
                            ImageEditor.openEditor(ImageHistory.this, img, record.getPath());
                        }
                    });

                    imagePanel.add(label);
                }
            } catch (Exception e) {
                System.out.println("讀取圖片失敗：" + record.getFilename());
            }
        }

        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void showMenu(MouseEvent e, BufferedImage img, File file, ImageRecord record) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("複製圖片");
        copyItem.addActionListener(ae -> {
            copyImageToClipboard(img);
            record.useCount++;
            record.lastUsed = LocalDateTime.now().toString();
            saveHistory();
        });

        JMenuItem deleteItem = new JMenuItem("刪除圖片");
        deleteItem.addActionListener(ae -> {
            if (file.delete()) {
                imageHistory.remove(record);
                JOptionPane.showMessageDialog(this, "圖片已刪除");
                loadImages();
                saveHistory();
            } else {
                JOptionPane.showMessageDialog(this, "刪除失敗");
            }
        });

        menu.add(copyItem);
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private ImageRecord findOrCreateRecord(File file) {
        for (ImageRecord r : imageHistory) {
            if (r.path.equals(file.getPath())) return r;
        }
        ImageRecord newRecord = new ImageRecord(file.getName(), file.getPath());
        imageHistory.add(newRecord);
        return newRecord;
    }

    private void copyImageToClipboard(BufferedImage img) {
        if (img == null) return;
        TransferableImage trans = new TransferableImage(img);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(trans, null);
        JOptionPane.showMessageDialog(this, "圖片已複製到剪貼簿");
    }

    // 支援圖片複製
    private static class TransferableImage implements Transferable {
        private final Image image;
        public TransferableImage(Image image) { this.image = image; }
        public Object getTransferData(DataFlavor flavor) { return image; }
        public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
        public boolean isDataFlavorSupported(DataFlavor flavor) { return flavor.equals(DataFlavor.imageFlavor); }
    }
}
