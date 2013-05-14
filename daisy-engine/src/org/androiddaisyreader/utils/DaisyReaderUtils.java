package org.androiddaisyreader.utils;

/**
 * DaisyBookUtils contains helper methods for DaisyBooks.
 * 
 * Currently it's limited to a couple of static methods, and as such doesn't
 * really justify being a class. So, expect things to change :)
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.FileSystemContext;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.model.ZippedBookContext;

public final class DaisyReaderUtils {
	// Don't allow anyone to create this utility class.
	private DaisyReaderUtils() {
	};

	/**
	 * Tests if the directory contains the essential root file for a Daisy book
	 * 
	 * Currently it's limited to checking for Daisy 2.02 books.
	 * 
	 * @param folder
	 *            for the directory to check
	 * @return true if the directory is deemed to contain a Daisy Book, else
	 *         false.
	 */

	public static boolean folderContainsDaisy2_02Book(File folder) {
		boolean result = false;
		if (!folder.isDirectory()) {
			result = false;
		}

		if (new File(folder, DaisyReaderConstants.FILE_NCC_NAME_NOT_CAPS).exists()) {
			result = true;
		}

		// Minor hack to cope with the potential of ALL CAPS filename, as per
		// http://www.daisy.org/z3986/specifications/daisy_202.html#ncc
		if (new File(folder, DaisyReaderConstants.FILE_NCC_NAME_CAPS).exists()) {
			result = true;
		}

		if (folder.getAbsolutePath().endsWith(".zip")) {
			result = zipFileContainsDaisy2_02Book(folder.getAbsolutePath());
		}
		return result;
	}

	/**
	 * Does the uri represent a DAISY 2.02 book?
	 * 
	 * @param uri
	 *            textual identifier e.g. a filename or path
	 * @return true if the uri represents a DAISY 2.02 book, else false.
	 */
	public static boolean isDaisy2_02Book(String uri) {
		try {
			ArrayList<String> temp = getContents(uri);
			if (temp != null) {
				temp = null;
				return true;
			}
		} catch (NullPointerException npe) {
			// TODO 20130318 (jharty) For now we will simply skip the error
			// and assume it's not a DAISY book.
			// e.g. .android_secure isn't a DAISY book
		}
		return false;
	}

	private static boolean zipFileContainsDaisy2_02Book(String filename) {
		ZipEntry entry;
		try {
			ZipFile zipContents = new ZipFile(filename);
			Enumeration<? extends ZipEntry> e = zipContents.entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				if (entry.getName().contains(DaisyReaderConstants.FILE_NCC_NAME_NOT_CAPS)
						|| entry.getName().contains(DaisyReaderConstants.FILE_NCC_NAME_CAPS)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * return the NccFileName for a given book's root folder.
	 * 
	 * @param currentDirectory
	 * 
	 * @return the filename as a string if it exists, else null.
	 */
	public static String getNccFileName(File currentDirectory) {
		if (new File(currentDirectory, DaisyReaderConstants.FILE_NCC_NAME_NOT_CAPS).exists()) {
			return DaisyReaderConstants.FILE_NCC_NAME_NOT_CAPS;
		}

		if (new File(currentDirectory, DaisyReaderConstants.FILE_NCC_NAME_CAPS).exists()) {
			return DaisyReaderConstants.FILE_NCC_NAME_CAPS;
		}

		return null;
	}

	/**
	 * get book context from filename.
	 * 
	 * @param filename
	 * @return book context
	 * @throws IOException
	 */
	public static BookContext openBook(String filename) throws IOException {
		BookContext bookContext;
<<<<<<< HEAD

		File directory = new File(filename);
		boolean isDirectory = directory.isDirectory();
		if (isDirectory) {
			bookContext = new FileSystemContext(filename);
		} else {
			// TODO 20130329 (jharty): think through why I used getParent previously.
			bookContext = new FileSystemContext(directory.getParent());
		}
		directory = null;
=======
		if (filename.endsWith(".zip")) {
			bookContext = new ZippedBookContext(filename);
		} else {
			File directory = new File(filename);
			bookContext = new FileSystemContext(directory.getParent());
			directory = null;
		}
>>>>>>> 054cd067ab590d7b270e06c359d1b900a63e034b
		return bookContext;
	}

	/**
	 * get contents of book from path.
	 * 
	 * @param path
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> getContents(String path)
	{
		// Guard code to protect the private methods from needing to check for invalid inputs.
		if (path == null) {
			return null;  // TODO 20130326 (jharty) consider better error reporting.
		}
		
		String chapter = "Chapter";
		Daisy202Book book = getDaisy202Book(path);
		Object[] sections = null;
		if(book == null) {
			// Short circuit the processing, and avoid a NPE bug
			return null;
		}
		
		ArrayList<String> listResult = new ArrayList<String>();
		sections = book.getChildren().toArray();
		for (int i = 0; i < sections.length; i++) {
			Section section = (Section) sections[i];
			int numOfChapter = i + 1;
			listResult.add(String.format("%s %s: %s", chapter, numOfChapter, section.getTitle()));
		}
		return listResult;
	}

	/**
	 * open book from path
	 * 
	 * @param path
	 * @return Daisy202Book
	 */
	public static Daisy202Book getDaisy202Book(String path) {
		InputStream contents;
		Daisy202Book book = null;
		
		try {
			BookContext bookContext = DaisyReaderUtils.openBook(path);
			contents = bookContext.getResource(DaisyReaderConstants.FILE_NCC_NAME_NOT_CAPS);
			book = NccSpecification.readFromStream(contents);
		} catch (Exception e) {
			// TODO 20120515 (jharty): Add test for SDCARD being available
			// so we can tell the user...
			e.printStackTrace();
			return null;
		}
		return book;
	}

	private static ArrayList<String> sResult;

	public static ArrayList<String> getDaisyBook(File path, boolean isLoop) {
		if (!isLoop) {
			sResult = new ArrayList<String>();
		}
		if (DaisyReaderUtils.folderContainsDaisy2_02Book(path)) {
			sResult.add(path.getAbsolutePath());
		} else if (path.listFiles() != null) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (DaisyReaderUtils.folderContainsDaisy2_02Book(file)) {
					sResult.add(file.getAbsolutePath());
				} else if (file.isDirectory()) {
					getDaisyBook(file, true);
				}
			}
		}
		return sResult;
	}
}
