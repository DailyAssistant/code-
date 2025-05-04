import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageHistory extends JFrame {
    private final JPanel imagePanel;

    public ImageHistory(){
        super("圖片歷史紀錄");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        imagePanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        loadImagesFromFolder();
    }

    private void loadImagesFromFolder() {
        File folder = new File("../images");
        if (!folder.exists() || !folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "找不到資料夾");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg") || lower.endsWith(".gif");
        });

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "該資料夾內沒有圖片");
            return;
        }

        for (File file : files) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(200, 150, Image.SCALE_SMOOTH));
                    JLabel label = new JLabel(icon);
                    label.setText(file.getName());
                    label.setHorizontalTextPosition(JLabel.CENTER);
                    label.setVerticalTextPosition(JLabel.BOTTOM);
                    imagePanel.add(label);
                }
            } catch (Exception e) {
                System.out.println("無法讀取圖片：" + file.getName());
            }
        }

        // 更新界面
        imagePanel.revalidate();
        imagePanel.repaint();
    }
}
