import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * The revision screen: shows today's due cards one at a time.
 * Supports 4-level Leitner / SM-2 ratings, Dark Mode, tags, sounds, and celebration.
 */
public class ReviewPanel extends JPanel {
    private final RecallApp app;
    private final FlipCardPanel flipCard = new FlipCardPanel();
    private final JLabel infoLabel = new JLabel(" ", SwingConstants.CENTER);
    private final CardLayout buttonLayout = new CardLayout();
    private final JPanel buttonHolder = new JPanel(buttonLayout);

    private List<StudyItem> queue = new ArrayList<>();
    private StudyItem current = null;
    private boolean revealed = false;
    private int sessionDone = 0;
    private String sessionKey = "";
    private boolean cramMode = false;

    public ReviewPanel(RecallApp app) {
        this.app = app;
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        setFocusable(true);

        infoLabel.setFont(Theme.font(Font.BOLD, 14));
        infoLabel.setForeground(Theme.MUTED);

        RoundButton showBtn = new RoundButton("Show Answer   (Space)", Theme.INDIGO);
        showBtn.addActionListener(e -> reveal());

        RoundButton againBtn = new RoundButton("Again  (1 / Left)", Theme.RED);
        againBtn.addActionListener(e -> grade(Rating.AGAIN));

        RoundButton hardBtn = new RoundButton("Hard  (2 / Down)", Theme.AMBER);
        hardBtn.addActionListener(e -> grade(Rating.HARD));

        RoundButton goodBtn = new RoundButton("Good  (3 / Right)", Theme.GREEN);
        goodBtn.addActionListener(e -> grade(Rating.GOOD));

        RoundButton easyBtn = new RoundButton("Easy  (4 / Up)", Theme.BLUE);
        easyBtn.addActionListener(e -> grade(Rating.EASY));

        RoundButton cramBtn = new RoundButton("Practice All Cards (Cram Mode)", Theme.INDIGO_LIGHT);
        cramBtn.addActionListener(e -> startCramSession());

        JPanel showRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        showRow.setOpaque(false);
        showRow.add(showBtn);

        JPanel gradeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        gradeRow.setOpaque(false);
        gradeRow.add(againBtn);
        gradeRow.add(hardBtn);
        gradeRow.add(goodBtn);
        gradeRow.add(easyBtn);

        JPanel doneRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        doneRow.setOpaque(false);
        doneRow.add(cramBtn);

        JPanel none = new JPanel();
        none.setOpaque(false);

        buttonHolder.setOpaque(false);
        buttonHolder.add(showRow, "show");
        buttonHolder.add(gradeRow, "grade");
        buttonHolder.add(doneRow, "done");
        buttonHolder.add(none, "none");

        add(infoLabel, BorderLayout.NORTH);
        add(flipCard, BorderLayout.CENTER);
        add(buttonHolder, BorderLayout.SOUTH);

        // Key bindings
        bindKey("reveal", KeyEvent.VK_SPACE, this::reveal, () -> current != null && !revealed);
        bindKey("again_arrow", KeyEvent.VK_LEFT, () -> grade(Rating.AGAIN), () -> current != null && revealed);
        bindKey("again_num", KeyEvent.VK_1, () -> grade(Rating.AGAIN), () -> current != null && revealed);
        bindKey("hard_arrow", KeyEvent.VK_DOWN, () -> grade(Rating.HARD), () -> current != null && revealed);
        bindKey("hard_num", KeyEvent.VK_2, () -> grade(Rating.HARD), () -> current != null && revealed);
        bindKey("good_arrow", KeyEvent.VK_RIGHT, () -> grade(Rating.GOOD), () -> current != null && revealed);
        bindKey("good_num", KeyEvent.VK_3, () -> grade(Rating.GOOD), () -> current != null && revealed);
        bindKey("easy_arrow", KeyEvent.VK_UP, () -> grade(Rating.EASY), () -> current != null && revealed);
        bindKey("easy_num", KeyEvent.VK_4, () -> grade(Rating.EASY), () -> current != null && revealed);

        app.addListener(this::reload);
        Theme.addListener(() -> {
            setBackground(Theme.BG);
            infoLabel.setForeground(Theme.MUTED);
            repaint();
        });
        reload();
    }

    private void bindKey(String name, int keyCode, Runnable action, BooleanSupplier condition) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public boolean isEnabled() { return isShowing() && condition.getAsBoolean(); }
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void startCramSession() {
        Deck deck = app.getCurrentDeck();
        if (deck.isEmpty()) return;
        cramMode = true;
        queue = new ArrayList<>(deck.getItems());
        sessionDone = 0;
        current = null;
        revealed = false;
        loadNextCard();
    }

    /** Rebuilds today's queue from the deck. Called whenever app data changes. */
    private void reload() {
        Deck deck = app.getCurrentDeck();
        LocalDate today = AppClock.today();
        String key = deck.getName() + "|" + today;

        boolean deckChanged = !key.equals(sessionKey);
        if (deckChanged) {
            sessionKey = key;
            sessionDone = 0;
            current = null;
            revealed = false;
            cramMode = false;
        } else if (cramMode) {
            return;
        }

        try {
            queue = app.getScheduler().buildSession(deck, today);
        } catch (EmptyDeckException e) {
            queue = new ArrayList<>();
            current = null;
            revealed = false;
            flipCard.showMessage("This deck is empty", "Open the 'Add / Manage' tab to create your first card.");
            infoLabel.setText(" ");
            buttonLayout.show(buttonHolder, "none");
            revalidate();
            repaint();
            return;
        }

        if (queue.isEmpty()) {
            boolean justFinished = (current != null && sessionDone > 0);
            current = null;
            revealed = false;
            LocalDate next = app.getScheduler().nextDueDate(deck);
            String sub = (next == null) ? "" : "Next scheduled review: " + next + " ("
                    + ChronoUnit.DAYS.between(today, next) + " day(s) from now)";
            if (sessionDone > 0) {
                if (justFinished) SoundEffect.playCelebration();
                flipCard.showMessage("🎉 Session Complete!", "Awesome work! " + sessionDone + " cards revised today. " + sub);
                infoLabel.setText("Session complete: " + sessionDone + " card(s) reviewed today");
            } else {
                flipCard.showMessage("All caught up!", "No cards currently due today. " + sub);
                infoLabel.setText("Nothing is due today in " + deck.getName());
            }
            buttonLayout.show(buttonHolder, "done");
            revalidate();
            repaint();
            return;
        }

        if (current != null && queue.get(0) == current) {
            updateInfo();
            return;
        }
        loadNextCard();
    }

    private void loadNextCard() {
        if (queue.isEmpty()) {
            reload();
            return;
        }
        current = queue.get(0);
        revealed = false;
        String tags = current.getTags();
        flipCard.showCard(current.getType().toUpperCase(), Theme.colorForType(current.getType()),
                current.getFront(), current.getBackText(),
                "Box " + current.getBoxLevel() + " of " + StudyItem.MAX_BOX
                        + "    |    Intervals: " + current.getIntervalsSummary(),
                tags);
        buttonLayout.show(buttonHolder, "show");
        updateInfo();
        revalidate();
        repaint();
    }

    private void updateInfo() {
        String modeLabel = cramMode ? " (Cram Mode)" : "";
        infoLabel.setText("Card " + (sessionDone + 1) + " of " + (sessionDone + queue.size())
                + modeLabel + "     |     Deck: " + app.getCurrentDeck().getName());
    }

    private void reveal() {
        if (current == null || revealed) return;
        revealed = true;
        flipCard.flip();
        buttonLayout.show(buttonHolder, "grade");
    }

    private void grade(Rating rating) {
        if (current == null || !revealed) return;
        StudyItem item = current;
        sessionDone++;
        revealed = false;
        current = null;

        // Sound feedback
        switch (rating) {
            case AGAIN: SoundEffect.playAgain(); break;
            case HARD: SoundEffect.playHard(); break;
            case GOOD: SoundEffect.playGood(); break;
            case EASY: SoundEffect.playEasy(); break;
        }

        try {
            app.recordReview(item, rating);
            if (cramMode) {
                if (!queue.isEmpty()) queue.remove(0);
                loadNextCard();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Your answer was counted, but saving failed:\n" + ex.getMessage(),
                    "Could not save", JOptionPane.ERROR_MESSAGE);
            reload();
        }
    }
}
