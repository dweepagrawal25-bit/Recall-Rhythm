 import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point: run this class to start RecallRhythm. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look
            }
            RecallApp app = new RecallApp();
            app.init();

            MainPanel panel = new MainPanel(app);
            JFrame frame = new JFrame("RecallRhythm - Spaced Revision Planner");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.setSize(1060, 730);
            frame.setMinimumSize(new Dimension(920, 640));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.focusReview();

            if (!app.getWarnings().isEmpty()) {
                JOptionPane.showMessageDialog(frame, String.join("\n\n", app.getWarnings()),
                        "Some data could not be loaded", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
