// 修改 ImageSearchService.java
package service;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.Image;

public class ImageSearchService {
    private static final String ACCESS_KEY = "gf2TQSyXfvzDjp-qprEtZmm6-KJXVhfL_gAX4BL44Uc"; // Unsplash API 金鑰

    public static ArrayList<Image> searchImages(String keyword) throws IOException {
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
        String apiUrl = "https://api.unsplash.com/search/photos?query=" +
                URLEncoder.encode(keyword, "UTF-8") +
                "&per_page=9&client_id=" + ACCESS_KEY;

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            String json = content.toString();
            int index = 0;
            while ((index = json.indexOf("\"small\":\"", index)) != -1) {
                index += 9; // 移動到網址開始
                int endIndex = json.indexOf("\"", index);
                if (endIndex > index) {
                    String imageUrl = json.substring(index, endIndex).replace("\\u0026", "&");
                    urls.add(imageUrl);
                }
            }
        }
        return urls;
    }
}