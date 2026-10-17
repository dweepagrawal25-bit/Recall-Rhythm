/** Thrown when a card (or deck name) breaks a rule, e.g. empty question or duplicate card. */
public class InvalidCardException extends Exception {
    public InvalidCardException(String message) { super(message); }
}
