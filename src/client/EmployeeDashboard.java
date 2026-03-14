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
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        menu.add(logout);
        mb.add(menu);
        setJMenuBar(mb);

        add(tabbedPane);
        
        // Initial data load for Leave tab
        refreshLeaveData();
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // --- Personal Details Form ---
        JPanel personalPanel = new JPanel(new GridBagLayout());
        personalPanel.setBorder(BorderFactory.createTitledBorder("Personal Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFirst = new JTextField(currentEmployee.getFirstName(), 15);
        JTextField txtLast = new JTextField(currentEmployee.getLastName(), 15);
        JTextField txtIc = new JTextField(currentEmployee.getIcPassport(), 15);
        JTextField txtDesig = new JTextField(currentEmployee.getDesignation(), 15);
        JTextField txtAddr = new JTextField(currentEmployee.getAddress(), 15);
        
        int r = 0;
        addLabelField(personalPanel, gbc, r++, "First Name:", txtFirst);
        addLabelField(personalPanel, gbc, r++, "Last Name:", txtLast);
        addLabelField(personalPanel, gbc, r++, "IC/Passport:", txtIc);
        addLabelField(personalPanel, gbc, r++, "Designation:", txtDesig);
        addLabelField(personalPanel, gbc, r++, "Address:", txtAddr);

        // --- Family Details Table ---
        JPanel familyPanel = new JPanel(new BorderLayout());
        familyPanel.setBorder(BorderFactory.createTitledBorder("Family Members"));
        
        String[] famCols = {"Relation", "First Name", "Last Name", "Email", "Phone"};
        DefaultTableModel famModel = new DefaultTableModel(famCols, 0);
        JTable famTable = new JTable(famModel);
        
        // Load initial family data
        if (currentEmployee.getFamilyMembers() != null) {
            for (FamilyMember fm : currentEmployee.getFamilyMembers()) {
                famModel.addRow(new Object[]{fm.getRelation(), fm.getFirstName(), fm.getLastName(), fm.getEmail(), fm.getPhone()});
            }
        }
        
        JPanel famControls = new JPanel();
        JButton btnAddFam = new JButton("Add Member");
        JButton btnRemFam = new JButton("Remove Selected");
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
            
            Object[] message = {
                "Relation:", combo, "First Name:", fName, "Last Name:", lName, "Email:", email, "Phone:", phone
            };
            
            int option = JOptionPane.showConfirmDialog(this, message, "Add Family Member", JOptionPane.OK_CANCEL_OPTION);
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
        JLabel lblStatus = new JLabel(" ");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnUpdate);
        bottomPanel.add(lblStatus);

        btnUpdate.addActionListener(e -> {
            // Update Object
            currentEmployee.setFirstName(txtFirst.getText());
            currentEmployee.setLastName(txtLast.getText());
            currentEmployee.setIcPassport(txtIc.getText());
            currentEmployee.setDesignation(txtDesig.getText());
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
        JButton btnApply = new JButton("Apply Leave");
        
        topPanel.add(lblBalance);
        topPanel.add(new JLabel("Start:"));
        topPanel.add(txtStart);
        topPanel.add(new JLabel("End:"));
        topPanel.add(txtEnd);
        topPanel.add(btnApply);
        
        // Center: Table
        String[] cols = {"Leave ID", "Start", "End", "Status", "Year"};
        tableModel = new DefaultTableModel(cols, 0);
        historyTable = new JTable(tableModel);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        btnApply.addActionListener(e -> {
            String start = txtStart.getText();
            String end = txtEnd.getText();
            // Simple validation could be added here
            
            LeaveApplication leave = new LeaveApplication(
                    UUID.randomUUID().toString().substring(0, 8),
                    currentEmployee.getId(),
                    start,
                    end,
                    2026 // Hardcoded year for simplicity or extract from date
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
                        tableModel.addRow(new Object[]{ l.getLeaveID(), l.getStartDate(), l.getEndDate(), l.getStatus(), l.getYear() });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }
}

