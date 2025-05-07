package view;

import javax.swing.*;
import java.awt.*;

public class ButtonPanel extends JPanel {
    private JButton editButton;

    public ButtonPanel() {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        editButton = new JButton("編輯選中圖片");
        editButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        editButton.setEnabled(false);

        add(editButton);
    }

    public JButton getEditButton() {
        return editButton;
    }
}
