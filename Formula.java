import java.time.LocalDate;

/** Formulas are easy to forget, so they come back more often. */
public class Formula extends StudyItem {
    private static final int[] INTERVALS = {1, 2, 4, 8, 16};

    public Formula(int id, String front, String back, String extra, int box,
                   LocalDate last, LocalDate next, int reviews, int correct) {
        super(id, front, back, extra, box, last, next, reviews, correct);
    }

    @Override public String getType() { return "Formula"; }
    @Override public String getExtraLabel() { return "Use when (optional)"; }
    @Override public LocalDate calculateNextReview(int box, LocalDate from) { return from.plusDays(INTERVALS[box - 1]); }
    @Override public String getIntervalsSummary() { return describe(INTERVALS); }
}
