//搜尋照片的程式
package imageSearchBar;//初代搜尋程式，已經不會用到了
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ImageSearcher extends JFrame {
    private final JTextField searchField;
    private final JButton searchButton;
    private final JPanel imagePanel;
    private final String ACCESS_KEY = "gf2TQSyXfvzDjp-qprEtZmm6-KJXVhfL_gAX4BL44Uc"; // <<<<< 放你的 Unsplash API 金鑰
    private final JPanel topPanel;
    private final JLabel loadingMsg;

    public ImageSearcher() {
        super("梗圖蒐尋器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 設置 topPanel 使用 BorderLayout，這樣可以將 loading 信息放在底部
        topPanel = new JPanel(new BorderLayout());

        // 搜尋框和按鈕的panel
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(30);
        searchButton = new JButton("搜尋");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 置中搜尋框
                topPanel.add(searchPanel, BorderLayout.CENTER);

        // loading message
        loadingMsg = new JLabel("搜尋中，請稍等", SwingConstants.CENTER);
        loadingMsg.setPreferredSize(new Dimension(topPanel.getWidth(), 30));

        // 將topPanel加到主框架
        add(topPanel, BorderLayout.NORTH);

        imagePanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);
        searchButton.addActionListener(e -> searchImages());
    }

    private void searchImages() {
        imagePanel.removeAll();
        imagePanel.revalidate();
        imagePanel.repaint();

        //show loading message
        topPanel.add(loadingMsg, BorderLayout.SOUTH);
        topPanel.revalidate();
        topPanel.repaint();

        String keyword = searchField.getText();
        new Thread(() -> {
            try {
                ArrayList<String> imageUrls = fetchImageUrls(keyword);
                for (String url : imageUrls) {
                    URL imageUrl = new URL(url);
                    Image img = ImageIO.read(imageUrl);
                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                        JLabel label = new JLabel(new ImageIcon(scaledImg));

                        MouseAdapter clickListener = new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                //showZoomedImage.showimage(img);
                            }
                        };

                        label.addMouseListener(clickListener);

                        SwingUtilities.invokeLater(() -> imagePanel.add(label));
                        SwingUtilities.invokeLater(() -> {
                            imagePanel.revalidate();//如果內容有變，自動調整
                            imagePanel.repaint();
                        });
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    // 完成後移除 loading message
                    topPanel.remove(loadingMsg);
                    topPanel.revalidate();
                    topPanel.repaint();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    // 如果出錯也要移除 loading 信息
                    topPanel.remove(loadingMsg);
                    topPanel.revalidate();
                    topPanel.repaint();
                });
            }
        }).start();
    }

    private ArrayList<String> fetchImageUrls(String keyword) throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        String apiUrl = "https://api.unsplash.com/search/photos?query=" + URLEncoder.encode(keyword, "UTF-8") + "&per_page=9&client_id=" + ACCESS_KEY;

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        String json = content.toString();
        int index = 0;
        while ((index = json.indexOf("\"small\":\"", index)) != -1) {
            index += 9; // 移動到網址開始
            int endIndex = json.indexOf("\"", index);
            if (endIndex > index) {
                String imageUrl = json.substring(index, endIndex).replace("\\u0026", "&"); // 處理網址中的 &
                urls.add(imageUrl);
            }
        }
        return urls;
    }

}