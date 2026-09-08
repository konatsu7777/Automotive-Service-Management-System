package ui;

import core.AppContext;
import service.IdGeneratorService;
import service.PaymentService;
import service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class GuestUI extends JFrame {

    private final PaymentService     paymentService;
    private final UserService        userService;
    private final IdGeneratorService idService;

    public GuestUI() {
        AppContext ctx      = AppContext.getInstance();
        this.paymentService = ctx.getPaymentService();
        this.userService    = ctx.getUserService();
        this.idService      = ctx.getIdGeneratorService();

        setTitle("APU ASC – Guest View");
        setSize(540, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Top bar ───────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel titleLabel = new JLabel("APU – ASC System", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        topBar.add(titleLabel, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton signInBtn = new JButton("Sign In");
        JButton backBtn   = new JButton("← Back");
        topRight.add(backBtn);
        topRight.add(signInBtn);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        backBtn.addActionListener(e   -> { new StartUI(); dispose(); });
        signInBtn.addActionListener(e -> showSignUpDialog());

        // ── Content ───────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 24, 24, 24));

        // Service info section
        JPanel serviceSection = new JPanel(new GridBagLayout());
        serviceSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Our Services",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 8, 6, 16);
        c.fill   = GridBagConstraints.HORIZONTAL;

        double normalPrice = paymentService.getNormalServicePrice();
        double majorPrice  = paymentService.getMajorServicePrice();

        String[][] services = {
            {"Normal Service", "Routine inspection and maintenance (approx. 1 hour)",
             String.format("RM %.2f", normalPrice)},
            {"Major Service",  "Full overhaul and comprehensive repair (approx. 3 hours)",
             String.format("RM %.2f", majorPrice)}
        };

        int row = 0;
        for (String[] svc : services) {
            c.gridx = 0; c.gridy = row;
            JLabel name = new JLabel(svc[0]);
            name.setFont(new Font("Arial", Font.BOLD, 13));
            serviceSection.add(name, c);

            c.gridy = row + 1;
            JLabel desc = new JLabel(svc[1]);
            desc.setFont(new Font("Arial", Font.PLAIN, 12));
            desc.setForeground(Color.DARK_GRAY);
            serviceSection.add(desc, c);

            c.gridx = 1; c.gridy = row;
            JLabel price = new JLabel(svc[2]);
            price.setFont(new Font("Arial", Font.BOLD, 14));
            price.setForeground(new Color(30, 100, 180));
            serviceSection.add(price, c);

            row += 2;
            if (row < services.length * 2) {
                c.gridx = 0; c.gridy = row; c.gridwidth = 2;
                serviceSection.add(new JSeparator(), c);
                c.gridwidth = 1;
                row++;
            }
        }

        content.add(serviceSection);
        content.add(Box.createVerticalStrut(16));

        // Business hours section
        JPanel hoursSection = new JPanel(new GridBagLayout());
        hoursSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Business Hours",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)));
        GridBagConstraints ch = new GridBagConstraints();
        ch.anchor = GridBagConstraints.WEST;
        ch.insets = new Insets(4, 8, 4, 24);

        String[][] hours = {
            {"Monday – Sunday", "9:00 AM – 6:00 PM"}
        };
        for (int i = 0; i < hours.length; i++) {
            ch.gridx = 0; ch.gridy = i;
            hoursSection.add(new JLabel(hours[i][0]), ch);
            ch.gridx = 1;
            JLabel h = new JLabel(hours[i][1]);
            h.setFont(new Font("Arial", Font.BOLD, 12));
            hoursSection.add(h, ch);
        }
        content.add(hoursSection);
        content.add(Box.createVerticalStrut(16));

        // How to book note
        JPanel noteSection = new JPanel(new BorderLayout());
        noteSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "How to Book",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13)));
        JLabel note = new JLabel("<html><body style='padding:6px'>"
                + "1. Click <b>Sign In</b> above to create a customer account.<br>"
                + "2. Our staff will process your appointment request.<br>"
                + "3. You will receive your Customer ID to log in and track your booking."
                + "</body></html>");
        note.setFont(new Font("Arial", Font.PLAIN, 12));
        noteSection.add(note, BorderLayout.CENTER);
        content.add(noteSection);

        add(new JScrollPane(content), BorderLayout.CENTER);
        setVisible(true);
    }

    // Sign up dialog — auto-registers as Customer and shows generated ID
    private void showSignUpDialog() {
        JDialog d = new JDialog(this, "Create Account", true);
        d.setSize(380, 280);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 36, 20, 36));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0;

        c.gridy = 0; form.add(new JLabel("Name"), c);
        c.gridy = 1;
        JTextField nameField = new JTextField();
        form.add(nameField, c);

        c.gridy = 2; form.add(new JLabel("Email"), c);
        c.gridy = 3;
        JTextField emailField = new JTextField();
        form.add(emailField, c);

        c.gridy = 4; form.add(new JLabel("Password"), c);
        c.gridy = 5;
        JPasswordField passField = new JPasswordField();
        form.add(passField, c);

        c.gridy = 6;
        JLabel roleNote = new JLabel("Role: Customer (assigned automatically)");
        roleNote.setFont(new Font("Arial", Font.ITALIC, 11));
        roleNote.setForeground(Color.GRAY);
        form.add(roleNote, c);

        d.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton submitBtn = new JButton("Create Account");
        south.add(cancelBtn);
        south.add(submitBtn);
        d.add(south, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> d.dispose());
        submitBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword()).trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(d, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Auto-register as Customer — ID is generated automatically
            String userId = userService.registerUser(name, email, pass, "Customer");

            JOptionPane.showMessageDialog(d,
                    "Account created successfully!\n\n"
                    + "Your Customer ID:  " + userId + "\n\n"
                    + "Please remember this ID to log in.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();

            // Go to login screen
            new LoginUI();
            dispose();
        });

        d.setVisible(true);
    }
}