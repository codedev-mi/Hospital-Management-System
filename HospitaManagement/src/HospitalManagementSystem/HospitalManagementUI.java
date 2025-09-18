package HospitalManagementSystem;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class HospitalManagementUI extends JFrame {
    private final Connection conn;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    // Dashboard labels (count)
    private JLabel lblPatientsCount;
    private JLabel lblDoctorsCount;
    private JLabel lblAppointmentsCount;

    // Table models
    private final DefaultTableModel patientModel = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel doctorModel = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel appointmentModel = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Date format
    private final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public HospitalManagementUI(Connection connection) {
        // Look and feel (optional)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}

        this.conn = connection;

        setTitle("Hospital Management System");
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Top header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createPatientsPanel(), "Patients");
        contentPanel.add(createDoctorsPanel(), "Doctors");
        contentPanel.add(createAppointmentsPanel(), "Appointments");
        add(contentPanel, BorderLayout.CENTER);

        refreshAllData();
    }

    // =========== Header ===========
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(250, 250, 252));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Hospital Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Comprehensive healthcare management");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        JLabel status = new JLabel("  System Online  ");
        status.setBorder(BorderFactory.createLineBorder(new Color(200, 230, 200)));
        status.setOpaque(true);
        status.setBackground(new Color(240, 255, 240));
        status.setForeground(new Color(30, 130, 50));

        header.add(left, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        return header;
    }

    // =========== Sidebar ===========
    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(220, 0));
        side.setBackground(new Color(36, 55, 77));
        side.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8, 10, 8, 10);

        JButton btnDashboard = sidebarButton("📊  Dashboard");
        JButton btnPatients = sidebarButton("👤  Patients");
        JButton btnDoctors = sidebarButton("🩺  Doctors");
        JButton btnAppointments = sidebarButton("📅  Appointments");

        btnDashboard.addActionListener(e -> showCard("Dashboard"));
        btnPatients.addActionListener(e -> showCard("Patients"));
        btnDoctors.addActionListener(e -> showCard("Doctors"));
        btnAppointments.addActionListener(e -> showCard("Appointments"));

        gbc.gridy = 0; side.add(btnDashboard, gbc);
        gbc.gridy++; side.add(btnPatients, gbc);
        gbc.gridy++; side.add(btnDoctors, gbc);
        gbc.gridy++; side.add(btnAppointments, gbc);

        // spacer
        gbc.gridy++; gbc.weighty = 1.0; side.add(new JLabel(), gbc);

        return side;
    }

    private JButton sidebarButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(23, 35, 51));
        b.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        return b;
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
        if (name.equals("Dashboard")) refreshStats();
    }

    // =========== Dashboard Panel ===========
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 246, 248));
        JPanel topCards = new JPanel(new GridLayout(1, 3, 20, 0));
        topCards.setBorder(new EmptyBorder(30, 30, 30, 30));
        topCards.setOpaque(false);

        lblPatientsCount = makeCard("Total Patients", "👥", new Color(52, 152, 219));
        lblDoctorsCount = makeCard("Active Doctors", "🩺", new Color(46, 204, 113));
        lblAppointmentsCount = makeCard("Today's Appointments", "📅", new Color(231, 76, 60));

        topCards.add(wrapCard(lblPatientsCount));
        topCards.add(wrapCard(lblDoctorsCount));
        topCards.add(wrapCard(lblAppointmentsCount));

        panel.add(topCards, BorderLayout.NORTH);

        // below: two columns—recent patients + available doctors
        JPanel lower = new JPanel(new GridLayout(1, 2, 20, 20));
        lower.setBorder(new EmptyBorder(0, 30, 30, 30));
        lower.setOpaque(false);
        lower.add(createRecentPatientsPanel());
        lower.add(createAvailableDoctorsPanel());

        panel.add(lower, BorderLayout.CENTER);
        return panel;
    }

    private JLabel makeCard(String title, String icon, Color bg) {
        JLabel lbl = new JLabel("<html><div style='text-align:center;'>" +
                "<div style='font-size:18px;'>" + icon + " " + title + "</div>" +
                "<div style='margin-top:18px; font-weight:bold; font-size:32px;'>0</div>" +
                "</div></html>", SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return lbl;
    }

    private JPanel wrapCard(JLabel cardLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(cardLabel, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        return p;
    }

    private JPanel createRecentPatientsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Recent Patients"));

        // list area
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        // load recent
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, age, gender FROM patients ORDER BY id DESC LIMIT 5")) {
            while (rs.next()) {
                listModel.addElement(rs.getString("name") + " — age " + rs.getInt("age") + ", " + rs.getString("gender"));
            }
        } catch (Exception ignored) {}

        return p;
    }

    private JPanel createAvailableDoctorsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Available Doctors"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, specialization FROM doctors ORDER BY id DESC LIMIT 5")) {
            while (rs.next()) {
                listModel.addElement(rs.getString("name") + " — " + rs.getString("specialization"));
            }
        } catch (Exception ignored) {}

        return p;
    }

    // =========== Patients Panel ===========
    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        // top toolbar: search + buttons
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton addBtn = new JButton("➕  Add Patient");
        JButton editBtn = new JButton("✏️  Edit");
        JButton delBtn = new JButton("🗑️  Delete");
        JButton refreshBtn = new JButton("🔄 Refresh");

        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(delBtn);

        panel.add(top, BorderLayout.NORTH);

        // table
        patientModel.setColumnIdentifiers(new String[]{"id", "name", "age", "gender"});
        JTable table = new JTable(patientModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // actions
        loadPatients();
        refreshBtn.addActionListener(e -> loadPatients());
        searchBtn.addActionListener(e -> searchPatients(searchField.getText().trim()));
        addBtn.addActionListener(e -> showPatientDialog(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { showMsg("Select a patient to edit."); return; }
            showPatientDialog((Integer) patientModel.getValueAt(r, 0));
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { showMsg("Select a patient to delete."); return; }
            if (confirm("Delete selected patient?")) {
                deletePatient((Integer) patientModel.getValueAt(r, 0));
            }
        });

        return panel;
    }

    // =========== Doctors Panel ===========
    private JPanel createDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton addBtn = new JButton("➕  Add Doctor");
        JButton editBtn = new JButton("✏️  Edit");
        JButton delBtn = new JButton("🗑️  Delete");
        JButton refreshBtn = new JButton("🔄 Refresh");

        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(delBtn);

        panel.add(top, BorderLayout.NORTH);

        doctorModel.setColumnIdentifiers(new String[]{"id", "name", "specialization"});
        JTable table = new JTable(doctorModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadDoctors();
        refreshBtn.addActionListener(e -> loadDoctors());
        searchBtn.addActionListener(e -> searchDoctors(searchField.getText().trim()));
        addBtn.addActionListener(e -> showDoctorDialog(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { showMsg("Select a doctor to edit."); return; }
            showDoctorDialog((Integer) doctorModel.getValueAt(r, 0));
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { showMsg("Select a doctor to delete."); return; }
            if (confirm("Delete selected doctor?")) {
                deleteDoctor((Integer) doctorModel.getValueAt(r, 0));
            }
        });

        return panel;
    }

    // =========== Appointments Panel ===========
    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton addBtn = new JButton("➕  Book Appointment");
        JButton editBtn = new JButton("✏️  Edit");
        JButton delBtn = new JButton("🗑️  Cancel");
        JButton refreshBtn = new JButton("🔄 Refresh");
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(delBtn);
        panel.add(top, BorderLayout.NORTH);

        appointmentModel.setColumnIdentifiers(new String[]{"id", "patient", "doctor", "appointment_date"});
        JTable table = new JTable(appointmentModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadAppointments();
        refreshBtn.addActionListener(e -> loadAppointments());
        addBtn.addActionListener(e -> showAppointmentDialog(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { showMsg("Select an appointment to edit."); return; }
            showAppointmentDialog((Integer) appointmentModel.getValueAt(r, 0));
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { showMsg("Select an appointment to cancel."); return; }
            if (confirm("Cancel selected appointment?")) deleteAppointment((Integer) appointmentModel.getValueAt(r, 0));
        });

        return panel;
    }

    // =========== Table styling ===========
    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    // =========== Data loading / refresh ===========
    private void refreshAllData() {
        loadPatients();
        loadDoctors();
        loadAppointments();
        refreshStats();
    }

    private void refreshStats() {
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            lblPatientsCount.setText(cardHtml("Total Patients", getCount(st, "patients")));
            lblDoctorsCount.setText(cardHtml("Active Doctors", getCount(st, "doctors")));
            // count appointments for today
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()");
            ResultSet rs = ps.executeQuery();
            int todayCount = 0;
            if (rs.next()) todayCount = rs.getInt(1);
            lblAppointmentsCount.setText(cardHtml("Today's Appointments", todayCount));
        } catch (Exception ex) {
            // ignore
        }
    }

    private int getCount(Statement st, String table) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
        rs.next();
        return rs.getInt(1);
    }

    private String cardHtml(String title, int value) {
        return "<html><div style='text-align:center;'>" +
                "<div style='font-size:16px;'>" + title + "</div>" +
                "<div style='margin-top:12px; font-weight:bold; font-size:28px;'>" + value + "</div>" +
                "</div></html>";
    }

    // =========== Patients CRUD ===========
    private void loadPatients() {
        patientModel.setRowCount(0);
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, age, gender FROM patients ORDER BY id")) {
            while (rs.next()) {
                patientModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender")
                });
            }
        } catch (Exception ex) {
            showError("Error loading patients: " + ex.getMessage());
        }
    }

    private void searchPatients(String q) {
        patientModel.setRowCount(0);
        if (q.isEmpty()) { loadPatients(); return; }
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, age, gender FROM patients WHERE name LIKE ? ORDER BY id")) {
            ps.setString(1, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patientModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("gender")});
                }
            }
        } catch (Exception ex) { showError("Search failed: " + ex.getMessage()); }
    }

    private void showPatientDialog(Integer editId) {
        JTextField name = new JTextField();
        JTextField age = new JTextField();
        JTextField gender = new JTextField();

        if (editId != null) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, age, gender FROM patients WHERE id = ?")) {
                ps.setInt(1, editId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        name.setText(rs.getString("name"));
                        age.setText(String.valueOf(rs.getInt("age")));
                        gender.setText(rs.getString("gender"));
                    }
                }
            } catch (Exception ex) { showError("Error: " + ex.getMessage()); return; }
        }

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Name:")); p.add(name);
        p.add(new JLabel("Age:")); p.add(age);
        p.add(new JLabel("Gender:")); p.add(gender);

        int opt = JOptionPane.showConfirmDialog(this, p, (editId == null ? "Add Patient" : "Edit Patient"), JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;

        String nm = name.getText().trim();
        if (nm.isEmpty()) { showMsg("Name required."); return; }
        try {
            int ageVal = age.getText().trim().isEmpty() ? 0 : Integer.parseInt(age.getText().trim());
            if (editId == null) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO patients(name, age, gender) VALUES(?, ?, ?)")) {
                    ps.setString(1, nm); ps.setInt(2, ageVal); ps.setString(3, gender.getText().trim()); ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE patients SET name=?, age=?, gender=? WHERE id=?")) {
                    ps.setString(1, nm); ps.setInt(2, ageVal); ps.setString(3, gender.getText().trim()); ps.setInt(4, editId); ps.executeUpdate();
                }
            }
            refreshAllData();
        } catch (NumberFormatException nfe) {
            showMsg("Age must be a number.");
        } catch (Exception ex) { showError("DB error: " + ex.getMessage()); }
    }

    private void deletePatient(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
            refreshAllData();
        } catch (Exception ex) { showError("Delete failed: " + ex.getMessage()); }
    }

    // =========== Doctors CRUD ===========
    private void loadDoctors() {
        doctorModel.setRowCount(0);
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, specialization FROM doctors ORDER BY id")) {
            while (rs.next()) {
                doctorModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("specialization")});
            }
        } catch (Exception ex) { showError("Error loading doctors: " + ex.getMessage()); }
    }

    private void searchDoctors(String q) {
        doctorModel.setRowCount(0);
        if (q.isEmpty()) { loadDoctors(); return; }
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, specialization FROM doctors WHERE name LIKE ? OR specialization LIKE ? ORDER BY id")) {
            ps.setString(1, "%" + q + "%"); ps.setString(2, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) doctorModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("specialization")});
            }
        } catch (Exception ex) { showError("Search failed: " + ex.getMessage()); }
    }

    private void showDoctorDialog(Integer editId) {
        JTextField name = new JTextField();
        JTextField spec = new JTextField();

        if (editId != null) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, specialization FROM doctors WHERE id=?")) {
                ps.setInt(1, editId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { name.setText(rs.getString("name")); spec.setText(rs.getString("specialization")); }
                }
            } catch (Exception ex) { showError("Error: " + ex.getMessage()); return; }
        }

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Name:")); p.add(name);
        p.add(new JLabel("Specialization:")); p.add(spec);

        int opt = JOptionPane.showConfirmDialog(this, p, (editId == null ? "Add Doctor" : "Edit Doctor"), JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;

        String nm = name.getText().trim(), sp = spec.getText().trim();
        if (nm.isEmpty() || sp.isEmpty()) { showMsg("All fields required."); return; }
        try {
            if (editId == null) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO doctors(name, specialization) VALUES(?, ?)")) {
                    ps.setString(1, nm); ps.setString(2, sp); ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE doctors SET name=?, specialization=? WHERE id=?")) {
                    ps.setString(1, nm); ps.setString(2, sp); ps.setInt(3, editId); ps.executeUpdate();
                }
            }
            refreshAllData();
        } catch (Exception ex) { showError("DB error: " + ex.getMessage()); }
    }

    private void deleteDoctor(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM doctors WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate(); refreshAllData();
        } catch (Exception ex) { showError("Delete failed: " + ex.getMessage()); }
    }

    // =========== Appointments CRUD ===========
    private void loadAppointments() {
        appointmentModel.setRowCount(0);
        String q = "SELECT a.id, p.name AS patient, d.name AS doctor, a.appointment_date " +
                "FROM appointments a JOIN patients p ON a.patient_id=p.id JOIN doctors d ON a.doctor_id=d.id ORDER BY a.appointment_date";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) appointmentModel.addRow(new Object[]{rs.getInt("id"), rs.getString("patient"), rs.getString("doctor"), rs.getDate("appointment_date")});
        } catch (Exception ex) { showError("Error loading appointments: " + ex.getMessage()); }
    }

    private void showAppointmentDialog(Integer editId) {
        try {
            Vector<String> patients = new Vector<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, name FROM patients")) {
                while (rs.next()) patients.add(rs.getInt("id") + " - " + rs.getString("name"));
            }
            if (patients.isEmpty()) { showMsg("No patients found. Add patients first."); return; }

            Vector<String> doctors = new Vector<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, name FROM doctors")) {
                while (rs.next()) doctors.add(rs.getInt("id") + " - " + rs.getString("name"));
            }
            if (doctors.isEmpty()) { showMsg("No doctors found. Add doctors first."); return; }

            JComboBox<String> patientBox = new JComboBox<>(patients);
            JComboBox<String> doctorBox = new JComboBox<>(doctors);
            SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
            JSpinner dateSpinner = new JSpinner(dateModel);
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

            if (editId != null) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT patient_id, doctor_id, appointment_date FROM appointments WHERE id=?")) {
                    ps.setInt(1, editId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int pid = rs.getInt("patient_id");
                            int did = rs.getInt("doctor_id");
                            Date d = rs.getDate("appointment_date");
                            for (int i = 0; i < patientBox.getItemCount(); i++) if (patientBox.getItemAt(i).startsWith(pid + " -")) { patientBox.setSelectedIndex(i); break; }
                            for (int i = 0; i < doctorBox.getItemCount(); i++) if (doctorBox.getItemAt(i).startsWith(did + " -")) { doctorBox.setSelectedIndex(i); break; }
                            dateSpinner.setValue(d);
                        }
                    }
                }
            }

            Object[] fields = {"Patient:", patientBox, "Doctor:", doctorBox, "Date:", dateSpinner};
            int opt = JOptionPane.showConfirmDialog(this, fields, (editId == null ? "Book Appointment" : "Edit Appointment"), JOptionPane.OK_CANCEL_OPTION);
            if (opt != JOptionPane.OK_OPTION) return;

            int patientId = Integer.parseInt(((String) patientBox.getSelectedItem()).split(" - ")[0]);
            int doctorId = Integer.parseInt(((String) doctorBox.getSelectedItem()).split(" - ")[0]);
            Date sel = (Date) dateSpinner.getValue();
            String dateStr = sqlDateFormat.format(sel);

            // check availability
            String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND appointment_date=?";
            if (editId != null) checkSql += " AND id<>?";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, doctorId); check.setString(2, dateStr);
                if (editId != null) check.setInt(3, editId);
                try (ResultSet rs = check.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) { showMsg("Doctor not available on this date."); return; }
                }
            }

            if (editId == null) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)")) {
                    ins.setInt(1, patientId); ins.setInt(2, doctorId); ins.setString(3, dateStr); ins.executeUpdate();
                    showMsg("Appointment booked.");
                }
            } else {
                try (PreparedStatement upd = conn.prepareStatement("UPDATE appointments SET patient_id=?, doctor_id=?, appointment_date=? WHERE id=?")) {
                    upd.setInt(1, patientId); upd.setInt(2, doctorId); upd.setString(3, dateStr); upd.setInt(4, editId); upd.executeUpdate();
                    showMsg("Appointment updated.");
                }
            }
            refreshAllData();
        } catch (Exception ex) { showError("Error booking appointment: " + ex.getMessage()); }
    }

    private void deleteAppointment(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM appointments WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate(); refreshAllData();
        } catch (Exception ex) { showError("Cancel failed: " + ex.getMessage()); }
    }

    // =========== Helpers ===========
    private void showMsg(String m) { JOptionPane.showMessageDialog(this, m); }
    private boolean confirm(String m) { return JOptionPane.showConfirmDialog(this, m, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
    private void showError(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); System.err.println(m); }

}
