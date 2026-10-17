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

/** The revision screen: shows today's due cards one at a time. */
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
        RoundButton forgotBtn = new RoundButton("I forgot   (Left arrow)", Theme.RED);
        forgotBtn.addActionListener(e -> grade(false));
        RoundButton rememberedBtn = new RoundButton("I remembered   (Right arrow)", Theme.GREEN);
        rememberedBtn.addActionListener(e -> grade(true));

        JPanel showRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        showRow.setOpaque(false);
        showRow.add(showBtn);
        JPanel gradeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        gradeRow.setOpaque(false);
        gradeRow.add(forgotBtn);
        gradeRow.add(rememberedBtn);
        JPanel none = new JPanel();
        none.setOpaque(false);

        buttonHolder.setOpaque(false);
        buttonHolder.add(showRow, "show");
        buttonHolder.add(gradeRow, "grade");
        buttonHolder.add(none, "none");

        add(infoLabel, BorderLayout.NORTH);
        add(flipCard, BorderLayout.CENTER);
        add(buttonHolder, BorderLayout.SOUTH);

        bindKey("reveal", KeyEvent.VK_SPACE, this::reveal, () -> current != null && !revealed);
        bindKey("forgot", KeyEvent.VK_LEFT, () -> grade(false), () -> current != null && revealed);
        bindKey("remembered", KeyEvent.VK_RIGHT, () -> grade(true), () -> current != null && revealed);

        app.addListener(this::reload);
        reload();
    }

    private void bindKey(String name, int keyCode, Runnable action, BooleanSupplier condition) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public boolean isEnabled() { return isShowing() && condition.getAsBoolean(); }
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    /** Rebuilds today's queue from the deck. Called whenever app data changes. */
    private void reload() {
        Deck deck = app.getCurrentDeck();
        LocalDate today = AppClock.today();
        String key = deck.getName() + "|" + today;
        if (!key.equals(sessionKey)) {
            sessionKey = key;
            sessionDone = 0;
            current = null;
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
            return;
        }

        if (queue.isEmpty()) {
            current = null;
            revealed = false;
            LocalDate next = app.getScheduler().nextDueDate(deck);
            String sub = (next == null) ? "" : "Next review: " + next + "  (in "
                    + ChronoUnit.DAYS.between(today, next) + " day(s))";
            flipCard.showMessage("All caught up!", sub);
            infoLabel.setText(sessionDone > 0
                    ? "Session complete: " + sessionDone + " card(s) reviewed today"
                    : "Nothing is due today");
            buttonLayout.show(buttonHolder, "none");
            return;
        }

        if (current != null && queue.get(0) == current) {      // same card still on screen: keep it
            updateInfo();
            return;
        }
        current = queue.get(0);
        revealed = false;
        flipCard.showCard(current.getType().toUpperCase(), Theme.colorForType(current.getType()),
                current.getFront(), current.getBackText(),
                "Box " + current.getBoxLevel() + " of " + StudyItem.MAX_BOX
                        + "    |    gaps for this type: " + current.getIntervalsSummary());
        buttonLayout.show(buttonHolder, "show");
        updateInfo();
    }

    private void updateInfo() {
        infoLabel.setText("Card " + (sessionDone + 1) + " of " + (sessionDone + queue.size())
                + "     |     Deck: " + app.getCurrentDeck().getName());
    }

    private void reveal() {
        if (current == null || revealed) return;
        revealed = true;
        flipCard.flip();
        buttonLayout.show(buttonHolder, "grade");
    }

    private void grade(boolean remembered) {
        if (current == null || !revealed) return;
        StudyItem item = current;
        sessionDone++;
        revealed = false;
        current = null;
        try {
            app.recordReview(item, remembered);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Your answer was counted, but saving failed:\n" + ex.getMessage(),
                    "Could not save", JOptionPane.ERROR_MESSAGE);
            reload();
        }
    }
}
