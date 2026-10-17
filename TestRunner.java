import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Built-in test suite to verify core logic:
 * - Spaced repetition scheduling
 * - 4-level rating transitions (Again, Hard, Good, Easy)
 * - JSON serialization and deserialization
 * - CSV import and export
 * - Deck and card validation
 *
 * Can be run anytime via: java TestRunner
 */
public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("  Running RecallRhythm Verification Suite ");
        System.out.println("=========================================");

        testLeitnerTransitions();
        testFourLevelRatings();
        testSchedulerQueueOrdering();
        testJsonSerializationRoundTrip();
        testCsvExportAndImport();
        testValidationRules();

        System.out.println("=========================================");
        System.out.printf("  Summary: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("=========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName + " - Expected: <" + expected + ">, Got: <" + actual + ">");
            failed++;
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName + " - Condition was false");
            failed++;
        }
    }

    private static void testLeitnerTransitions() {
        System.out.println("\nTesting Leitner Transitions...");
        LocalDate today = LocalDate.of(2026, 1, 1);
        Flashcard card = new Flashcard(1, "What is polymorphism?", "Ability of object to take many forms",
                "OOP core", "oop,java", 1, null, today, 0, 0);

        assertEquals(1, card.getBoxLevel(), "Initial box should be 1");
        assertEquals("oop,java", card.getTags(), "Tags should be preserved");

        // Review GOOD
        card.review(Rating.GOOD, today);
        assertEquals(2, card.getBoxLevel(), "Box should advance to 2 on GOOD");
        assertEquals(1, card.getTimesReviewed(), "Times reviewed should be 1");
        assertEquals(1, card.getTimesCorrect(), "Times correct should be 1");
        assertEquals(today.plusDays(3), card.getNextReviewDate(), "Flashcard box 2 interval is 3 days");

        // Review AGAIN
        card.review(Rating.AGAIN, today);
        assertEquals(1, card.getBoxLevel(), "Box should reset to 1 on AGAIN");
        assertEquals(2, card.getTimesReviewed(), "Times reviewed should be 2");
        assertEquals(1, card.getTimesCorrect(), "Times correct should still be 1");
        assertEquals(50, card.getAccuracyPercent(), "Accuracy should be 50%");
    }

    private static void testFourLevelRatings() {
        System.out.println("\nTesting 4-Level Ratings (Again, Hard, Good, Easy)...");
        LocalDate today = LocalDate.of(2026, 1, 1);
        Concept concept = new Concept(2, "Encapsulation", "Hiding internal data", "Data protection", "oop",
                2, null, today, 0, 0);

        // HARD
        concept.review(Rating.HARD, today);
        assertEquals(2, concept.getBoxLevel(), "Box level should stay at 2 on HARD");
        assertEquals(1, concept.getTimesCorrect(), "HARD counts as correct recall");

        // EASY
        concept.review(Rating.EASY, today);
        assertEquals(4, concept.getBoxLevel(), "EASY should jump box level by +2 (from 2 to 4)");
    }

    private static void testSchedulerQueueOrdering() {
        System.out.println("\nTesting Scheduler Queue Ordering...");
        Scheduler scheduler = new Scheduler();
        Deck deck = new Deck("Algorithms");
        LocalDate today = LocalDate.of(2026, 1, 10);

        try {
            // Card 1: Overdue (nextReview = Jan 5, Box 2)
            deck.addItem(new Flashcard(1, "Binary Search", "O(log n)", "", 2, null, LocalDate.of(2026, 1, 5), 0, 0));
            // Card 2: Due today (nextReview = Jan 10, Box 1)
            deck.addItem(new Flashcard(2, "Bubble Sort", "O(n^2)", "", 1, null, today, 0, 0));
            // Card 3: Due in future (nextReview = Jan 15)
            deck.addItem(new Flashcard(3, "Quick Sort", "O(n log n)", "", 1, null, LocalDate.of(2026, 1, 15), 0, 0));

            List<StudyItem> session = scheduler.buildSession(deck, today);
            assertEquals(2, session.size(), "Only overdue and due cards should be in session");
            assertEquals(1, session.get(0).getId(), "Most overdue card (Jan 5) should come first");
            assertEquals(2, session.get(1).getId(), "Card due today (Jan 10) should come second");
        } catch (Exception e) {
            failed++;
            System.err.println("  [FAIL] testSchedulerQueueOrdering encountered exception: " + e.getMessage());
        }
    }

    private static void testJsonSerializationRoundTrip() {
        System.out.println("\nTesting JSON Serialization and Parsing...");
        LocalDate today = LocalDate.of(2026, 1, 1);
        Deck deck = new Deck("TestDeck");
        try {
            deck.addItem(new Formula(10, "E = mc^2", "Energy-mass equivalence", "Einstein", "physics, relativity",
                    3, today, today.plusDays(4), 5, 4));

            String json = deck.toJson();
            assertTrue(json.contains("\"name\": \"TestDeck\""), "JSON should contain deck name");
            assertTrue(json.contains("\"tags\": \"physics, relativity\""), "JSON should contain tags");

            Object parsed = JsonParser.parse(json);
            assertTrue(parsed instanceof Map, "Parsed JSON should be a Map");
        } catch (Exception e) {
            failed++;
            System.err.println("  [FAIL] testJsonSerializationRoundTrip exception: " + e.getMessage());
        }
    }

    private static void testCsvExportAndImport() {
        System.out.println("\nTesting CSV Export and Import...");
        LocalDate today = LocalDate.of(2026, 1, 1);
        DeckStorage storage = new DeckStorage("test_data");
        Deck original = new Deck("CSV Test");
        try {
            original.addItem(new Flashcard(1, "What is JVM?", "Java Virtual Machine", "Execution engine", "java",
                    1, null, today, 0, 0));
            original.addItem(new Concept(2, "Garbage Collection", "Automatic memory management", "Heap cleaner", "memory",
                    2, null, today, 0, 0));

            Path tempCsv = Files.createTempFile("deck_export", ".csv");
            storage.exportToCsv(original, tempCsv);

            assertTrue(Files.exists(tempCsv), "CSV file should be created");
            List<String> lines = Files.readAllLines(tempCsv);
            assertTrue(lines.size() >= 3, "CSV should contain header + 2 cards");

            Deck importedDeck = new Deck("Import Target");
            int count = storage.importFromCsv(importedDeck, tempCsv, today);
            assertEquals(2, count, "Should import 2 cards from CSV");
            assertEquals(2, importedDeck.size(), "Import target should contain 2 items");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            failed++;
            System.err.println("  [FAIL] testCsvExportAndImport exception: " + e.getMessage());
        }
    }

    private static void testValidationRules() {
        System.out.println("\nTesting Validation Rules...");
        LocalDate today = LocalDate.of(2026, 1, 1);

        // Empty question validation
        boolean caughtEmpty = false;
        try {
            ItemFactory.create("Flashcard", 1, "", "Valid Answer", "", today);
        } catch (InvalidCardException e) {
            caughtEmpty = true;
        }
        assertTrue(caughtEmpty, "Empty question should throw InvalidCardException");

        // Duplicate card validation
        Deck deck = new Deck("UniqueTest");
        boolean caughtDuplicate = false;
        try {
            deck.addItem(new Flashcard(1, "Question A", "Answer A", "", 1, null, today, 0, 0));
            deck.addItem(new Flashcard(2, "Question A", "Answer B", "", 1, null, today, 0, 0));
        } catch (InvalidCardException e) {
            caughtDuplicate = true;
        }
        assertTrue(caughtDuplicate, "Duplicate question in same deck should throw InvalidCardException");
    }
}
