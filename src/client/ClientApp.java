package client;

import javax.swing.SwingUtilities;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // persistent error handling not critical for UI look
            }

            // Apply theme tweaks
            UITheme.applyTheme();
            // Launch
            new LoginFrame().setVisible(true);
        });
    }
}
