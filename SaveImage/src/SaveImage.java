import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class SaveImage {
    private SaveImage() {}

    public static boolean save(BufferedImage img, String pathname, String format) {
        try {
            File imgFile = new File(pathname);
            if(imgFile.mkdirs()){
                System.out.println("路徑建立成功");
            }
            ImageIO.write(img, format, imgFile);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "儲存失敗", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
