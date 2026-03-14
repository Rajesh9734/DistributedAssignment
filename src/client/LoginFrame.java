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
        setSize(620, 420); // a bit wider / taller to avoid crowding
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG); // Ensure background matches
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); // More outer padding

        // Header
        JLabel lblHeader = new JLabel("Welcome back", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 28)); // Larger font
        lblHeader.setForeground(UITheme.HEADER_FG);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0)); // More padding
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Form Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email Label
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(lblEmail, gbc);

        // Email Field
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        txtEmail = new JTextField(22);
        UITheme.styleInput(txtEmail);
        txtEmail.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(txtEmail, gbc);

        // Password Label
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(lblPass, gbc);

        // Password Field
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        txtPass = new JPasswordField(22);
        UITheme.styleInput(txtPass);
        txtPass.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(txtPass, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(28, 12, 8, 12);
        btnLogin = UITheme.createPrimaryButton("Login");
        btnLogin.setPreferredSize(new Dimension(150, 42));
        panel.add(btnLogin, gbc);

        // status label styling
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 12, 0, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(UITheme.MUTED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblStatus, gbc);

        // wrap panel in a white card
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BG, 1), BorderFactory.createEmptyBorder(20,20,20,20)));
        card.add(panel, BorderLayout.CENTER);
        mainPanel.add(card, BorderLayout.CENTER);
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
