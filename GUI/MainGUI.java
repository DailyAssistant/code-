import view.MemeSearchFrame;

public class MainGUI {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MemeSearchFrame frame = new MemeSearchFrame();
            frame.setVisible(true);
        });
    }
}
