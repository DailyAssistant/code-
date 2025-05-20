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
import javax.swing.SwingUtilities;   // 用于获取窗口祖先（getWindowAncestor）
import java.awt.datatransfer.Transferable; // 添加此行
import java.io.IOException;
import service.ImageWithUrl;//import class

public class ResultsPanel extends JScrollPane {
    private JPanel contentPanel;
    private String Query = "";
    public ResultsPanel() {
        contentPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);
        JScrollBar sideBar = getVerticalScrollBar();//右邊的滾動條
        sideBar.setUnitIncrement(25); // scrollpane靈敏度
        /*sideBar.addAdjustmentListener(e -> {待修正
            int extent = sideBar.getModel().getExtent();
            int maximum = sideBar.getMaximum();//scrollbar最大值
            int value = sideBar.getValue();//scrollbar目前值

            if (value + extent >= maximum - 10) {
                try {
                    ImageSearchService.searchImages(Query);
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });*/

        System.out.println(getVerticalScrollBar().getValue());
        setViewportView(contentPanel);
        setBorder(BorderFactory.createEmptyBorder());
    }
    public void setQuery(String query) {
        this.Query = query;
    }
    public void displayImages(List<ImageWithUrl> images) {
        contentPanel.removeAll();

        for (int i = 0; i < images.size(); i++) {
            Image img = images.get(i).image;
            BufferedImage originalBuffered = toBufferedImage(img);
            Image scaledImg = originalBuffered.getScaledInstance(250, 200, Image.SCALE_SMOOTH);
            BufferedImage scaledBuffered = new BufferedImage(250, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledBuffered.createGraphics();
            g2d.drawImage(scaledImg, 0, 0, null);
            g2d.dispose();
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

            setupDragSource(imageLabel, scaledBuffered);
            String url = images.get(i).url;
            // 設置點擊事件
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 這裡可以調用您的圖片編輯功能
                    ImageEditor.openEditor((JFrame)SwingUtilities.getWindowAncestor(ResultsPanel.this),
                            toBufferedImage(img),
                            url);
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

    private void setupDragSource(JLabel label, BufferedImage image) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(
                label,
                DnDConstants.ACTION_COPY,
                new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        try {
                            Transferable transferable = new TransferableImage(image);
                            // 设置拖拽图标为缩略图
                            Image dragImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                                    dragImage, new Point(0, 0), "drag"
                            );
                            // 启动拖拽，显示图标
                            dge.startDrag(cursor, transferable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}