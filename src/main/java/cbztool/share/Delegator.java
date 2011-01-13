package cbztool.share;

/**
 * Delegate user/terminal input to appropriate task.
 */
public interface Delegator {

	/**
	 * Handle accepted arguments
	 * 
	 * @param args
	 *            the arguments to handle.
	 */
	void handle(String[] args) throws Exception;

	/**
	 * 
	 * @return the description for this delegator.
	 */
	String getDescription();

	/**
	 * 
	 * @param args
	 *            the arguments to check.
	 * @return <code>true</code> if this handler accepts the arguments.
	 */
	boolean accept(String[] args);
}