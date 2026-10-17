import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Colours and fonts used everywhere, so the whole app looks consistent.
 * Supports dynamic Dark Mode toggling and theme change listeners.
 */
public final class Theme {
    public static final Color INDIGO = new Color(0x4F46E5);
    public static final Color INDIGO_DARK = new Color(0x3730A3);
    public static final Color INDIGO_LIGHT = new Color(0x6366F1);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color RED = new Color(0xEF4444);
    public static final Color AMBER = new Color(0xD97706);
    public static final Color TEAL = new Color(0x0D9488);
    public static final Color BLUE = new Color(0x3B82F6);

    // Dynamic colors that adjust with theme
    public static Color BG = new Color(0xF3F4F8);
    public static Color CARD_BG = Color.WHITE;
    public static Color TEXT = new Color(0x1F2937);
    public static Color MUTED = new Color(0x6B7280);
    public static Color BORDER = new Color(0xE5E7EB);

    private static boolean darkMode = false;
    private static final List<Runnable> listeners = new ArrayList<>();

    private Theme() { }

    public static boolean isDarkMode() { return darkMode; }

    public static void setDarkMode(boolean dark) {
        if (darkMode == dark) return;
        darkMode = dark;
        if (darkMode) {
            BG = new Color(0x0F172A);       // Slate 900
            CARD_BG = new Color(0x1E293B);  // Slate 800
            TEXT = new Color(0xF8FAFC);     // Slate 50
            MUTED = new Color(0x94A3B8);    // Slate 400
            BORDER = new Color(0x334155);   // Slate 700
        } else {
            BG = new Color(0xF3F4F8);
            CARD_BG = Color.WHITE;
            TEXT = new Color(0x1F2937);
            MUTED = new Color(0x6B7280);
            BORDER = new Color(0xE5E7EB);
        }
        for (Runnable r : new ArrayList<>(listeners)) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    public static void toggleDarkMode() {
        setDarkMode(!darkMode);
    }

    public static void addListener(Runnable r) {
        listeners.add(r);
    }

    public static Color colorForType(String type) {
        switch (type) {
            case "Formula": return TEAL;
            case "Concept": return AMBER;
            default: return INDIGO;
        }
    }

    public static Font font(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }
}
