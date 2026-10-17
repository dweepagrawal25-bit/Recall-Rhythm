import java.time.LocalDate;

/** Factory: the one place that knows how to build each card type. */
public final class ItemFactory {
    public static final String[] TYPES = {"Flashcard", "Formula", "Concept"};
    public static final int MAX_TEXT = 400;

    private ItemFactory() { }

    public static String getExtraLabel(String type) {
        switch (type) {
            case "Formula": return "Use when (optional)";
            case "Concept": return "Real-life example (optional)";
            default: return "Hint (optional)";
        }
    }

    /** Builds a brand-new card (box 1, due today) after validating the text. */
    public static StudyItem create(String type, int id, String front, String back, String extra,
                                   LocalDate today) throws InvalidCardException {
        String f = front == null ? "" : front.trim();
        String b = back == null ? "" : back.trim();
        String e = extra == null ? "" : extra.trim();
        if (f.isEmpty()) throw new InvalidCardException("The question / front side cannot be empty.");
        if (b.isEmpty()) throw new InvalidCardException("The answer / back side cannot be empty.");
        if (f.length() > MAX_TEXT || b.length() > MAX_TEXT || e.length() > MAX_TEXT) {
            throw new InvalidCardException("Each field can have at most " + MAX_TEXT + " characters.");
        }
        StudyItem item = build(type, id, f, b, e, StudyItem.MIN_BOX, null, today, 0, 0);
        if (item == null) throw new InvalidCardException("Unknown card type: " + type);
        return item;
    }

    /** Rebuilds a card from saved data. */
    public static StudyItem restore(String type, int id, String front, String back, String extra,
                                    int box, LocalDate last, LocalDate next,
                                    int reviews, int correct) throws CorruptFileException {
        if (box < StudyItem.MIN_BOX || box > StudyItem.MAX_BOX) {
            throw new CorruptFileException("box level must be between 1 and 5 but was " + box);
        }
        if (reviews < 0 || correct < 0 || correct > reviews) {
            throw new CorruptFileException("review counts are invalid (reviews=" + reviews + ", correct=" + correct + ")");
        }
        StudyItem item = build(type, id, front, back, extra, box, last, next, reviews, correct);
        if (item == null) throw new CorruptFileException("unknown card type '" + type + "'");
        return item;
    }

    private static StudyItem build(String type, int id, String f, String b, String e, int box,
                                   LocalDate last, LocalDate next, int reviews, int correct) {
        switch (type) {
            case "Flashcard": return new Flashcard(id, f, b, e, box, last, next, reviews, correct);
            case "Formula":   return new Formula(id, f, b, e, box, last, next, reviews, correct);
            case "Concept":   return new Concept(id, f, b, e, box, last, next, reviews, correct);
            default: return null;
        }
    }
}
