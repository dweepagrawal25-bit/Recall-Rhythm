import java.time.LocalDate;

/** Concepts stick longer once understood, so the gaps grow faster. */
public class Concept extends StudyItem {
    private static final int[] INTERVALS = {1, 4, 10, 21, 45};

    public Concept(int id, String front, String back, String extra, int box,
                   LocalDate last, LocalDate next, int reviews, int correct) {
        super(id, front, back, extra, box, last, next, reviews, correct);
    }

    @Override public String getType() { return "Concept"; }
    @Override public String getExtraLabel() { return "Real-life example (optional)"; }
    @Override public LocalDate calculateNextReview(int box, LocalDate from) { return from.plusDays(INTERVALS[box - 1]); }
    @Override public String getIntervalsSummary() { return describe(INTERVALS); }
}
