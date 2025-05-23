package service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageSearchService {

    public static ArrayList<ImageWithUrl> searchImages(String keyword) throws IOException {
        ArrayList<String> imageUrls = fetchImageUrls(keyword);
        ArrayList<ImageWithUrl> result = new ArrayList<>();

        for (String url : imageUrls) {
            URL imageUrl = new URL(url);
            BufferedImage img = ImageIO.read(imageUrl);
            if (img != null) {
                result.add(new ImageWithUrl(img, url));
            }
        }
        return result;
    }

    // 新增隨機圖片方法
    public static ArrayList<ImageWithUrl> fetchRandomImages() throws IOException {
        ArrayList<String> imageUrls = fetchRandomImageUrls();
        ArrayList<ImageWithUrl> result = new ArrayList<>();

        for (String url : imageUrls) {
            try {
                URL imageUrl = new URL(url);
                BufferedImage img = ImageIO.read(imageUrl);
                if (img != null) {
                    result.add(new ImageWithUrl(img, url));
                }
            } catch (Exception e) {
                System.err.printf("Error loading random image: %s\n", e.getMessage());
            }
        }
        return result;
    }

    private static ArrayList<String> fetchImageUrls(String keyword) throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        String searchUrl = "https://memes.tw/maker?from=trending&q=" + java.net.URLEncoder.encode(keyword, "UTF-8");
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Document doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();
        Elements links = doc.select("a[href*=/wtf?template=]");

        int imgcount = 0;
        for (Element link : links) {
            String href = link.attr("href");
            String detailUrl = href.startsWith("http") ? href : "https://memes.tw" + href;
            if (imgcount >= 9) break;
            try {
                Document detailDoc = Jsoup.connect(detailUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .get();
                Element img = detailDoc.selectFirst("img.img-fluid.img-sample");
                if (img != null) {
                    String src = img.absUrl("src");
                    System.out.println(src);
                    if (!src.isEmpty()) {
                        urls.add(src);
                        imgcount++;
                    }
                }
            } catch (Exception e) {
                System.err.printf("Error: %s\n", e.getMessage());
            }
        }
        return urls;
    }

    private static ArrayList<String> fetchRandomImageUrls() throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        // 從 memes.tw 的熱門或首頁抓取圖片
        String randomUrl = "https://memes.tw/maker?from=trending";
        Document doc = Jsoup.connect(randomUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();
        Elements links = doc.select("a[href*=/wtf?template=]");

        // 將所有圖片連結收集後隨機打亂
        ArrayList<String> allImageUrls = new ArrayList<>();
        for (Element link : links) {
            String href = link.attr("href");
            String detailUrl = href.startsWith("http") ? href : "https://memes.tw" + href;
            try {
                Document detailDoc = Jsoup.connect(detailUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .get();
                Element img = detailDoc.selectFirst("img.img-fluid.img-sample");
                if (img != null) {
                    String src = img.absUrl("src");
                    if (!src.isEmpty()) {
                        allImageUrls.add(src);
                    }
                }
            } catch (Exception e) {
                System.err.printf("Error fetching random image URL: %s\n", e.getMessage());
            }
        }

        // 隨機打亂並選取最多9張圖片
        Collections.shuffle(allImageUrls);
        int maxImages = Math.min(9, allImageUrls.size());
        for (int i = 0; i < maxImages; i++) {
            urls.add(allImageUrls.get(i));
        }
        return urls;
    }

    public static String getImageFormat(String url) {
        if (url == null || url.isEmpty()) return null;

        url = url.toLowerCase();
        if (url.contains(".jpg")) {
            return "jpg";
        } else if (url.contains(".png")) {
            return "png";
        } else if (url.contains(".gif")) {
            return "gif";
        } else if (url.contains(".jpeg")) {
            return "jpeg";
        } else if (url.contains(".webp")) {
            return "webp";
        }
        return null;
    }
}