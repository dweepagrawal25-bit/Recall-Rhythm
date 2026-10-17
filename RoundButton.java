import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** A rounded, coloured button that looks the same on every operating system. */
public class RoundButton extends JButton {
    private final Color base;
    private boolean hover = false;

    public RoundButton(String text, Color base) {
        super(text);
        this.base = base;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setForeground(Color.WHITE);
        setFont(Theme.font(Font.BOLD, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    /** Makes the button smaller (used in the header bar). */
    public RoundButton compact() {
        setFont(Theme.font(Font.BOLD, 12));
        setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = !isEnabled() ? new Color(0xC7C9D1) : (hover ? base.darker() : base);
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}
