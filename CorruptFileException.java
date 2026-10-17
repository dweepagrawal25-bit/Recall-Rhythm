/** Thrown when a deck file (JSON) or the review history (CSV) cannot be understood. */
public class CorruptFileException extends Exception {
    public CorruptFileException(String message) { super(message); }
}
