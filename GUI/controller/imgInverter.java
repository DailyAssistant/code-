package controller;

import java.awt.image.BufferedImage;
public class imgInverter {
    public static BufferedImage imgInvert(BufferedImage image){
        try{
            int width=image.getWidth();
            int height=image.getHeight();
            //new一個buffered image避免覆寫原圖
            BufferedImage invertImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int i=0;i<width;i++){
                for(int j=0;j<height;j++){
                    int pixel=image.getRGB(i,j);
                    int r=(pixel>>16) & 0xFF;
                    int g=(pixel>>8) & 0xFF;
                    int b=pixel & 0xFF;
                    //反色就是255-各個channel
                    int finalPixel=((255-r) << 16) | ((255-g) << 8) | (255-b);
                    invertImage.setRGB(i,j,finalPixel);
                }
            }
            return invertImage;
        }
        catch(Exception e){
            System.err.println(e);
        }
        return null;
    }
}
