package honours.ing.banq.auth;

/**
 * @author jeffrey
 * @since 31-5-17
 */
public class InvalidPINError extends Exception {
    public InvalidPINError() {
        super();
    }

    public InvalidPINError(String message) {
        super(message);
    }
}
