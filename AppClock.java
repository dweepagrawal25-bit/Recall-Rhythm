import java.time.LocalDate;

/**
 * The app's own clock. Normally it is today's real date.
 * The demo buttons can move it forward so you can show how cards come due on later days.
 */
public final class AppClock {
    private static int offsetDays = 0;

    private AppClock() { }

    public static LocalDate today() { return LocalDate.now().plusDays(offsetDays); }
    public static void skipDay() { offsetDays++; }
    public static void reset() { offsetDays = 0; }
    public static int getOffsetDays() { return offsetDays; }
}
