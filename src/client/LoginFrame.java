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
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email Label // Changed
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Email:"), gbc);

        // Email Field // Changed
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);

        // Password Label
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        // Password Field
        gbc.gridx = 1;
        txtPass = new JPasswordField(15);
        panel.add(txtPass, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        btnLogin = new JButton("Login");
        panel.add(btnLogin, gbc);

        // Status Label
        gbc.gridy = 3;
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblStatus, gbc);

        add(panel);

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

