package ui;

import javax.swing.*;
import java.awt.*;

public class StartUI extends JFrame {

    public StartUI() {
        setTitle("APU Automotive Service Centre");
        setSize(400, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("APU – ASC System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        JButton loginBtn = new JButton("Login");
        JButton guestBtn = new JButton("Continue as Guest");
        guestBtn.setForeground(new Color(80, 80, 80));
        guestBtn.setFont(new Font("Arial", Font.ITALIC, 12));

        panel.add(title);
        panel.add(loginBtn);
        panel.add(guestBtn);

        add(panel, BorderLayout.CENTER);
        loginBtn.addActionListener(e -> { new LoginUI(); dispose(); });
        guestBtn.addActionListener(e -> { new GuestUI(); dispose(); });

        setVisible(true);
    }
}