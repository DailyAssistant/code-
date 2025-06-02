package controller;

import model.CropPanel;
import model.DrawPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import imagehistoryAndStore.SaveImage;
import java.io.File;
import java.io.IOException;
import model.TransferableImage;
import controller.ImageBlur;
import view.HomePanel;

import static controller.ImageToGray.img2binary;
import static controller.ImageToGray.img2gray;
import static controller.imgInverter.imgInvert;
import static imagehistoryAndStore.SaveImage.save;
import static service.ImageSearchService.getImageFormat;

public class ImageEditor {
    private static int imgnumber = 0;

    public static void openEditor(JFrame parent, BufferedImage image, String imgUrls) {
        if (image == null) return;

        JDialog editorDialog = new JDialog(parent, "圖片編輯器", true);
        editorDialog.setSize(600, 500);
        editorDialog.setLayout(new BorderLayout());

        final BufferedImage[] imageWrapper = new BufferedImage[]{image};
        final String[] formatWrapper = new String[]{getImageFormat(imgUrls)};

        JLabel editorLabel = new JLabel(new ImageIcon(scaleImage(imageWrapper[0], 500, 400)));
        enableDragForLabel(editorLabel, imageWrapper);
        editorDialog.add(editorLabel, BorderLayout.CENTER);

        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        boolean isDarkMode = parent.getContentPane().getBackground().equals(Color.DARK_GRAY);
        toolPanel.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);

        JButton backButton = new JButton("返回");
        backButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        backButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        backButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        backButton.addActionListener(e -> editorDialog.dispose());

        JButton cropButton = new JButton("裁剪");
        cropButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        cropButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        cropButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        cropButton.addActionListener(e -> showCropDialog(parent, editorLabel, imageWrapper));

        JButton filterButton = new JButton("濾鏡");
        filterButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        filterButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        filterButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        filterButton.addActionListener(e -> applyFilter(editorLabel, imageWrapper));

        JButton drawButton = new JButton("標記");
        drawButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        drawButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        drawButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        drawButton.addActionListener(e -> showDrawDialog(parent, editorLabel, imageWrapper));

        JButton setBackgroundButton = new JButton("設為背景");
        setBackgroundButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        setBackgroundButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        setBackgroundButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        setBackgroundButton.addActionListener(e -> {
            if (imageWrapper[0] == null) {
                JOptionPane.showMessageDialog(editorDialog, "無效的圖片，無法設為背景", "錯誤", JOptionPane.ERROR_MESSAGE);
                System.err.println("錯誤：imageWrapper[0] 為空");
                return;
            }
            try {
                String userHome = System.getProperty("user.home");
                File appDir = new File(userHome + "/.memeSearchApp");
                if (!appDir.exists()) {
                    boolean created = appDir.mkdirs();
                    System.out.println("創建目錄 " + appDir.getPath() + ": " + (created ? "成功" : "失敗"));
                }
                String backgroundPath = userHome + "/.memeSearchApp/background.png";
                System.out.println("嘗試儲存背景圖片到: " + backgroundPath);

                // 儲存圖片，保留原始路徑
                save(imageWrapper[0], backgroundPath, "PNG", false, true);
                System.out.println("圖片儲存成功: " + backgroundPath);

                // 更新現有 HomePanel 的背景（如果存在）
                for (Window window : Window.getWindows()) {
                    if (window instanceof HomePanel homePanel) {
                        homePanel.setBackgroundImage(imageWrapper[0]);
                        System.out.println("現有 HomePanel 背景已更新");
                        break;
                    }
                }

                // 顯示成功提示，保持在 ImageEditor
                JOptionPane.showMessageDialog(editorDialog, "背景已設定並保存為 " + backgroundPath + "！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                System.err.println("儲存背景圖片失敗: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editorDialog, "儲存背景失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                System.err.println("設定背景時發生未知錯誤: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editorDialog, "設定背景失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton saveButton = new JButton("保存");
        saveButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        saveButton.setBackground(isDarkMode ? Color.GRAY : UIManager.getColor("Button.background"));
        saveButton.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        saveButton.addActionListener(e -> {
            String path = "../ImageHistory/images/img";
            String format = formatWrapper[0];

            try {
                save(imageWrapper[0], path + imgnumber, format, true, false);
                JOptionPane.showMessageDialog(parent, "儲存成功! 儲存位置：\n" + path, "儲存成功", JOptionPane.INFORMATION_MESSAGE);
                imgnumber += 1;
            } catch (IOException ex) {
                System.err.println("儲存圖片失敗: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent, "儲存失敗! 儲存時發生錯誤：\n" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        toolPanel.add(backButton);
        toolPanel.add(cropButton);
        toolPanel.add(filterButton);
        toolPanel.add(drawButton);
        toolPanel.add(setBackgroundButton);
        toolPanel.add(saveButton);

        editorDialog.add(toolPanel, BorderLayout.SOUTH);
        editorDialog.setLocationRelativeTo(parent);
        editorDialog.setVisible(true);
    }

    private static void showCropDialog(JFrame parent, JLabel imageLabel, BufferedImage[] imageWrapper) {
        JDialog cropDialog = new JDialog(parent, "裁剪圖片", true);
        cropDialog.setLayout(new BorderLayout());
        cropDialog.setSize(parent.getSize());
        cropDialog.setLocationRelativeTo(parent);

        CropPanel cropPanel = new CropPanel(imageWrapper[0]);

        cropDialog.add(cropPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("確認裁剪");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            BufferedImage croppedImage = cropPanel.getCroppedImage();
            BufferedImage resizedCroppedImage = resizeImage(croppedImage, imageWrapper[0].getWidth(), imageWrapper[0].getHeight());
            imageWrapper[0] = resizedCroppedImage;
            imageLabel.setIcon(new ImageIcon(scaleImage(resizedCroppedImage, imageLabel.getWidth(), imageLabel.getHeight())));
            enableDragForLabel(imageLabel, imageWrapper);
            cropDialog.dispose();
        });

        cancelButton.addActionListener(e -> cropDialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        cropDialog.add(buttonPanel, BorderLayout.SOUTH);
        cropDialog.setVisible(true);
    }

    private static void showDrawDialog(JFrame parent, JLabel imageLabel, BufferedImage[] imageWrapper) {
        JDialog drawDialog = new JDialog(parent, "標記圖片", true);
        drawDialog.setLayout(new BorderLayout());
        drawDialog.setSize(parent.getSize());
        drawDialog.setLocationRelativeTo(parent);

        BufferedImage drawImage = new BufferedImage(
                imageWrapper[0].getWidth(), imageWrapper[0].getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = drawImage.createGraphics();
        g2d.drawImage(imageWrapper[0], 0, 0, null);
        g2d.dispose();

        DrawPanel drawPanel = new DrawPanel(drawImage);

        drawDialog.add(drawPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("確認標記");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            imageWrapper[0] = drawPanel.getDrawnImage();
            imageLabel.setIcon(new ImageIcon(scaleImage(imageWrapper[0], imageLabel.getWidth(), imageLabel.getHeight())));
            enableDragForLabel(imageLabel, imageWrapper);
            drawDialog.dispose();
        });

        cancelButton.addActionListener(e -> drawDialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        drawDialog.add(buttonPanel, BorderLayout.SOUTH);
        drawDialog.setVisible(true);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }

    private static void enableDragForLabel(JLabel label, BufferedImage[] imageWrapper) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(
                label,
                DnDConstants.ACTION_COPY,
                dge -> {
                    try {
                        Transferable transferable = new TransferableImage(imageWrapper[0]);
                        Image dragImage = imageWrapper[0].getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                                dragImage, new Point(0, 0), "drag"
                        );
                        dge.startDrag(cursor, transferable);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        );
    }

    private static void applyFilter(JLabel imageLabel, BufferedImage[] imageWrapper) {
        String[] filters = {"灰度", "反色", "模糊", "二值化"};
        String choice = (String) JOptionPane.showInputDialog(
                null, "選擇濾鏡效果:", "濾鏡選擇",
                JOptionPane.PLAIN_MESSAGE, null, filters, filters[0]
        );

        if (choice != null) {
            BufferedImage filteredImage = applyImageFilter(imageWrapper[0], choice);
            imageLabel.setIcon(new ImageIcon(scaleImage(filteredImage, 500, 400)));
            imageWrapper[0] = filteredImage;
        }
    }

    private static BufferedImage applyImageFilter(BufferedImage image, String filter) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        switch (filter) {
            case "灰度":
                return img2gray(image);
            case "反色":
                return imgInvert(image);
            case "模糊":
                JTextField sizeField = new JTextField("3");
                JTextField sigmaField = new JTextField("1.0");
                JPanel blurPanel = new JPanel(new GridLayout(2, 2));
                blurPanel.add(new JLabel("請輸入尺寸(奇數):"));
                blurPanel.add(sizeField);
                blurPanel.add(new JLabel("請輸入模糊效果(Sigma):"));
                blurPanel.add(sigmaField);
                JOptionPane.showConfirmDialog(blurPanel, blurPanel, "輸入模糊參數", JOptionPane.OK_CANCEL_OPTION);

                try {
                    int size = Integer.parseInt(sizeField.getText());
                    float sigma = Float.parseFloat(sigmaField.getText());
                    if (size % 2 == 0 || size < 1 || size > 30) {
                        JOptionPane.showMessageDialog(null, "尺寸必須是介於1到30的奇數!!!");
                        size = 3;
                    }
                    if (sigma <= 0) {
                        JOptionPane.showMessageDialog(null, "sigma值不可以<=0");
                        sigma = 1.0f;
                    }
                    return ImageBlur.GaussianBlur(image, size, sigma);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "輸入無效");
                    return ImageBlur.GaussianBlur(image, 3, 1.0f);
                }
            case "二值化":
                JTextField setThreshold = new JTextField("128");
                JPanel panel = new JPanel(new GridLayout(1, 2));
                panel.add(new JLabel("請輸入二值化閾值(0-255):"));
                panel.add(setThreshold);
                JOptionPane.showConfirmDialog(panel, panel, "輸入閾值", JOptionPane.OK_CANCEL_OPTION);
                try {
                    int threshold = Integer.parseInt(setThreshold.getText().trim());
                    if (threshold < 0 || threshold > 255) {
                        JOptionPane.showMessageDialog(null, "閾值必須是介於0-255的整數!!!");
                        threshold = 128;
                    }
                    return img2binary(image, threshold);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "輸入無效");
                    return ImageBlur.GaussianBlur(image, 3, 1.0f);
                }
            default:
                return image;
        }
    }

    private static Image scaleImage(BufferedImage original, int width, int height) {
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}