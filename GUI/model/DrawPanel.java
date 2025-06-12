package model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawPanel extends JPanel {
    private BufferedImage image;
    private BufferedImage drawLayer; // Composite layer for rendering
    private BufferedImage drawingLayer; // Layer for freehand drawings
    private BufferedImage textLayer; // Layer for text
    private Point lastPoint;
    private float penThickness;
    private Color penColor;
    private int fontSize; // Font size for text
    private List<TextObject> textObjects; // Store text objects with position and content
    private TextObject selectedText; // Currently selected text for dragging or scaling
    private Point dragOffset; // Offset for dragging text
    private boolean isDrawingMode; // Flag for drawing mode
    private boolean isScaling; // Flag for scaling text
    private Point scaleHandle; // Handle for scaling

    // Class to store text properties
    private static class TextObject {
        String text;
        int x, y; // Position in image coordinates (top-left of text)
        Color color;
        int fontSize;
        Rectangle scaleHandle; // Bounding box for scaling handle

        TextObject(String text, int x, int y, Color color, int fontSize) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.fontSize = fontSize;
            this.scaleHandle = new Rectangle();
        }

        // Check if a point is within the text bounds
        boolean contains(Point p, FontMetrics fm, double scale) {
            int width = fm.stringWidth(text);
            int height = fm.getHeight();
            int scaledX = (int)(x * scale);
            int scaledY = (int)(y * scale);
            return p.x >= scaledX && p.x <= scaledX + width && p.y >= scaledY - fm.getAscent() && p.y <= scaledY + fm.getDescent();
        }

        // Check if a point is within the scale handle
        boolean containsScaleHandle(Point p, FontMetrics fm, double scale) {
            int width = fm.stringWidth(text);
            int height = fm.getHeight();
            int scaledX = (int)(x * scale);
            int scaledY = (int)(y * scale);
            // Scale handle is a 10x10 pixel square at bottom-right corner
            scaleHandle.setBounds(scaledX + width - 5, scaledY + fm.getDescent() - 5, 10, 10);
            return scaleHandle.contains(p);
        }

        // Update scale handle position
        void updateScaleHandle(FontMetrics fm, double scale) {
            int width = fm.stringWidth(text);
            int height = fm.getHeight();
            int scaledX = (int)(x * scale);
            int scaledY = (int)(y * scale);
            scaleHandle.setBounds(scaledX + width - 5, scaledY + fm.getDescent() - 5, 10, 10);
        }
    }

    public DrawPanel(BufferedImage image) {
        this.image = image;
        this.drawLayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.drawingLayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.textLayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.penThickness = 3.0f; // Default: fine
        this.penColor = Color.RED; // Default color
        this.fontSize = 20; // Default font size
        this.textObjects = new ArrayList<>();
        this.selectedText = null;
        this.isDrawingMode = false; // Default: no mode selected
        this.isScaling = false;
        setLayout(new BorderLayout());

        // Initialize layers as transparent
        clearLayer(drawLayer);
        clearLayer(drawingLayer);
        clearLayer(textLayer);

        initToolBar();
        initMouseListeners();
    }

    // Helper to clear a BufferedImage
    private void clearLayer(BufferedImage layer) {
        Graphics2D g2d = layer.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, layer.getWidth(), layer.getHeight());
        g2d.dispose();
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

        // Font size selector
        String[] fontSizes = {"小 (12pt)", "中 (20pt)", "大 (28pt)"};
        JComboBox<String> fontSizeCombo = new JComboBox<>(fontSizes);
        fontSizeCombo.setSelectedIndex(1); // Default: medium
        fontSizeCombo.addActionListener(e -> {
            switch (fontSizeCombo.getSelectedIndex()) {
                case 0: fontSize = 12; break;
                case 1: fontSize = 20; break;
                case 2: fontSize = 28; break;
            }
        });

        // Text button
        JButton textButton = new JButton("文字");
        textButton.addActionListener(e -> {
            isDrawingMode = false;
            String text = JOptionPane.showInputDialog(DrawPanel.this, "請輸入文字:", "輸入文字", JOptionPane.PLAIN_MESSAGE);
            if (text != null && !text.trim().isEmpty()) {
                // Center text using FontMetrics
                Graphics2D g2d = textLayer.createGraphics();
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g2d.dispose();

                int centerX = (image.getWidth() - textWidth) / 2;
                int centerY = (image.getHeight() / 2) + (fm.getAscent() - textHeight / 2);
                textObjects.add(new TextObject(text, centerX, centerY, penColor, fontSize));
                redrawText();
                System.out.println("Added text: " + text + " at (" + centerX + ", " + centerY + ")");
            }
        });

        // Drawing button
        JButton drawButton = new JButton("繪畫");
        drawButton.addActionListener(e -> {
            isDrawingMode = true;
        });

        toolPanel.add(new JLabel("筆粗細:"));
        toolPanel.add(thicknessCombo);
        toolPanel.add(new JLabel("顏色:"));
        toolPanel.add(colorCombo);
        toolPanel.add(new JLabel("字型大小:"));
        toolPanel.add(fontSizeCombo);
        toolPanel.add(textButton);
        toolPanel.add(drawButton);

        add(toolPanel, BorderLayout.NORTH);
    }

    private void initMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isDrawingMode) {
                    // Convert screen coordinates to image coordinates for checking text selection
                    double scale = getScale();
                    int drawX = (getWidth() - (int)(image.getWidth() * scale)) / 2;
                    int drawY = (getHeight() - (int)(image.getHeight() * scale)) / 2;
                    Point imagePoint = new Point((int)((e.getPoint().x - drawX) / scale), (int)((e.getPoint().y - drawY) / scale));
                    Point scaledPoint = new Point((int)(imagePoint.x * scale), (int)(imagePoint.y * scale));

                    // Check if clicking on any text object or its scale handle
                    Graphics2D g2d = textLayer.createGraphics();
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.dispose();

                    selectedText = null;
                    isScaling = false;
                    for (TextObject textObj : textObjects) {
                        textObj.updateScaleHandle(fm, scale);
                        if (textObj.containsScaleHandle(scaledPoint, fm, scale)) {
                            selectedText = textObj;
                            isScaling = true;
                            scaleHandle = new Point(scaledPoint.x, scaledPoint.y);
                            break;
                        } else if (textObj.contains(scaledPoint, fm, scale)) {
                            selectedText = textObj;
                            dragOffset = new Point(imagePoint.x - textObj.x, imagePoint.y - textObj.y);
                            break;
                        }
                    }
                } else {
                    lastPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedText = null;
                dragOffset = null;
                isScaling = false;
                scaleHandle = null;
                if (isDrawingMode) {
                    lastPoint = null;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawingMode && selectedText != null) {
                    double scale = getScale();
                    int drawX = (getWidth() - (int)(image.getWidth() * scale)) / 2;
                    int drawY = (getHeight() - (int)(image.getHeight() * scale)) / 2;
                    int newX = (int)((e.getPoint().x - drawX) / scale);
                    int newY = (int)((e.getPoint().y - drawY) / scale);

                    if (isScaling) {
                        // Adjust font size based on drag distance
                        int deltaY = (int)((e.getPoint().y - scaleHandle.y) / scale);
                        selectedText.fontSize = Math.max(8, selectedText.fontSize + deltaY / 10); // Scale font size
                        scaleHandle = new Point(e.getPoint().x, e.getPoint().y);
                    } else {
                        // Drag text to new position
                        newX -= dragOffset.x;
                        newY -= dragOffset.y;
                        // Ensure text stays within image bounds
                        newX = Math.max(0, Math.min(newX, image.getWidth()));
                        newY = Math.max(0, Math.min(newY, image.getHeight()));
                        selectedText.x = newX;
                        selectedText.y = newY;
                    }
                    redrawText();
                } else if (isDrawingMode && lastPoint != null) {
                    // Convert screen coordinates to image coordinates
                    Point currentPoint = e.getPoint();
                    double scale = getScale();
                    int drawX = (getWidth() - (int)(image.getWidth() * scale)) / 2;
                    int drawY = (getHeight() - (int)(image.getHeight() * scale)) / 2;

                    int x1 = (int)((lastPoint.x - drawX) / scale);
                    int y1 = (int)((lastPoint.y - drawY) / scale);
                    int x2 = (int)((currentPoint.x - drawX) / scale);
                    int y2 = (int)((currentPoint.y - drawY) / scale);

                    // Draw on the drawingLayer
                    Graphics2D g2d = drawingLayer.createGraphics();
                    g2d.setColor(penColor);
                    g2d.setStroke(new BasicStroke(penThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.dispose();

                    lastPoint = currentPoint;
                    repaint();
                }
            }
        });

        // Add mouse wheel listener for scaling text
        addMouseWheelListener(e -> {
            if (!isDrawingMode && selectedText != null) {
                int notches = e.getWheelRotation();
                selectedText.fontSize = Math.max(8, selectedText.fontSize - notches); // Adjust font size
                redrawText();
            }
        });
    }

    // Redraw text objects on the textLayer
    private void redrawText() {
        // Clear and redraw textLayer
        clearLayer(textLayer);
        Graphics2D g2d = textLayer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw all text objects
        for (TextObject textObj : textObjects) {
            g2d.setColor(textObj.color);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, textObj.fontSize));
            g2d.drawString(textObj.text, textObj.x, textObj.y);
            System.out.println("Rendering text: " + textObj.text + " at (" + textObj.x + ", " + textObj.y + ")");
        }
        g2d.dispose();

        // Force repaint
        invalidate();
        repaint();
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        // Composite drawingLayer and textLayer onto drawLayer
        clearLayer(drawLayer);
        Graphics2D drawG2d = drawLayer.createGraphics();
        drawG2d.setComposite(AlphaComposite.SrcOver);
        drawG2d.drawImage(drawingLayer, 0, 0, null);
        drawG2d.drawImage(textLayer, 0, 0, null);
        drawG2d.dispose();

        // Draw the composite layer
        g2d.drawImage(drawLayer, drawX, drawY, drawWidth, drawHeight, this);

        // Draw scale handle for selected text
        if (!isDrawingMode && selectedText != null) {
            Graphics2D g2dTemp = (Graphics2D) g.create();
            g2dTemp.setColor(selectedText.color);
            g2dTemp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, selectedText.fontSize));
            FontMetrics fm = g2dTemp.getFontMetrics();
            selectedText.updateScaleHandle(fm, scale);
            g2dTemp.fillRect(selectedText.scaleHandle.x, selectedText.scaleHandle.y, selectedText.scaleHandle.width, selectedText.scaleHandle.height);
            g2dTemp.dispose();
        }
    }

    public BufferedImage getDrawnImage() {
        // Combine the original image and the drawLayer
        BufferedImage combined = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combined.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.drawImage(drawLayer, 0, 0, null);
        g2d.dispose();
        return combined;
    }
}