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
import java.awt.image.BufferedImage;

public class ImageSearchService {

    public static ArrayList<ImageWithUrl> searchImages(String keyword) throws IOException {//回傳爬到的照片，預設抓9張
        ArrayList<String> imageUrls = fetchImageUrls(keyword);
        ArrayList<ImageWithUrl> result = new ArrayList<>();

        for (String url : imageUrls) {
            URL imageUrl = new URL(url);
            BufferedImage  img = ImageIO.read(imageUrl);
            if (img != null) {
                result.add(new ImageWithUrl(img,url));
            }
        }
        return result;
    }

    private static ArrayList<String> fetchImageUrls(String keyword) throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        String searchUrl = "https://memes.tw/maker?from=trending&q=" + java.net.URLEncoder.encode(keyword, "UTF-8");
        if (keyword == "" || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Document doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get();
        Elements links = doc.select("a[href*=/wtf?template=]");//高畫質圖片的所在網站

        int imgcount=0;
        //加入找到的圖片的url到arraylist
        for (Element link : links) {
            String href = link.attr("href");//抓通往高畫質照片網站連結
            String detailUrl = href.startsWith("http") ? href : "https://memes.tw" + href;
            if(imgcount>=9) break;
            //System.out.println(detailUrl);
            try{
                Document detailDoc = Jsoup.connect(detailUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .get();
                //高畫質圖片class="img-fluid img-sample"空白表示有兩個class
                Element img = detailDoc.selectFirst("img.img-fluid.img-sample");
                if (img != null) {
                    String src = img.absUrl("src");
                    System.out.println(src);
                    if (!src.isEmpty()) {
                        urls.add(src);
                        imgcount++;
                    }
                }
            }
            catch(Exception e){
                System.err.printf("Error: %s",e.getMessage());
            }
        }
        return urls;
    }
    public static String getImageFormat(String url){
        if (url == null || url.isEmpty()) return null;

        url = url.toLowerCase();//將字串內所有字母轉成小寫
        if (url.contains(".jpg") ) {
            return "jpg";
        }
        else if (url.contains(".png")) {
            return "png";
        }
        else if (url.contains(".gif")) {
            return "gif";
        }
        else if (url.contains(".jpeg")) {
            return "jpeg";
        }
        else if (url.contains(".webp")) {
            return "webp";
        }
        return null;
    }
}
