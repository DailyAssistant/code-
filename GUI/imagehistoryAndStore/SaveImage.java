package imagehistoryAndStore;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.*;

public final class SaveImage {
    public static void save(BufferedImage img, String pathname, String format, boolean saveToHistory, boolean useOriginalPath) throws IOException {
        if (img == null) {
            throw new IOException("圖片為空，無法儲存");
        }

        String finalPath = pathname;
        if (saveToHistory && !useOriginalPath) {
            finalPath = "../ImageHistory/images/" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".jpg";
        }

        File imgFile = new File(finalPath);
        if (!imgFile.getParentFile().exists()) {
            if (imgFile.getParentFile().mkdirs()) {
                System.out.println("路徑建立成功: " + imgFile.getParent());
            } else {
                throw new IOException("無法建立目錄: " + imgFile.getParent());
            }
        }

        ImageIO.write(img, format, imgFile);
        System.out.println("儲存成功: " + imgFile.getAbsolutePath());
    }

    // 使用者儲存圖片用
    public void saveImageWithDialog(BufferedImage img, String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("選擇儲存位置");
        fileChooser.setSelectedFile(new File("image_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + format));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // 確保副檔名存在
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith("." + format.toLowerCase())) {
                fileToSave = new File(path + "." + format);
            }

            try {
                ImageIO.write(img, format, fileToSave);
                JOptionPane.showMessageDialog(null, "儲存成功：" + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "儲存失敗：" + e.getMessage());
            }
        }
    }
}