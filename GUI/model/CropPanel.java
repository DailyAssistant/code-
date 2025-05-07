package model;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class CropPanel extends JPanel {
    private BufferedImage image;
    private Rectangle cropRect;
    private Point startPoint;

    public CropPanel(BufferedImage image) {
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                cropRect = new Rectangle(startPoint);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (cropRect.width < 10 || cropRect.height < 10) {
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
        g.drawImage(image, 0, 0, this);

        if (cropRect != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 255, 100));
            g2d.fill(cropRect);

            g2d.setColor(Color.BLUE);
            g2d.draw(cropRect);
        }
    }

    public BufferedImage getCroppedImage() {
        if (cropRect == null) return image;

        BufferedImage cropped = new BufferedImage(
                cropRect.width, cropRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = cropped.createGraphics();
        g2d.drawImage(image, 0, 0, cropRect.width, cropRect.height,
                cropRect.x, cropRect.y, cropRect.x + cropRect.width,
                cropRect.y + cropRect.height, null);
        g2d.dispose();
        return cropped;
    }
}
