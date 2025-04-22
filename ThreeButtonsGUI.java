import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ThreeButtonsGUI {
    public static void main(String[] args) {
        // 創建主視窗
        JFrame frame = new JFrame("三個按鈕範例");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout()); // 使用流式布局

        // 創建第一個按鈕
        JButton button1 = new JButton("按鈕 1");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "你點擊了按鈕 1");
            }
        });

        // 創建第二個按鈕
        JButton button2 = new JButton("按鈕 2");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "按鈕 2 被點擊了!");
            }
        });

        // 創建第三個按鈕
        JButton button3 = new JButton("按鈕 3");
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("按鈕 3 在控制台輸出訊息");
            }
        });

        // 將按鈕添加到視窗
        frame.add(button1);
        frame.add(button2);
        frame.add(button3);

        // 顯示視窗
        frame.setVisible(true);
    }
}