/** Thrown when a revision session is requested for a deck that has no cards. */
public class EmptyDeckException extends Exception {
    public EmptyDeckException(String message) { super(message); }
}
