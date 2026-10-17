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

/** Saves decks as JSON files and loads them back (file handling + validation). */
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
            throw new CorruptFileException("invalid date '" + text + "' (expected yyyy-mm-dd)");
        }
    }

    private static LocalDate parseDateOrNull(String text, String where) throws CorruptFileException {
        return text.isEmpty() ? null : parseDate(text, where);
    }
}
