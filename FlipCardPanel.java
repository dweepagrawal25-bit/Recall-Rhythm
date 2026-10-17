import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Draws one big flash card. flip() plays a real flip animation:
 * the card narrows to a line, the face changes, then it widens again.
 */
public class FlipCardPanel extends JPanel {
    private String badge = "";
    private String footer = "";
    private String frontText = "";
    private String backText = "";
    private Color accent = Theme.INDIGO;
    private boolean showingFront = true;
    private double scale = 1.0;
    private boolean animating = false;
    private boolean messageMode = false;
    private String messageTitle = "";
    private String messageSub = "";
    private Timer timer;

    public FlipCardPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(660, 330));
    }

    public void showCard(String badge, Color accent, String frontText, String backText, String footer) {
        stopAnimation();
        this.badge = badge;
        this.accent = accent;
        this.frontText = frontText;
        this.backText = backText;
        this.footer = footer;
        this.showingFront = true;
        this.messageMode = false;
        repaint();
    }

    public void showMessage(String title, String sub) {
        stopAnimation();
        this.messageMode = true;
        this.messageTitle = title;
        this.messageSub = sub;
        repaint();
    }

    private void stopAnimation() {
        if (timer != null) timer.stop();
        animating = false;
        scale = 1.0;
    }

    public void flip() {
        if (animating || messageMode) return;
        animating = true;
        final long start = System.nanoTime();
        final double durationMs = 420;
        final boolean[] swapped = {false};
        timer = new Timer(15, e -> {
            double t = (System.nanoTime() - start) / 1e6 / durationMs;
            if (t >= 1) {
                t = 1;
                timer.stop();
                animating = false;
            }
            scale = Math.abs(Math.cos(t * Math.PI));
            if (t >= 0.5 && !swapped[0]) {
                showingFront = !showingFront;
                swapped[0] = true;
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int cardW = Math.min(w - 40, 720);
        int cardH = Math.min(h - 24, 340);
        int fullX = (w - cardW) / 2;
        int y = (h - cardH) / 2;
        int drawW = Math.max(2, (int) (cardW * scale));
        int x = (w - drawW) / 2;

        g2.setColor(new Color(0, 0, 0, 28));
        g2.fillRoundRect(x + 4, y + 9, drawW, cardH, 30, 30);

        if (messageMode) {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, drawW, cardH, 30, 30);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(x, y, drawW - 1, cardH - 1, 30, 30);
            g2.setFont(Theme.font(Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Theme.INDIGO);
            g2.drawString(messageTitle, (w - fm.stringWidth(messageTitle)) / 2, y + cardH / 2 - 6);
            g2.setFont(Theme.font(Font.PLAIN, 16));
            fm = g2.getFontMetrics();
            g2.setColor(Theme.MUTED);
            g2.drawString(messageSub, (w - fm.stringWidth(messageSub)) / 2, y + cardH / 2 + 28);
            g2.dispose();
            return;
        }

        g2.setColor(showingFront ? accent : Color.WHITE);
        g2.fillRoundRect(x, y, drawW, cardH, 30, 30);
        if (!showingFront) {
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x + 1, y + 1, drawW - 3, cardH - 3, 30, 30);
        }

        // everything below is clipped to the (possibly narrowed) card
        g2.setClip(new RoundRectangle2D.Float(x, y, drawW, cardH, 30, 30));

        g2.setFont(Theme.font(Font.BOLD, 12));
        g2.setColor(showingFront ? new Color(255, 255, 255, 210) : accent);
        g2.drawString(badge + "   |   " + (showingFront ? "QUESTION" : "ANSWER"), fullX + 26, y + 32);

        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(showingFront ? new Color(255, 255, 255, 190) : Theme.MUTED);
        g2.drawString(footer, fullX + 26, y + cardH - 18);

        int pad = 40;
        int areaW = cardW - 2 * pad;
        int areaTop = y + 52;
        int areaBottom = y + cardH - 44;
        String body = showingFront ? frontText : backText;

        List<String> lines = new ArrayList<>();
        FontMetrics fm = null;
        for (int size = 28; size >= 13; size -= 2) {
            g2.setFont(Theme.font(Font.BOLD, size));
            fm = g2.getFontMetrics();
            lines = wrap(body, fm, areaW);
            if (lines.size() * fm.getHeight() <= areaBottom - areaTop) break;
        }
        int totalH = lines.size() * fm.getHeight();
        int ty = areaTop + (areaBottom - areaTop - totalH) / 2 + fm.getAscent();
        g2.setColor(showingFront ? Color.WHITE : Theme.TEXT);
        for (String line : lines) {
            g2.drawString(line, fullX + (cardW - fm.stringWidth(line)) / 2, ty);
            ty += fm.getHeight();
        }
        g2.dispose();
    }

    private List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            String current = "";
            for (String word : paragraph.split(" ")) {
                String trial = current.isEmpty() ? word : current + " " + word;
                if (current.isEmpty() || fm.stringWidth(trial) <= maxWidth) {
                    current = trial;
                } else {
                    lines.add(current);
                    current = word;
                }
            }
            lines.add(current);
        }
        return lines;
    }
}
