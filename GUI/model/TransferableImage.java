package model;

import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO; // 添加此行
import java.util.Collections; // 添加此行

public class TransferableImage implements Transferable {
    private static final DataFlavor[] FLAVORS = {
            DataFlavor.imageFlavor,
            DataFlavor.javaFileListFlavor  // 添加对文件列表的支持
    };

    private final BufferedImage image;
    private final File tempFile;  // 临时文件

    public TransferableImage(BufferedImage image) throws IOException {
        this.image = image;
        // 创建临时文件
        this.tempFile = File.createTempFile("meme_", ".png");
        tempFile.deleteOnExit();
        // 将 BufferedImage 保存为 PNG 文件
        ImageIO.write(image, "PNG", tempFile);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : FLAVORS) {
            if (f.equals(flavor)) return true;
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DataFlavor.imageFlavor)) {
            return image;
        } else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            // 返回包含临时文件的列表
            return Collections.singletonList(tempFile);
        }
        throw new UnsupportedFlavorException(flavor);
    }
}