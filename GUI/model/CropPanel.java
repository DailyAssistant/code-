package model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class CropPanel extends JPanel {//處理圖片裁切的class
    private BufferedImage image;
    private Rectangle cropRect;
    private Point startPoint;

    public CropPanel(BufferedImage image) {
        this.image = image;
        setLayout(new BorderLayout());
        initMouseListeners();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    private void initMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                cropRect = new Rectangle(startPoint);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (cropRect != null && (cropRect.width < 10 || cropRect.height < 10)) {
                    JOptionPane.showMessageDialog(CropPanel.this,
                            "裁剪區域太小（最小 10x10 像素）", "提示", JOptionPane.WARNING_MESSAGE);
                    cropRect = null;
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(e.getX() - startPoint.x);
                    int height = Math.abs(e.getY() - startPoint.y);
                    cropRect = new Rectangle(x, y, width, height);
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // 計算等比縮放
        double scaleX = (double) panelWidth / image.getWidth();
        double scaleY = (double) panelHeight / image.getHeight();
        double scale = Math.max(scaleX, scaleY); // 填滿視窗

        int drawWidth = (int) (image.getWidth() * scale);
        int drawHeight = (int) (image.getHeight() * scale);
        int drawX = (panelWidth - drawWidth) / 2;
        int drawY = (panelHeight - drawHeight) / 2;

        g2d.drawImage(image, drawX, drawY, drawWidth, drawHeight, this);

        if (cropRect != null) {
            g2d.setColor(new Color(0, 0, 255, 100));
            g2d.fill(cropRect);
            g2d.setColor(Color.BLUE);
            g2d.draw(cropRect);
        }
    }


    public BufferedImage getCroppedImage() {
        if (cropRect == null || cropRect.width <= 0 || cropRect.height <= 0) return image;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        double scaleX = (double) panelWidth / image.getWidth();
        double scaleY = (double) panelHeight / image.getHeight();
        double scale = Math.max(scaleX, scaleY); // 用同樣的 scale

        int drawWidth = (int) (image.getWidth() * scale);
        int drawHeight = (int) (image.getHeight() * scale);
        int drawX = (panelWidth - drawWidth) / 2;
        int drawY = (panelHeight - drawHeight) / 2;

        int x = (int) ((cropRect.x - drawX) / scale);
        int y = (int) ((cropRect.y - drawY) / scale);
        int width = (int) (cropRect.width / scale);
        int height = (int) (cropRect.height / scale);

        // 邊界檢查
        x = Math.max(0, Math.min(x, image.getWidth() - 1));
        y = Math.max(0, Math.min(y, image.getHeight() - 1));
        width = Math.min(width, image.getWidth() - x);
        height = Math.min(height, image.getHeight() - y);

        BufferedImage cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = cropped.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, x, y, x + width, y + height, null);
        g2d.dispose();
        return cropped;
    }

}
