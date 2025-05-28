package controller;

import java.awt.image.BufferedImage;

public class ImageToGray {//將彩色影像轉為灰階圖
    public static BufferedImage img2gray(BufferedImage image){
        try{
            int width=image.getWidth();
            int height=image.getHeight();
            //new一個buffered image避免覆寫原圖
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for(int i=0;i<width;i++){
                for(int j=0;j<height;j++){
                    int pixel=image.getRGB(i,j);
                    int r=(pixel>>16) & 0xFF;
                    int g=(pixel>>8) & 0xFF;
                    int b=pixel & 0xFF;
                    int avg=(r+b+g)/3;
                    int gray=(avg << 16) | (avg << 8) | avg;
                    grayImage.setRGB(i,j,gray);
                }
            }
            return grayImage;
        }
        catch(Exception e){
            System.err.println(e);
        }
        return null;
    }
    public static BufferedImage img2binary(BufferedImage image,int threshold){//影像二值化
        try{
            int width=image.getWidth();
            int height=image.getHeight();
            BufferedImage grayImage=img2gray(image);
            BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for(int i=0;i<width;i++){
                for(int j=0;j<height;j++){
                    int pixel=grayImage.getRGB(i,j);
                    int grayPixel=(pixel>>16) & 0xFF;//灰階圖3個channel長一樣，抓一個就好
                    int value= (grayPixel>=threshold) ? 0:255;//0是黑255是白
                    int binaryPixel = (value << 16) | (value << 8) | value;
                    binaryImage.setRGB(i,j,binaryPixel);
                }
            }
            return binaryImage;
        }
        catch(Exception e){
            System.err.println(e);
        }
        return null;
    }
}
