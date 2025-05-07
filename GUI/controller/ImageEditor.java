package controller;

import model.CropPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import imagehistoryAndStore.SaveImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import static imagehistoryAndStore.SaveImage.save;


public class ImageEditor {
    private static Point lastPoint;
    private static int imgnumber=0;
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

        JButton backButton = new JButton("返回");
        backButton.addActionListener(e -> editorDialog.dispose());

        JButton cropButton = new JButton("裁剪");
        cropButton.addActionListener(e -> showCropDialog(parent, editorLabel, image));

        JButton filterButton = new JButton("濾鏡");
        filterButton.addActionListener(e -> applyFilter(editorLabel, image));

        JButton drawButton = new JButton("標記");
        drawButton.addActionListener(e -> enableDrawing(editorLabel, image));

        JButton saveButton = new JButton("保存");//從GUI資料夾退回上一層，並存在imagehistory下的images
        imgnumber+=1;
        saveButton.addActionListener(e -> save(image,"../ImageHistory/images/img"+imgnumber+".jpg","jpg"));

        toolPanel.add(backButton);
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
                        JTextField sizeField = new JTextField("3");
                        JTextField sigmaField = new JTextField("1.0");

                        JPanel panel = new JPanel(new GridLayout(2, 2));
                        panel.add(new JLabel("請輸入尺寸(奇數):"));
                        panel.add(sizeField);
                        panel.add(new JLabel("請輸入模糊效果(Sigma):"));
                        panel.add(sigmaField);
                        JOptionPane.showConfirmDialog(panel, panel, "輸入模糊參數",//show出dialog
                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                        try {
                            //從文字框取得數值
                            int size = Integer.parseInt(sizeField.getText());
                            float sigma = Float.parseFloat(sigmaField.getText());

                            if (size % 2 == 0 || size < 1 || size >10) {
                                JOptionPane.showMessageDialog(null, "尺寸必須是大於 0 的奇數!!!", "不合法數值", JOptionPane.WARNING_MESSAGE);
                                size = 3;
                            }


                            if(sigma<=0){
                                JOptionPane.showInputDialog(null, "sigma值不可以<=0 ", "不合法數值", JOptionPane.WARNING_MESSAGE);
                                sigma=1.0f;
                            }
                            return ImageBlur.GaussianBlur(image, size, sigma);//呼叫另一個class檔的method
                        }
                        catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "輸入無效", "警告", JOptionPane.WARNING_MESSAGE);
                            return ImageBlur.GaussianBlur(image,3,1.0f);
                        }


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



    private static Image scaleImage(BufferedImage original, int width, int height) {
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}