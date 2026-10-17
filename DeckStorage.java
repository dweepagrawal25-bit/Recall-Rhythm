import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Saves decks as JSON files and loads them back (file handling + validation + CSV import/export). */
public class DeckStorage {
    private final Path dir;

    public DeckStorage(String directory) { this.dir = Paths.get(directory); }

    public static String fileNameFor(String deckName) {
        return deckName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase() + ".json";
    }

    public void save(Deck deck) throws IOException {
        Files.createDirectories(dir);
        Files.write(dir.resolve(fileNameFor(deck.getName())), deck.toJson().getBytes(StandardCharsets.UTF_8));
    }

    public List<Path> listDeckFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        if (!Files.isDirectory(dir)) return files;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : stream) files.add(p);
        }
        Collections.sort(files);
        return files;
    }

    public Deck load(Path file) throws IOException, CorruptFileException {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Object root = JsonParser.parse(text);
        Map<String, Object> obj = asObject(root, "the deck file");

        String name = getString(obj, "name");
        if (name.trim().isEmpty()) throw new CorruptFileException("the deck name is empty");
        Object itemsValue = obj.get("items");
        if (!(itemsValue instanceof List)) throw new CorruptFileException("'items' must be a list");
        List<?> array = (List<?>) itemsValue;

        Deck deck = new Deck(name);
        for (int i = 0; i < array.size(); i++) {
            String where = "card #" + (i + 1);
            Map<String, Object> it = asObject(array.get(i), where);
            try {
                StudyItem item = ItemFactory.restore(
                        getString(it, "type"),
                        getInt(it, "id"),
                        getString(it, "front"),
                        getString(it, "back"),
                        getOptionalString(it, "extra"),
                        getOptionalString(it, "tags"),
                        getInt(it, "box"),
                        parseDateOrNull(getOptionalString(it, "lastReviewed"), where),
                        parseDate(getString(it, "nextReview"), where),
                        getInt(it, "reviews"),
                        getInt(it, "correct"));
                deck.addItem(item);
            } catch (InvalidCardException e) {
                throw new CorruptFileException(where + ": " + e.getMessage());
            } catch (CorruptFileException e) {
                throw new CorruptFileException(where + ": " + e.getMessage());
            }
        }
        return deck;
    }

    /** Exports the given deck to a comma-separated values (CSV) file. */
    public void exportToCsv(Deck deck, Path target) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,question,answer,extra,tags,box,next_review,reviews,correct\n");
        for (StudyItem item : deck.getItems()) {
            sb.append(item.getId()).append(',')
              .append(csvEscape(item.getType())).append(',')
              .append(csvEscape(item.getFront())).append(',')
              .append(csvEscape(item.getBack())).append(',')
              .append(csvEscape(item.getExtra())).append(',')
              .append(csvEscape(item.getTags())).append(',')
              .append(item.getBoxLevel()).append(',')
              .append(item.getNextReviewDate()).append(',')
              .append(item.getTimesReviewed()).append(',')
              .append(item.getTimesCorrect()).append('\n');
        }
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        Files.write(target, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Imports flashcards from a CSV file into the deck. Returns the number of successfully imported cards. */
    public int importFromCsv(Deck deck, Path source, LocalDate today) throws IOException, InvalidCardException {
        List<String> lines = Files.readAllLines(source, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return 0;
        int imported = 0;
        int startLine = 0;

        // Check if first line is a header
        String first = lines.get(0).toLowerCase();
        if (first.contains("question") || first.contains("front") || first.contains("answer")) {
            startLine = 1;
        }

        for (int i = startLine; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            List<String> cols = ReviewLog.parseCsvLine(line);
            if (cols.size() < 2) continue;

            String type = "Flashcard";
            String front = "";
            String back = "";
            String extra = "";
            String tags = "";

            if (cols.size() >= 10 && tryParseInt(cols.get(0))) {
                // Full exported format: id,type,question,answer,extra,tags...
                type = cols.get(1).trim();
                front = cols.get(2).trim();
                back = cols.get(3).trim();
                extra = cols.get(4).trim();
                tags = cols.get(5).trim();
            } else {
                // Simple format: Question, Answer [, Type [, Extra [, Tags]]]
                front = cols.get(0).trim();
                back = cols.get(1).trim();
                if (cols.size() > 2 && isValidType(cols.get(2).trim())) {
                    type = cols.get(2).trim();
                    if (cols.size() > 3) extra = cols.get(3).trim();
                    if (cols.size() > 4) tags = cols.get(4).trim();
                } else {
                    if (cols.size() > 2) extra = cols.get(2).trim();
                    if (cols.size() > 3) tags = cols.get(3).trim();
                }
            }

            if (front.isEmpty() || back.isEmpty()) continue;
            StudyItem item = ItemFactory.create(type, deck.nextId(), front, back, extra, tags, today);
            deck.addItem(item);
            imported++;
        }
        if (imported > 0) save(deck);
        return imported;
    }

    private static boolean tryParseInt(String s) {
        try { Integer.parseInt(s.trim()); return true; } catch (Exception e) { return false; }
    }

    private static boolean isValidType(String t) {
        return "Flashcard".equalsIgnoreCase(t) || "Formula".equalsIgnoreCase(t) || "Concept".equalsIgnoreCase(t);
    }

    private static String csvEscape(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\"";
    }

    // ---------- small validation helpers ----------
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value, String where) throws CorruptFileException {
        if (!(value instanceof Map)) throw new CorruptFileException(where + " must be a JSON object");
        return (Map<String, Object>) value;
    }

    private static String getString(Map<String, Object> m, String key) throws CorruptFileException {
        Object v = m.get(key);
        if (!(v instanceof String)) throw new CorruptFileException("missing or invalid text field '" + key + "'");
        return (String) v;
    }

    private static String getOptionalString(Map<String, Object> m, String key) throws CorruptFileException {
        Object v = m.get(key);
        if (v == null) return "";
        if (!(v instanceof String)) throw new CorruptFileException("field '" + key + "' must be text");
        return (String) v;
    }

    private static int getInt(Map<String, Object> m, String key) throws CorruptFileException {
        Object v = m.get(key);
        if (!(v instanceof Double)) throw new CorruptFileException("missing or invalid number field '" + key + "'");
        double d = (Double) v;
        if (d != Math.floor(d)) throw new CorruptFileException("field '" + key + "' must be a whole number");
        return (int) d;
    }

    private static LocalDate parseDate(String text, String where) throws CorruptFileException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new CorruptFileException(where + " has an invalid date '" + text + "'");
        }
    }

    private static LocalDate parseDateOrNull(String text, String where) throws CorruptFileException {
        if (text == null || text.trim().isEmpty()) return null;
        return parseDate(text, where);
    }
}
