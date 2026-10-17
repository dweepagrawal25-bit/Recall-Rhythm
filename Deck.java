import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

/** A named collection of study items (composition: a Deck HAS StudyItems). */
public class Deck implements JsonSerializable {
    private final String name;
    private final List<StudyItem> items = new ArrayList<>();

    public Deck(String name) { this.name = name.trim(); }

    public String getName() { return name; }
    public boolean isEmpty() { return items.isEmpty(); }
    public int size() { return items.size(); }
    public List<StudyItem> getItems() { return Collections.unmodifiableList(items); }

    public void addItem(StudyItem item) throws InvalidCardException {
        for (StudyItem existing : items) {
            if (existing.getId() == item.getId()) {
                throw new InvalidCardException("Duplicate card id " + item.getId() + ".");
            }
            if (existing.getFront().trim().equalsIgnoreCase(item.getFront().trim())) {
                throw new InvalidCardException("A card with this question already exists in the deck.");
            }
        }
        items.add(item);
    }

    public boolean removeItem(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    public int nextId() {
        int max = 0;
        for (StudyItem s : items) max = Math.max(max, s.getId());
        return max + 1;
    }

    public List<StudyItem> getDueItems(LocalDate date) {
        List<StudyItem> due = new ArrayList<>();
        for (StudyItem s : items) {
            if (s.isDueOn(date)) due.add(s);
        }
        return due;
    }

    /** index 1..5 = number of cards in that Leitner box (index 0 unused). */
    public int[] countByBox() {
        int[] counts = new int[StudyItem.MAX_BOX + 1];
        for (StudyItem s : items) counts[s.getBoxLevel()]++;
        return counts;
    }

    public int getMasteredCount() {
        int n = 0;
        for (StudyItem s : items) if (s.isMastered()) n++;
        return n;
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"name\": ").append(JsonUtil.quote(name)).append(",\n  \"items\": [\n");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toJson());
            if (i < items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}\n");
        return sb.toString();
    }
}
