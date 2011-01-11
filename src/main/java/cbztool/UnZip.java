package cbztool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

import cbztool.share.InitializationException;
import cbztool.share.OutputConfig;
import cbztool.share.ProcessException;
import cbztool.share.Task;

/**
 * Extract a ZipFile to a directory.
 * 
 * <p>
 * Example, unzip all files found in a zip file to 'folder':
 * 
 * <pre>
 * UnZip unzip = new UnZip(&quot;myZip.zip&quot;, ALL(&quot;folder/&quot;));
 * unzip.execute();
 * 
 * // GET Path to files that was extracted
 * List&lt;String&gt; extracted = unzip.get();
 * 
 * </pre>
 * 
 * </p>
 * 
 */
public class UnZip extends SwingWorker<List<String>, String> implements
		Task<ZipEntry, ZipInputStream, String> {

	private static final Logger LOG = Logger.getLogger(UnZip.class.getName());
	private final String zipFile;
	private static final int BUFFER_SIZE = 2048;
	private final OutputConfig<ZipEntry> outputConfig;

	/**
	 * Create a output configuration that will extract all files found in a Zip
	 * archive to given unzip path.
	 * 
	 * @param unzipPath
	 *            the path to unzip the file to.
	 * @return new instance that will accept all files.
	 */
	public static OutputConfig<ZipEntry> ALL(String unzipPath) {
		return new OutputConfig<ZipEntry>(unzipPath) {

			@Override
			public boolean isAccepted(ZipEntry entity) {
				return true;
			}
		};

	};

	/**
	 * Extract a zip file to a directory.
	 * 
	 * @param sourceFile
	 *            the file to extract
	 * @param outputConfig
	 *            the output directory for this task.
	 */
	public UnZip(String sourceFile, OutputConfig<ZipEntry> outputConfig) {
		assert sourceFile != null;
		assert outputConfig != null;
		this.zipFile = sourceFile;
		this.outputConfig = outputConfig;
	}

	/**
	 * Execute the unzip.
	 */
	@Override
	protected List<String> doInBackground() throws Exception {

		ZipInputStream zipInput = prepareContext();
		Iterator<ZipEntry> iterator = iterate(zipInput);

		List<String> processed = new ArrayList<String>();
		while (iterator.hasNext() && !isCancelled()) {

			String unzipPath = process(iterator.next(), zipInput);
			if (unzipPath == null) {
				continue;
			}

			publish(unzipPath);
			processed.add(unzipPath);
			setProgress(getProgress() + 1);
		}

		return processed;
	}

	/**
	 * Extract a ZipEntry to a directory.
	 * 
	 * @param entry
	 *            the entry to extract.
	 * @param zipStream
	 *            the stream to extract the entry from.
	 * @param directory
	 *            the directory to extract the entry to.
	 * @throws IOException
	 */
	protected String extract(ZipEntry entry, ZipInputStream zipStream,
			String directory) throws IOException {
		byte data[] = new byte[BUFFER_SIZE];

		File output = new File(directory, entry.getName());
		FileOutputStream fos = new FileOutputStream(output);
		BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);

		int count;
		while ((count = zipStream.read(data, 0, BUFFER_SIZE)) != -1) {
			dest.write(data, 0, count);
		}
		dest.flush();
		dest.close();

		return output.getAbsolutePath();
	}

	/**
	 * Read ZipInputStream and prepares the output directory.
	 */
	public ZipInputStream prepareContext() throws InitializationException {
		try {
			outputConfig.initOutputDirectory(outputConfig.getOutputDirectory());
		} catch (IOException e1) {
			throw new InitializationException("Unable to unzip to: " + zipFile,
					e1);
		}
		try {
			return new ZipInputStream(new BufferedInputStream(
					new FileInputStream(zipFile)));
		} catch (FileNotFoundException e) {
			throw new InitializationException("File not found: " + zipFile, e);
		}
	}

	/**
	 * Unzip a ZipEntry.
	 */
	public String process(ZipEntry entry, ZipInputStream zipStream)
			throws ProcessException {

		if (!outputConfig.isAccepted(entry)) {
			return null;
		}

		String outputDirectory = outputConfig.getOutputDirectory();
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.fine(String.format("extracting %s to %s", entry.getName(),
					outputDirectory));
		}

		try {
			return extract(entry, zipStream, outputDirectory);
		} catch (IOException e) {
			throw new ProcessException("IOException processing: "
					+ entry.getName(), e);
		}
	}

	/**
	 * Close ZipInputStream, the context created.
	 */
	public void endContext(ZipInputStream context) {
		try {
			context.close();
		} catch (IOException e) {
			LOG.severe("Unable to close context\n" + e.getMessage());
		}
	}

	/**
	 * Iterator for the ZipEntry found in the ZipInputStream.
	 */
	public Iterator<ZipEntry> iterate(final ZipInputStream stream) {
		return new Iterator<ZipEntry>() {

			private ZipEntry currentEntry;

			public boolean hasNext() {
				try {
					currentEntry = stream.getNextEntry();
				} catch (IOException e) {
				}

				return currentEntry != null;
			}

			public ZipEntry next() {
				return currentEntry;
			}

			public void remove() {
			}

		};
	}

}
