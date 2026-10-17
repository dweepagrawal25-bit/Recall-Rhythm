import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** A small translucent box in the header showing one number (due today, streak, accuracy). */
public class StatChip extends JPanel {
    private final JLabel valueLabel = new JLabel("0", SwingConstants.CENTER);

    public StatChip(String caption) {
        setOpaque(false);
        setLayout(new GridLayout(2, 1));
        setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        valueLabel.setFont(Theme.font(Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        JLabel captionLabel = new JLabel(caption, SwingConstants.CENTER);
        captionLabel.setFont(Theme.font(Font.BOLD, 10));
        captionLabel.setForeground(new Color(0xE0E7FF));
        add(valueLabel);
        add(captionLabel);
    }

    public void setValue(String value) { valueLabel.setText(value); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}
