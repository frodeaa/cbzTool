package cbztool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cbztool.share.Delegator;

/**
 * Execute tools from terminal without any UI.
 */
public final class MainApp {

	/**
	 * Print usage details for this application.
	 * 
	 * @param delegators
	 *            the delegators registered that usage details will be created
	 *            from.
	 */
	private static void printUsage(List<Delegator> delegators) {
		System.out.println("Usage: -g <options> <parameters>");
		System.out.println("options:");
		for (Delegator delegator : delegators) {
			System.out.println(delegator.getDescription());
		}
	}

	/**
	 * A Delegator that converts a CBZ file into a PDF file.
	 */
	private static class DelegateCbzToPdf implements Delegator {

		@Override
		public boolean accept(String[] args) {
			return args.length == 4 && args[0].equals("-g")
					&& args[1].equals("cbzToPdf");
		}

		@Override
		public String getDescription() {
			return "cbzToPdf <cbzFile> <pdfFile>";
		}

		@Override
		public void handle(String[] args) throws Exception {
			File inputCbz = new File(args[2]);

			if (!inputCbz.isFile() || !inputCbz.exists()) {
				printError("CBZ file not found:" + args[2], this);
			}

			File outputPdf = new File(args[3]);
			if (!inputCbz.isFile()) {
				printError("Not a valid pdf output file:" + args[3], this);
			}

			final CbzToPdf cbzPdf = new CbzToPdf(inputCbz.getAbsolutePath(),
					outputPdf.getAbsolutePath());
					
			// execute this on current thread.
			cbzPdf.doInBackground();					
		}

	}

	/** Delegators that handles the inputs for this application. */
	private static final List<Delegator> DELEGATORS;

	/** Initialize delegators. */
	static {
		DELEGATORS = new ArrayList<Delegator>();
		DELEGATORS.add(new DelegateCbzToPdf());
	}

	/**
	 * Run applications
	 * 
	 * @param args
	 *            the parameters to pass, will print usage if no arguments or no
	 *            valid arguments are provided.
	 * @throws InterruptedException
	 *             if executing task is canceled.
	 * @throws ExecutionException
	 *             if any error happends while executing the task.
	 */
	public static void main(String[] args) throws InterruptedException,
			ExecutionException {

		for (Delegator delegator : DELEGATORS) {
			if (delegator.accept(args)) {
				try {
					delegator.handle(args);
				} catch (Exception e) {
					printError(e.getMessage(), null);
				}
				System.exit(0);
			}
		}
		printUsage(DELEGATORS);
	}

	/**
	 * Print error message and correct usage example.
	 * 
	 * @param errorMessage
	 *            the error message to print.
	 * @param delegator
	 *            the delegator that usage example us provided from, if
	 *            <code>null</code> no help message/usage message will be
	 *            printed.
	 */
	private static void printError(String errorMessage, Delegator delegator) {
		System.err.println(errorMessage);

		if (delegator != null) {
			System.out.println("Usage : -g " + delegator.getDescription());
		}

		System.exit(-1);
	}

}
