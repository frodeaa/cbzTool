package cbztool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
public class CbzToPdf extends SwingWorker<String, byte[]> implements
	Task<byte[], Document, String> {

    private final String cbzFile;
    private final String pdfFile;
    private final Rectangle pageSize;
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
	Iterator<byte[]> images = iterate(document);
	while (images.hasNext() && !isCancelled()) {
	    process(images.next(), document);
	    setProgress(getProgress() + 1);
	}

	endContext(document);
	Logger.getLogger(getClass().getName())
		.finest("Created pdf: " + pdfFile);
	return pdfFile;
    }

    @Override
    public Document prepareContext() throws InitializationException {
	try {
	    zipContext = new ZipInputStream(new FileInputStream(cbzFile));
	} catch (FileNotFoundException e1) {
	    throw new InitializationException("Unable to open zip archive: "
		    + cbzFile, e1);
	}
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
    @Override
    public String process(byte[] value, Document context)
	    throws ProcessException {

	try {
	    com.lowagie.text.Image pdfImage = com.lowagie.text.Image
		    .getInstance(value);
	    pdfImage.setAlignment(Element.ALIGN_CENTER);

	    if (pdfImage.getWidth() > pdfImage.getHeight()) {
		pdfImage.setRotationDegrees(90);
		pdfImage.rotate();
	    }

	    pdfImage.scaleToFit(637.28f, 835.7f);
	    context.add(pdfImage);
	    context.newPage();
	} catch (BadElementException e) {
	    throw new ProcessException("Unable to add image to pdf", e);
	} catch (IOException e) {
	    throw new ProcessException("Unable to add image to pdf", e);
	} catch (DocumentException e) {
	    throw new ProcessException("Unable to add image to pdf", e);
	}
	return null;
    }

    /**
     * Clean up the task, close all open stream.
     */
    public void endContext(Document context) {
	context.close();
	try {
	    zipContext.close();
	} catch (IOException e) {
	}
    }

    /**
     * Iterator for retrieve the image bytes in the ZIP.
     */
    public Iterator<byte[]> iterate(Document context) {
	return ZipIterator.bytes(zipContext);
    }

}
