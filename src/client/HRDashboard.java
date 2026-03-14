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
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        menu.add(logout);
        mb.add(menu);
        setJMenuBar(mb);
        
        add(tabbedPane);
    }

    private JPanel createManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // --- Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Register New Employee"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtFirst = new JTextField(15);
        JTextField txtLast = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        JTextField txtIc = new JTextField(15);
        JTextField txtDesig = new JTextField(15);
        JTextField txtAddr = new JTextField(15);
        
        JButton btnRegister = new JButton("Register");
        JLabel lblResult = new JLabel(" "); // To display generated ID
        
        int r = 0;
        addRow(formPanel, gbc, r++, "First Name:", txtFirst);
        addRow(formPanel, gbc, r++, "Last Name:", txtLast);
        addRow(formPanel, gbc, r++, "Email:", txtEmail);
        addRow(formPanel, gbc, r++, "Password:", txtPass);
        addRow(formPanel, gbc, r++, "IC/Passport:", txtIc);
        addRow(formPanel, gbc, r++, "Designation:", txtDesig);
        addRow(formPanel, gbc, r++, "Address:", txtAddr);
        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2; formPanel.add(btnRegister, gbc);
        gbc.gridy = r++; formPanel.add(lblResult, gbc);

        // --- List ---
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Designation", "Balance"}, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        
        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(scroll, BorderLayout.CENTER);
        
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
                             model.addRow(new Object[]{e.getId(), e.getFirstName() + " " + e.getLastName(), e.getEmail(), e.getDesignation(), e.getLeaveBalance()});
                         }
                     } catch (Exception ex) { ex.printStackTrace(); }
                 }
             }.execute();
        };
        
        // Initial Refresh
        refreshTable.run();

        btnRegister.addActionListener(e -> {
            String f = txtFirst.getText().trim();
            String l = txtLast.getText().trim();
            String em = txtEmail.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String ic = txtIc.getText().trim();
            String d = txtDesig.getText().trim();
            String a = txtAddr.getText().trim();
            
            if (f.isEmpty() || em.isEmpty() || p.isEmpty()) {
                lblResult.setText("Name, Email, Pass required.");
                return;
            }
            
            // Auto-Generate ID
            String id = (f.length() > 0 ? f.substring(0, 1).toLowerCase() : "u") + (int)(Math.random() * 10000);
            
            Employee emp = new Employee(id, em, p, "EMP", f, l, ic, d, a);
            
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
                            txtFirst.setText(""); txtLast.setText(""); txtEmail.setText(""); txtPass.setText("");
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
        
        return mainPanel;
    }
    
    private void addRow(JPanel p, GridBagConstraints gbc, int y, String lbl, Component c) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; p.add(new JLabel(lbl), gbc);
        gbc.gridx = 1; p.add(c, gbc);
    }
    
    private JPanel createLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] cols = {"Leave ID", "Emp ID", "Start", "End", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        
        JPanel controls = new JPanel();
        JButton btnRefresh = new JButton("Refresh Pending");
        JButton btnApprove = new JButton("Approve Selected");
        JButton btnReject = new JButton("Reject Selected");
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
                            model.addRow(new Object[]{la.getLeaveID(), la.getEmployeeID(), la.getStartDate(), la.getEndDate(), la.getStatus()});
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
        JTextField txtYear = new JTextField(5);
        JButton btnGen = new JButton("Generate Report");
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
        JTextField txtLeaves = new JTextField(5);
        JButton btnSync = new JButton("Sync to Payroll System");
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
