import java.awt.Color;
import java.awt.Font;

/** Colours and fonts used everywhere, so the whole app looks consistent. */
public final class Theme {
    public static final Color INDIGO = new Color(0x4F46E5);
    public static final Color INDIGO_DARK = new Color(0x3730A3);
    public static final Color INDIGO_LIGHT = new Color(0x6366F1);
    public static final Color BG = new Color(0xF3F4F8);
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xE5E7EB);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color RED = new Color(0xEF4444);
    public static final Color AMBER = new Color(0xD97706);
    public static final Color TEAL = new Color(0x0D9488);

    private Theme() { }

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
