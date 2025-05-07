package imagehistoryAndStore;
import javax.swing.*;

public class TestImageHistory {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageHistory imgHistory = new ImageHistory();
            imgHistory.setVisible(true);
        });
    }
}

