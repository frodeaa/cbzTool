package cbztool.share;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OutputConfiguration.
 * 
 * @param <E>
 *            the output type.
 */
public abstract class OutputConfig<E> {
	private final String outputDirectory;

	/**
	 * @param outputDirectory
	 *            the output directory for this configuration.
	 */
	public OutputConfig(String outputDirectory) {
		assert outputDirectory != null;
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Test if given input should be included in the output task.
	 * 
	 * @param entity
	 *            the entity to check.
	 * @return <code>true</code> if the entity should be included in the result
	 *         output.
	 */
	public abstract boolean isAccepted(E entity);

	/**
	 * Initialize the output directory, creates the directory if the file does
	 * not exist.
	 * 
	 * @param path
	 *            the output directory.
	 * @throws IllegalArgumentException
	 *             if the path is a file.
	 * @throws IOException
	 *             if any error occurs.
	 */
	public void initOutputDirectory(String path) throws IOException {

		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		} else {
			if (file.isFile()) {
				throw new IllegalArgumentException(
						"Extract directory can't be a file. :" + path);
			}
		}

		Logger.getLogger(getClass().getName()).log(Level.INFO,
				"Prepared path: " + path);
	}

	/**
	 * @return the output directory for this configuration.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

}
