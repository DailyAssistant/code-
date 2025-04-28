package controller;

import model.CropPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import model.CropPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageEditor {
    private static Point lastPoint;
    public static void openEditor(JFrame parent, BufferedImage image) {
        if (image == null) return;

        JDialog editorDialog = new JDialog(parent, "圖片編輯器", true);
        editorDialog.setSize(600, 500);
        editorDialog.setLayout(new BorderLayout());

        // 圖片顯示區域
        JLabel editorLabel = new JLabel(new ImageIcon(scaleImage(image, 500, 400)));
        editorDialog.add(editorLabel, BorderLayout.CENTER);

        // 編輯工具面板
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton cropButton = new JButton("裁剪");
        cropButton.addActionListener(e -> showCropDialog(parent, editorLabel, image));

        JButton filterButton = new JButton("濾鏡");
        filterButton.addActionListener(e -> applyFilter(editorLabel, image));

        JButton drawButton = new JButton("標記");
        drawButton.addActionListener(e -> enableDrawing(editorLabel, image));

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveImage(parent, editorLabel));

        toolPanel.add(cropButton);
        toolPanel.add(filterButton);
        toolPanel.add(drawButton);
        toolPanel.add(saveButton);

        editorDialog.add(toolPanel, BorderLayout.SOUTH);
        editorDialog.setLocationRelativeTo(parent);
        editorDialog.setVisible(true);
    }

    private static void showCropDialog(JFrame parent, JLabel imageLabel, BufferedImage image) {
        JDialog cropDialog = new JDialog(parent, "裁剪圖片", true);
        cropDialog.setSize(650, 550);

        CropPanel cropPanel = new CropPanel(image);
        cropDialog.add(cropPanel);

        JButton confirmButton = new JButton("確認裁剪");
        confirmButton.addActionListener(e -> {
            BufferedImage croppedImage = cropPanel.getCroppedImage();
            imageLabel.setIcon(new ImageIcon(scaleImage(croppedImage, 500, 400)));
            cropDialog.dispose();
        });

        cropDialog.add(confirmButton, BorderLayout.SOUTH);
        cropDialog.setLocationRelativeTo(parent);
        cropDialog.setVisible(true);
    }

    private static void applyFilter(JLabel imageLabel, BufferedImage image) {
        String[] filters = {"灰度", "反色", "模糊", "邊緣檢測"};
        String choice = (String) JOptionPane.showInputDialog(
                null,
                "選擇濾鏡效果:",
                "濾鏡選擇",
                JOptionPane.PLAIN_MESSAGE,
                null,
                filters,
                filters[0]
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
                        // 簡單的模糊效果
                        if (x > 0 && y > 0 && x < image.getWidth() - 1 && y < image.getHeight() - 1) {
                            int avgR = 0, avgG = 0, avgB = 0;
                            for (int dy = -1; dy <= 1; dy++) {
                                for (int dx = -1; dx <= 1; dx++) {
                                    int neighborRGB = image.getRGB(x + dx, y + dy);
                                    avgR += (neighborRGB >> 16) & 0xFF;
                                    avgG += (neighborRGB >> 8) & 0xFF;
                                    avgB += neighborRGB & 0xFF;
                                }
                            }
                            avgR /= 9; avgG /= 9; avgB /= 9;
                            result.setRGB(x, y, (avgR << 16) | (avgG << 8) | avgB);
                        } else {
                            result.setRGB(x, y, rgb);
                        }
                        break;
                    case "邊緣檢測":
                        // 簡單的邊緣檢測
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

    private static void enableDrawing(JLabel imageLabel, BufferedImage originalImage) {
        BufferedImage copy = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        imageLabel.setIcon(new ImageIcon(scaleImage(copy, 500, 400)));

        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            Point lastPoint;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Graphics2D g2d = copy.createGraphics();
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                    g2d.dispose();

                    imageLabel.setIcon(new ImageIcon(scaleImage(copy, 500, 400)));
                }
                lastPoint = e.getPoint();
            }
        });

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }
        });
    }

    private static void saveImage(JFrame parent, JLabel imageLabel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存圖片");
        fileChooser.setSelectedFile(new File("meme_edited.png"));

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIcon icon = (ImageIcon) imageLabel.getIcon();
                BufferedImage bi = new BufferedImage(
                        icon.getIconWidth(),
                        icon.getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.createGraphics();
                icon.paintIcon(null, g, 0, 0);
                g.dispose();

                ImageIO.write(bi, "png", fileToSave);
                JOptionPane.showMessageDialog(parent, "圖片保存成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "保存圖片時出錯: " + e.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Image scaleImage(BufferedImage original, int width, int height) {
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}
