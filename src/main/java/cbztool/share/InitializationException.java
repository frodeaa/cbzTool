package cbztool.share;

/**
 * Exception that indicates that some error occured while initializing a Task.
 */
public class InitializationException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *            error message.
	 * @param cause
	 *            exception causing the error.
	 */
	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}

}
