package ui;

import core.AppContext;
import model.Feedback;
import model.User;
import service.FeedbackService;
import service.PaymentService;
import service.ReportService;
import service.UserService;
import service.AppointmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ManagerUI extends JFrame {

    private final UserService        userService;
    private final ReportService      reportService;
    private final FeedbackService    feedbackService;
    private final PaymentService     paymentService;
    private final AppointmentService appointmentService;

    private JPanel     contentPanel;
    private CardLayout cardLayout;

    public ManagerUI() {
        AppContext ctx        = AppContext.getInstance();
        this.userService      = ctx.getUserService();
        this.reportService    = ctx.getReportService();
        this.feedbackService  = ctx.getFeedbackService();
        this.paymentService   = ctx.getPaymentService();
        this.appointmentService = ctx.getAppointmentService();

        setTitle("Manager Dashboard – APU ASC");
        setSize(900, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel roleLabel = new JLabel("Manager");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        logoutBtn.setMargin(new Insets(3, 10, 3, 10));
        topRight.add(roleLabel);
        topRight.add(logoutBtn);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        logoutBtn.addActionListener(e -> { new StartUI(); dispose(); });

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(buildHomePanel(),     "home");
        contentPanel.add(buildStaffPanel(),    "staff");
        contentPanel.add(buildSetPricePanel(), "price");
        contentPanel.add(buildFeedbackPanel(), "feedback");
        contentPanel.add(buildReportPanel(),   "report");
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "home");
        setVisible(true);
    }

    // ── Home ──────────────────────────────────────────────────────
    private JPanel buildHomePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        String[][] btns = {
            {"Staff Management",   "staff"},
            {"Set Service Prices", "price"},
            {"View Feedbacks",     "feedback"},
            {"Analyzed Reports",   "report"}
        };
        for (String[] b : btns) {
            JButton btn = new JButton(b[0]);
            btn.setPreferredSize(new Dimension(280, 48));
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            String card = b[1];
            btn.addActionListener(e -> cardLayout.show(contentPanel, card));
            p.add(btn, c);
            c.gridy++;
        }
        return p;
    }

    // ── Staff Panel ───────────────────────────────────────────────
    private JPanel buildStaffPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 8, 0);
        c.gridx = 0;

        for (String role : new String[]{"Manager", "CounterStaff", "Technician"}) {
            JButton btn = new JButton("Manage " + role + "s");
            btn.setPreferredSize(new Dimension(260, 44));
            c.gridy = role.equals("Manager") ? 0 : role.equals("CounterStaff") ? 1 : 2;
            btn.addActionListener(e -> showCrudDialog(role));
            center.add(btn, c);
        }
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ── Manage Users Dialog ───────────────────────────────────────
    private void showCrudDialog(String role) {
        JDialog dialog = new JDialog(this, "Manage " + role + "s", true);
        dialog.setSize(720, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] cols = {"User ID", "Name", "Email", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshTable(model, role);

        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JTextField searchField = new JTextField(18);
        JButton searchBtn = new JButton("Search");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        JButton addBtn = new JButton("+ Add New");
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        addPanel.add(addBtn);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(addPanel, BorderLayout.EAST);
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton editBtn = new JButton("Edit");
        JButton delBtn  = new JButton("Delete");
        botPanel.add(editBtn);
        botPanel.add(delBtn);
        dialog.add(botPanel, BorderLayout.SOUTH);

        Runnable doSearch = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            model.setRowCount(0);
            for (User u : userService.getUsersByRole(role)) {
                if (keyword.isEmpty()
                        || u.getUserId().toLowerCase().contains(keyword)
                        || u.getName().toLowerCase().contains(keyword)) {
                    model.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getRole()});
                }
            }
        };
        searchBtn.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run());

        addBtn.addActionListener(e -> {
            String name  = JOptionPane.showInputDialog(dialog, "Name:");     if (name == null) return;
            String email = JOptionPane.showInputDialog(dialog, "Email:");    if (email == null) return;
            String pass  = JOptionPane.showInputDialog(dialog, "Password:"); if (pass == null) return;
            try {
                userService.registerUser(name, email, pass, role);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            doSearch.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(dialog, "Select a user first."); return; }
            String userId = (String) model.getValueAt(row, 0);
            showEditDialog(dialog, userId, model, role);
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(dialog, "Select a user first."); return; }
            String userId = (String) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(dialog, "Delete " + userId + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                userService.deleteUser(userId);
                doSearch.run();
            }
        });

        dialog.setVisible(true);
    }

    private void showEditDialog(JDialog parent, String userId, DefaultTableModel tableModel, String role) {
        User u = userService.getUserById(userId);
        if (u == null) return;

        JDialog d = new JDialog(parent, "Edit User", true);
        d.setSize(380, 320);
        d.setLocationRelativeTo(parent);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 32, 16, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(5, 4, 5, 4);
        c.gridx = 0;

        c.gridy = 0; form.add(new JLabel("User ID"), c);
        c.gridy = 1;
        JTextField idField = new JTextField(u.getUserId());
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        form.add(idField, c);

        c.gridy = 2; form.add(new JLabel("Name"), c);
        c.gridy = 3;
        JTextField nameField = new JTextField(u.getName());
        form.add(nameField, c);

        c.gridy = 4; form.add(new JLabel("Email"), c);
        c.gridy = 5;
        JTextField emailField = new JTextField(u.getEmail());
        form.add(emailField, c);

        c.gridy = 6; form.add(new JLabel("New Password (leave blank to keep current)"), c);
        c.gridy = 7;
        JPasswordField passField = new JPasswordField();
        form.add(passField, c);

        d.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn   = new JButton("Save");
        south.add(cancelBtn);
        south.add(saveBtn);
        d.add(south, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> d.dispose());
        saveBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword()).trim();
            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Name and Email cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String finalPass = pass.isEmpty() ? u.getPassword() : pass;
            try {
                userService.updateUser(userId, name, email, finalPass);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshTable(tableModel, role);
            d.dispose();
        });

        d.setVisible(true);
    }

    private void refreshTable(DefaultTableModel model, String role) {
        model.setRowCount(0);
        for (User u : userService.getUsersByRole(role))
            model.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getRole()});
    }

    // ── Set Price Panel ───────────────────────────────────────────
    private JPanel buildSetPricePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(12, 8, 12, 8);

        JLabel normalVal = new JLabel(String.format("RM %.2f", paymentService.getNormalServicePrice()));
        normalVal.setFont(new Font("Arial", Font.BOLD, 14));
        JButton editNormal = new JButton("Edit");

        c.gridy = 0; c.gridx = 0; center.add(new JLabel("Normal Service (1 hour)"), c);
        c.gridx = 1; center.add(normalVal, c);
        c.gridx = 2; center.add(editNormal, c);

        JLabel majorVal = new JLabel(String.format("RM %.2f", paymentService.getMajorServicePrice()));
        majorVal.setFont(new Font("Arial", Font.BOLD, 14));
        JButton editMajor = new JButton("Edit");

        c.gridy = 1; c.gridx = 0; center.add(new JLabel("Major Service (3 hours)"), c);
        c.gridx = 1; center.add(majorVal, c);
        c.gridx = 2; center.add(editMajor, c);

        p.add(center, BorderLayout.CENTER);

        editNormal.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this,
                    "New price for Normal Service (RM):", String.format("%.2f", paymentService.getNormalServicePrice()));
            if (input == null) return;
            try {
                double v = Double.parseDouble(input);
                paymentService.setPrices(v, paymentService.getMajorServicePrice());
                normalVal.setText(String.format("RM %.2f", v));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editMajor.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this,
                    "New price for Major Service (RM):", String.format("%.2f", paymentService.getMajorServicePrice()));
            if (input == null) return;
            try {
                double v = Double.parseDouble(input);
                paymentService.setPrices(paymentService.getNormalServicePrice(), v);
                majorVal.setText(String.format("RM %.2f", v));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return p;
    }

    // ── Feedback Panel ────────────────────────────────────────────
    // Rating filter dropdown (All / 1★ ~ 5★)
    private JPanel buildFeedbackPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);

        // Rating filter
        JLabel filterLabel = new JLabel("Filter by Rating:");
        String[] ratingOptions = {"All", "★☆☆☆☆ (1)", "★★☆☆☆ (2)", "★★★☆☆ (3)", "★★★★☆ (4)", "★★★★★ (5)"};
        JComboBox<String> ratingFilter = new JComboBox<>(ratingOptions);
        top.add(filterLabel);
        top.add(ratingFilter);
        p.add(top, BorderLayout.NORTH);

        // Average rating label
        double avg = feedbackService.getAllFeedbacks().stream()
                .filter(f -> f.getRating() > 0)
                .mapToInt(f -> f.getRating())
                .average().orElse(0.0);
        JLabel avgLabel = new JLabel(String.format("  Average Rating: %.1f / 5.0", avg));
        avgLabel.setFont(new Font("Arial", Font.BOLD, 12));
        avgLabel.setForeground(new Color(200, 140, 0));

        String[] cols = {"Feedback ID", "Customer ID", "Appt ID", "Rating", "Customer Comment", "Tech Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();
        Runnable doFilter = () -> {
            model.setRowCount(0);
            int selectedRating = ratingFilter.getSelectedIndex(); // 0=All, 1=1star, ...
            for (Feedback f : allFeedbacks) {
                if (selectedRating > 0 && f.getRating() != selectedRating) continue;
                String stars = f.getRating() > 0 ? "★".repeat(f.getRating()) : "-";
                model.addRow(new Object[]{
                    f.getFeedbackId(),
                    f.getCustomerId(),
                    f.getAppointmentId(),
                    stars,
                    f.getCustomerComment() == null || f.getCustomerComment().isEmpty() ? "(none)" : f.getCustomerComment(),
                    f.getTechnicianFeedback() == null || f.getTechnicianFeedback().isEmpty() ? "(none)" : f.getTechnicianFeedback()
                });
            }
        };
        doFilter.run();
        ratingFilter.addActionListener(e -> doFilter.run());

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel tableTopBar = new JPanel(new BorderLayout());
        tableTopBar.add(avgLabel, BorderLayout.WEST);
        JButton detailBtn = new JButton("Detail");
        JPanel detailPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        detailPanel.add(detailBtn);
        tableTopBar.add(detailPanel, BorderLayout.EAST);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(tableTopBar, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(tableArea, BorderLayout.CENTER);

        detailBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a feedback row first."); return; }
            String feedbackId = (String) model.getValueAt(row, 0);
            Feedback f = null;
            for (Feedback fb : allFeedbacks)
                if (fb.getFeedbackId().equals(feedbackId)) { f = fb; break; }
            if (f != null) showFeedbackDetail(f);
        });

        return p;
    }

    private void showFeedbackDetail(Feedback f) {
        JDialog d = new JDialog(this, "Feedback Detail", true);
        d.setSize(560, 460);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout(0, 8));
        d.getRootPane().setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel idArea = new JPanel(new GridBagLayout());
        idArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 4, 3, 12);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel fbIdLabel = new JLabel("Feedback ID:  " + f.getFeedbackId());
        fbIdLabel.setFont(new Font("Arial", Font.BOLD, 13));
        idArea.add(fbIdLabel, c);
        c.gridwidth = 1;

        c.gridy = 1; c.gridx = 0; idArea.add(new JLabel("Appointment ID:"), c);
        c.gridx = 1; idArea.add(new JLabel(f.getAppointmentId()), c);

        c.gridy = 2; c.gridx = 0; idArea.add(new JLabel("Customer ID:"), c);
        c.gridx = 1;
        String custName = "";
        User cust = userService.getUserById(f.getCustomerId());
        if (cust != null) custName = "  (" + cust.getName() + ")";
        idArea.add(new JLabel(f.getCustomerId() + custName), c);

        c.gridy = 3; c.gridx = 0; idArea.add(new JLabel("Technician ID:"), c);
        c.gridx = 1;
        String techId = "", techName = "";
        try {
            model.Appointment appt = AppContext.getInstance().getAppointmentService()
                    .getAppointmentById(f.getAppointmentId());
            if (appt != null) {
                techId = appt.getTechnicianId();
                User tech = userService.getUserById(techId);
                if (tech != null) techName = "  (" + tech.getName() + ")";
            }
        } catch (Exception ex) { /* ignore */ }
        idArea.add(new JLabel(techId.isEmpty() ? "-" : techId + techName), c);

        main.add(idArea);
        main.add(Box.createVerticalStrut(12));

        JPanel centerArea = new JPanel();
        centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
        centerArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        String stars = f.getRating() > 0 ? "★".repeat(f.getRating()) + "  (" + f.getRating() + "/5)" : "-";
        JLabel ratingLabel = new JLabel("Rating:  " + stars);
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerArea.add(ratingLabel);
        centerArea.add(Box.createVerticalStrut(8));
        centerArea.add(makeDetailRow("Comment", f.getCustomerComment()));
        centerArea.add(Box.createVerticalStrut(6));
        centerArea.add(makeDetailRow("Tech Feedback", f.getTechnicianFeedback()));

        main.add(centerArea);
        main.add(Box.createVerticalStrut(14));

        d.add(new JScrollPane(main), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(ev -> d.dispose());
        south.add(closeBtn);
        d.add(south, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private JPanel makeDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 4));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        row.add(lbl, BorderLayout.NORTH);

        JTextArea area = new JTextArea((value == null || value.isEmpty()) ? "(none)" : value);
        area.setFont(new Font("Arial", Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(null);
        row.add(area, BorderLayout.CENTER);

        return row;
    }

    // ── Report Panel ──────────────────────────────────────────────
    // JTabbedPane with one tab per month + one "All" tab
    private JPanel buildReportPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        p.add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        // "All" tab
        tabs.addTab("All", buildReportTab(null, "Overall"));

        // Monthly tabs
        List<String> months = reportService.getAvailableMonths();
        for (String ym : months)
            tabs.addTab(ym, buildReportTab(ym, ym));

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildReportTab(String yearMonth, String title) {
        double totalSales = reportService.calculateTotalSalesForMonth(yearMonth);
        int    totalAppts = reportService.countAppointmentsForMonth(yearMonth);
        double avgRating  = reportService.calculateAverageRatingForMonth(yearMonth);
        Map<String, Integer> statusStats  = reportService.getAppointmentStatusStatisticsForMonth(yearMonth);
        Map<String, Integer> serviceStats = reportService.getServiceTypeStatisticsForMonth(yearMonth);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Summary cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        summaryRow.add(makeSummaryCard("Total Sales", String.format("RM %.2f", totalSales)));
        summaryRow.add(makeSummaryCard("Total Appointments", String.valueOf(totalAppts)));
        summaryRow.add(makeSummaryCard("Avg Rating", String.format("%.1f / 5.0", avgRating)));
        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(summaryRow);
        inner.add(Box.createVerticalStrut(16));

        // Appointment Status bar chart
        JLabel statusTitle = new JLabel("Appointment Status");
        statusTitle.setFont(new Font("Arial", Font.BOLD, 13));
        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(statusTitle);
        inner.add(Box.createVerticalStrut(4));

        BarChartPanel statusChart = new BarChartPanel(statusStats, new Color(70, 130, 180));
        statusChart.setPreferredSize(new Dimension(600, 130));
        statusChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statusChart.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(statusChart);
        inner.add(Box.createVerticalStrut(16));

        // Service Type bar chart
        JLabel serviceTitle = new JLabel("Service Type Breakdown");
        serviceTitle.setFont(new Font("Arial", Font.BOLD, 13));
        serviceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(serviceTitle);
        inner.add(Box.createVerticalStrut(4));

        BarChartPanel serviceChart = new BarChartPanel(serviceStats, new Color(100, 180, 100));
        serviceChart.setPreferredSize(new Dimension(600, 100));
        serviceChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        serviceChart.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(serviceChart);
        inner.add(Box.createVerticalStrut(16));

        // Detail table
        JLabel tableTitle = new JLabel("Detail Table");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 13));
        tableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tableTitle);
        inner.add(Box.createVerticalStrut(4));

        String[] cols = {"Category", "Count"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Map.Entry<String, Integer> e : statusStats.entrySet())
            tableModel.addRow(new Object[]{"Status: " + e.getKey(), e.getValue()});
        for (Map.Entry<String, Integer> e : serviceStats.entrySet())
            tableModel.addRow(new Object[]{"Service: " + e.getKey(), e.getValue()});

        JTable reportTable = new JTable(tableModel);
        reportTable.setRowHeight(26);
        reportTable.setShowGrid(true);
        JScrollPane tableScroll = new JScrollPane(reportTable);
        tableScroll.setPreferredSize(new Dimension(400, 110));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tableScroll);

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(inner), BorderLayout.CENTER); }};
    }

    private JPanel makeSummaryCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 16));
        val.setForeground(new Color(30, 100, 180));
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    // ── Bar chart (Swing paintComponent) ─────────────────────────
    private static class BarChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private final Color barColor;

        BarChartPanel(Map<String, Integer> data, Color barColor) {
            this.data     = data;
            this.barColor = barColor;
            setBackground(Color.WHITE);
            setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) { g.drawString("No data", 20, 40); return; }

            int maxVal   = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int rows     = data.size();
            int padLeft  = 130;
            int padRight = 20;
            int padTop   = 10;
            int rowH     = (getHeight() - padTop * 2) / Math.max(rows, 1);
            int barH     = Math.max(rowH - 10, 8);
            int availW   = getWidth() - padLeft - padRight;

            g.setFont(new Font("Arial", Font.PLAIN, 11));
            FontMetrics fm = g.getFontMetrics();
            int i = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int y    = padTop + i * rowH;
                int barW = maxVal == 0 ? 0 : (int) ((double) e.getValue() / maxVal * availW);
                g.setColor(Color.DARK_GRAY);
                g.drawString(e.getKey(), 4, y + rowH / 2 + fm.getAscent() / 2);
                g.setColor(barColor);
                g.fillRoundRect(padLeft, y + (rowH - barH) / 2, barW, barH, 4, 4);
                g.setColor(Color.DARK_GRAY);
                g.drawString(String.valueOf(e.getValue()), padLeft + barW + 4, y + rowH / 2 + fm.getAscent() / 2);
                i++;
            }
        }
    }
}