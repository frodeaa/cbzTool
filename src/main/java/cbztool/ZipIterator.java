package cbztool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Iterate over the entries in a ZipInputStream.
 * 
 * @author frode
 * 
 */
public class ZipIterator<E> implements Iterator<E>, Iterable<E> {

    public static ZipIterator<byte[]> bytes(ZipInputStream zipInput) {
	return new ZipIterator<byte[]>(zipInput, new EntryReader<byte[]>() {

	    @Override
	    public byte[] read(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
		    buffer.write(data, 0, nRead);
		}

		buffer.flush();
		return buffer.toByteArray();
	    }

	    @Override
	    public boolean accept(ZipEntry entry) {
		return true;
	    }
	});
    }

    /**
     * Read an ZipEntry to object.
     * 
     * @author frode
     * 
     * @param <E>
     *            the type to return from the reader.
     */
    interface EntryReader<E> {

	/**
	 * @param entry
	 *            the entry to check.
	 * @return <code>true</code> if the reader accept the entry.
	 */
	boolean accept(ZipEntry entry);

	/**
	 * @param entry
	 *            input stream for a ZipEntry.
	 * @return the ZIP entry as Object.
	 * @throws IOException
	 *             if any errors while reading the entry.
	 */
	E read(InputStream entry) throws IOException;
    }

    private final ZipInputStream zip;
    private final EntryReader<E> reader;
    private ZipEntry currentEntry;

    public ZipIterator(ZipInputStream zip, EntryReader<E> reader) {
	this.zip = zip;
	this.reader = reader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<E> iterator() {
	return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
	try {
	    do {
		currentEntry = zip.getNextEntry();
	    } while (currentEntry != null && !reader.accept(currentEntry));
	} catch (IOException e) {
	    return false;
	}
	return currentEntry != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    @Override
    public E next() {
	try {
	    return reader.read(zip);
	} catch (IOException e) {
	    return null;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
	throw new UnsupportedOperationException("Remove not supported");
    }

}
