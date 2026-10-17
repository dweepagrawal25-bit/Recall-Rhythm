import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Progress screen: three charts and a summary box. Fully supports Dark Mode. */
public class ProgressPanel extends JPanel {
    private final RecallApp app;
    private final BarChartPanel dailyChart = new BarChartPanel("Reviews - last 7 days");
    private final BarChartPanel boxChart = new BarChartPanel("Cards in each Leitner box");
    private final BarChartPanel upcomingChart = new BarChartPanel("Reviews due - next 7 days");
    private final JLabel summary = new JLabel();
    private final JPanel summaryBox = new JPanel(new BorderLayout());

    public ProgressPanel(RecallApp app) {
        this.app = app;
        setLayout(new GridLayout(2, 2, 14, 14));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        summary.setVerticalAlignment(SwingConstants.TOP);
        summaryBox.setBackground(Theme.CARD_BG);
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        summaryBox.add(summary, BorderLayout.CENTER);

        add(dailyChart);
        add(boxChart);
        add(upcomingChart);
        add(summaryBox);

        app.addListener(this::refresh);
        Theme.addListener(() -> {
            setBackground(Theme.BG);
            summaryBox.setBackground(Theme.CARD_BG);
            summaryBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(14, 18, 14, 18)));
            refresh();
        });
        refresh();
    }

    public void refresh() {
        Deck deck = app.getCurrentDeck();
        LocalDate today = AppClock.today();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE d");

        // 1. reviews per day (all decks)
        Map<LocalDate, int[]> counts = app.getLog().dailyCounts(today, 7);
        String[] dayLabels = new String[7];
        int[] ok = new int[7];
        int[] bad = new int[7];
        int i = 0;
        for (Map.Entry<LocalDate, int[]> e : counts.entrySet()) {
            dayLabels[i] = e.getKey().format(fmt);
            ok[i] = e.getValue()[0];
            bad[i] = e.getValue()[1];
            i++;
        }
        dailyChart.setData(dayLabels, new int[][]{ok, bad}, new Color[]{Theme.GREEN, Theme.RED},
                new String[]{"Remembered", "Forgot"});

        // 2. cards per box (current deck)
        int[] byBox = deck.countByBox();
        String[] boxLabels = new String[StudyItem.MAX_BOX];
        int[] boxValues = new int[StudyItem.MAX_BOX];
        for (int b = 1; b <= StudyItem.MAX_BOX; b++) {
            boxLabels[b - 1] = "Box " + b;
            boxValues[b - 1] = byBox[b];
        }
        boxChart.setData(boxLabels, new int[][]{boxValues}, new Color[]{Theme.INDIGO}, new String[]{deck.getName()});

        // 3. upcoming reviews (current deck)
        Map<LocalDate, Integer> up = app.getScheduler().upcoming(deck, today, 7);
        String[] upLabels = new String[7];
        int[] upValues = new int[7];
        i = 0;
        for (Map.Entry<LocalDate, Integer> e : up.entrySet()) {
            upLabels[i] = (i == 0) ? "Today" : e.getKey().format(fmt);
            upValues[i] = e.getValue();
            i++;
        }
        upcomingChart.setData(upLabels, new int[][]{upValues}, new Color[]{Theme.TEAL}, new String[]{deck.getName()});

        // 4. summary
        StudyItem weakest = null;
        for (StudyItem s : deck.getItems()) {
            if (s.getTimesReviewed() == 0) continue;
            if (weakest == null || s.getAccuracyPercent() < weakest.getAccuracyPercent()) weakest = s;
        }
        int acc = app.getLog().accuracyPercent();
        String weakText = (weakest == null) ? "Review some cards to find out."
                : escape(weakest.getFront()) + " (" + weakest.getAccuracyPercent() + "% correct)";

        String titleColor = Theme.isDarkMode() ? "#F8FAFC" : "#1F2937";
        String mutedColor = Theme.isDarkMode() ? "#94A3B8" : "#6B7280";

        summary.setText("<html><div style='font-family:sans-serif;color:" + titleColor + "'>"
                + "<div style='font-size:15px;font-weight:bold;'>Summary: " + escape(deck.getName()) + "</div><br>"
                + "Cards in deck: <b>" + deck.size() + "</b><br>"
                + "Mastered (box 5): <b>" + deck.getMasteredCount() + "</b><br>"
                + "Due today: <b>" + deck.getDueItems(today).size() + "</b><br>"
                + "Total reviews logged: <b>" + app.getLog().getTotalReviews() + "</b><br>"
                + "Overall accuracy: <b>" + (acc < 0 ? "-" : acc + "%") + "</b><br>"
                + "Current streak: <b>" + app.getLog().getStreak(today) + " day(s)</b><br><br>"
                + "<span style='color:" + mutedColor + "'>Needs attention:</span><br>" + weakText
                + "</div></html>");
        summary.setFont(Theme.font(Font.PLAIN, 13));
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
