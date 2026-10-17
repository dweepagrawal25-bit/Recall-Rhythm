import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Decides WHAT to revise today and what is coming up. */
public class Scheduler {

    /** Today's queue: most overdue first, then weakest (lowest box) first. */
    public List<StudyItem> buildSession(Deck deck, LocalDate today) throws EmptyDeckException {
        if (deck.isEmpty()) {
            throw new EmptyDeckException("The deck '" + deck.getName() + "' has no cards yet.");
        }
        List<StudyItem> due = new ArrayList<>(deck.getDueItems(today));
        due.sort(Comparator.comparing(StudyItem::getNextReviewDate)
                .thenComparingInt(StudyItem::getBoxLevel)
                .thenComparingInt(StudyItem::getId));
        return due;
    }

    /** Number of reviews due on each of the next N days (today also includes overdue cards). */
    public Map<LocalDate, Integer> upcoming(Deck deck, LocalDate from, int days) {
        Map<LocalDate, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = from.plusDays(i);
            int count = 0;
            for (StudyItem s : deck.getItems()) {
                LocalDate due = s.getNextReviewDate();
                boolean hit = (i == 0) ? !due.isAfter(from) : due.equals(day);
                if (hit) count++;
            }
            result.put(day, count);
        }
        return result;
    }

    /** Earliest upcoming review date in the deck, or null if the deck is empty. */
    public LocalDate nextDueDate(Deck deck) {
        LocalDate best = null;
        for (StudyItem s : deck.getItems()) {
            if (best == null || s.getNextReviewDate().isBefore(best)) best = s.getNextReviewDate();
        }
        return best;
    }
}
