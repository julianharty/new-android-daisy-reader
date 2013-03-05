package org.androiddaisyreader.testapps;

import static org.androiddaisyreader.model.XmlUtilities.obtainEncodingStringFromInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.FileSystemContext;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.model.ZippedBookContext;

public class ProcessDaisy202Book {

	private static InputStream contents;
	private static String encoding;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			CommandLineUtilities.printUsage("ProcessDaisy202Book");
			System.exit(1);
		}
		
		StringBuilder filenameBuilder = new StringBuilder();
		
		// To help cope with spaces in the filename e.g. on my windows machine.
		for (int i = 0; i < args.length; i++) {
			filenameBuilder.append(args[i]);
		}
		
		String filename = filenameBuilder.toString();
		
		BookContext bookContext = openBook(filename);
		
		contents = bookContext.getResource("ncc.html");
		
		encoding = obtainEncodingStringFromInputStream(contents);
		Daisy202Book book = NccSpecification.readFromStream(contents);
		System.out.println("Book: " + book.getTitle() + ", encoding: " + encoding);
		Navigator navigator = new Navigator(book);
		while (navigator.hasNext()) {
			Section section = (Section) navigator.next();
			System.out.println(section.getLevel() + " " + section.getTitle());
		}
		
		System.exit(0);
	}

	private static BookContext openBook(String filename) throws IOException {
		BookContext bookContext;
		
		if (filename.endsWith(".zip")) {
			bookContext = new ZippedBookContext(filename);
		} else {
			File directory = new File(filename);
			bookContext = new FileSystemContext(directory.getParent());
			directory = null;
		}
		return bookContext;
	}

}
