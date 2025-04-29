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



public class ResultsPanel extends JScrollPane {
    private JPanel contentPanel;

    public ResultsPanel() {
        contentPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        setViewportView(contentPanel);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void displayImages(List<Image> images) {
        contentPanel.removeAll();

        for (Image img : images) {
            Image scaledImg = img.getScaledInstance(250, 200, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

            // 設置拖拽功能
            BufferedImage scaledBuffered = toBufferedImage(scaledImg);
            setupDragSource(imageLabel, scaledBuffered);

            // 設置點擊事件
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 這裡可以調用您的圖片編輯功能
                    ImageEditor.openEditor((JFrame)SwingUtilities.getWindowAncestor(ResultsPanel.this),
                            toBufferedImage(img));
                }
            });

            contentPanel.add(imageLabel);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
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

    private void setupDragSource(JLabel label, Image image) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(
                label,
                DnDConstants.ACTION_COPY,
                new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        BufferedImage bufferedImage = toBufferedImage(image);
                        if (bufferedImage != null) {
                            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                            TransferableImage transferable = new TransferableImage(bufferedImage);
                            dge.startDrag(cursor, transferable);
                        }
                    }
                }
        );
    }
}