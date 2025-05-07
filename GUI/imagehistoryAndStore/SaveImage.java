package imagehistoryAndStore;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class SaveImage {
    public static void save(BufferedImage img, String pathname, String format) {
        try {
            File imgFile = new File(pathname);
            if(imgFile.mkdirs()){
                System.out.println("路徑建立成功");
            }
            ImageIO.write(img, format, imgFile);
            System.out.println("儲存成功！儲存位置: " + imgFile.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "儲存失敗", JOptionPane.ERROR_MESSAGE);
        }
    }
}
