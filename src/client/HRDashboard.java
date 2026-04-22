package client;

import common.Employee;
import common.HRMInterface;
import common.LeaveApplication;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HRDashboard extends JFrame {

    public HRDashboard() {
        setTitle("HR Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Employee Management", createManagementPanel()); // Renamed
        tabbedPane.addTab("Leave Management", createLeavePanel()); // New
        tabbedPane.addTab("Report", createReportPanel());
        tabbedPane.addTab("PRS Sync", createSyncPanel());
        
        // Logout Button in Menu Bar
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
        JLabel lblTitle = new JLabel("HR Dashboard");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(UITheme.HEADER_FG);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(tabbedPane, BorderLayout.CENTER);
        add(root);
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

    private JPanel createManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG);
        
        // --- Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Manage Employees"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID is needed for updates but not usually entered manually for register
        // We'll store it in a hidden way or use a label for updates.
        JLabel lblIdValue = new JLabel("-");

        JTextField txtFirst = new JTextField(15);
        UITheme.styleInput(txtFirst);
        txtFirst.setToolTipText("Enter first name");
        JTextField txtLast = new JTextField(15);
        UITheme.styleInput(txtLast);
        txtLast.setToolTipText("Enter last name");
        JTextField txtEmail = new JTextField(15);
        UITheme.styleInput(txtEmail);
        txtEmail.setToolTipText("Enter email address");
        JPasswordField txtPass = new JPasswordField(15);
        UITheme.styleInput(txtPass);
        txtPass.setToolTipText("Enter password");
        JTextField txtIc = new JTextField(15);
        UITheme.styleInput(txtIc);
        txtIc.setToolTipText("Enter IC or Passport number");
        JTextField txtDesig = new JTextField(15);
        UITheme.styleInput(txtDesig);
        txtDesig.setToolTipText("Enter designation");
        JTextField txtAddr = new JTextField(15);
        UITheme.styleInput(txtAddr);
        txtAddr.setToolTipText("Enter address");
        JTextField txtBalance = new JTextField(15);
        UITheme.styleInput(txtBalance);
        txtBalance.setToolTipText("Enter Leave Balance");
        txtBalance.setText("20"); // Default

        JButton btnRegister = UITheme.createPrimaryButton("Register");
        JButton btnUpdate = UITheme.createAccentButton("Update");
        JButton btnClear = UITheme.createPrimaryButton("Clear / New");
        btnUpdate.setEnabled(false); // Disabled until selection

        JLabel lblResult = new JLabel(" "); // To display status
        
        int r = 0;
        addRow(formPanel, gbc, r++, "ID:", lblIdValue);
        addRow(formPanel, gbc, r++, "First Name:", txtFirst);
        addRow(formPanel, gbc, r++, "Last Name:", txtLast);
        addRow(formPanel, gbc, r++, "Email:", txtEmail);
        addRow(formPanel, gbc, r++, "Password:", txtPass);
        addRow(formPanel, gbc, r++, "IC/Passport:", txtIc);
        addRow(formPanel, gbc, r++, "Designation:", txtDesig);
        addRow(formPanel, gbc, r++, "Address:", txtAddr);
        addRow(formPanel, gbc, r++, "Leave Balance:", txtBalance);
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnRegister);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnClear);
        
        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2; formPanel.add(btnPanel, gbc);
        gbc.gridy = r++; formPanel.add(lblResult, gbc);

        // --- List ---
        // Changed column header from "Balance" to "Leave Balance"
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Designation", "Leave Balance", "First Name", "Last Name", "IC", "Address"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setBackground(UITheme.PANEL_BG);
        // Hide extra columns that are just for data holding
        table.removeColumn(table.getColumnModel().getColumn(8)); // Address
        table.removeColumn(table.getColumnModel().getColumn(7)); // IC
        table.removeColumn(table.getColumnModel().getColumn(6)); // Last Name
        table.removeColumn(table.getColumnModel().getColumn(5)); // First Name

        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(UITheme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setAutoCreateRowSorter(true);
        // alternate row color via renderer
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) c.setBackground(row % 2 == 0 ? UITheme.PANEL_BG : UITheme.BG);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.PANEL_BG);
        
        // Use SplitPane instead of BorderLayout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, scroll);
        splitPane.setDividerLocation(400); // Increased for extra balance field
        splitPane.setContinuousLayout(true);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Refresh Table Function
        Runnable refreshTable = () -> {
             new SwingWorker<List<Employee>, Void>() {
                 @Override
                 protected List<Employee> doInBackground() throws Exception {
                     return RMIClient.getService().getAllEmployees();
                 }
                 @Override
                 protected void done() {
                     try {
                         List<Employee> list = get();
                         model.setRowCount(0);
                         for (Employee e : list) {
                             model.addRow(new Object[]{
                                 e.getId(), 
                                 e.getFirstName() + " " + e.getLastName(), 
                                 e.getEmail(), 
                                 e.getDesignation(), 
                                 e.getLeaveBalance(),
                                 e.getFirstName(),
                                 e.getLastName(),
                                 e.getIcPassport(),
                                 e.getAddress()
                             });
                         }
                     } catch (Exception ex) { ex.printStackTrace(); }
                 }
             }.execute();
        };
        
        // Initial Refresh
        refreshTable.run();

        // Table Selection Listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                lblIdValue.setText(model.getValueAt(row, 0).toString());
                txtFirst.setText(model.getValueAt(row, 5).toString());
                txtLast.setText(model.getValueAt(row, 6).toString());
                txtEmail.setText(model.getValueAt(row, 2).toString());
                txtDesig.setText(model.getValueAt(row, 3).toString());
                txtBalance.setText(model.getValueAt(row, 4).toString());
                txtIc.setText(model.getValueAt(row, 7) != null ? model.getValueAt(row, 7).toString() : "");
                txtAddr.setText(model.getValueAt(row, 8) != null ? model.getValueAt(row, 8).toString() : "");
                txtPass.setText("");
                txtPass.setToolTipText("Leave blank to keep existing password; enter a new one to update.");
                
                // Disable ID/Pass editing during update might be wise, but keeping it flexible
                // Note: Updating Email/ID/Pass is not fully supported by updateProfile if they change PKs, 
                // but the form allows input. We are using ID for lookup.
                
                btnRegister.setEnabled(false);
                btnUpdate.setEnabled(true);
                lblResult.setText("Editing Employee: " + lblIdValue.getText());
            }
        });

        btnClear.addActionListener(e -> {
            table.clearSelection();
            lblIdValue.setText("-");
            txtFirst.setText(""); txtLast.setText(""); txtEmail.setText(""); txtPass.setText("");
            txtIc.setText(""); txtDesig.setText(""); txtAddr.setText(""); txtBalance.setText("20");
            btnRegister.setEnabled(true);
            btnUpdate.setEnabled(false);
            lblResult.setText("Ready to Register");
        });

        btnRegister.addActionListener(e -> {
            String f = txtFirst.getText().trim();
            String l = txtLast.getText().trim();
            String em = txtEmail.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String ic = txtIc.getText().trim();
            String d = txtDesig.getText().trim();
            String a = txtAddr.getText().trim();
            int bal = 20;
            try { bal = Integer.parseInt(txtBalance.getText().trim()); } catch(Exception ex) {}

            
            if (f.isEmpty() || em.isEmpty() || p.isEmpty()) {
                lblResult.setText("Name, Email, Pass required needs.");
                return;
            }
            
            // Auto-Generate ID
            String id = (f.length() > 0 ? f.substring(0, 1).toLowerCase() : "u") + (int)(Math.random() * 10000);
            
            Employee emp = new Employee(id, em, p, "EMP", f, l, ic, d, a);
            emp.setLeaveBalance(bal);
            
            btnRegister.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return RMIClient.getService().registerEmployee(emp);
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            lblResult.setText("Registered! ID: " + id);
                            lblResult.setForeground(new Color(0, 128, 0));
                            btnClear.doClick();
                            refreshTable.run();
                        } else {
                            lblResult.setText("Failed.");
                            lblResult.setForeground(Color.RED);
                        }
                    } catch (Exception ex) {
                        lblResult.setText("Error: " + ex.getMessage());
                    }
                    btnRegister.setEnabled(true);
                }
            }.execute();
        });

        btnUpdate.addActionListener(e -> {
            String id = lblIdValue.getText();
            if (id.equals("-")) return;

            String f = txtFirst.getText().trim();
            String l = txtLast.getText().trim();
            String em = txtEmail.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String ic = txtIc.getText().trim();
            String d = txtDesig.getText().trim();
            String a = txtAddr.getText().trim();
            int bal = 0;
            try { bal = Integer.parseInt(txtBalance.getText().trim()); } catch (Exception ex) {
                lblResult.setText("Invalid Balance"); return;
            }

            // Construct employee object for update
            // Note: Password update is not handled by updateProfile currently, nor is Email.
            // We pass them to constructor but updateProfile might ignore them if not coded in server.
            // Server's updateProfile updates: first, last, ic, designation, address.
            // And we will call updateLeaveBalance separately.
            
            Employee emp = new Employee(id, em, p, "EMP", f, l, ic, d, a);
            
            btnUpdate.setEnabled(false);
            int finalBal = bal;
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    boolean pUpdate = RMIClient.getService().updateProfile(emp);
                    boolean bUpdate = RMIClient.getService().updateLeaveBalance(id, finalBal);
                    return pUpdate && bUpdate;
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            lblResult.setText("Updated: " + id);
                            lblResult.setForeground(new Color(0, 128, 0));
                            refreshTable.run();
                            // try to reselect same id in the table to show updated row
                            SwingUtilities.invokeLater(() -> {
                                for (int i = 0; i < table.getRowCount(); i++) {
                                    if (model.getValueAt(i,0).equals(id)) {
                                        table.setRowSelectionInterval(i, i);
                                        table.scrollRectToVisible(table.getCellRect(i, 0, true));
                                        break;
                                    }
                                }
                            });
                        } else {
                            lblResult.setText("Update Failed.");
                            lblResult.setForeground(Color.RED);
                        }
                    } catch (Exception ex) {
                        lblResult.setText("Error: " + ex.getMessage());
                    }
                    btnUpdate.setEnabled(true);
                }
            }.execute();
        });
        
        return mainPanel;
    }
    
    private void addRow(JPanel p, GridBagConstraints gbc, int y, String lbl, Component c) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; p.add(new JLabel(lbl), gbc);
        gbc.gridx = 1; p.add(c, gbc);
    }
    
    private JPanel createLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] cols = {"Leave ID", "Emp ID", "Start", "End", "Status", "Reason"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        
        JPanel controls = new JPanel();
        JButton btnRefresh = UITheme.createPrimaryButton("Refresh Pending");
        JButton btnApprove = UITheme.createPrimaryButton("Approve Selected");
        JButton btnReject = UITheme.createPrimaryButton("Reject Selected");
        controls.add(btnRefresh); controls.add(btnApprove); controls.add(btnReject);
        
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        btnRefresh.addActionListener(e -> {
            new SwingWorker<List<LeaveApplication>, Void>() {
                @Override
                protected List<LeaveApplication> doInBackground() throws Exception {
                    return RMIClient.getService().getAllPendingLeaves();
                }
                @Override
                protected void done() {
                    try {
                        model.setRowCount(0);
                        for (LeaveApplication la : get()) {
                            model.addRow(new Object[]{
                                la.getLeaveID(),
                                la.getEmployeeID(),
                                la.getStartDate(),
                                la.getEndDate(),
                                la.getStatus(),
                                la.getReason()
                            });
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });
        
        Runnable updateStatus = () -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String lid = (String) model.getValueAt(row, 0);
            String action = (String) model.getValueAt(row, 4); // Hacky way to pass status
            System.out.println("Updating leave ID: " + lid + " to " + action);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return RMIClient.getService().updateLeaveStatus(lid, action);
                }
                @Override
                protected void done() {
                   btnRefresh.doClick();
                }
            }.execute();
        };
        
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                model.setValueAt("Approved", row, 4);
                updateStatus.run();
            }
        });
        
        btnReject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                 model.setValueAt("Rejected", row, 4);
                 updateStatus.run();
            }
        });

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        
        JTextField txtEmpId = new JTextField(10);
        UITheme.styleInput(txtEmpId);
        JTextField txtYear = new JTextField(5);
        UITheme.styleInput(txtYear);
        JButton btnGen = UITheme.createPrimaryButton("Generate Report");
        JTextArea txtReport = new JTextArea();
        txtReport.setEditable(false);
        
        inputPanel.add(new JLabel("Emp ID:"));
        inputPanel.add(txtEmpId);
        inputPanel.add(new JLabel("Year:"));
        inputPanel.add(txtYear);
        inputPanel.add(btnGen);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtReport), BorderLayout.CENTER);
        
        btnGen.addActionListener(e -> {
            String id = txtEmpId.getText().trim();
            String yStr = txtYear.getText().trim();
            
            if (id.isEmpty() || yStr.isEmpty()) return;
            
            try {
                int year = Integer.parseInt(yStr);
                btnGen.setEnabled(false);
                txtReport.setText("Loading...");
                
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return RMIClient.getService().generateYearlyReport(id, year);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            txtReport.setText(get());
                        } catch (Exception ex) {
                            txtReport.setText("Error: " + ex.getMessage());
                        }
                        btnGen.setEnabled(true);
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                txtReport.setText("Invalid Year.");
            }
        });
        
        return panel;
    }

    private JPanel createSyncPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtId = new JTextField(15);
        UITheme.styleInput(txtId);
        JTextField txtLeaves = new JTextField(5);
        UITheme.styleInput(txtLeaves);
        JButton btnSync = UITheme.createPrimaryButton("Sync to Payroll System");
        JLabel lblRes = new JLabel("Status: Idle");
        
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1; panel.add(txtId, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Unpaid Leaves for deduction:"), gbc);
        gbc.gridx = 1; panel.add(txtLeaves, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(btnSync, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(lblRes, gbc);

        btnSync.addActionListener(e -> {
            String id = txtId.getText().trim();
            String lStr = txtLeaves.getText().trim();
            
            if (id.isEmpty() || lStr.isEmpty()) {
                lblRes.setText("Please enter all fields.");
                return;
            }
            
            try {
                int unpaid = Integer.parseInt(lStr);
                btnSync.setEnabled(false);
                lblRes.setText("Syncing (Encrypted)...");
                lblRes.setForeground(Color.BLUE);
                
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return RMIClient.getService().syncWithPayrollSystem(id, unpaid);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                lblRes.setText("Sync Successful!");
                                lblRes.setForeground(new Color(0, 128, 0));
                            } else {
                                lblRes.setText("Sync Failed.");
                                lblRes.setForeground(Color.RED);
                            }
                        } catch (Exception ex) {
                            lblRes.setText("Error: " + ex.getMessage());
                            lblRes.setForeground(Color.RED);
                        }
                        btnSync.setEnabled(true);
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                lblRes.setText("Invalid number for leaves.");
            }
        });

        return panel;
    }
}
