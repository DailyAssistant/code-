package imagehistoryAndStore;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class TestSaveImage {
    public static void main(String[] args) {
        try {
            // 讀取網路圖片
            URL imageUrl = new URL("https://www.biaopan8.com/wp-content/uploads/2022/05/google_g_hero.jpg");
            BufferedImage image = ImageIO.read(imageUrl);

            // 儲存圖片至指定路徑，注意要包含檔名與副檔名
            SaveImage.save(image, "../ImageHistory/images/google_g_hero.jpg", "jpg", false,false);
        } catch (IOException e) {
            System.err.println("讀取圖片失敗：" + e.getMessage());
        }
    }
}

