package client;

import javax.swing.SwingUtilities;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Eagerly connect or connect inside LoginFrame
            // Better to eager connect? But if server is down app might hang.
            // RMIClient handles lazy connect.
            new LoginFrame().setVisible(true);
        });
    }
}

