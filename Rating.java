import java.awt.Color;

/**
 * 4-level Leitner / SM-2 spaced repetition rating.
 * Allows fine-grained grading: Again (reset), Hard (conservative), Good (standard), Easy (accelerated).
 */
public enum Rating {
    AGAIN("Again", "1 / Left", new Color(0xEF4444), 1),
    HARD("Hard", "2 / Down", new Color(0xF59E0B), 2),
    GOOD("Good", "3 / Right", new Color(0x10B981), 3),
    EASY("Easy", "4 / Up", new Color(0x3B82F6), 4);

    private final String label;
    private final String shortcut;
    private final Color color;
    private final int value;

    Rating(String label, String shortcut, Color color, int value) {
        this.label = label;
        this.shortcut = shortcut;
        this.color = color;
        this.value = value;
    }

    public String getLabel() { return label; }
    public String getShortcut() { return shortcut; }
    public Color getColor() { return color; }
    public int getValue() { return value; }
    public boolean isSuccessful() { return this != AGAIN; }
}
