// 修改 ImageSearchService.java
//抓取照片的程式
package service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.Image;

public class ImageSearchService {

    public static ArrayList<Image> searchImages(String keyword) throws IOException {//回傳爬到的照片，預設抓9張
        ArrayList<String> imageUrls = fetchImageUrls(keyword);
        ArrayList<Image> images = new ArrayList<>();

        for (String url : imageUrls) {
            URL imageUrl = new URL(url);
            Image img = ImageIO.read(imageUrl);
            if (img != null) {
                images.add(img);
            }
        }
        return images;
    }

    private static ArrayList<String> fetchImageUrls(String keyword) throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        String searchUrl = "https://memes.tw/maker?from=trending&q=" + java.net.URLEncoder.encode(keyword, "UTF-8");

        Document doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();

        Elements imgs = doc.select("img");//抓取html 的img tag
        //加入找到的圖片的url到arraylist
        for (Element img : imgs) {
            String src = img.absUrl("src");
            urls.add(src);
            if (urls.size() >= 9) break;
        }

        return urls;
    }
}