package ui;

import core.AppContext;
import model.Appointment;
import model.Payment;
import model.Schedule;
import model.User;
import service.AppointmentService;
import service.IdGeneratorService;
import service.PaymentService;
import service.ScheduleService;
import service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class StaffUI extends JFrame {

    private final UserService        userService;
    private final AppointmentService appointmentService;
    private final PaymentService     paymentService;
    private final IdGeneratorService idService;
    private final ScheduleService    scheduleService;
    private final User               currentUser;

    private JPanel     contentPanel;
    private CardLayout cardLayout;

    public StaffUI(User currentUser) {
        AppContext ctx          = AppContext.getInstance();
        this.userService        = ctx.getUserService();
        this.appointmentService = ctx.getAppointmentService();
        this.paymentService     = ctx.getPaymentService();
        this.idService          = ctx.getIdGeneratorService();
        this.scheduleService    = ctx.getScheduleService();
        this.currentUser        = currentUser;

        setTitle("Counter Staff Dashboard – APU ASC");
        setSize(980, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel roleLabel = new JLabel("Counter Staff");
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
        contentPanel.add(buildHomePanel(),        "home");
        contentPanel.add(buildCustomerPanel(),    "customer");
        contentPanel.add(buildAppointmentPanel(), "appointment");
        contentPanel.add(buildPaymentPanel(),     "payment");
        contentPanel.add(buildShiftViewPanel(),   "shiftview");
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
            {"Edit Profile",           "profile"},
            {"Manage Customers",       "customer"},
            {"Appointment & Dispatch", "appointment"},
            {"Collect Payment",        "payment"},
            {"Technician Schedule",    "shiftview"}
        };
        for (String[] b : btns) {
            JButton btn = new JButton(b[0]);
            btn.setPreferredSize(new Dimension(280, 48));
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            String card = b[1];
            btn.addActionListener(e -> {
                if (card.equals("profile")) showEditProfileDialog();
                else cardLayout.show(contentPanel, card);
            });
            p.add(btn, c);
            c.gridy++;
        }
        return p;
    }

    // ── Edit Profile ──────────────────────────────────────────────
    private void showEditProfileDialog() {
        JDialog d = new JDialog(this, "Edit Profile", true);
        d.setSize(400, 320);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 40, 20, 40));
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

        d.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn   = new JButton("Save Changes");
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
            String finalPass = pass.isEmpty() ? currentUser.getPassword() : pass;
            try {
                userService.updateUser(currentUser.getUserId(), name, email, finalPass);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentUser.setName(name);
            currentUser.setEmail(email);
            if (!pass.isEmpty()) currentUser.setPassword(finalPass);
            JOptionPane.showMessageDialog(d, "Profile updated successfully.");
            d.dispose();
        });

        d.setVisible(true);
    }

    // ── Manage Customers ──────────────────────────────────────────
    private JPanel buildCustomerPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JTextField searchField = new JTextField(18);
        JButton searchBtn = new JButton("Search");
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(top, BorderLayout.WEST);
        northPanel.add(searchBar, BorderLayout.CENTER);
        p.add(northPanel, BorderLayout.NORTH);

        String[] cols = {"User ID", "Name", "Email", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshCustomerTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton addBtn  = new JButton("+ Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn  = new JButton("Delete");
        botPanel.add(addBtn);
        botPanel.add(editBtn);
        botPanel.add(delBtn);
        p.add(botPanel, BorderLayout.SOUTH);

        Runnable doSearch = () -> {
            String keyword = searchField.getText().trim().toLowerCase();
            model.setRowCount(0);
            for (User u : userService.getUsersByRole("Customer")) {
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
            String name  = JOptionPane.showInputDialog(this, "Name:");     if (name == null) return;
            String email = JOptionPane.showInputDialog(this, "Email:");    if (email == null) return;
            String pass  = JOptionPane.showInputDialog(this, "Password:"); if (pass == null) return;
            try {
                userService.registerUser(name, email, pass, "Customer");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            doSearch.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            String userId = (String) model.getValueAt(row, 0);
            User u = userService.getUserById(userId);
            if (u == null) return;
            showCustomerEditDialog(u, doSearch);
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            String userId = (String) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete " + userId + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                userService.deleteUser(userId);
                doSearch.run();
            }
        });

        return p;
    }

    private void showCustomerEditDialog(User u, Runnable refresh) {
        JDialog d = new JDialog(this, "Edit Customer", true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(6, 4, 6, 4);
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
        JButton saveBtn   = new JButton("Save Changes");
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
                userService.updateUser(u.getUserId(), name, email, finalPass);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(d, "Customer updated.");
            d.dispose();
            refresh.run();
        });

        d.setVisible(true);
    }

    private void refreshCustomerTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (User u : userService.getUsersByRole("Customer"))
            model.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getRole()});
    }

    // ── Appointment & Dispatch ────────────────────────────────────
    private JPanel buildAppointmentPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton backBtn   = new JButton("← Back");
        JButton newBtn    = new JButton("+ New Appointment");
        JButton assignBtn = new JButton("Assign Technician");
        JButton editBtn   = new JButton("Edit Appointment");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        top.add(newBtn);
        top.add(assignBtn);
        top.add(editBtn);
        p.add(top, BorderLayout.NORTH);

        // cols: 0=ApptID 1=CustID 2=CustName 3=CarPlate 4=TechID 5=Type 6=Date 7=Time 8=Status
        String[] cols = {"Appt ID", "Customer ID", "Customer Name", "Car Plate", "Technician ID", "Type", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // PENDING rows highlighted red
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                int modelRow = convertRowIndexToModel(row);
                String status = (String) model.getValueAt(modelRow, 8);
                if (!isRowSelected(row) && "PENDING".equalsIgnoreCase(status)) {
                    comp.setBackground(new Color(255, 200, 200));
                } else if (!isRowSelected(row)) {
                    comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        };
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Default sort by Date (col 6) ascending
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(6, SortOrder.ASCENDING)));

        refreshAppointmentTable(model);

        JPanel tableTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton detailBtn = new JButton("Detail");
        tableTopBar.add(detailBtn);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(tableTopBar, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(tableArea, BorderLayout.CENTER);

        // ── New Appointment ───────────────────────────────────────
        newBtn.addActionListener(e -> {
            String custId = JOptionPane.showInputDialog(this, "Customer ID:");
            if (custId == null) return;
            User customer = userService.getUserById(custId.trim());
            if (customer == null || !customer.getRole().equalsIgnoreCase("Customer")) {
                JOptionPane.showMessageDialog(this,
                        "Customer ID \"" + custId.trim() + "\" does not exist.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String carPlate = JOptionPane.showInputDialog(this, "Car Plate Number:");
            if (carPlate == null) return;
            String[] types = {"Normal", "Major"};
            String type = (String) JOptionPane.showInputDialog(this, "Service Type:", "New Appointment",
                    JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
            if (type == null) return;
            JSpinner datePicker = makeDateSpinner();
            int dateResult = JOptionPane.showConfirmDialog(this, datePicker,
                    "Select Appointment Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (dateResult != JOptionPane.OK_OPTION) return;
            String date = getDateFromSpinner(datePicker);
            String[] times = buildTimeSlots();
            String time = (String) JOptionPane.showInputDialog(this, "Select Appointment Time:",
                    "New Appointment", JOptionPane.PLAIN_MESSAGE, null, times, times[0]);
            if (time == null) return;

            String apptId = idService.generateAppointmentId();
            appointmentService.createAppointment(apptId, custId.trim(), type, date + " " + time, carPlate.trim());

            List<User> techs = userService.getUsersByRole("Technician");
            int durationHours = type.equalsIgnoreCase("Major") ? 3 : 1;
            List<User> available = scheduleService.getAvailableTechnicians(
                    date, time, durationHours, techs, appointmentService.getAllAppointments());

            StringBuilder sb = new StringBuilder("Appointment " + apptId + " created.\n\n");
            if (available.isEmpty()) {
                sb.append("No technicians available for this slot.\nPlease assign manually later.");
            } else {
                sb.append("Available Technicians:\n");
                for (User t : available)
                    sb.append("  ").append(t.getUserId()).append(" – ").append(t.getName()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
            refreshAppointmentTable(model);
        });

        // ── Assign Technician ─────────────────────────────────────
        assignBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = table.convertRowIndexToModel(row);
            String apptId = (String) model.getValueAt(modelRow, 0);
            String status = (String) model.getValueAt(modelRow, 8);
            if (!"PENDING".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Only PENDING appointments can be assigned.");
                return;
            }
            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (appt == null) return;
            String date = appt.getDateTime().contains(" ") ? appt.getDateTime().split(" ")[0] : appt.getDateTime();
            String time = appt.getDateTime().contains(" ") ? appt.getDateTime().split(" ")[1] : "09:00";
            List<User> allTechs = userService.getUsersByRole("Technician");
            List<User> available = scheduleService.getAvailableTechnicians(
                    date, time, appt.getDurationHours(), allTechs, appointmentService.getAllAppointments());
            List<User> options = available.isEmpty() ? allTechs : available;
            String[] dropdownOptions = new String[options.size()];
            for (int i = 0; i < options.size(); i++)
                dropdownOptions[i] = options.get(i).getUserId() + " – " + options.get(i).getName();
            if (dropdownOptions.length == 0) { JOptionPane.showMessageDialog(this, "No technicians found."); return; }
            String label = available.isEmpty() ? "No scheduled technicians found. Showing all:" : "Select Available Technician:";
            String assignChoice = (String) JOptionPane.showInputDialog(this,
                    label, "Assign Technician", JOptionPane.PLAIN_MESSAGE, null, dropdownOptions, dropdownOptions[0]);
            if (assignChoice == null) return;
            appointmentService.assignTechnician(apptId, assignChoice.split(" – ")[0]);
            JOptionPane.showMessageDialog(this, "Assigned: " + assignChoice + " → " + apptId);
            refreshAppointmentTable(model);
        });

        // ── Edit Appointment ──────────────────────────────────────
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = table.convertRowIndexToModel(row);
            String apptId = (String) model.getValueAt(modelRow, 0);
            String status = (String) model.getValueAt(modelRow, 8);
            if ("COMPLETED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this,
                        "Completed appointments cannot be edited.", "Locked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (appt == null) return;
            String existingDt   = appt.getDateTime();
            String existingDate = existingDt.contains(" ") ? existingDt.split(" ")[0] : existingDt;
            String existingTime = existingDt.contains(" ") ? existingDt.split(" ")[1] : "09:00";
            String[] fields = {"Date", "Time", "Service Type", "Car Plate", "Technician"};
            String editChoice = (String) JOptionPane.showInputDialog(this,
                    "Select field to edit:", "Edit Appointment",
                    JOptionPane.PLAIN_MESSAGE, null, fields, fields[0]);
            if (editChoice == null) return;
            String newDate     = existingDate;
            String newTime     = existingTime;
            String newType     = appt.getServiceType();
            String newCarPlate = appt.getCarPlate();
            String newTechId   = appt.getTechnicianId();
            switch (editChoice) {
                case "Date":
                    JSpinner datePicker = makeDateSpinner();
                    int res = JOptionPane.showConfirmDialog(this, datePicker,
                            "Select New Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (res != JOptionPane.OK_OPTION) return;
                    newDate = getDateFromSpinner(datePicker);
                    break;
                case "Time":
                    String[] times = buildTimeSlots();
                    String pickedTime = (String) JOptionPane.showInputDialog(this, "Select New Time:",
                            "Edit Time", JOptionPane.PLAIN_MESSAGE, null, times, existingTime);
                    if (pickedTime == null) return;
                    newTime = pickedTime;
                    break;
                case "Service Type":
                    String[] types = {"Normal", "Major"};
                    String pickedType = (String) JOptionPane.showInputDialog(this, "New Service Type:",
                            "Edit Appointment", JOptionPane.PLAIN_MESSAGE, null, types, appt.getServiceType());
                    if (pickedType == null) return;
                    newType = pickedType;
                    break;
                case "Car Plate":
                    String pickedPlate = JOptionPane.showInputDialog(this, "New Car Plate:", appt.getCarPlate());
                    if (pickedPlate == null) return;
                    newCarPlate = pickedPlate.trim();
                    break;
                case "Technician": {
                    List<User> allTechs = userService.getUsersByRole("Technician");
                    List<User> available = scheduleService.getAvailableTechnicians(
                            newDate, newTime, appt.getDurationHours(), allTechs, appointmentService.getAllAppointments());
                    List<User> options = available.isEmpty() ? allTechs : available;
                    String[] techOptions = new String[options.size()];
                    for (int i = 0; i < options.size(); i++)
                        techOptions[i] = options.get(i).getUserId() + " – " + options.get(i).getName();
                    if (techOptions.length == 0) { JOptionPane.showMessageDialog(this, "No technicians found."); return; }
                    String label = available.isEmpty() ? "No scheduled technicians. Showing all:"
                            : "Available Technicians for " + newDate + " " + newTime + ":";
                    String techChoice = (String) JOptionPane.showInputDialog(this,
                            label, "Select Technician", JOptionPane.PLAIN_MESSAGE, null, techOptions, techOptions[0]);
                    if (techChoice == null) return;
                    newTechId = techChoice.split(" – ")[0];
                    break;
                }
            }
            appointmentService.updateAppointment(
                    apptId, appt.getCustomerId(), newTechId,
                    newType, newDate + " " + newTime, appt.getStatus());
            Appointment updated = appointmentService.getAppointmentById(apptId);
            if (updated != null) {
                updated.setCarPlate(newCarPlate);
                appointmentService.updateAppointment(
                        apptId, updated.getCustomerId(), updated.getTechnicianId(),
                        updated.getServiceType(), updated.getDateTime(), updated.getStatus());
            }
            JOptionPane.showMessageDialog(this, "Appointment updated.");
            refreshAppointmentTable(model);
        });

        // ── Detail ────────────────────────────────────────────────
        detailBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = table.convertRowIndexToModel(row);
            String apptId = (String) model.getValueAt(modelRow, 0);
            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (appt != null) showAppointmentDetail(appt);
        });

        return p;
    }

    private void showAppointmentDetail(Appointment appt) {
        JDialog d = new JDialog(this, "Appointment Detail", true);
        d.setSize(460, 340);
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

        String techDisplay = "(not assigned)";
        if (appt.getTechnicianId() != null && !appt.getTechnicianId().isEmpty()) {
            techDisplay = appt.getTechnicianId();
            User tech = userService.getUserById(appt.getTechnicianId());
            if (tech != null) techDisplay += "  (" + tech.getName() + ")";
        }

        String dt   = appt.getDateTime();
        String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
        String time = dt.contains(" ") ? dt.split(" ")[1] : "-";

        String[][] rows = {
            {"Appointment ID",   appt.getAppointmentId()},
            {"Customer",         custDisplay},
            {"Car Plate",        appt.getCarPlate().isEmpty() ? "-" : appt.getCarPlate()},
            {"Technician",       techDisplay},
            {"Service Type",     appt.getServiceType()},
            {"Appointment Date", date},
            {"Appointment Time", time},
            {"Status",           appt.getStatus()}
        };

        for (int i = 0; i < rows.length; i++) {
            c.gridx = 0; c.gridy = i;
            JLabel lbl = new JLabel(rows[i][0] + ":");
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            form.add(lbl, c);
            c.gridx = 1;
            form.add(new JLabel(rows[i][1]), c);
        }

        d.add(form, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(ev -> d.dispose());
        south.add(closeBtn);
        d.add(south, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void refreshAppointmentTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Appointment a : appointmentService.getAllAppointments()) {
            String custName = "";
            User cust = userService.getUserById(a.getCustomerId());
            if (cust != null) custName = cust.getName();
            String dt   = a.getDateTime();
            String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
            String time = dt.contains(" ") ? dt.split(" ")[1] : "-";
            model.addRow(new Object[]{
                a.getAppointmentId(), a.getCustomerId(), custName,
                a.getCarPlate().isEmpty() ? "-" : a.getCarPlate(),
                (a.getTechnicianId() == null || a.getTechnicianId().isEmpty()) ? "-" : a.getTechnicianId(),
                a.getServiceType(), date, time, a.getStatus()
            });
        }
    }

    // ── Technician Schedule View ──────────────────────────────────
    private JPanel buildShiftViewPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        int[] nav = {LocalDate.now().getYear(), LocalDate.now().getMonthValue()};
        JLabel monthLbl = new JLabel("", SwingConstants.CENTER);
        monthLbl.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel navBar = new JPanel(new BorderLayout());
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton backBtn   = new JButton("← Back");
        JButton assignBtn = new JButton("Auto-Assign Shifts");
        JButton clearBtn  = new JButton("Clear Shifts");
        clearBtn.setForeground(new Color(160, 40, 40));
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        topRow.add(backBtn);
        topRow.add(assignBtn);
        topRow.add(clearBtn);
        navBar.add(topRow, BorderLayout.WEST);
        navBar.add(monthLbl, BorderLayout.CENTER);

        JPanel navBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton prevBtn = new JButton("◀");
        JButton nextBtn = new JButton("▶");
        navBtns.add(prevBtn);
        navBtns.add(nextBtn);
        navBar.add(navBtns, BorderLayout.EAST);
        p.add(navBar, BorderLayout.NORTH);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        legend.add(makeLegendBox(new Color(180, 220, 180), "3 workers"));
        legend.add(makeLegendBox(new Color(255, 230, 150), "1–2 workers"));
        legend.add(makeLegendBox(new Color(255, 160, 160), "Shortage / conflict"));
        legend.add(makeLegendBox(Color.WHITE, "No shifts"));
        legend.add(new JLabel("  ★ Click a date cell to edit"));
        p.add(legend, BorderLayout.SOUTH);

        JPanel calHolder = new JPanel(new BorderLayout());
        p.add(calHolder, BorderLayout.CENTER);

        List<User> technicians = userService.getUsersByRole("Technician");

        Runnable[] refresh = {null};
        refresh[0] = () -> {
            String yearMonth = String.format("%04d-%02d", nav[0], nav[1]);
            monthLbl.setText(String.format("%04d / %02d", nav[0], nav[1]));
            calHolder.removeAll();
            calHolder.add(buildStaffCalendar(yearMonth, technicians, refresh[0]), BorderLayout.CENTER);
            calHolder.revalidate();
            calHolder.repaint();
        };

        prevBtn.addActionListener(e -> { nav[1]--; if (nav[1] < 1)  { nav[1] = 12; nav[0]--; } refresh[0].run(); });
        nextBtn.addActionListener(e -> { nav[1]++; if (nav[1] > 12) { nav[1] = 1;  nav[0]++; } refresh[0].run(); });

        assignBtn.addActionListener(e -> {
            String yearMonth = String.format("%04d-%02d", nav[0], nav[1]);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Auto-assign shifts for " + yearMonth + "?\nExisting WORK shifts will be replaced.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            scheduleService.assignMonthlyShifts(yearMonth, technicians);
            Set<String> shortfall = scheduleService.getShortfallDates(yearMonth);
            if (!shortfall.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Shifts assigned. " + shortfall.size() + " day(s) have fewer than 3 workers (highlighted in red).",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Shifts assigned successfully.");
            }
            refresh[0].run();
        });

        clearBtn.addActionListener(e -> {
            String yearMonth = String.format("%04d-%02d", nav[0], nav[1]);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Clear all WORK shifts for " + yearMonth + "?\nDay-off requests will be kept.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            List<Schedule> monthSchedules = scheduleService.getSchedulesByMonth(yearMonth);
            for (Schedule s : monthSchedules)
                if (s.isWork()) scheduleService.deleteSchedule(s.getScheduleId());
            JOptionPane.showMessageDialog(this, "Work shifts cleared for " + yearMonth + ".");
            refresh[0].run();
        });

        refresh[0].run();
        return p;
    }

    private JPanel buildStaffCalendar(String yearMonth, List<User> technicians, Runnable refresh) {
        YearMonth ym          = YearMonth.parse(yearMonth);
        int       daysInMonth = ym.lengthOfMonth();
        int       firstDow    = ym.atDay(1).getDayOfWeek().getValue();

        List<Schedule> monthSchedules = scheduleService.getSchedulesByMonth(yearMonth);
        Set<String>    shortfall      = scheduleService.getShortfallDates(yearMonth);

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
            List<String> workers = new ArrayList<>();
            for (Schedule s : monthSchedules)
                if (s.getDate().equals(dateStr) && s.isWork()) workers.add(s.getTechnicianId());

            StringBuilder sb = new StringBuilder("<html><center><b>" + day + "</b><br>");
            for (String techId : workers) {
                User tech = userService.getUserById(techId);
                String name = tech != null ? tech.getName().split(" ")[0] : techId;
                sb.append(name).append("<br>");
            }
            sb.append("</center></html>");

            JButton cell = new JButton(sb.toString());
            cell.setFont(new Font("Arial", Font.PLAIN, 10));
            cell.setMargin(new Insets(2, 2, 2, 2));
            cell.setFocusPainted(false);
            cell.setHorizontalAlignment(SwingConstants.CENTER);
            cell.setVerticalAlignment(SwingConstants.TOP);
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (shortfall.contains(dateStr) && !workers.isEmpty()) {
                cell.setBackground(new Color(255, 160, 160));
            } else if (workers.isEmpty()) {
                cell.setBackground(Color.WHITE);
            } else if (workers.size() >= 3) {
                cell.setBackground(new Color(180, 220, 180));
            } else {
                cell.setBackground(new Color(255, 230, 150));
            }

            cell.addActionListener(e -> showShiftEditDialog(dateStr, technicians, refresh));
            grid.add(cell);
        }

        return grid;
    }

    private void showShiftEditDialog(String date, List<User> technicians, Runnable refresh) {
        JDialog d = new JDialog(this, "Edit Shift: " + date, true);
        d.setSize(320, 260);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        List<Schedule> daySchedules = scheduleService.getSchedulesByDate(date);
        Set<String> currentWorkers = new HashSet<>();
        for (Schedule s : daySchedules)
            if (s.isWork()) currentWorkers.add(s.getTechnicianId());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0; c.gridy = 0;

        JLabel info = new JLabel("Technicians working on " + date + ":");
        info.setFont(new Font("Arial", Font.BOLD, 12));
        form.add(info, c);
        c.gridy++;

        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (User tech : technicians) {
            JCheckBox cb = new JCheckBox(tech.getUserId() + " – " + tech.getName());
            cb.setSelected(currentWorkers.contains(tech.getUserId()));
            cb.putClientProperty("techId", tech.getUserId());
            checkBoxes.add(cb);
            form.add(cb, c);
            c.gridy++;
        }

        JLabel warnLabel = new JLabel("");
        warnLabel.setForeground(new Color(160, 40, 40));
        warnLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        form.add(warnLabel, c);

        for (JCheckBox cb : checkBoxes)
            cb.addItemListener(e -> warnLabel.setText(
                    checkBoxes.stream().filter(JCheckBox::isSelected).count() > 3
                            ? "⚠ Maximum 3 technicians per day" : ""));

        d.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn   = new JButton("Save");
        south.add(cancelBtn);
        south.add(saveBtn);
        d.add(south, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> d.dispose());
        saveBtn.addActionListener(e -> {
            if (checkBoxes.stream().filter(JCheckBox::isSelected).count() > 3) {
                JOptionPane.showMessageDialog(d, "Maximum 3 technicians per day.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Schedule s : daySchedules)
                if (s.isWork()) scheduleService.deleteSchedule(s.getScheduleId());
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    String techId = (String) cb.getClientProperty("techId");
                    String scId   = idService.generateScheduleId();
                    scheduleService.registerSchedule(scId, techId, date, "09:00", "18:00");
                }
            }
            d.dispose();
            refresh.run();
        });

        d.setVisible(true);
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

    // ── Collect Payment ───────────────────────────────────────────
    private JPanel buildPaymentPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton backBtn    = new JButton("← Back");
        JButton processBtn = new JButton("Process Payment");
        JButton receiptBtn = new JButton("Issue Receipt");
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        top.add(backBtn);
        top.add(processBtn);
        top.add(receiptBtn);
        p.add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setDividerSize(6);

        String[] unpaidCols = {"Appt ID", "Customer ID", "Car Plate", "Type", "Appt Date", "Appt Time", "Amount Due", "Status"};
        DefaultTableModel unpaidModel = new DefaultTableModel(unpaidCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable unpaidTable = new JTable(unpaidModel);
        unpaidTable.setRowHeight(28);
        unpaidTable.setShowGrid(true);
        unpaidTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel unpaidPanel = new JPanel(new BorderLayout());
        JLabel unpaidLabel = new JLabel("  Unpaid Appointments (Completed)", SwingConstants.LEFT);
        unpaidLabel.setFont(new Font("Arial", Font.BOLD, 12));
        unpaidLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        unpaidPanel.add(unpaidLabel, BorderLayout.NORTH);
        unpaidPanel.add(new JScrollPane(unpaidTable), BorderLayout.CENTER);

        String[] paidCols = {"Payment ID", "Appt ID", "Customer ID", "Amount", "Method", "Payment Date"};
        DefaultTableModel paidModel = new DefaultTableModel(paidCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable paidTable = new JTable(paidModel);
        paidTable.setRowHeight(28);
        paidTable.setShowGrid(true);
        paidTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel paidPanel = new JPanel(new BorderLayout());
        JLabel paidLabel = new JLabel("  Payment History", SwingConstants.LEFT);
        paidLabel.setFont(new Font("Arial", Font.BOLD, 12));
        paidLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        paidPanel.add(paidLabel, BorderLayout.NORTH);
        paidPanel.add(new JScrollPane(paidTable), BorderLayout.CENTER);

        split.setTopComponent(unpaidPanel);
        split.setBottomComponent(paidPanel);
        p.add(split, BorderLayout.CENTER);

        refreshUnpaidTable(unpaidModel);
        refreshPaidTable(paidModel);

        processBtn.addActionListener(e -> {
            int row = unpaidTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an unpaid appointment first."); return; }
            String apptId  = (String) unpaidModel.getValueAt(row, 0);
            String amtText = (String) unpaidModel.getValueAt(row, 6);
            double amount;
            try { amount = Double.parseDouble(amtText.replace("RM", "").trim()); }
            catch (NumberFormatException ex) { amount = 0.0; }
            String[] methods = {"Cash", "Card", "Online Transfer"};
            String method = (String) JOptionPane.showInputDialog(this,
                    "Payment Method:", "Process Payment",
                    JOptionPane.PLAIN_MESSAGE, null, methods, methods[0]);
            if (method == null) return;
            String paymentId = idService.generatePaymentId();
            paymentService.processPayment(paymentId, apptId, amount, LocalDate.now().toString(), method);
            JOptionPane.showMessageDialog(this,
                    "Payment processed.\nPayment ID: " + paymentId
                    + "\nAmount: RM " + String.format("%.2f", amount));
            refreshUnpaidTable(unpaidModel);
            refreshPaidTable(paidModel);
        });

        receiptBtn.addActionListener(e -> {
            int row = paidTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a payment from Payment History first."); return; }
            String paymentId = (String) paidModel.getValueAt(row, 0);
            String custId    = (String) paidModel.getValueAt(row, 2);
            String custName  = "";
            User cust = userService.getUserById(custId);
            if (cust != null) custName = cust.getName();
            String baseReceipt = paymentService.generateReceipt(paymentId);
            String receipt = baseReceipt.replace(
                    "Appointment  :",
                    "Customer     : " + custId + (custName.isEmpty() ? "" : " (" + custName + ")\nAppointment  :")
            );
            JTextArea area = new JTextArea(receipt);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Receipt", JOptionPane.INFORMATION_MESSAGE);
        });

        return p;
    }

    private void refreshUnpaidTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<Payment> allPayments = paymentService.getAllPayments();
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (!a.isCompleted()) continue;
            boolean paid = allPayments.stream()
                    .anyMatch(pay -> pay.getAppointmentId().equals(a.getAppointmentId()));
            if (paid) continue;
            String dt   = a.getDateTime();
            String date = dt.contains(" ") ? dt.split(" ")[0] : dt;
            String time = dt.contains(" ") ? dt.split(" ")[1] : "-";
            double amount = paymentService.getPriceForService(a.getServiceType());
            model.addRow(new Object[]{
                a.getAppointmentId(), a.getCustomerId(),
                a.getCarPlate().isEmpty() ? "-" : a.getCarPlate(),
                a.getServiceType(), date, time,
                "RM " + String.format("%.2f", amount), a.getStatus()
            });
        }
    }

    private void refreshPaidTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Payment pay : paymentService.getAllPayments()) {
            String custId = "";
            Appointment appt = appointmentService.getAppointmentById(pay.getAppointmentId());
            if (appt != null) custId = appt.getCustomerId();
            model.addRow(new Object[]{
                pay.getPaymentId(), pay.getAppointmentId(), custId,
                "RM " + String.format("%.2f", pay.getAmount()),
                pay.getPaymentMethod(), pay.getPaymentDate()
            });
        }
    }

    private JSpinner makeDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(160, 28));
        return spinner;
    }

    private String getDateFromSpinner(JSpinner spinner) {
        java.util.Date d = (java.util.Date) spinner.getValue();
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private String[] buildTimeSlots() {
        String[] slots = new String[10];
        for (int h = 9; h <= 18; h++)
            slots[h - 9] = String.format("%02d:00", h);
        return slots;
    }
}