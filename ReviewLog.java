import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Records every review in a CSV file and calculates streak, accuracy and daily counts from it. */
public class ReviewLog {

    /** One row of the history file. */
    public static class Entry {
        private final LocalDate date;
        private final String deck;
        private final boolean remembered;
        private final Rating rating;

        public Entry(LocalDate date, String deck, boolean remembered) {
            this(date, deck, remembered ? Rating.GOOD : Rating.AGAIN);
        }

        public Entry(LocalDate date, String deck, Rating rating) {
            this.date = date;
            this.deck = deck;
            this.rating = rating;
            this.remembered = rating.isSuccessful();
        }

        public LocalDate getDate() { return date; }
        public String getDeck() { return deck; }
        public boolean isRemembered() { return remembered; }
        public Rating getRating() { return rating; }
    }

    private static final String HEADER = "date,deck,item_id,type,result,new_box,question";
    private final Path file;
    private final List<Entry> entries = new ArrayList<>();

    public ReviewLog(String path) { this.file = Paths.get(path); }

    public void load() throws IOException, CorruptFileException {
        entries.clear();
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<Entry> loaded = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {          // line 0 is the header
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            List<String> f = parseCsvLine(line);
            if (f.size() < 7) throw new CorruptFileException("review_history.csv, line " + (i + 1) + " has missing columns");
            try {
                LocalDate date = LocalDate.parse(f.get(0));
                Integer.parseInt(f.get(2));
                Integer.parseInt(f.get(5));
                String result = f.get(4).toUpperCase();
                Rating rating;
                if (result.equals("REMEMBERED") || result.equals("GOOD")) rating = Rating.GOOD;
                else if (result.equals("FORGOT") || result.equals("AGAIN")) rating = Rating.AGAIN;
                else if (result.equals("HARD")) rating = Rating.HARD;
                else if (result.equals("EASY")) rating = Rating.EASY;
                else throw new CorruptFileException("review_history.csv, line " + (i + 1) + " has an unknown result '" + result + "'");
                loaded.add(new Entry(date, f.get(1), rating));
            } catch (DateTimeParseException | NumberFormatException e) {
                throw new CorruptFileException("review_history.csv, line " + (i + 1) + " contains an invalid date or number");
            }
        }
        entries.addAll(loaded);
    }

    public void log(String deckName, StudyItem item, Rating rating, LocalDate date) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        boolean isNewFile = !Files.exists(file) || Files.size(file) == 0;
        StringBuilder sb = new StringBuilder();
        if (isNewFile) sb.append(HEADER).append("\n");
        sb.append(date).append(',')
          .append(csv(deckName)).append(',')
          .append(item.getId()).append(',')
          .append(item.getType()).append(',')
          .append(rating.name()).append(',')
          .append(item.getBoxLevel()).append(',')
          .append(csv(item.getFront())).append("\n");
        Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        entries.add(new Entry(date, deckName, rating));
    }

    public void log(String deckName, StudyItem item, boolean remembered, LocalDate date) throws IOException {
        log(deckName, item, remembered ? Rating.GOOD : Rating.AGAIN, date);
    }

    public int getTotalReviews() { return entries.size(); }

    /** Overall accuracy in percent, or -1 when there is no history yet. */
    public int accuracyPercent() {
        if (entries.isEmpty()) return -1;
        int ok = 0;
        for (Entry e : entries) if (e.isRemembered()) ok++;
        return (int) Math.round(ok * 100.0 / entries.size());
    }

    /** Consecutive days with at least one review, ending today (or yesterday if today has none yet). */
    public int getStreak(LocalDate today) {
        Set<LocalDate> days = new HashSet<>();
        for (Entry e : entries) days.add(e.getDate());
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** For each of the last N days ending at 'end': {remembered, forgot}. */
    public Map<LocalDate, int[]> dailyCounts(LocalDate end, int days) {
        Map<LocalDate, int[]> result = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) result.put(end.minusDays(i), new int[2]);
        for (Entry e : entries) {
            int[] counts = result.get(e.getDate());
            if (counts != null) counts[e.isRemembered() ? 0 : 1]++;
        }
        return result;
    }

    // ---------- CSV helpers ----------
    private static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\"";
    }

    static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQuotes = false;
                } else cur.append(c);
            } else if (c == '"') inQuotes = true;
            else if (c == ',') { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }
}
