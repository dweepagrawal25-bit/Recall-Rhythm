import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Holds the app's data and services; the screens listen to it and redraw when it changes. */
public class RecallApp {
    public static final String DATA_DIR = "data";

    private final DeckStorage storage;
    private final ReviewLog log;
    private final Scheduler scheduler = new Scheduler();
    private final List<Deck> decks = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private Deck currentDeck;

    public RecallApp() { this(DATA_DIR); }

    public RecallApp(String dataDir) {
        storage = new DeckStorage(dataDir);
        log = new ReviewLog(dataDir + "/review_history.csv");
    }

    /** Loads decks and history. Problems are collected as warnings instead of crashing the app. */
    public void init() {
        boolean hadFiles = false;
        try {
            List<Path> files = storage.listDeckFiles();
            hadFiles = !files.isEmpty();
            for (Path file : files) {
                try {
                    decks.add(storage.load(file));
                } catch (CorruptFileException e) {
                    warnings.add("The deck file '" + file.getFileName() + "' is corrupted and was skipped.\nReason: " + e.getMessage());
                } catch (IOException e) {
                    warnings.add("Could not read '" + file.getFileName() + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            warnings.add("Could not open the data folder: " + e.getMessage());
        }

        if (decks.isEmpty()) {
            try {
                if (!hadFiles) {
                    for (Deck d : SampleData.createDecks(AppClock.today())) {
                        decks.add(d);
                        storage.save(d);
                    }
                } else {
                    Deck d = new Deck("My Deck");
                    decks.add(d);
                    storage.save(d);
                }
            } catch (InvalidCardException | IOException e) {
                warnings.add("Could not create the starter deck: " + e.getMessage());
                if (decks.isEmpty()) decks.add(new Deck("My Deck"));
            }
        }

        try {
            log.load();
        } catch (CorruptFileException e) {
            warnings.add("The review history file is corrupted, so progress charts start empty.\nReason: " + e.getMessage());
        } catch (IOException e) {
            warnings.add("Could not read the review history: " + e.getMessage());
        }
        currentDeck = decks.get(0);
    }

    // ----- access -----
    public List<Deck> getDecks() { return decks; }
    public Deck getCurrentDeck() { return currentDeck; }
    public Scheduler getScheduler() { return scheduler; }
    public ReviewLog getLog() { return log; }
    public List<String> getWarnings() { return warnings; }

    public void addListener(Runnable r) { listeners.add(r); }

    private void fireChanged() {
        for (Runnable r : new ArrayList<>(listeners)) r.run();
    }

    // ----- actions -----
    public void selectDeck(String name) {
        for (Deck d : decks) {
            if (d.getName().equals(name)) {
                currentDeck = d;
                fireChanged();
                return;
            }
        }
    }

    public void createDeck(String rawName) throws InvalidCardException, IOException {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) throw new InvalidCardException("The deck name cannot be empty.");
        if (name.length() > 40) throw new InvalidCardException("The deck name can have at most 40 characters.");
        for (Deck d : decks) {
            if (d.getName().equalsIgnoreCase(name)
                    || DeckStorage.fileNameFor(d.getName()).equals(DeckStorage.fileNameFor(name))) {
                throw new InvalidCardException("A deck with this name already exists.");
            }
        }
        Deck deck = new Deck(name);
        decks.add(deck);
        currentDeck = deck;
        storage.save(deck);
        fireChanged();
    }

    public void addCard(String type, String front, String back, String extra)
            throws InvalidCardException, IOException {
        StudyItem item = ItemFactory.create(type, currentDeck.nextId(), front, back, extra, AppClock.today());
        currentDeck.addItem(item);
        storage.save(currentDeck);
        fireChanged();
    }

    public void deleteCard(int id) throws IOException {
        if (currentDeck.removeItem(id)) {
            storage.save(currentDeck);
            fireChanged();
        }
    }

    public void recordReview(StudyItem item, boolean remembered) throws IOException {
        LocalDate today = AppClock.today();
        item.review(remembered, today);
        storage.save(currentDeck);
        log.log(currentDeck.getName(), item, remembered, today);
        fireChanged();
    }

    public void skipDay() { AppClock.skipDay(); fireChanged(); }
    public void resetDate() { AppClock.reset(); fireChanged(); }
}
