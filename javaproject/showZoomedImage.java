//顯示放大圖片的方法
import javax.swing.*;
import java.awt.*;

public class showZoomedImage {
    public static void showimage(Image img) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int)(screenSize.width*0.85);//預計顯示的最大寬度
        int maxHeight = (int)(screenSize.height*0.85);
        // 計算縮放比例
        double scale = Math.min((double)maxWidth / width, (double)maxHeight / height);
        int finalWidth = (int)(width * scale);
        int finalHeight = (int)(height * scale);

        Image scaledImg = img.getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
        JFrame frame = new JFrame("Zoomedimage");
        frame.setLayout(new BorderLayout());
        frame.add(imageLabel,BorderLayout.CENTER);//加入縮放後圖片
        frame.setSize(finalWidth, finalHeight);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}