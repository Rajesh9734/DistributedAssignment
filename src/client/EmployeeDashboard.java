package client;

import common.Employee;
import common.FamilyMember;
import common.LeaveApplication;
import common.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmployeeDashboard extends JFrame {
    private Employee currentEmployee;
    
    private JLabel lblBalance;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public EmployeeDashboard(User user) {
        if (user instanceof Employee) {
            this.currentEmployee = (Employee) user;
        } else {
            // Provide default values for new fields if User cannot be cast (shouldn't happen)
            this.currentEmployee = new Employee(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), "", "", "", "", "");
        }
        
        setTitle("Employee Dashboard - " + currentEmployee.getId());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Profile", createProfilePanel());
        tabbedPane.addTab("Leave Management", createLeavePanel());
        
        // Logout Button
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("System");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> performLogout());
        menu.add(logout);
        mb.add(menu);
        setJMenuBar(mb);

        JButton btnLogout = UITheme.createPrimaryButton("Logout");
        btnLogout.addActionListener(e -> performLogout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PANEL_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblTitle = new JLabel("Employee Dashboard - " + currentEmployee.getId());
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(UITheme.HEADER_FG);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(tabbedPane, BorderLayout.CENTER);
        add(root);
        
        // Initial data load for Leave tab
        refreshLeaveData();
    }

    private void performLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Logout from current session?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // --- Personal Details Form ---
        JPanel personalPanel = new JPanel(new GridBagLayout());
        personalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Personal Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // More spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFirst = new JTextField(currentEmployee.getFirstName(), 20);
        JTextField txtLast = new JTextField(currentEmployee.getLastName(), 20);
        JTextField txtIc = new JTextField(currentEmployee.getIcPassport(), 20);
        JLabel lblDesignation = new JLabel(currentEmployee.getDesignation());
        JTextField txtAddr = new JTextField(currentEmployee.getAddress(), 20);
        
        int r = 0;
        addLabelField(personalPanel, gbc, r++, "First Name:", txtFirst);
        addLabelField(personalPanel, gbc, r++, "Last Name:", txtLast);
        addLabelField(personalPanel, gbc, r++, "IC/Passport:", txtIc);
        addLabelField(personalPanel, gbc, r++, "Designation:", lblDesignation);
        addLabelField(personalPanel, gbc, r++, "Address:", txtAddr);

        // --- Family Details Table ---
        JPanel familyPanel = new JPanel(new BorderLayout());
        familyPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Family Members"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        String[] famCols = {"Relation", "First Name", "Last Name", "Email", "Phone"};
        DefaultTableModel famModel = new DefaultTableModel(famCols, 0);
        JTable famTable = new JTable(famModel);
        famTable.setRowHeight(25);
        famTable.setShowGrid(true);
        famTable.setGridColor(Color.LIGHT_GRAY);
        
        // Load initial family data
        if (currentEmployee.getFamilyMembers() != null) {
            for (FamilyMember fm : currentEmployee.getFamilyMembers()) {
                famModel.addRow(new Object[]{fm.getRelation(), fm.getFirstName(), fm.getLastName(), fm.getEmail(), fm.getPhone()});
            }
        }
        
        JPanel famControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddFam = new JButton("Add Member");
        UITheme.stylePrimaryButton(btnAddFam);
        JButton btnRemFam = new JButton("Remove Selected");
        UITheme.stylePrimaryButton(btnRemFam);
        famControls.add(btnAddFam);
        famControls.add(btnRemFam);
        
        familyPanel.add(new JScrollPane(famTable), BorderLayout.CENTER);
        familyPanel.add(famControls, BorderLayout.SOUTH);
        
        btnAddFam.addActionListener(e -> {
            String[] relations = {"Spouse", "Father", "Mother", "Child", "Sibling", "Other"};
            JComboBox<String> combo = new JComboBox<>(relations);
            JTextField fName = new JTextField();
            JTextField lName = new JTextField();
            JTextField email = new JTextField();
            JTextField phone = new JTextField();
            
            JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
            p.add(new JLabel("Relation:")); p.add(combo);
            p.add(new JLabel("First Name:")); p.add(fName);
            p.add(new JLabel("Last Name:")); p.add(lName);
            p.add(new JLabel("Email:")); p.add(email);
            p.add(new JLabel("Phone:")); p.add(phone);
            
            int option = JOptionPane.showConfirmDialog(this, p, "Add Family Member", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                famModel.addRow(new Object[]{combo.getSelectedItem(), fName.getText(), lName.getText(), email.getText(), phone.getText()});
            }
        });
        
        btnRemFam.addActionListener(e -> {
            int row = famTable.getSelectedRow();
            if (row != -1) famModel.removeRow(row);
        });

        // --- Save All Button ---
        JButton btnUpdate = new JButton("Save All Changes");
        UITheme.stylePrimaryButton(btnUpdate);
        btnUpdate.setPreferredSize(new Dimension(150, 40));
        btnUpdate.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel lblStatus = new JLabel(" ");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(btnUpdate);
        bottomPanel.add(lblStatus);

        btnUpdate.addActionListener(e -> {
            // Update Object
            currentEmployee.setFirstName(txtFirst.getText());
            currentEmployee.setLastName(txtLast.getText());
            currentEmployee.setIcPassport(txtIc.getText());
            currentEmployee.setAddress(txtAddr.getText());
            
            List<FamilyMember> newFamily = new ArrayList<>();
            for (int i = 0; i < famModel.getRowCount(); i++) {
                newFamily.add(new FamilyMember(
                    (String) famModel.getValueAt(i, 1),
                    (String) famModel.getValueAt(i, 2),
                    (String) famModel.getValueAt(i, 0),
                    (String) famModel.getValueAt(i, 3),
                    (String) famModel.getValueAt(i, 4)
                ));
            }
            currentEmployee.setFamilyMembers(newFamily);
            
            btnUpdate.setEnabled(false);
            lblStatus.setText("Saving...");
            
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return RMIClient.getService().updateProfile(currentEmployee);
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            lblStatus.setText("Saved Successfully.");
                            lblStatus.setForeground(new Color(0, 128, 0));
                        } else {
                            lblStatus.setText("Save Failed.");
                            lblStatus.setForeground(Color.RED);
                        }
                    } catch (Exception ex) {
                         lblStatus.setText("Error: " + ex.getMessage());
                    }
                    btnUpdate.setEnabled(true);
                }
            }.execute();
        });
        
        // Assemble
        panel.add(personalPanel, BorderLayout.NORTH);
        panel.add(familyPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void addLabelField(JPanel p, GridBagConstraints gbc, int y, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; p.add(comp, gbc);
    }

    private JPanel createLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top: Apply
        JPanel topPanel = new JPanel(new FlowLayout());
        lblBalance = new JLabel("Balance: " + currentEmployee.getLeaveBalance());
        JTextField txtStart = new JTextField("YYYY-MM-DD", 10);
        JTextField txtEnd = new JTextField("YYYY-MM-DD", 10);
        JTextField txtReason = new JTextField(18);
        txtReason.setToolTipText("Reason for leave");
        UITheme.styleInput(txtReason);
        JButton btnApply = new JButton("Apply Leave");
        UITheme.stylePrimaryButton(btnApply);
        
        topPanel.add(lblBalance);
        topPanel.add(new JLabel("Start:"));
        topPanel.add(txtStart);
        topPanel.add(new JLabel("End:"));
        topPanel.add(txtEnd);
        topPanel.add(new JLabel("Reason:"));
        topPanel.add(txtReason);
        topPanel.add(btnApply);
        
        // Center: Table
        String[] cols = {"Leave ID", "Start", "End", "Status", "Year", "Reason"};
        tableModel = new DefaultTableModel(cols, 0);
        historyTable = new JTable(tableModel);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        btnApply.addActionListener(e -> {
            String start = txtStart.getText().trim();
            String end = txtEnd.getText().trim();
            String reason = txtReason.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(EmployeeDashboard.this, "Please enter a leave reason.");
                return;
            }
            
            LeaveApplication leave = new LeaveApplication(
                    UUID.randomUUID().toString().substring(0, 8),
                    currentEmployee.getId(),
                    start,
                    end,
                    2026, // Hardcoded year for simplicity or extract from date
                    reason
            );
            
            btnApply.setEnabled(false);
            
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return RMIClient.getService().applyForLeave(leave);
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(EmployeeDashboard.this, "Leave Applied!");
                            refreshLeaveData();
                            txtReason.setText("");
                        } else {
                            JOptionPane.showMessageDialog(EmployeeDashboard.this, "Application Failed (Balance?).");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(EmployeeDashboard.this, "Error: " + ex.getMessage());
                    }
                    btnApply.setEnabled(true);
                }
            }.execute();
        });
        
        return panel;
    }
    
    private void refreshLeaveData() {
        // Refresh Balance
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return RMIClient.getService().getLeaveBalance(currentEmployee.getId());
            }
            @Override
            protected void done() {
                try {
                    int bal = get();
                    currentEmployee.setLeaveBalance(bal);
                    lblBalance.setText("Balance: " + bal);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
        
        // Refresh History
        new SwingWorker<List<LeaveApplication>, Void>() {
            @Override
            protected List<LeaveApplication> doInBackground() throws Exception {
                return RMIClient.getService().getLeaveHistory(currentEmployee.getId());
            }
            @Override
            protected void done() {
                try {
                    List<LeaveApplication> list = get();
                    tableModel.setRowCount(0);
                    for (LeaveApplication l : list) {
                        tableModel.addRow(new Object[]{
                            l.getLeaveID(),
                            l.getStartDate(),
                            l.getEndDate(),
                            l.getStatus(),
                            l.getYear(),
                            l.getReason()
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }
}
