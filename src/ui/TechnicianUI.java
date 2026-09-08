package ui;

import core.AppContext;
import model.Appointment;
import model.Feedback;
import model.Schedule;
import model.User;
import service.AppointmentService;
import service.FeedbackService;
import service.IdGeneratorService;
import service.ScheduleService;
import service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

public class TechnicianUI extends JFrame {

    private final UserService        userService;
    private final AppointmentService appointmentService;
    private final FeedbackService    feedbackService;
    private final ScheduleService    scheduleService;
    private final IdGeneratorService idService;
    private final User               currentUser;

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 16);

    private JPanel            contentPanel;
    private CardLayout        cardLayout;
    private DefaultTableModel feedbackTableModel;

    public TechnicianUI(User currentUser) {
        AppContext ctx          = AppContext.getInstance();
        this.userService        = ctx.getUserService();
        this.appointmentService = ctx.getAppointmentService();
        this.feedbackService    = ctx.getFeedbackService();
        this.scheduleService    = ctx.getScheduleService();
        this.idService          = ctx.getIdGeneratorService();
        this.currentUser        = currentUser;

        setTitle("Technician Dashboard – APU ASC");
        setSize(960, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel roleLabel = new JLabel("Technician");
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
        contentPanel.add(buildMyApptPanel(),   "myappt");
        contentPanel.add(buildSchedulePanel(), "schedule");
        contentPanel.add(buildFeedbackPanel(), "feedback");
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
            {"Edit Profile",    "profile"},
            {"My Appointments", "myappt"},
            {"My Schedule",     "schedule"},
            {"Give Feedback",   "feedback"}
        };
        for (String[] b : btns) {
            JButton btn = new JButton(b[0]);
            btn.setPreferredSize(new Dimension(280, 48));
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            String card = b[1];
            btn.addActionListener(e -> {
                if (card.equals("feedback")) refreshFeedbackTable(feedbackTableModel);
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

    // ── My Appointments ───────────────────────────────────────────
    private JPanel buildMyApptPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        // dashHolder allows the dashboard to be rebuilt in-place
        JPanel[] dashHolder = {buildTodayDashboard()};
        p.add(dashHolder[0], BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JTextField searchField = new JTextField(18);
        JButton searchBtn = new JButton("Search");
        JButton backBtn   = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        searchBar.add(backBtn);
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);

        String[] cols = {"Appt ID", "Customer ID", "Customer Name", "Car Plate", "Type", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Runnable doSearch = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            model.setRowCount(0);
            for (Appointment a : appointmentService.getAllAppointments()) {
                if (!currentUser.getUserId().equals(a.getTechnicianId())) continue;
                String custName = "";
                User cust = userService.getUserById(a.getCustomerId());
                if (cust != null) custName = cust.getName();
                String dt   = a.getDateTime();
                String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
                String time = dt.contains(" ") ? dt.split(" ")[1] : "-";
                if (!keyword.isEmpty()
                        && !a.getAppointmentId().toLowerCase().contains(keyword)
                        && !a.getCustomerId().toLowerCase().contains(keyword)
                        && !custName.toLowerCase().contains(keyword)
                        && !date.contains(keyword)) continue;
                model.addRow(new Object[]{
                    a.getAppointmentId(), a.getCustomerId(), custName,
                    a.getCarPlate().isEmpty() ? "-" : a.getCarPlate(),
                    a.getServiceType(), date, time, a.getStatus()
                });
            }
        };
        doSearch.run();
        searchBtn.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run());

        JPanel tableTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton detailBtn = new JButton("Detail");
        tableTopBar.add(detailBtn);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(searchBar, BorderLayout.NORTH);
        tableArea.add(tableTopBar, BorderLayout.CENTER);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.add(tableArea, BorderLayout.NORTH);
        centerArea.add(tableWrap, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        JButton completeBtn = new JButton("✔  Mark as Complete");
        completeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        completeBtn.setPreferredSize(new Dimension(260, 44));
        completeBtn.setBackground(new Color(60, 160, 80));
        completeBtn.setForeground(Color.WHITE);
        completeBtn.setOpaque(true);
        completeBtn.setFocusPainted(false);
        bottomBar.add(completeBtn);

        p.add(centerArea, BorderLayout.CENTER);
        p.add(bottomBar, BorderLayout.SOUTH);

        detailBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            String apptId = (String) model.getValueAt(row, 0);
            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (appt != null) showMyApptDetail(appt);
        });

        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            String apptId = (String) model.getValueAt(row, 0);
            String status = (String) model.getValueAt(row, 7);

            if ("COMPLETED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This appointment is already completed.");
                return;
            }

            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (appt == null) return;

            appointmentService.updateAppointment(
                    apptId, appt.getCustomerId(), appt.getTechnicianId(),
                    appt.getServiceType(), appt.getDateTime(), "COMPLETED");
            JOptionPane.showMessageDialog(this, "Appointment " + apptId + " marked as completed.");

            // Refresh table
            doSearch.run();

            // Rebuild dashboard in-place
            p.remove(dashHolder[0]);
            dashHolder[0] = buildTodayDashboard();
            p.add(dashHolder[0], BorderLayout.NORTH);
            p.revalidate();
            p.repaint();

            int choice = JOptionPane.showConfirmDialog(this,
                    "Would you like to write feedback now?",
                    "Give Feedback", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) showCreateFeedbackDialog(apptId);
        });

        return p;
    }

    // ── Today's Dashboard ─────────────────────────────────────────
    private JPanel buildTodayDashboard() {
        String todayStr = TODAY.toString();

        List<Appointment> todayAppts = new ArrayList<>();
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (currentUser.getUserId().equals(a.getTechnicianId())
                    && a.getDateTime() != null
                    && a.getDateTime().startsWith(todayStr)) {
                todayAppts.add(a);
            }
        }

        long completed = todayAppts.stream().filter(Appointment::isCompleted).count();
        long remaining = todayAppts.stream().filter(a -> !a.isCompleted()).count();

        JPanel dashboard = new JPanel(new BorderLayout(0, 4));
        dashboard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 160, 220), 1, true),
                "Today  –  Saturday, " + todayStr,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), new Color(60, 100, 180)));

        JPanel cards = new JPanel(new GridLayout(1, 3, 8, 0));
        cards.setBorder(new EmptyBorder(6, 6, 6, 6));
        cards.add(makeDashCard("Today's Appointments", String.valueOf(todayAppts.size()), new Color(70, 130, 200)));
        cards.add(makeDashCard("Completed", String.valueOf(completed), new Color(60, 160, 80)));
        cards.add(makeDashCard("Remaining", String.valueOf(remaining), new Color(200, 100, 60)));
        dashboard.add(cards, BorderLayout.WEST);

        if (!todayAppts.isEmpty()) {
            JPanel listPanel = new JPanel(new GridLayout(todayAppts.size(), 1, 0, 2));
            listPanel.setBorder(new EmptyBorder(6, 8, 6, 6));
            for (Appointment a : todayAppts) {
                String time = a.getDateTime().contains(" ") ? a.getDateTime().split(" ")[1] : "-";
                String custName = "";
                User cust = userService.getUserById(a.getCustomerId());
                if (cust != null) custName = cust.getName();
                String statusIcon = a.isCompleted() ? "✔" : "◎";
                JLabel lbl = new JLabel(statusIcon + "  " + time + "  |  " + a.getServiceType()
                        + "  |  " + custName + "  (" + a.getCarPlate() + ")");
                lbl.setFont(new Font("Arial", Font.PLAIN, 12));
                lbl.setForeground(a.isCompleted() ? new Color(60, 160, 80) : Color.DARK_GRAY);
                listPanel.add(lbl);
            }
            dashboard.add(listPanel, BorderLayout.CENTER);
        } else {
            JLabel noAppt = new JLabel("  No appointments today.", SwingConstants.LEFT);
            noAppt.setFont(new Font("Arial", Font.ITALIC, 12));
            noAppt.setForeground(Color.GRAY);
            dashboard.add(noAppt, BorderLayout.CENTER);
        }

        return dashboard;
    }

    private JPanel makeDashCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 20));
        val.setForeground(color);
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private void showMyApptDetail(Appointment appt) {
        JDialog d = new JDialog(this, "Appointment Detail", true);
        d.setSize(480, 380);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 4, 5, 12);
        c.fill   = GridBagConstraints.HORIZONTAL;

        String custDisplay = appt.getCustomerId();
        User cust = userService.getUserById(appt.getCustomerId());
        if (cust != null) custDisplay += "  (" + cust.getName() + ")";

        String dt   = appt.getDateTime();
        String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
        String time = dt.contains(" ") ? dt.split(" ")[1] : "-";

        String ratingDisplay  = "(no rating yet)";
        String commentDisplay = "(no comment yet)";
        for (Feedback f : feedbackService.getAllFeedbacks()) {
            if (f.getAppointmentId().equals(appt.getAppointmentId())) {
                if (f.getRating() > 0)
                    ratingDisplay = "★".repeat(f.getRating()) + "  (" + f.getRating() + " / 5)";
                if (f.getCustomerComment() != null && !f.getCustomerComment().isEmpty())
                    commentDisplay = f.getCustomerComment();
                break;
            }
        }

        String[][] rows = {
            {"Appointment ID",   appt.getAppointmentId()},
            {"Customer",         custDisplay},
            {"Car Plate",        appt.getCarPlate().isEmpty() ? "-" : appt.getCarPlate()},
            {"Service Type",     appt.getServiceType()},
            {"Appointment Date", date},
            {"Appointment Time", time},
            {"Status",           appt.getStatus()},
            {"Customer Comment", commentDisplay},
            {"Customer Rating",  ratingDisplay}
        };

        for (int i = 0; i < rows.length; i++) {
            c.gridx = 0; c.gridy = i;
            JLabel lbl = new JLabel(rows[i][0] + ":");
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            form.add(lbl, c);
            c.gridx = 1;
            JLabel val = new JLabel(rows[i][1]);
            if (rows[i][0].equals("Customer Rating"))
                val.setForeground(new Color(200, 140, 0));
            form.add(val, c);
        }

        d.add(form, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(ev -> d.dispose());
        south.add(closeBtn);
        d.add(south, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── My Schedule ───────────────────────────────────────────────
    private JPanel buildSchedulePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        int[] nav = {TODAY.getYear(), TODAY.getMonthValue()};
        JLabel monthLbl = new JLabel("", SwingConstants.CENTER);
        monthLbl.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel navBar = new JPanel(new BorderLayout());
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        topRow.add(backBtn);
        navBar.add(topRow, BorderLayout.WEST);
        navBar.add(monthLbl, BorderLayout.CENTER);

        JPanel navBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton prevBtn = new JButton("◀");
        JButton nextBtn = new JButton("▶");
        navBtns.add(prevBtn);
        navBtns.add(nextBtn);
        navBar.add(navBtns, BorderLayout.EAST);
        p.add(navBar, BorderLayout.NORTH);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        legend.add(makeLegendBox(new Color(180, 220, 180), "Work Day"));
        legend.add(makeLegendBox(new Color(255, 180, 180), "Day-Off Request"));
        legend.add(makeLegendBox(Color.WHITE, "Not Scheduled"));
        p.add(legend, BorderLayout.SOUTH);

        JPanel calendarHolder = new JPanel(new BorderLayout());
        p.add(calendarHolder, BorderLayout.CENTER);

        Runnable[] refresh = {null};
        refresh[0] = () -> {
            String yearMonth = String.format("%04d-%02d", nav[0], nav[1]);
            monthLbl.setText(String.format("%04d / %02d", nav[0], nav[1]));
            calendarHolder.removeAll();
            calendarHolder.add(buildTechCalendar(yearMonth, refresh[0]), BorderLayout.CENTER);
            calendarHolder.revalidate();
            calendarHolder.repaint();
        };

        prevBtn.addActionListener(e -> { nav[1]--; if (nav[1] < 1) { nav[1] = 12; nav[0]--; } refresh[0].run(); });
        nextBtn.addActionListener(e -> { nav[1]++; if (nav[1] > 12) { nav[1] = 1; nav[0]++; } refresh[0].run(); });

        refresh[0].run();
        return p;
    }

    private JPanel buildTechCalendar(String yearMonth, Runnable refresh) {
        java.time.YearMonth ym = java.time.YearMonth.parse(yearMonth);
        int daysInMonth = ym.lengthOfMonth();
        int firstDow    = ym.atDay(1).getDayOfWeek().getValue();

        List<Schedule> mySchedules = scheduleService.getMySchedules(currentUser.getUserId());
        Map<String, String> dateTypeMap = new HashMap<>();
        for (Schedule s : mySchedules)
            if (s.getDate().startsWith(yearMonth)) dateTypeMap.put(s.getDate(), s.getType());

        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setBorder(new EmptyBorder(4, 4, 4, 4));

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String dn : dayNames) {
            JLabel lbl = new JLabel(dn, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));
            grid.add(lbl);
        }

        for (int i = 1; i < firstDow; i++) grid.add(new JLabel(""));

        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = String.format("%s-%02d", yearMonth, day);
            String type    = dateTypeMap.getOrDefault(dateStr, "NONE");

            JButton cell = new JButton(String.valueOf(day));
            cell.setFont(new Font("Arial", Font.PLAIN, 12));
            cell.setMargin(new Insets(2, 2, 2, 2));
            cell.setFocusPainted(false);

            if (dateStr.equals(TODAY.toString())) {
                cell.setFont(new Font("Arial", Font.BOLD, 12));
                cell.setBorder(BorderFactory.createLineBorder(new Color(60, 100, 200), 2));
            }

            if (Schedule.WORK.equals(type)) {
                cell.setBackground(new Color(180, 220, 180));
            } else if (Schedule.DAYOFF_REQUEST.equals(type)) {
                cell.setBackground(new Color(255, 180, 180));
            } else {
                cell.setBackground(Color.WHITE);
            }

            final String ds = dateStr;
            final String tp = type;
            cell.addActionListener(e -> {
                if (Schedule.DAYOFF_REQUEST.equals(tp)) {
                    scheduleService.cancelDayOffRequest(currentUser.getUserId(), ds);
                    JOptionPane.showMessageDialog(this, "Day-off request cancelled for " + ds);
                } else if ("NONE".equals(tp)) {
                    String scId = idService.generateScheduleId();
                    scheduleService.requestDayOff(scId, currentUser.getUserId(), ds);
                    JOptionPane.showMessageDialog(this, "Day-off requested for " + ds);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "This day is already assigned by staff.",
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                }
                refresh.run();
            });

            grid.add(cell);
        }

        return grid;
    }

    private JPanel makeLegendBox(Color color, String label) {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JLabel colorBox = new JLabel("   ");
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        box.add(colorBox);
        box.add(new JLabel(label));
        return box;
    }

    // ── Give Feedback ─────────────────────────────────────────────
    private JPanel buildFeedbackPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Customer ID", "Customer Name", "Type", "Date", "Status", "Feedback"};
        feedbackTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(feedbackTableModel);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshFeedbackTable(feedbackTableModel);

        JPanel tableTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton createBtn = new JButton("Create Feedback");
        tableTopBar.add(createBtn);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(tableTopBar, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(tableArea, BorderLayout.CENTER);

        createBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
           
            String apptId = (String) feedbackTableModel.getValueAt(row, 0);
            showCreateFeedbackDialog(apptId);
            refreshFeedbackTable(feedbackTableModel);
        });

        return p;
    }

    private void showCreateFeedbackDialog(String apptId) {
        JDialog d = new JDialog(this, "Create Feedback", true);
        d.setSize(460, 340);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
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

        c.gridy = 2; form.add(new JLabel("Template (optional)"), c);
        c.gridy = 3;
        String[] templates = {
            "— Select a template —",
            "No issues found. Routine service completed successfully.",
            "Parts replaced. Vehicle is in good working condition.",
            "Minor adjustments made. No further action required.",
            "Additional repair recommended. Please advise customer to rebook.",
            "Vehicle inspected. Customer advised on maintenance schedule."
        };
        JComboBox<String> templateBox = new JComboBox<>(templates);
        form.add(templateBox, c);

        c.gridy = 4; form.add(new JLabel("Technician Feedback"), c);
        c.gridy = 5;
        JTextArea feedbackArea = new JTextArea(4, 20);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        form.add(new JScrollPane(feedbackArea), c);

        templateBox.addActionListener(ev -> {
            int idx = templateBox.getSelectedIndex();
            if (idx > 0) feedbackArea.setText((String) templateBox.getSelectedItem());
        });

        d.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton submitBtn = new JButton("Submit");
        south.add(cancelBtn);
        south.add(submitBtn);
        d.add(south, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> d.dispose());
        submitBtn.addActionListener(e -> {
            String feedbackText = feedbackArea.getText().trim();
            if (feedbackText.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Feedback cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Feedback existing = null;
            for (Feedback f : feedbackService.getAllFeedbacks())
                if (f.getAppointmentId().equals(apptId)) { existing = f; break; }

            if (existing != null) {
                feedbackService.addTechnicianFeedback(existing.getFeedbackId(), feedbackText);
            } else {
                String feedbackId = idService.generateFeedbackId();
                feedbackService.createFeedback(feedbackId, currentUser.getUserId(), apptId);
                feedbackService.addTechnicianFeedback(feedbackId, feedbackText);
            }
            JOptionPane.showMessageDialog(d, "Feedback submitted.");
            d.dispose();
        });

        d.setVisible(true);
    }

    private void refreshFeedbackTable(DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (!currentUser.getUserId().equals(a.getTechnicianId())) continue;
            String custName = "";
            User cust = userService.getUserById(a.getCustomerId());
            if (cust != null) custName = cust.getName();
            String dt   = a.getDateTime();
            String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
            boolean hasFeedback = false;
            for (Feedback f : allFeedbacks) {
                if (f.getAppointmentId().equals(a.getAppointmentId())
                        && f.getTechnicianFeedback() != null
                        && !f.getTechnicianFeedback().isEmpty()) {
                    hasFeedback = true; break;
                }
            }
            model.addRow(new Object[]{
                a.getAppointmentId(), a.getCustomerId(), custName,
                a.getServiceType(), date, a.getStatus(),
                hasFeedback ? "✔ Done" : "— Pending"
            });
        }
    }
}