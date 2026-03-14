package client;

import common.HRMInterface;
import common.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {

    private JTextField txtEmail; // Changed
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginFrame() {
        setTitle("HRM System Login");
        setSize(500, 350); // Increased size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JLabel lblHeader = new JLabel("Welcome back", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 28)); // Larger font
        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0)); // More padding
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Form Panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email Label // Changed
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3; // Give labels some weight
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(lblEmail, gbc);

        // Email Field // Changed
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Give fields more weight to expand
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(250, 35)); // Specific size
        txtEmail.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(txtEmail, gbc);

        // Password Label
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(lblPass, gbc);

        // Password Field
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(250, 35)); // Specific size
        txtPass.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(txtPass, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10); // More spacing before button
        btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(150, 45)); // Much larger button
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(btnLogin, gbc);

        // Status Label
        gbc.gridy = 3;
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblStatus, gbc);

        mainPanel.add(panel, BorderLayout.CENTER); // Add form to center
        add(mainPanel); // Add main panel to frame

        // Action
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Please enter Email and Password.");
            return;
        }

        // Disable UI
        btnLogin.setEnabled(false);
        lblStatus.setText("Authenticating...");
        lblStatus.setForeground(Color.BLUE);

        // SwingWorker for background RMI call
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                HRMInterface service = RMIClient.getService();
                if (service == null) {
                    throw new Exception("Cannot connect to server.");
                }
                return service.login(email, pass);
            }

            @Override
            protected void done() {
                try {
                    User user = get(); // This blocks only until background task is done
                    if (user != null) {
                        // Success
                        openDashboard(user);
                        dispose(); // Close login frame
                    } else {
                        // Failed
                        lblStatus.setText("Invalid credentials.");
                        lblStatus.setForeground(Color.RED);
                        btnLogin.setEnabled(true);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    lblStatus.setText("Error: " + ex.getMessage());
                    lblStatus.setForeground(Color.RED);
                    ex.printStackTrace();
                    btnLogin.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void openDashboard(User user) {
        if ("HR".equalsIgnoreCase(user.getRole())) {
            SwingUtilities.invokeLater(() -> new HRDashboard().setVisible(true));
        } else {
            SwingUtilities.invokeLater(() -> new EmployeeDashboard(user).setVisible(true));
        }
    }
}

