import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

/** The whole window content: gradient header with live stats, controls, plus the three tabs. */
public class MainPanel extends JPanel {
    private final RecallApp app;
    private final StatChip dueChip = new StatChip("DUE TODAY");
    private final StatChip streakChip = new StatChip("DAY STREAK");
    private final StatChip accuracyChip = new StatChip("ACCURACY");
    private final JComboBox<String> deckBox = new JComboBox<>();
    private final JLabel dateLabel = new JLabel();
    private final ReviewPanel reviewPanel;
    private final ManagePanel managePanel;
    private final ProgressPanel progressPanel;
    private final JTabbedPane tabs = new JTabbedPane();
    private final RoundButton themeBtn = new RoundButton("🌙 Dark", Theme.INDIGO_LIGHT).compact();
    private final RoundButton soundBtn = new RoundButton("🔊 Sound", Theme.INDIGO_LIGHT).compact();
    private boolean updating = false;

    public MainPanel(RecallApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        reviewPanel = new ReviewPanel(app);
        managePanel = new ManagePanel(app);
        progressPanel = new ProgressPanel(app);

        tabs.setFont(Theme.font(Font.BOLD, 14));
        tabs.setFocusable(false);
        tabs.addTab("  Review (Ctrl+1)  ", reviewPanel);
        tabs.addTab("  Add / Manage (Ctrl+2)  ", managePanel);
        tabs.addTab("  Progress (Ctrl+3)  ", progressPanel);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == reviewPanel) reviewPanel.requestFocusInWindow();
        });
        add(tabs, BorderLayout.CENTER);

        // Global keyboard shortcuts for tab navigation
        bindTabShortcut(KeyEvent.VK_1, 0);
        bindTabShortcut(KeyEvent.VK_2, 1);
        bindTabShortcut(KeyEvent.VK_3, 2);

        app.addListener(this::refreshHeader);
        Theme.addListener(() -> {
            themeBtn.setText(Theme.isDarkMode() ? "☀️ Light" : "🌙 Dark");
            repaint();
        });
        refreshHeader();
    }

    private void bindTabShortcut(int keyCode, int tabIndex) {
        String key = "switch_tab_" + tabIndex;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK), key);
        getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectTab(tabIndex);
            }
        });
    }

    public void focusReview() { reviewPanel.requestFocusInWindow(); }
    public void selectTab(int index) { tabs.setSelectedIndex(index); }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Color c1 = Theme.isDarkMode() ? new Color(0x1E1B4B) : Theme.INDIGO;
                Color c2 = Theme.isDarkMode() ? new Color(0x0F172A) : Theme.INDIGO_DARK;
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 12, 24));

        JLabel title = new JLabel("RecallRhythm 🎵🧠");
        title.setFont(Theme.font(Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("Revise at the right moment, not the last night");
        tagline.setFont(Theme.font(Font.PLAIN, 13));
        tagline.setForeground(new Color(0xE0E7FF));
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(tagline);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chips.setOpaque(false);
        chips.add(dueChip);
        chips.add(streakChip);
        chips.add(accuracyChip);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titles, BorderLayout.WEST);
        top.add(chips, BorderLayout.EAST);

        // toolbar: deck selection (left) and actions / demo date controls (right)
        JLabel deckLabel = new JLabel("Deck:");
        deckLabel.setFont(Theme.font(Font.BOLD, 13));
        deckLabel.setForeground(Color.WHITE);
        deckBox.setFocusable(false);
        deckBox.setPreferredSize(new Dimension(240, 30));
        deckBox.addActionListener(e -> {
            if (!updating && deckBox.getSelectedItem() != null) {
                app.selectDeck((String) deckBox.getSelectedItem());
            }
        });
        RoundButton newDeckBtn = new RoundButton("+ New Deck", Theme.INDIGO_LIGHT).compact();
        newDeckBtn.addActionListener(e -> createDeck());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(deckLabel);
        left.add(deckBox);
        left.add(newDeckBtn);

        dateLabel.setFont(Theme.font(Font.BOLD, 12));
        dateLabel.setForeground(Color.WHITE);
        RoundButton skipBtn = new RoundButton("+1 day (demo)", Theme.INDIGO_LIGHT).compact();
        skipBtn.addActionListener(e -> app.skipDay());
        RoundButton todayBtn = new RoundButton("Today", Theme.INDIGO_LIGHT).compact();
        todayBtn.addActionListener(e -> app.resetDate());

        themeBtn.addActionListener(e -> Theme.toggleDarkMode());
        soundBtn.addActionListener(e -> {
            SoundEffect.toggleMute();
            soundBtn.setText(SoundEffect.isMuted() ? "🔇 Muted" : "🔊 Sound");
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(themeBtn);
        right.add(soundBtn);
        right.add(dateLabel);
        right.add(skipBtn);
        right.add(todayBtn);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        header.add(top, BorderLayout.NORTH);
        header.add(bar, BorderLayout.SOUTH);
        return header;
    }

    private void createDeck() {
        String name = JOptionPane.showInputDialog(this, "Name of the new deck:", "New Deck", JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;
        try {
            app.createDeck(name);
        } catch (InvalidCardException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot create deck", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the deck:\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshHeader() {
        Deck deck = app.getCurrentDeck();
        java.time.LocalDate today = AppClock.today();
        dueChip.setValue(String.valueOf(deck.getDueItems(today).size()));
        streakChip.setValue(String.valueOf(app.getLog().getStreak(today)));
        int acc = app.getLog().accuracyPercent();
        accuracyChip.setValue(acc < 0 ? "--" : acc + "%");

        String date = today.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"));
        int offset = AppClock.getOffsetDays();
        if (offset > 0) date += "   (demo: +" + offset + "d)";
        dateLabel.setText(date);

        updating = true;
        deckBox.removeAllItems();
        for (Deck d : app.getDecks()) deckBox.addItem(d.getName());
        deckBox.setSelectedItem(deck.getName());
        updating = false;
    }
}
