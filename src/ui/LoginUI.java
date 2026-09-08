package ui;

import core.AppContext;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginUI extends JFrame {

    private JTextField     userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton        loginBtn;
    private boolean        passwordVisible = false;

    public LoginUI() {
        setTitle("Login – APU ASC");
        setSize(440, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("APU – ASC System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 4, 5, 4);
        c.weightx = 1.0;

        // Role
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(new JLabel("Role"), c);
        c.gridy = 1;
        roleBox = new JComboBox<>(new String[]{"Manager", "CounterStaff", "Technician", "Customer"});
        form.add(roleBox, c);

        // User ID
        c.gridy = 2;
        form.add(new JLabel("User ID  (e.g. M001)"), c);
        c.gridy = 3;
        userIdField = new JTextField();
        userIdField.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (!Character.isLetterOrDigit(e.getKeyChar())) e.consume();
            }
        });
        form.add(userIdField, c);

        // Password
        c.gridy = 4; c.gridwidth = 2;
        form.add(new JLabel("Password"), c);
        c.gridy = 5; c.gridwidth = 1; c.weightx = 1.0;
        passwordField = new JPasswordField();
        form.add(passwordField, c);

        c.gridx = 1; c.weightx = 0;
        JButton eyeBtn = new JButton("👁");
        eyeBtn.setMargin(new Insets(2, 6, 2, 6));
        eyeBtn.setFocusPainted(false);
        eyeBtn.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            passwordField.setEchoChar(passwordVisible ? (char) 0 : '●');
            eyeBtn.setText(passwordVisible ? "🙈" : "👁");
        });
        form.add(eyeBtn, c);
        add(form, BorderLayout.CENTER);

        loginBtn = new JButton("Login");
        loginBtn.setEnabled(false);

        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> { new StartUI(); dispose(); });

        JPanel south = new JPanel(new GridLayout(2, 1, 0, 6));
        south.setBorder(BorderFactory.createEmptyBorder(8, 50, 20, 50));
        south.add(loginBtn);
        south.add(backBtn);
        add(south, BorderLayout.SOUTH);

        KeyAdapter watcher = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                loginBtn.setEnabled(
                    !userIdField.getText().trim().isEmpty() &&
                    passwordField.getPassword().length > 0
                );
            }
        };
        userIdField.addKeyListener(watcher);
        passwordField.addKeyListener(watcher);
        loginBtn.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {
        String userId   = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role     = (String) roleBox.getSelectedItem();

        User user = AppContext.getInstance().getUserService().login(userId, password, role);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                "Invalid User ID or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (user.getRole()) {
            case "Manager":      new ManagerUI();          break;
            case "CounterStaff": new StaffUI(user);        break;
            case "Technician":   new TechnicianUI(user);   break;
            case "Customer":     new CustomerUI(user);     break;
            default:
                JOptionPane.showMessageDialog(this,
                    "Unknown role: " + user.getRole(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
        dispose();
    }
}