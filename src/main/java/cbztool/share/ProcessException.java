package cbztool.share;

/**
 * Exception indicating that Task was unable to process a Value.
 */
public class ProcessException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *            error message.
	 * @param cause
	 *            exception causing the error.
	 */
	public ProcessException(String message, Throwable cause) {
		super(message, cause);
	}

}
