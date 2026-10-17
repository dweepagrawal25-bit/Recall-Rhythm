import java.time.LocalDate;

/** Contract for anything that can be scheduled for revision. */
public interface Reviewable {
    boolean isDueOn(LocalDate date);
    void review(boolean remembered, LocalDate today);
}
