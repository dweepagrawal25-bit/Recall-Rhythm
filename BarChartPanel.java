import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** A simple stacked bar chart drawn by hand with Java 2D (no chart library needed). */
public class BarChartPanel extends JPanel {
    private final String title;
    private String[] labels = new String[0];
    private int[][] series = new int[0][];
    private Color[] colors = new Color[0];
    private String[] legend = new String[0];

    public BarChartPanel(String title) {
        this.title = title;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER));
    }

    /** series[s][i] = value of series s for bar i. Series are stacked on top of each other. */
    public void setData(String[] labels, int[][] series, Color[] colors, String[] legend) {
        this.labels = labels;
        this.series = series;
        this.colors = colors;
        this.legend = legend;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setFont(Theme.font(Font.BOLD, 14));
        g2.setColor(Theme.TEXT);
        g2.drawString(title, 16, 26);

        // legend (right-aligned on the title row)
        g2.setFont(Theme.font(Font.PLAIN, 11));
        FontMetrics lfm = g2.getFontMetrics();
        int lx = w - 16;
        for (int i = legend.length - 1; i >= 0; i--) {
            lx -= lfm.stringWidth(legend[i]);
            g2.setColor(Theme.MUTED);
            g2.drawString(legend[i], lx, 26);
            lx -= 14;
            g2.setColor(colors[i]);
            g2.fillRect(lx, 17, 9, 9);
            lx -= 14;
        }

        int left = 38, right = w - 18, top = 46, bottom = h - 30;
        int n = labels.length;

        int maxTotal = 0;
        int[] totals = new int[n];
        for (int i = 0; i < n; i++) {
            for (int[] s : series) totals[i] += s[i];
            maxTotal = Math.max(maxTotal, totals[i]);
        }
        int step = Math.max(1, (int) Math.ceil(maxTotal / 4.0));
        int scaleMax = step * 4;

        // grid + y labels
        g2.setFont(Theme.font(Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        for (int t = 0; t <= 4; t++) {
            int y = bottom - t * (bottom - top) / 4;
            g2.setColor(Theme.BORDER);
            g2.drawLine(left, y, right, y);
            String text = String.valueOf(t * step);
            g2.setColor(Theme.MUTED);
            g2.drawString(text, left - 6 - fm.stringWidth(text), y + 4);
        }

        if (n == 0 || maxTotal == 0) {
            String msg = "No data yet";
            g2.setColor(Theme.MUTED);
            g2.drawString(msg, (left + right - fm.stringWidth(msg)) / 2, (top + bottom) / 2);
        }

        // bars
        if (n > 0) {
            int slot = (right - left) / n;
            int barW = Math.min(48, (int) (slot * 0.6));
            for (int i = 0; i < n; i++) {
                int x = left + i * slot + (slot - barW) / 2;
                int yBase = bottom;
                for (int s = 0; s < series.length; s++) {
                    int hgt = (int) Math.round(series[s][i] * (double) (bottom - top) / scaleMax);
                    if (hgt > 0) {
                        g2.setColor(colors[s]);
                        g2.fillRect(x, yBase - hgt, barW, hgt);
                        yBase -= hgt;
                    }
                }
                if (totals[i] > 0) {
                    String text = String.valueOf(totals[i]);
                    g2.setColor(Theme.TEXT);
                    g2.drawString(text, x + (barW - fm.stringWidth(text)) / 2, yBase - 4);
                }
                g2.setColor(Theme.MUTED);
                g2.drawString(labels[i], left + i * slot + (slot - fm.stringWidth(labels[i])) / 2, bottom + 18);
            }
        }
        g2.dispose();
    }
}
