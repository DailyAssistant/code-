package model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class DrawPanel extends JPanel {
    private BufferedImage image;
    private BufferedImage drawLayer; // Layer for drawing
    private Point lastPoint;
    private float penThickness;
    private Color penColor;

    public DrawPanel(BufferedImage image) {
        this.image = image;
        this.drawLayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.penThickness = 3.0f; // Default: fine
        this.penColor = Color.RED; // Default color
        setLayout(new BorderLayout());

        // Initialize draw layer as transparent
        Graphics2D g2d = drawLayer.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, drawLayer.getWidth(), drawLayer.getHeight());
        g2d.dispose();

        initToolBar();
        initMouseListeners();
    }

    private void initToolBar() {
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Pen thickness selector
        String[] thicknesses = {"細 (3px)", "中 (6px)", "粗 (9px)"};
        JComboBox<String> thicknessCombo = new JComboBox<>(thicknesses);
        thicknessCombo.setSelectedIndex(0); // Default: fine
        thicknessCombo.addActionListener(e -> {
            switch (thicknessCombo.getSelectedIndex()) {
                case 0: penThickness = 3.0f; break;
                case 1: penThickness = 6.0f; break;
                case 2: penThickness = 9.0f; break;
            }
        });

        // Color selector
        String[] colors = {"紅色", "橙色", "黃色", "綠色", "藍色", "紫色", "黑色"};
        JComboBox<String> colorCombo = new JComboBox<>(colors);
        colorCombo.setSelectedIndex(0); // Default: red
        colorCombo.addActionListener(e -> {
            switch (colorCombo.getSelectedIndex()) {
                case 0: penColor = Color.RED; break;
                case 1: penColor = Color.ORANGE; break;
                case 2: penColor = Color.YELLOW; break;
                case 3: penColor = Color.GREEN; break;
                case 4: penColor = Color.BLUE; break;
                case 5: penColor = new Color(128, 0, 128); // Purple
                    break;
                case 6: penColor = Color.BLACK; break;
            }
        });

        toolPanel.add(new JLabel("筆粗細:"));
        toolPanel.add(thicknessCombo);
        toolPanel.add(new JLabel("顏色:"));
        toolPanel.add(colorCombo);

        add(toolPanel, BorderLayout.NORTH);
    }

    private void initMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    // Convert screen coordinates to image coordinates
                    Point currentPoint = e.getPoint();
                    double scale = getScale();
                    int drawX = (getWidth() - (int)(image.getWidth() * scale)) / 2;
                    int drawY = (getHeight() - (int)(image.getHeight() * scale)) / 2;

                    int x1 = (int)((lastPoint.x - drawX) / scale);
                    int y1 = (int)((lastPoint.y - drawY) / scale);
                    int x2 = (int)((currentPoint.x - drawX) / scale);
                    int y2 = (int)((currentPoint.y - drawY) / scale);

                    // Draw on the drawLayer
                    Graphics2D g2d = drawLayer.createGraphics();
                    g2d.setColor(penColor);
                    g2d.setStroke(new BasicStroke(penThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.dispose();

                    lastPoint = currentPoint;
                    repaint();
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    private double getScale() {
        double scaleX = (double) getWidth() / image.getWidth();
        double scaleY = (double) getHeight() / image.getHeight();
        return Math.max(scaleX, scaleY); // Fill the window
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Calculate scaling to fill the window
        double scale = getScale();
        int drawWidth = (int) (image.getWidth() * scale);
        int drawHeight = (int) (image.getHeight() * scale);
        int drawX = (panelWidth - drawWidth) / 2;
        int drawY = (panelHeight - drawHeight) / 2;

        // Draw the base image
        g2d.drawImage(image, drawX, drawY, drawWidth, drawHeight, this);

        // Draw the drawing layer
        g2d.drawImage(drawLayer, drawX, drawY, drawWidth, drawHeight, this);
    }

    public BufferedImage getDrawnImage() {
        // Combine the original image and the drawing layer
        BufferedImage combined = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combined.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.drawImage(drawLayer, 0, 0, null);
        g2d.dispose();
        return combined;
    }
}