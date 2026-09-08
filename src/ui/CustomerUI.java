package ui;

import core.AppContext;
import model.Appointment;
import model.Feedback;
import model.Payment;
import model.User;
import service.AppointmentService;
import service.FeedbackService;
import service.IdGeneratorService;
import service.PaymentService;
import service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerUI extends JFrame {

    private final UserService        userService;
    private final AppointmentService appointmentService;
    private final PaymentService     paymentService;
    private final FeedbackService    feedbackService;
    private final IdGeneratorService idService;
    private final User               currentUser;

    private JPanel     contentPanel;
    private CardLayout cardLayout;

    public CustomerUI(User currentUser) {
        AppContext ctx          = AppContext.getInstance();
        this.userService        = ctx.getUserService();
        this.appointmentService = ctx.getAppointmentService();
        this.paymentService     = ctx.getPaymentService();
        this.feedbackService    = ctx.getFeedbackService();
        this.idService          = ctx.getIdGeneratorService();
        this.currentUser        = currentUser;

        setTitle("Customer Dashboard – APU ASC");
        setSize(960, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel roleLabel = new JLabel("Customer");
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
        contentPanel.add(buildProfilePanel(),  "profile");
        contentPanel.add(buildHistoryPanel(),  "history");
        contentPanel.add(buildFeedbackPanel(), "feedback");
        contentPanel.add(buildRatePanel(null), "rate");
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
            {"Edit Profile",              "profile"},
            {"Service & Payment History", "history"},
            {"View Appointment Feedback", "feedback"},
            {"Rate & Comment",            "rate"}
        };
        for (String[] b : btns) {
            JButton btn = new JButton(b[0]);
            btn.setPreferredSize(new Dimension(280, 48));
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            String card = b[1];
            btn.addActionListener(e -> {
                if (card.equals("rate")) rebuildRatePanel(null);
                cardLayout.show(contentPanel, card);
            });
            p.add(btn, c);
            c.gridy++;
        }
        return p;
    }

    // ── Edit Profile ──────────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 60, 20, 60));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0;

        c.gridy = 0; form.add(new JLabel("User ID"), c);
        c.gridy = 1;
        JTextField idField = new JTextField(currentUser.getUserId());
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        form.add(idField, c);

        c.gridy = 2; form.add(new JLabel("Name"), c);
        c.gridy = 3;
        JTextField nameField = new JTextField(currentUser.getName());
        form.add(nameField, c);

        c.gridy = 4; form.add(new JLabel("Email"), c);
        c.gridy = 5;
        JTextField emailField = new JTextField(currentUser.getEmail());
        form.add(emailField, c);

        c.gridy = 6; form.add(new JLabel("New Password (leave blank to keep current)"), c);
        c.gridy = 7;
        JPasswordField passField = new JPasswordField();
        form.add(passField, c);

        c.gridy = 8;
        JButton saveBtn = new JButton("Save Changes");
        form.add(saveBtn, c);
        p.add(form, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword()).trim();
            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Email cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String finalPass = pass.isEmpty() ? currentUser.getPassword() : pass;
            try {
                userService.updateUser(currentUser.getUserId(), name, email, finalPass);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
                return;
            }
            currentUser.setName(name);
            currentUser.setEmail(email);
            if (!pass.isEmpty()) currentUser.setPassword(finalPass);
            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
        });

        return p;
    }

    // ── Service & Payment History ─────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        JTextField searchField = new JTextField(16);
        JButton searchBtn = new JButton("Search");
        top.add(backBtn);
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        p.add(top, BorderLayout.NORTH);

        List<Payment>     allPayments = paymentService.getAllPayments();
        List<Appointment> myAppts     = appointmentService.getAppointmentsByCustomer(currentUser.getUserId());

        String[] cols = {"Appt ID", "Car Plate", "Service Type", "Date", "Time", "Status", "Payment"};
        DefaultTableModel inProgressModel = makeReadOnlyModel(cols);
        DefaultTableModel awaitingModel   = makeReadOnlyModel(cols);
        DefaultTableModel completedModel  = makeReadOnlyModel(cols);

        Runnable doFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            inProgressModel.setRowCount(0);
            awaitingModel.setRowCount(0);
            completedModel.setRowCount(0);
            for (Appointment a : myAppts) {
                String dt   = a.getDateTime();
                String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
                String time = dt.contains(" ") ? dt.split(" ")[1] : "-";
                boolean paid = allPayments.stream()
                        .anyMatch(pay -> pay.getAppointmentId().equals(a.getAppointmentId()));
                String payStatus = paid ? "✔ Paid" : a.isCompleted() ? "⚠ Unpaid" : "— Not yet due";
                if (!keyword.isEmpty()
                        && !a.getAppointmentId().toLowerCase().contains(keyword)
                        && !a.getServiceType().toLowerCase().contains(keyword)
                        && !date.contains(keyword)) continue;
                Object[] row = {
                    a.getAppointmentId(),
                    a.getCarPlate().isEmpty() ? "-" : a.getCarPlate(),
                    a.getServiceType(), date, time, a.getStatus(), payStatus
                };
                if (!a.isCompleted())   inProgressModel.addRow(row);
                else if (!paid)         awaitingModel.addRow(row);
                else                    completedModel.addRow(row);
            }
        };
        doFilter.run();
        searchBtn.addActionListener(e -> doFilter.run());
        searchField.addActionListener(e -> doFilter.run());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("In Progress",      buildHistoryTab(inProgressModel, allPayments));
        tabs.addTab("Awaiting Payment", buildHistoryTab(awaitingModel,   allPayments));
        tabs.addTab("Completed",        buildHistoryTab(completedModel,   allPayments));

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildHistoryTab(DefaultTableModel model, List<Payment> allPayments) {
        JPanel tab = new JPanel(new BorderLayout());
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel tableTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton detailBtn = new JButton("Detail");
        tableTopBar.add(detailBtn);
        tab.add(tableTopBar, BorderLayout.NORTH);
        tab.add(new JScrollPane(table), BorderLayout.CENTER);

        detailBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            String apptId = (String) model.getValueAt(row, 0);
            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (appt != null) showHistoryDetail(appt, allPayments);
        });

        return tab;
    }

    private void showHistoryDetail(Appointment appt, List<Payment> allPayments) {
        JDialog d = new JDialog(this, "Appointment Detail", true);
        d.setSize(480, 420);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout(0, 8));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel apptSection = new JPanel(new GridBagLayout());
        apptSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Appointment",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12)));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 6, 4, 12);
        c.fill   = GridBagConstraints.HORIZONTAL;

        String dt   = appt.getDateTime();
        String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
        String time = dt.contains(" ") ? dt.split(" ")[1] : "-";

        String[][] apptRows = {
            {"Appointment ID", appt.getAppointmentId()},
            {"Car Plate",      appt.getCarPlate().isEmpty() ? "-" : appt.getCarPlate()},
            {"Service Type",   appt.getServiceType()},
            {"Date",           date},
            {"Time",           time},
            {"Status",         appt.getStatus()}
        };
        for (int i = 0; i < apptRows.length; i++) {
            c.gridx = 0; c.gridy = i;
            JLabel lbl = new JLabel(apptRows[i][0] + ":");
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            apptSection.add(lbl, c);
            c.gridx = 1;
            apptSection.add(new JLabel(apptRows[i][1]), c);
        }
        main.add(apptSection);
        main.add(Box.createVerticalStrut(10));

        Payment payment = allPayments.stream()
                .filter(pay -> pay.getAppointmentId().equals(appt.getAppointmentId()))
                .findFirst().orElse(null);

        if (payment != null) {
            JPanel paySection = new JPanel(new GridBagLayout());
            paySection.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(60, 140, 60)), "Payment",
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 12)));
            GridBagConstraints cp = new GridBagConstraints();
            cp.anchor = GridBagConstraints.WEST;
            cp.insets = new Insets(4, 6, 4, 12);
            cp.fill   = GridBagConstraints.HORIZONTAL;
            String[][] payRows = {
                {"Payment ID",   payment.getPaymentId()},
                {"Amount",       "RM " + String.format("%.2f", payment.getAmount())},
                {"Method",       payment.getPaymentMethod()},
                {"Payment Date", payment.getPaymentDate()}
            };
            for (int i = 0; i < payRows.length; i++) {
                cp.gridx = 0; cp.gridy = i;
                JLabel lbl = new JLabel(payRows[i][0] + ":");
                lbl.setFont(new Font("Arial", Font.BOLD, 12));
                paySection.add(lbl, cp);
                cp.gridx = 1;
                paySection.add(new JLabel(payRows[i][1]), cp);
            }
            main.add(paySection);
        } else if (appt.isCompleted()) {
            JLabel warn = new JLabel("⚠  Payment required. Please contact the counter.");
            warn.setForeground(new Color(180, 80, 0));
            warn.setAlignmentX(Component.LEFT_ALIGNMENT);
            main.add(warn);
        }

        d.add(new JScrollPane(main), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(ev -> d.dispose());
        south.add(closeBtn);
        d.add(south, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── View Appointment Feedback ─────────────────────────────────
    private JPanel buildFeedbackPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        JTextField searchField = new JTextField(14);
        JButton searchBtn = new JButton("Search");
        top.add(backBtn);
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Tech ID", "Date", "My Comment", "Tech Feedback"};
        DefaultTableModel model = makeReadOnlyModel(cols);
        List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();

        JTable table = new JTable(model) {
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        table.setRowHeight(60);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(460);

        table.getColumnModel().getColumn(4).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            JTextArea area = new JTextArea(value == null ? "" : value.toString());
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(tbl.getFont());
            area.setBorder(new EmptyBorder(4, 4, 4, 4));
            area.setBackground(isSelected ? tbl.getSelectionBackground() : tbl.getBackground());
            area.setForeground(isSelected ? tbl.getSelectionForeground() : tbl.getForeground());
            return area;
        });

        Runnable doRefresh = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            model.setRowCount(0);
            for (Appointment a : appointmentService.getAppointmentsByCustomer(currentUser.getUserId())) {
                if (!a.isCompleted()) continue;
                String techId = a.getTechnicianId() == null || a.getTechnicianId().isEmpty() ? "-" : a.getTechnicianId();
                String techFeedback  = "(no feedback yet)";
                String commentStatus = "— Not yet";
                for (Feedback f : allFeedbacks) {
                    if (f.getAppointmentId().equals(a.getAppointmentId())) {
                        if (f.getTechnicianFeedback() != null && !f.getTechnicianFeedback().isEmpty())
                            techFeedback = f.getTechnicianFeedback();
                        if (f.getServiceRating() > 0 || f.getStaffRating() > 0)
                            commentStatus = "✔ Done";
                        break;
                    }
                }
                String dt   = a.getDateTime();
                String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
                if (!keyword.isEmpty()
                        && !a.getAppointmentId().toLowerCase().contains(keyword)
                        && !techId.toLowerCase().contains(keyword)) continue;
                model.addRow(new Object[]{a.getAppointmentId(), techId, date, commentStatus, techFeedback});
            }
        };
        doRefresh.run();
        searchBtn.addActionListener(e -> doRefresh.run());
        searchField.addActionListener(e -> doRefresh.run());

        JPanel tableTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton leaveCommentBtn = new JButton("Leave Comment");
        leaveCommentBtn.setBackground(new Color(60, 120, 200));
        leaveCommentBtn.setForeground(Color.WHITE);
        leaveCommentBtn.setOpaque(true);
        leaveCommentBtn.setFocusPainted(false);
        tableTopBar.add(leaveCommentBtn);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(tableTopBar, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(tableArea, BorderLayout.CENTER);

        leaveCommentBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            String apptId       = (String) model.getValueAt(row, 0);
            String commentStatus = (String) model.getValueAt(row, 3);
            if ("✔ Done".equals(commentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "You have already reviewed this appointment.",
                        "Already Reviewed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            rebuildRatePanel(apptId);
            cardLayout.show(contentPanel, "rate");
        });

        return p;
    }

    // ── Rate & Comment ────────────────────────────────────────────
    private JPanel buildRatePanel(String preSelectedApptId) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        JTextField searchField = new JTextField(16);
        JButton searchBtn = new JButton("Search");
        top.add(backBtn);
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        p.add(top, BorderLayout.NORTH);

        JLabel notice = new JLabel("⚠  Once submitted, your rating and comment cannot be changed.");
        notice.setForeground(new Color(160, 80, 0));
        notice.setFont(new Font("Arial", Font.ITALIC, 12));
        notice.setBorder(new EmptyBorder(0, 4, 6, 4));

        // Pending tab: Appt ID | Service Type | Date
        String[] pendingCols  = {"Appt ID", "Service Type", "Date"};
        // Done tab: Appt ID | Service Type | Date | Staff ★ | Service ★ | Comment
        String[] reviewedCols = {"Appt ID", "Service Type", "Date", "Staff ★", "Service ★", "Comment"};

        DefaultTableModel pendingModel  = makeReadOnlyModel(pendingCols);
        DefaultTableModel reviewedModel = makeReadOnlyModel(reviewedCols);
        List<Feedback>    allFeedbacks  = feedbackService.getAllFeedbacks();
        List<Appointment> myAppts       = appointmentService.getAppointmentsByCustomer(currentUser.getUserId());

        Runnable doFilter = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            pendingModel.setRowCount(0);
            reviewedModel.setRowCount(0);
            for (Appointment a : myAppts) {
                if (!a.isCompleted()) continue;
                String dt   = a.getDateTime();
                String date = dt.contains(" ") ? dt.split(" ")[0] : dt;

                Feedback matchFeedback = null;
                for (Feedback f : allFeedbacks)
                    if (f.getAppointmentId().equals(a.getAppointmentId())) { matchFeedback = f; break; }

                boolean reviewed = matchFeedback != null
                        && (matchFeedback.getServiceRating() > 0 || matchFeedback.getStaffRating() > 0);

                if (!keyword.isEmpty()
                        && !a.getAppointmentId().toLowerCase().contains(keyword)
                        && !a.getServiceType().toLowerCase().contains(keyword)
                        && !date.contains(keyword)) continue;

                if (reviewed) {
                    String staffStars   = "★".repeat(matchFeedback.getStaffRating());
                    String serviceStars = "★".repeat(matchFeedback.getServiceRating());
                    String comment      = matchFeedback.getCustomerComment() == null
                            || matchFeedback.getCustomerComment().isEmpty()
                            ? "(no comment)" : matchFeedback.getCustomerComment();
                    reviewedModel.addRow(new Object[]{
                        a.getAppointmentId(), a.getServiceType(), date,
                        staffStars, serviceStars, comment
                    });
                } else {
                    pendingModel.addRow(new Object[]{a.getAppointmentId(), a.getServiceType(), date});
                }
            }
        };
        doFilter.run();
        searchBtn.addActionListener(e -> doFilter.run());
        searchField.addActionListener(e -> doFilter.run());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Pending (" + pendingModel.getRowCount() + ")",
                buildPendingTab(pendingModel));
        tabs.addTab("Done (" + reviewedModel.getRowCount() + ")",
                buildDoneTab(reviewedModel));

        JPanel center = new JPanel(new BorderLayout());
        center.add(notice, BorderLayout.NORTH);
        center.add(tabs, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        if (preSelectedApptId != null)
            SwingUtilities.invokeLater(() -> showRateDialog(preSelectedApptId));

        return p;
    }

    // Pending tab — Rate & Comment button
    private JPanel buildPendingTab(DefaultTableModel model) {
        JPanel tab = new JPanel(new BorderLayout());
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel barTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton rateBtn = new JButton("Rate & Comment");
        barTop.add(rateBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.add(barTop, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        tab.add(center, BorderLayout.CENTER);

        rateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            String apptId = (String) model.getValueAt(row, 0);
            showRateDialog(apptId);
            rebuildRatePanel(null);
            cardLayout.show(contentPanel, "rate");
        });

        return tab;
    }

    // Done tab — shows Rating + Comment, wide comment column, wrap text
    private JPanel buildDoneTab(DefaultTableModel model) {
        JPanel tab = new JPanel(new BorderLayout());

        JTable table = new JTable(model) {
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        table.setRowHeight(52);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths: narrow ID/type/date/rating, wide comment
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(400);

        // Wrap text renderer for Comment column
        table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            JTextArea area = new JTextArea(value == null ? "" : value.toString());
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(tbl.getFont());
            area.setBorder(new EmptyBorder(4, 4, 4, 4));
            area.setBackground(isSelected ? tbl.getSelectionBackground() : tbl.getBackground());
            area.setForeground(isSelected ? tbl.getSelectionForeground() : tbl.getForeground());
            return area;
        });

        tab.add(new JScrollPane(table), BorderLayout.CENTER);
        return tab;
    }

    private void rebuildRatePanel(String preSelectedApptId) {
        contentPanel.remove(contentPanel.getComponent(4));
        contentPanel.add(buildRatePanel(preSelectedApptId), "rate", 4);
        contentPanel.revalidate();
    }

    private void showRateDialog(String apptId) {
        JDialog d = new JDialog(this, "Rate & Comment", true);
        d.setSize(480, 380);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 28, 16, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0;

        c.gridy = 0; form.add(new JLabel("Appointment ID"), c);
        c.gridy = 1;
        JTextField apptField = new JTextField(apptId);
        apptField.setEditable(false);
        apptField.setBackground(new Color(240, 240, 240));
        form.add(apptField, c);

        c.gridy = 2; form.add(new JLabel("Staff Rating"), c);
        c.gridy = 3;
        JPanel staffStars = makeStarPanel();
        form.add(staffStars, c);

        c.gridy = 4; form.add(new JLabel("Service Rating"), c);
        c.gridy = 5;
        JPanel serviceStars = makeStarPanel();
        form.add(serviceStars, c);

        c.gridy = 6; form.add(new JLabel("Comment (optional)"), c);
        c.gridy = 7;
        JTextArea commentArea = new JTextArea(3, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        form.add(new JScrollPane(commentArea), c);

        c.gridy = 8;
        JLabel noticeLabel = new JLabel("⚠  Once submitted, this cannot be changed.");
        noticeLabel.setForeground(new Color(160, 80, 0));
        noticeLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        form.add(noticeLabel, c);

        d.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton submitBtn = new JButton("Submit");
        south.add(cancelBtn);
        south.add(submitBtn);
        d.add(south, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> d.dispose());
        submitBtn.addActionListener(e -> {
            int staffRating   = getStarRating(staffStars);
            int serviceRating = getStarRating(serviceStars);
            if (staffRating == 0 || serviceRating == 0) {
                JOptionPane.showMessageDialog(d,
                        "Please select a rating for both Staff and Service.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String comment = commentArea.getText().trim();
            List<Feedback> all = feedbackService.getAllFeedbacks();
            Feedback existing = null;
            for (Feedback f : all)
                if (f.getAppointmentId().equals(apptId)) { existing = f; break; }
            if (existing != null) {
                feedbackService.addCustomerReview(existing.getFeedbackId(), comment, serviceRating, staffRating);
            } else {
                String feedbackId = idService.generateFeedbackId();
                feedbackService.createCustomerReview(feedbackId, currentUser.getUserId(),
                        apptId, comment, serviceRating, staffRating);
            }
            JOptionPane.showMessageDialog(d, "Thank you for your feedback!");
            d.dispose();
        });

        d.setVisible(true);
    }

    private JPanel makeStarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        JLabel[] stars = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            int rating = i + 1;
            stars[i] = new JLabel("☆");
            stars[i].setFont(new Font("Arial", Font.PLAIN, 22));
            stars[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            stars[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (int j = 0; j < 5; j++) {
                        stars[j].setText(j < rating ? "★" : "☆");
                        stars[j].setForeground(j < rating ? new Color(220, 160, 0) : Color.GRAY);
                    }
                    panel.putClientProperty("rating", rating);
                }
            });
            stars[i].setForeground(Color.GRAY);
            panel.add(stars[i]);
        }
        panel.putClientProperty("rating", 0);
        return panel;
    }

    private int getStarRating(JPanel starPanel) {
        Object val = starPanel.getClientProperty("rating");
        return val instanceof Integer ? (Integer) val : 0;
    }

    private DefaultTableModel makeReadOnlyModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}