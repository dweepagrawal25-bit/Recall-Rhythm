import java.time.LocalDate;

/**
 * Abstract parent of everything you can revise (Flashcard, Formula, Concept).
 *
 * Leitner system: a card lives in a box from 1 to 5. Remember it -> it moves up a box and
 * is shown after a longer gap. Forget it -> it drops back to box 1.
 *
 * Encapsulation: boxLevel is private and can ONLY change inside review().
 * Polymorphism: each subclass decides its own gaps in calculateNextReview().
 */
public abstract class StudyItem implements Reviewable, JsonSerializable {
    public static final int MIN_BOX = 1;
    public static final int MAX_BOX = 5;

    private final int id;
    private final String front;
    private final String back;
    private final String extra;
    private int boxLevel;
    private LocalDate lastReviewed;     // null until the first review
    private LocalDate nextReviewDate;
    private int timesReviewed;
    private int timesCorrect;

    protected StudyItem(int id, String front, String back, String extra, int boxLevel,
                        LocalDate lastReviewed, LocalDate nextReviewDate,
                        int timesReviewed, int timesCorrect) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.extra = extra;
        this.boxLevel = boxLevel;
        this.lastReviewed = lastReviewed;
        this.nextReviewDate = nextReviewDate;
        this.timesReviewed = timesReviewed;
        this.timesCorrect = timesCorrect;
    }

    // ----- each card type must define these (abstraction) -----
    public abstract String getType();
    public abstract String getExtraLabel();
    public abstract LocalDate calculateNextReview(int box, LocalDate from);
    public abstract String getIntervalsSummary();

    // ----- getters (no setters: state changes only through review()) -----
    public int getId() { return id; }
    public String getFront() { return front; }
    public String getBack() { return back; }
    public String getExtra() { return extra; }
    public int getBoxLevel() { return boxLevel; }
    public LocalDate getLastReviewed() { return lastReviewed; }
    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public int getTimesReviewed() { return timesReviewed; }
    public int getTimesCorrect() { return timesCorrect; }

    public boolean isMastered() { return boxLevel == MAX_BOX; }

    /** Percentage of reviews answered correctly, or -1 if never reviewed. */
    public int getAccuracyPercent() {
        if (timesReviewed == 0) return -1;
        return (int) Math.round(timesCorrect * 100.0 / timesReviewed);
    }

    /** What the back of the card shows: the answer plus the optional extra line. */
    public String getBackText() {
        if (extra == null || extra.trim().isEmpty()) return back;
        String label = getExtraLabel().replace(" (optional)", "");
        return back + "\n\n" + label + ": " + extra;
    }

    @Override
    public boolean isDueOn(LocalDate date) {
        return !nextReviewDate.isAfter(date);
    }

    /** The ONLY place where the box level changes. */
    @Override
    public void review(boolean remembered, LocalDate today) {
        timesReviewed++;
        if (remembered) {
            timesCorrect++;
            if (boxLevel < MAX_BOX) boxLevel++;
        } else {
            boxLevel = MIN_BOX;
        }
        lastReviewed = today;
        nextReviewDate = calculateNextReview(boxLevel, today);
    }

    protected static String describe(int[] intervals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < intervals.length; i++) {
            if (i > 0) sb.append(" > ");
            sb.append(intervals[i]);
        }
        return sb.append(" days").toString();
    }

    @Override
    public String toJson() {
        return "    {\"id\": " + id
                + ", \"type\": " + JsonUtil.quote(getType())
                + ", \"front\": " + JsonUtil.quote(front)
                + ", \"back\": " + JsonUtil.quote(back)
                + ", \"extra\": " + JsonUtil.quote(extra == null ? "" : extra)
                + ", \"box\": " + boxLevel
                + ", \"lastReviewed\": " + JsonUtil.quote(lastReviewed == null ? "" : lastReviewed.toString())
                + ", \"nextReview\": " + JsonUtil.quote(nextReviewDate.toString())
                + ", \"reviews\": " + timesReviewed
                + ", \"correct\": " + timesCorrect + "}";
    }
}
