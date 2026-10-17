import java.time.LocalDate;

/**
 * Abstract parent of everything you can revise (Flashcard, Formula, Concept).
 *
 * Enhanced Leitner / SM-2 system: a card lives in a box from 1 to 5.
 * - AGAIN: drops back to box 1, due tomorrow.
 * - HARD: remains in current box, conservative interval.
 * - GOOD: moves up 1 box.
 * - EASY: moves up 2 boxes.
 *
 * Encapsulation: boxLevel is private and changes through review().
 * Polymorphism: each subclass decides its own gaps in calculateNextReview().
 */
public abstract class StudyItem implements Reviewable, JsonSerializable {
    public static final int MIN_BOX = 1;
    public static final int MAX_BOX = 5;

    private final int id;
    private String front;
    private String back;
    private String extra;
    private String tags;
    private int boxLevel;
    private LocalDate lastReviewed;     // null until the first review
    private LocalDate nextReviewDate;
    private int timesReviewed;
    private int timesCorrect;

    protected StudyItem(int id, String front, String back, String extra, String tags, int boxLevel,
                        LocalDate lastReviewed, LocalDate nextReviewDate,
                        int timesReviewed, int timesCorrect) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.extra = extra;
        this.tags = (tags == null) ? "" : tags.trim();
        this.boxLevel = boxLevel;
        this.lastReviewed = lastReviewed;
        this.nextReviewDate = nextReviewDate;
        this.timesReviewed = timesReviewed;
        this.timesCorrect = timesCorrect;
    }

    protected StudyItem(int id, String front, String back, String extra, int boxLevel,
                        LocalDate lastReviewed, LocalDate nextReviewDate,
                        int timesReviewed, int timesCorrect) {
        this(id, front, back, extra, "", boxLevel, lastReviewed, nextReviewDate, timesReviewed, timesCorrect);
    }

    // ----- each card type must define these (abstraction) -----
    public abstract String getType();
    public abstract String getExtraLabel();
    public abstract LocalDate calculateNextReview(int box, LocalDate from);
    public abstract String getIntervalsSummary();

    // ----- getters -----
    public int getId() { return id; }
    public String getFront() { return front; }
    public String getBack() { return back; }
    public String getExtra() { return extra; }
    public String getTags() { return tags == null ? "" : tags; }
    public int getBoxLevel() { return boxLevel; }
    public LocalDate getLastReviewed() { return lastReviewed; }
    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public int getTimesReviewed() { return timesReviewed; }
    public int getTimesCorrect() { return timesCorrect; }

    public boolean isMastered() { return boxLevel == MAX_BOX; }

    /** Updates card content without resetting review history or box level. */
    public void updateContent(String newFront, String newBack, String newExtra, String newTags) {
        if (newFront != null && !newFront.trim().isEmpty()) this.front = newFront.trim();
        if (newBack != null && !newBack.trim().isEmpty()) this.back = newBack.trim();
        this.extra = (newExtra == null) ? "" : newExtra.trim();
        this.tags = (newTags == null) ? "" : newTags.trim();
    }

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

    /** Review using 4-level rating: AGAIN, HARD, GOOD, EASY. */
    public void review(Rating rating, LocalDate today) {
        timesReviewed++;
        lastReviewed = today;
        switch (rating) {
            case AGAIN:
                boxLevel = MIN_BOX;
                nextReviewDate = today.plusDays(1);
                break;
            case HARD:
                timesCorrect++;
                // Remains in current box, scheduled with short interval
                nextReviewDate = calculateNextReview(Math.max(1, boxLevel - 1), today);
                break;
            case GOOD:
                timesCorrect++;
                if (boxLevel < MAX_BOX) boxLevel++;
                nextReviewDate = calculateNextReview(boxLevel, today);
                break;
            case EASY:
                timesCorrect++;
                boxLevel = Math.min(MAX_BOX, boxLevel + 2);
                nextReviewDate = calculateNextReview(boxLevel, today).plusDays(2);
                break;
        }
    }

    /** Legacy binary review delegate. */
    @Override
    public void review(boolean remembered, LocalDate today) {
        review(remembered ? Rating.GOOD : Rating.AGAIN, today);
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
                + ", \"tags\": " + JsonUtil.quote(tags == null ? "" : tags)
                + ", \"box\": " + boxLevel
                + ", \"lastReviewed\": " + JsonUtil.quote(lastReviewed == null ? "" : lastReviewed.toString())
                + ", \"nextReview\": " + JsonUtil.quote(nextReviewDate.toString())
                + ", \"reviews\": " + timesReviewed
                + ", \"correct\": " + timesCorrect + "}";
    }
}
