package cbztool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

import cbztool.share.InitializationException;
import cbztool.share.ProcessException;
import cbztool.share.Task;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Convert a Comic Cbz to a PDF file.
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * CbzToPdf task = new CbzToPdf(&quot;coolcomic.cbz&quot;, &quot;coolpdf.pdf&quot;);
 * task.execute();
 * 
 * // get the pdf that was created
 * String pdfFile = task.get();
 * </pre>
 * 
 * </p>
 */
public class CbzToPdf extends SwingWorker<String, String> implements
		Task<String, Document, String> {

	private final String cbzFile;
	private final String pdfFile;
	private final Rectangle pageSize;
	private UnZip unzipper;
	private ZipInputStream zipContext;

	/**
	 * Create a pdf from a comic cbz file, a zip file containing only images.
	 * 
	 * @param cbzFile
	 *            the comic file path to create pdf from.
	 * @param pdfFile
	 *            the path to the PDF that will be created.
	 */
	public CbzToPdf(String cbzFile, String pdfFile) {
		this(cbzFile, pdfFile, 637.28F, 835.7F);
	}

	/**
	 * Create a pdf from a comic cbz file, a zip file containing only images.
	 * 
	 * @param cbzFile
	 *            the comic file path to create pdf from.
	 * @param pdfFile
	 *            the path to the PDF that will be created.
	 * @param w
	 *            PDF page width
	 * @param h
	 *            PDF page height
	 */
	public CbzToPdf(String cbzFile, String pdfFile, float w, float h) {
		this.cbzFile = cbzFile;
		this.pdfFile = pdfFile;
		this.pageSize = new Rectangle(w, h);
	}

	/**
	 * Create PDF from CBZ file.
	 */
	@Override
	protected String doInBackground() throws Exception {

		Document document = prepareContext();
		Iterator<String> images = iterate(document);
		while (images.hasNext() && !isCancelled()) {
			process(images.next(), document);
			setProgress(getProgress() + 1);
		}

		endContext(document);
		Logger.getLogger(getClass().getName()).info("Created pdf: " + pdfFile);
		return pdfFile;
	}

	/**
	 * Initialize the task.
	 */
	public Document prepareContext() throws InitializationException {

		File source = new File(cbzFile);
		File file = new File(System.getProperty("java.io.tmpdir"), source
				.getName());
		file.deleteOnExit();

		unzipper = new UnZip(cbzFile, UnZip.ALL(file.getAbsolutePath()));
		zipContext = unzipper.prepareContext();
		Document document = new Document(pageSize);

		try {
			PdfWriter.getInstance(document, new BufferedOutputStream(
					new FileOutputStream(pdfFile)));
		} catch (FileNotFoundException e) {
			throw new InitializationException("Unable to create output: "
					+ pdfFile, e);
		} catch (DocumentException e) {
			throw new InitializationException("Unable to initialize PdfWriter",
					e);
		}
		document.setMarginMirroring(true);
		document.setMargins(0, 0, 0, 0);

		document.open();
		return document;
	}

	/**
	 * Add image to PDF document.
	 * 
	 * @param value
	 *            the image path.
	 * @param context
	 *            the PDF document.
	 */
	public String process(String value, Document context)
			throws ProcessException {
		try {
			Logger.getLogger(getClass().getName()).finer(
					String.format("Adding %s to %s", value, pdfFile));
			Image image = Image.getInstance(value);
			image.setAlignment(Element.ALIGN_CENTER);

			if (image.getWidth() > image.getHeight()) {
				image.setRotationDegrees(90);
				image.rotate();
			}

			image.scaleToFit(637.28f, 835.7f);
			context.add(image);
			context.newPage();

			new File(value).delete();
		} catch (BadElementException e) {
			throw new ProcessException("BadElement", e);
		} catch (DocumentException e) {
			throw new ProcessException("DocumentException", e);
		} catch (IOException e) {
			throw new ProcessException("IOException", e);
		}
		return null;
	}

	/**
	 * Clean up the task, close all open stream.
	 */
	public void endContext(Document context) {
		context.close();
		unzipper.endContext(zipContext);
	}

	/**
	 * Iterator for retriving the paths to the images that should be processed.
	 */
	public Iterator<String> iterate(Document context) {
		final Iterator<ZipEntry> itr = unzipper.iterate(zipContext);
		return new Iterator<String>() {

			public boolean hasNext() {
				return itr.hasNext();
			}

			public String next() {
				try {
					return unzipper.process(itr.next(), zipContext);
				} catch (ProcessException e) {
					e.printStackTrace();
					return null;
				}
			}

			public void remove() {
			}

		};
	}

}
