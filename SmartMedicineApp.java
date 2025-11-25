import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SmartMedicineApp extends JFrame {

    private Connection conn;
    private JTable reminderTable;
    private DefaultTableModel tableModel;
    private TrayIcon trayIcon;

    public SmartMedicineApp() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("Smart Medicine Reminder");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupTrayIcon();
        connectToDatabase();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton addMemberBtn = new JButton("Add Member");
        JButton deleteMemberBtn = new JButton("Delete Member");
        JButton addReminderBtn = new JButton("Add Reminder");
        JButton editReminderBtn = new JButton("Edit Reminder");
        JButton deleteReminderBtn = new JButton("Delete Reminder");

        styleButton(addMemberBtn);
        styleButton(deleteMemberBtn);
        styleButton(addReminderBtn);
        styleButton(editReminderBtn);
        styleButton(deleteReminderBtn);

        buttonPanel.add(addMemberBtn);
        buttonPanel.add(deleteMemberBtn);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(addReminderBtn);
        buttonPanel.add(editReminderBtn);
        buttonPanel.add(deleteReminderBtn);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // --- Table ---
        String[] columnNames = {"Reminder ID", "Person", "Medicine", "Dosage", "Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reminderTable = new JTable(tableModel);
        reminderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reminderTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reminderTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(reminderTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // --- Action Listeners ---
        addMemberBtn.addActionListener(e -> addMember());
        deleteMemberBtn.addActionListener(e -> deleteMember());
        addReminderBtn.addActionListener(e -> addReminder());
        editReminderBtn.addActionListener(e -> editReminder());
        deleteReminderBtn.addActionListener(e -> deleteReminder());

        refreshRemindersTable();
        startReminderService();
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smart_medicine", "root", "MySQL#2025Root!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public void refreshRemindersTable() {
        tableModel.setRowCount(0);
        String sql = "SELECT r.reminder_id, fm.name AS member_name, m.name AS medicine_name, r.dosage, r.time_of_day " +
                     "FROM reminder r " +
                     "JOIN family_member fm ON r.member_id = fm.member_id " +
                     "JOIN medicine m ON r.medicine_id = m.medicine_id " +
                     "ORDER BY r.time_of_day";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("reminder_id"),
                    rs.getString("member_name"),
                    rs.getString("medicine_name"),
                    rs.getString("dosage"),
                    rs.getTime("time_of_day").toLocalTime().format(outputFormatter)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading reminders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addMember() {
        MemberDialog dialog = new MemberDialog(this, conn);
        dialog.setVisible(true);
    }

    private void deleteMember() {
        DeleteMemberDialog dialog = new DeleteMemberDialog(this, conn);
        dialog.setVisible(true);
    }

    private void addReminder() {
        ReminderDialog dialog = new ReminderDialog(this, conn, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshRemindersTable();
        }
    }

    private void editReminder() {
        int selectedRow = reminderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int reminderId = (int) tableModel.getValueAt(selectedRow, 0);
        ReminderDialog dialog = new ReminderDialog(this, conn, reminderId);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshRemindersTable();
        }
    }

    private void deleteReminder() {
        int selectedRow = reminderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this reminder?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int reminderId = (int) tableModel.getValueAt(selectedRow, 0);
            String sql = "DELETE FROM reminder WHERE reminder_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, reminderId);
                pstmt.executeUpdate();
                refreshRemindersTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting reminder: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) return;
        URL imageURL = getClass().getResource("/medicine_icon.png");
        if (imageURL == null) {
            System.err.println("Warning: Icon file not found. Tray icon will not be displayed.");
            return;
        }
        Image icon = new ImageIcon(imageURL).getImage();
        trayIcon = new TrayIcon(icon, "Smart Medicine Reminder");
        trayIcon.setImageAutoSize(true);
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    private void startReminderService() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { checkReminders(); }
        }, 0, 60 * 1000);
    }

    private void checkReminders() {
        if (trayIcon == null) return;
        String sql = "SELECT fm.name AS member_name, m.name AS medicine_name, r.dosage " +
                     "FROM reminder r " +
                     "JOIN family_member fm ON r.member_id = fm.member_id " +
                     "JOIN medicine m ON r.medicine_id = m.medicine_id " +
                     "WHERE r.time_of_day = ?";
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTime(1, Time.valueOf(now));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String message = String.format("%s, it's time to take %s (%s).", rs.getString("member_name"), rs.getString("medicine_name"), rs.getString("dosage"));
                SwingUtilities.invokeLater(() -> trayIcon.displayMessage("Medicine Reminder", message, TrayIcon.MessageType.INFO));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartMedicineApp().setVisible(true));
    }

    static class MemberItem {
        private final int id; private final String name;
        public MemberItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; return id == ((MemberItem) o).id; }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    static class MedicineItem {
        private final int id; private final String name;
        public MedicineItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; return id == ((MedicineItem) o).id; }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    static class MemberDialog extends JDialog {
        private JTextField nameField = new JTextField(20);
        private JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 120, 1));
        private JTextField relationField = new JTextField(20);
        private JButton saveButton = new JButton("Save");
        private JButton cancelButton = new JButton("Cancel");
        private Connection conn;

        public MemberDialog(Frame owner, Connection conn) {
            super(owner, "Add New Member", true);
            this.conn = conn;

            setLayout(new BorderLayout(10, 10));
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx = 1; formPanel.add(nameField, gbc);
            gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Age:"), gbc);
            gbc.gridx = 1; formPanel.add(ageSpinner, gbc);
            gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Relation (e.g., Father):"), gbc);
            gbc.gridx = 1; formPanel.add(relationField, gbc);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(saveButton); buttonPanel.add(cancelButton);

            add(formPanel, BorderLayout.CENTER); add(buttonPanel, BorderLayout.SOUTH);
            saveButton.addActionListener(e -> saveMember());
            cancelButton.addActionListener(e -> dispose());

            pack(); setLocationRelativeTo(owner);
        }

        private void saveMember() {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int age = (Integer) ageSpinner.getValue();
            String relation = relationField.getText().trim();
            String sql = "INSERT INTO family_member (name, age, relation) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, relation);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving member: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class DeleteMemberDialog extends JDialog {
        private JComboBox<MemberItem> memberComboBox = new JComboBox<>();
        private JButton deleteButton = new JButton("Delete Permanently");
        private JButton cancelButton = new JButton("Cancel");
        private Connection conn;
        private SmartMedicineApp owner;

        public DeleteMemberDialog(SmartMedicineApp owner, Connection conn) {
            super(owner, "Delete Member", true);
            this.owner = owner;
            this.conn = conn;

            setLayout(new BorderLayout(10, 10));
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Select Member to Delete:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; panel.add(memberComboBox, gbc);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            deleteButton.setBackground(new Color(200, 50, 50));
            deleteButton.setForeground(Color.WHITE);
            buttonPanel.add(deleteButton);
            buttonPanel.add(cancelButton);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            populateMemberDropdown();

            deleteButton.addActionListener(e -> deleteSelectedMember());
            cancelButton.addActionListener(e -> dispose());

            pack();
            setLocationRelativeTo(owner);
        }

        private void populateMemberDropdown() {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT member_id, name FROM family_member")) {
                while (rs.next()) {
                    memberComboBox.addItem(new MemberItem(rs.getInt("member_id"), rs.getString("name")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void deleteSelectedMember() {
            MemberItem selectedMember = (MemberItem) memberComboBox.getSelectedItem();
            if (selectedMember == null) {
                JOptionPane.showMessageDialog(this, "Please select a member to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + selectedMember.name + "'?\nThis will also delete ALL of their reminders.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String deleteRemindersSql = "DELETE FROM reminder WHERE member_id = ?";
            String deleteMemberSql = "DELETE FROM family_member WHERE member_id = ?";

            try {
                conn.setAutoCommit(false); // Start transaction
                try (PreparedStatement pstmtReminders = conn.prepareStatement(deleteRemindersSql)) {
                    pstmtReminders.setInt(1, selectedMember.getId());
                    pstmtReminders.executeUpdate();
                }
                try (PreparedStatement pstmtMember = conn.prepareStatement(deleteMemberSql)) {
                    pstmtMember.setInt(1, selectedMember.getId());
                    pstmtMember.executeUpdate();
                }
                conn.commit(); // Commit transaction
                JOptionPane.showMessageDialog(this, "Member and their reminders were deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                owner.refreshRemindersTable(); // Refresh the main table
                dispose();
            } catch (SQLException e) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Error deleting member: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    conn.setAutoCommit(true); // Restore default behavior
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static class ReminderDialog extends JDialog {
        private JComboBox<MemberItem> memberComboBox = new JComboBox<>();
        private JComboBox<MedicineItem> medicineComboBox = new JComboBox<>();
        private JTextField dosageField = new JTextField(20);
        private JSpinner timeSpinner;
        private JButton saveButton = new JButton("Save");
        private JButton cancelButton = new JButton("Cancel");
        private Connection conn;
        private Integer reminderId;
        private boolean saved = false;

        public ReminderDialog(Frame owner, Connection conn, Integer reminderId) {
            super(owner, "Reminder Details", true);
            this.conn = conn; this.reminderId = reminderId;
            setLayout(new BorderLayout(10, 10));

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5); gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Family Member:"), gbc);
            gbc.gridx = 1; formPanel.add(memberComboBox, gbc);
            gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Medicine:"), gbc);
            gbc.gridx = 1; formPanel.add(medicineComboBox, gbc);
            gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Dosage (e.g., 1 pill):"), gbc);
            gbc.gridx = 1; formPanel.add(dosageField, gbc);
            gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Time:"), gbc);
            timeSpinner = new JSpinner(new SpinnerDateModel());
            timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
            timeSpinner.setValue(new Date());
            gbc.gridx = 1; formPanel.add(timeSpinner, gbc);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(saveButton); buttonPanel.add(cancelButton);
            add(formPanel, BorderLayout.CENTER); add(buttonPanel, BorderLayout.SOUTH);

            populateMemberDropdown(); populateMedicineDropdown();
            if (reminderId != null) loadReminderData();

            saveButton.addActionListener(e -> saveReminder());
            cancelButton.addActionListener(e -> dispose());

            pack(); setLocationRelativeTo(owner);
        }

        private void populateMemberDropdown() {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT member_id, name FROM family_member")) {
                while (rs.next()) memberComboBox.addItem(new MemberItem(rs.getInt("member_id"), rs.getString("name")));
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private void populateMedicineDropdown() {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT medicine_id, name FROM medicine")) {
                while (rs.next()) medicineComboBox.addItem(new MedicineItem(rs.getInt("medicine_id"), rs.getString("name")));
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private void loadReminderData() {
            String sql = "SELECT member_id, medicine_id, dosage, time_of_day FROM reminder WHERE reminder_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, reminderId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    memberComboBox.setSelectedItem(new MemberItem(rs.getInt("member_id"), ""));
                    medicineComboBox.setSelectedItem(new MedicineItem(rs.getInt("medicine_id"), ""));
                    dosageField.setText(rs.getString("dosage"));
                    timeSpinner.setValue(rs.getTime("time_of_day"));
                }
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error loading reminder data.", "Error", JOptionPane.ERROR_MESSAGE); }
        }

        private void saveReminder() {
            MemberItem member = (MemberItem) memberComboBox.getSelectedItem();
            MedicineItem medicine = (MedicineItem) medicineComboBox.getSelectedItem();
            if (member == null || medicine == null) {
                JOptionPane.showMessageDialog(this, "Please select a member and a medicine.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String dosage = dosageField.getText().trim();
            if (dosage.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Dosage cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Time time = new Time(((Date) timeSpinner.getValue()).getTime());
            String sql = (reminderId == null)
                    ? "INSERT INTO reminder (member_id, medicine_id, dosage, time_of_day) VALUES (?, ?, ?, ?)"
                    : "UPDATE reminder SET member_id = ?, medicine_id = ?, dosage = ?, time_of_day = ? WHERE reminder_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, member.getId());
                pstmt.setInt(2, medicine.getId());
                pstmt.setString(3, dosage);
                pstmt.setTime(4, time);
                if (reminderId != null) pstmt.setInt(5, reminderId);
                pstmt.executeUpdate();
                saved = true; dispose();
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error saving reminder: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
        }
        public boolean isSaved() { return saved; }
    }
}