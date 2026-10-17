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
 * Fully supports Dark Mode, code styling, tags display, and audio feedback.
 */
public class FlipCardPanel extends JPanel {
    private String badge = "";
    private String footer = "";
    private String frontText = "";
    private String backText = "";
    private String tags = "";
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

    public void showCard(String badge, Color accent, String frontText, String backText, String footer, String tags) {
        stopAnimation();
        this.badge = badge;
        this.accent = accent;
        this.frontText = frontText;
        this.backText = backText;
        this.footer = footer;
        this.tags = (tags == null) ? "" : tags.trim();
        this.showingFront = true;
        this.messageMode = false;
        repaint();
    }

    public void showCard(String badge, Color accent, String frontText, String backText, String footer) {
        showCard(badge, accent, frontText, backText, footer, "");
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
        SoundEffect.playFlip();
        animating = true;
        final long start = System.nanoTime();
        final double durationMs = 380;
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
        int cardW = Math.min(w - 40, 740);
        int cardH = Math.min(h - 24, 350);
        int fullX = (w - cardW) / 2;
        int y = (h - cardH) / 2;
        int drawW = Math.max(2, (int) (cardW * scale));
        int x = (w - drawW) / 2;

        // shadow
        g2.setColor(new Color(0, 0, 0, Theme.isDarkMode() ? 65 : 28));
        g2.fillRoundRect(x + 4, y + 8, drawW, cardH, 28, 28);

        if (messageMode) {
            g2.setColor(Theme.CARD_BG);
            g2.fillRoundRect(x, y, drawW, cardH, 28, 28);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(x, y, drawW - 1, cardH - 1, 28, 28);
            g2.setFont(Theme.font(Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Theme.INDIGO_LIGHT);
            g2.drawString(messageTitle, (w - fm.stringWidth(messageTitle)) / 2, y + cardH / 2 - 8);
            g2.setFont(Theme.font(Font.PLAIN, 15));
            fm = g2.getFontMetrics();
            g2.setColor(Theme.MUTED);
            g2.drawString(messageSub, (w - fm.stringWidth(messageSub)) / 2, y + cardH / 2 + 26);
            g2.dispose();
            return;
        }

        Color cardBackground = showingFront ? accent : Theme.CARD_BG;
        g2.setColor(cardBackground);
        g2.fillRoundRect(x, y, drawW, cardH, 28, 28);

        if (!showingFront) {
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x + 1, y + 1, drawW - 3, cardH - 3, 28, 28);
        }

        // everything below is clipped to the card
        g2.setClip(new RoundRectangle2D.Float(x, y, drawW, cardH, 28, 28));

        // Top Header Info
        g2.setFont(Theme.font(Font.BOLD, 12));
        g2.setColor(showingFront ? new Color(255, 255, 255, 220) : accent);
        String headerLine = badge + "   |   " + (showingFront ? "QUESTION" : "ANSWER");
        if (!tags.isEmpty()) headerLine += "   🏷️ " + tags;
        g2.drawString(headerLine, fullX + 26, y + 32);

        // Footer info
        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(showingFront ? new Color(255, 255, 255, 190) : Theme.MUTED);
        g2.drawString(footer, fullX + 26, y + cardH - 18);

        int pad = 42;
        int areaW = cardW - 2 * pad;
        int areaTop = y + 54;
        int areaBottom = y + cardH - 46;
        String rawBody = showingFront ? frontText : backText;

        // Detect if text looks like code (starts/ends with backticks or has multiple code symbols)
        boolean isCode = rawBody.contains("```") || rawBody.contains("class ") || rawBody.contains("public static") || rawBody.contains("def ") || rawBody.contains("{");
        String cleanBody = rawBody.replace("```", "").trim();

        List<String> lines = new ArrayList<>();
        FontMetrics fm = null;
        int startFontSize = isCode ? 20 : 26;
        int minFontSize = isCode ? 12 : 13;

        for (int size = startFontSize; size >= minFontSize; size -= 2) {
            Font f = isCode ? new Font(Font.MONOSPACED, Font.PLAIN, size) : Theme.font(Font.BOLD, size);
            g2.setFont(f);
            fm = g2.getFontMetrics();
            lines = wrap(cleanBody, fm, areaW);
            if (lines.size() * fm.getHeight() <= areaBottom - areaTop) break;
        }

        int totalH = lines.size() * fm.getHeight();
        int ty = areaTop + (areaBottom - areaTop - totalH) / 2 + fm.getAscent();

        Color textColor = showingFront ? Color.WHITE : Theme.TEXT;
        g2.setColor(textColor);
        for (String line : lines) {
            int lineX = isCode ? (fullX + pad + 10) : (fullX + (cardW - fm.stringWidth(line)) / 2);
            g2.drawString(line, lineX, ty);
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
