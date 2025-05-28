package controller;
import java.awt.image.BufferedImage;


public class ImageBlur {//高斯模糊的class
    public static BufferedImage GaussianBlur(BufferedImage image,int size, float sigma){
        float[][] kernel = generateGaussianKernel(size, sigma);
        return applyGaussianBlur(image, kernel);
    }
    private static float[][] generateGaussianKernel(int size, float sigma){
        float[][] kernel = new float[size][size];
        float sum = 0.0f;
        int radius = size / 2;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                float value = (float) (Math.exp(-(i * i + j * j) / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma));
                kernel[i + radius][j + radius] = value;
                sum += value;
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }
        return kernel;
    }

    private static BufferedImage applyGaussianBlur(BufferedImage image,float[][] kernel){
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int radius = kernel.length / 2;
        for (int y = 0; y < height ; y++) {
            for (int x = 0; x < width ; x++) {
                float red = 0, green = 0, blue = 0;
                if (x < radius || x >= width - radius || y < radius || y >= height - radius) {//邊界保持原樣
                    blurredImage.setRGB(x, y, image.getRGB(x, y));
                    continue;
                }
                // 对每个像素应用高斯核
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {

                        if(x+kx >= 0 && x+kx <= image.getWidth() && y+ky >= 0 && y+ky <= image.getHeight()){
                            int pixel = image.getRGB(x + kx, y + ky);
                            float weight = kernel[ky + radius][kx + radius];

                            red += ((pixel >> 16) & 0xFF) * weight;
                            green += ((pixel >> 8) & 0xFF) * weight;
                            blue += (pixel & 0xFF) * weight;

                        }
                    }
                }
                // 设置模糊后的像素值
                int blurredPixel = ((int) red << 16) | ((int) green << 8) | (int) blue;
                blurredImage.setRGB(x, y, blurredPixel);
            }
        }
        return blurredImage;
    }
}
