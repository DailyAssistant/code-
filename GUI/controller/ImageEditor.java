package controller;

import model.CropPanel;
import model.DrawPanel; // Import the new DrawPanel
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import imagehistoryAndStore.SaveImage;
import java.io.IOException;
import model.TransferableImage;
import controller.ImageBlur;
import static imagehistoryAndStore.SaveImage.save;

public class ImageEditor {
    private static int imgnumber = 0;

    public static void openEditor(JFrame parent, BufferedImage image) {
        if (image == null) return;

        JDialog editorDialog = new JDialog(parent, "圖片編輯器", true);
        editorDialog.setSize(600, 500);
        editorDialog.setLayout(new BorderLayout());

        final BufferedImage[] imageWrapper = new BufferedImage[]{image};

        JLabel editorLabel = new JLabel(new ImageIcon(scaleImage(imageWrapper[0], 500, 400)));
        enableDragForLabel(editorLabel, imageWrapper);
        editorDialog.add(editorLabel, BorderLayout.CENTER);

        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton backButton = new JButton("返回");
        backButton.addActionListener(e -> editorDialog.dispose());

        JButton cropButton = new JButton("裁剪");
        cropButton.addActionListener(e -> showCropDialog(parent, editorLabel, imageWrapper));

        JButton filterButton = new JButton("濾鏡");
        filterButton.addActionListener(e -> applyFilter(editorLabel, imageWrapper[0]));

        JButton drawButton = new JButton("標記");
        drawButton.addActionListener(e -> showDrawDialog(parent, editorLabel, imageWrapper));

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            String path = "../ImageHistory/images/img";
            try {
                save(imageWrapper[0],path + imgnumber + ".jpg", "jpg");
                JOptionPane.showMessageDialog(parent, "儲存成功! 儲存位置：\n" + path, "儲存成功", JOptionPane.INFORMATION_MESSAGE);
                imgnumber += 1; // 只在這裡加一次
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "儲存失敗!儲存時發生錯誤：\n" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        toolPanel.add(backButton);
        toolPanel.add(cropButton);
        toolPanel.add(filterButton);
        toolPanel.add(drawButton);
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
            // Resize cropped image to original image dimensions
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

        // Create a copy of the image to draw on
        BufferedImage drawImage = new BufferedImage(
                imageWrapper[0].getWidth(), imageWrapper[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
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

    private static void applyFilter(JLabel imageLabel, BufferedImage image) {
        String[] filters = {"灰度", "反色", "模糊", "邊緣檢測"};
        String choice = (String) JOptionPane.showInputDialog(
                null, "選擇濾鏡效果:", "濾鏡選擇",
                JOptionPane.PLAIN_MESSAGE, null, filters, filters[0]
        );

        if (choice != null) {
            BufferedImage filteredImage = applyImageFilter(image, choice);
            imageLabel.setIcon(new ImageIcon(scaleImage(filteredImage, 500, 400)));
        }
    }

    private static BufferedImage applyImageFilter(BufferedImage image, String filter) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                switch (filter) {
                    case "灰度":
                        int gray = (r + g + b) / 3;
                        result.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
                        break;
                    case "反色":
                        result.setRGB(x, y, ((255 - r) << 16) | ((255 - g) << 8) | (255 - b));
                        break;
                    case "模糊":
                        JTextField sizeField = new JTextField("3");
                        JTextField sigmaField = new JTextField("1.0");
                        JPanel panel = new JPanel(new GridLayout(2, 2));
                        panel.add(new JLabel("請輸入尺寸(奇數):"));
                        panel.add(sizeField);
                        panel.add(new JLabel("請輸入模糊效果(Sigma):"));
                        panel.add(sigmaField);
                        JOptionPane.showConfirmDialog(panel, panel, "輸入模糊參數", JOptionPane.OK_CANCEL_OPTION);

                        try {
                            int size = Integer.parseInt(sizeField.getText());
                            float sigma = Float.parseFloat(sigmaField.getText());
                            if (size % 2 == 0 || size < 1 || size > 10) {
                                JOptionPane.showMessageDialog(null, "尺寸必須是大於 0 的奇數!!!");
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
                    case "邊緣檢測":
                        if (x > 0 && y > 0) {
                            int prevRGB = image.getRGB(x - 1, y - 1);
                            int prevR = (prevRGB >> 16) & 0xFF;
                            int prevG = (prevRGB >> 8) & 0xFF;
                            int prevB = prevRGB & 0xFF;
                            int edge = Math.abs(r - prevR) + Math.abs(g - prevG) + Math.abs(b - prevB);
                            edge = Math.min(255, edge * 3);
                            result.setRGB(x, y, (edge << 16) | (edge << 8) | edge);
                        } else {
                            result.setRGB(x, y, 0);
                        }
                        break;
                    default:
                        result.setRGB(x, y, rgb);
                }
            }
        }
        return result;
    }

    private static Image scaleImage(BufferedImage original, int width, int height) {
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}