import java.time.LocalDate;

/** Standard question/answer card. Classic Leitner gaps. */
public class Flashcard extends StudyItem {
    private static final int[] INTERVALS = {1, 3, 7, 14, 30};

    public Flashcard(int id, String front, String back, String extra, String tags, int box,
                     LocalDate last, LocalDate next, int reviews, int correct) {
        super(id, front, back, extra, tags, box, last, next, reviews, correct);
    }

    public Flashcard(int id, String front, String back, String extra, int box,
                     LocalDate last, LocalDate next, int reviews, int correct) {
        super(id, front, back, extra, box, last, next, reviews, correct);
    }

    @Override public String getType() { return "Flashcard"; }
    @Override public String getExtraLabel() { return "Hint (optional)"; }
    @Override public LocalDate calculateNextReview(int box, LocalDate from) { return from.plusDays(INTERVALS[box - 1]); }
    @Override public String getIntervalsSummary() { return describe(INTERVALS); }
}
