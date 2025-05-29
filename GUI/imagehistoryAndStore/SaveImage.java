import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SaveImage {
    public void save(BufferedImage img, String pathname, String format, boolean saveToHistory) {
        try {
            if(saveToHistory){
                pathname = "../images/" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".jpg";
            }

            File imgFile = new File(pathname);

            if(imgFile.mkdirs()){
                System.out.println("路徑建立成功");
            }
            ImageIO.write(img, format, imgFile);
            System.out.println("儲存成功！");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "儲存失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    //使用者儲存圖片用
    public void saveImageWithDialog(BufferedImage img, String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("選擇儲存位置");
        fileChooser.setSelectedFile(new File("image_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + format)); // 預設檔名

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // 確保副檔名存在
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith("." + format)) {
                fileToSave = new File(path + "." + format);
            }

            try {
                ImageIO.write(img, format, fileToSave);
                JOptionPane.showMessageDialog(null, "儲存成功：" + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "儲存失敗：" + e.getMessage());
            }
        }
    }
}
