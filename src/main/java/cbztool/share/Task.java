package cbztool.share;

import java.util.Iterator;

/**
 * Defines a task.
 * 
 * @param <E>
 *            the type to process in the task.
 * @param <C>
 *            the context type for this task.
 * @param <P>
 *            the result type for the task process.
 */
public interface Task<E, C, P> {

	/**
	 * Prepares the context for this task.
	 * 
	 * @return a new Context to execute the task in.
	 */
	public C prepareContext() throws InitializationException;

	/**
	 * Process a value in given contect.
	 * 
	 * @param value
	 *            the value to process.
	 * @param context
	 *            the context to process the value in.
	 * @return the result from processing the value.
	 * 
	 */
	public P process(E value, C context) throws ProcessException;

	/**
	 * Clean up before ending the task.
	 * 
	 * @param context
	 *            the context
	 */
	public void endContext(C context);

	/**
	 * Construct a iterator for producing values that can be processed.
	 * 
	 * @param context
	 *            the context to produce value iterator from.
	 * @return a iterator that can be used to produce the values that needs to
	 *         be processed.
	 */
	public Iterator<E> iterate(C context);

}
