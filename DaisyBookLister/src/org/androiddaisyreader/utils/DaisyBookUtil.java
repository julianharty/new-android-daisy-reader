package org.androiddaisyreader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.androiddaisyreader.daisy30.Daisy30Book;
import org.androiddaisyreader.daisy30.OpfSpecification;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.FileSystemContext;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.model.ZippedBookContext;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author LogiGear
 * @date Jul 15, 2013
 */

public class DaisyBookUtil {
	/**
	 * Search book with text.
	 * 
	 * @param textSearch the text search
	 * @param listBook the list recent books
	 * @param listBookOriginal the list recent book original
	 */
	public static ArrayList<DaisyBook> searchBookWithText(CharSequence textSearch,
			ArrayList<DaisyBook> listBook, ArrayList<DaisyBook> listBookOriginal) {
		listBook.clear();
		for (int i = 0; i < listBookOriginal.size(); i++) {
			if (listBookOriginal.get(i).getTitle().toString().toUpperCase(Locale.getDefault())
					.contains(textSearch.toString().toUpperCase(Locale.getDefault()))) {
				listBook.add(listBookOriginal.get(i));
			}
		}
		return listBook;
	}

	/**
	 * Get status of connection.
	 * 
	 * @param context
	 * @return status of connection
	 */
	public static int getConnectivityStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (null != activeNetwork) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return Constants.TYPE_WIFI;

			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return Constants.TYPE_MOBILE;
		}
		return Constants.TYPE_NOT_CONNECTED;
	}

	/**
	 * Tests if the directory contains the essential root file for a Daisy book
	 * Currently it's limited to checking for Daisy 2.02 books.
	 * 
	 * @param folder for the directory to check
	 * @return true if the directory is deemed to contain a Daisy Book, else
	 *         false.
	 */

	public static boolean folderContainsDaisy2_02Book(File folder) {
		boolean result = false;
		if (!folder.isDirectory()) {
			result = false;
		}
		if (new File(folder, Constants.FILE_NCC_NAME_NOT_CAPS).exists()) {
			result = true;
		}
		// Minor hack to cope with the potential of ALL CAPS filename, as per
		// http://www.daisy.org/z3986/specifications/daisy_202.html#ncc
		if (new File(folder, Constants.FILE_NCC_NAME_CAPS).exists()) {
			result = true;
		}
		if (folder.getAbsolutePath().endsWith(".zip")) {
			result = zipFileContainsDaisy2_02Book(folder.getAbsolutePath());
		}
		return result;
	}

	public static boolean folderContainsDaisy3Book(File folder) {
		boolean result = false;
		if (!folder.isDirectory()) {
			result = false;
		}
		String fileName = getFileNameOpf(folder.getAbsolutePath());
		if (fileName != "") {
			result = true;
		}
		return result;
	}

	/**
	 * Does the uri represent a DAISY 2.02 book?
	 * 
	 * @param uri textual identifier e.g. a filename or path
	 * @return true if the uri represents a DAISY 2.02 book, else false.
	 */
	public static boolean isDaisy2_02Book(String uri) throws IOException {
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

	/**
	 * 
	 * @param filename
	 * @return true if the uri has a zip file daisy book, else false.
	 */
	private static boolean zipFileContainsDaisy2_02Book(String filename) {
		ZipEntry entry;
		try {
			ZipFile zipContents = new ZipFile(filename);
			Enumeration<? extends ZipEntry> e = zipContents.entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				if (entry.getName().contains(Constants.FILE_NCC_NAME_NOT_CAPS)
						|| entry.getName().contains(Constants.FILE_NCC_NAME_CAPS)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * return the NccFileName for a given book's root folder.
	 * 
	 * @param currentDirectory
	 * 
	 * @return the filename as a string if it exists, else null.
	 */
	public static String getNccFileName(File currentDirectory) {
		if (new File(currentDirectory, Constants.FILE_NCC_NAME_NOT_CAPS).exists()) {
			return Constants.FILE_NCC_NAME_NOT_CAPS;
		}

		if (new File(currentDirectory, Constants.FILE_NCC_NAME_CAPS).exists()) {
			return Constants.FILE_NCC_NAME_CAPS;
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
		File directory = new File(filename);
		boolean isDirectory = directory.isDirectory();
		if (isDirectory) {
			bookContext = new FileSystemContext(filename);
		} else {
			// TODO 20130329 (jharty): think through why I used getParent
			// previously.
			bookContext = new FileSystemContext(directory.getParent());
		}
		directory = null;
		if (filename.endsWith(".zip")) {
			bookContext = new ZippedBookContext(filename);
		} else {
			directory = new File(filename);
			bookContext = new FileSystemContext(directory.getParent());
			directory = null;
		}
		return bookContext;
	}

	/**
	 * get contents of book from path.
	 * 
	 * @param path
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> getContents(String path) throws IOException {
		String chapter = "Chapter";
		Daisy202Book book = getDaisy202Book(path);
		Object[] sections = null;
		if (book != null)
			sections = book.getChildren().toArray();
		ArrayList<String> listResult = new ArrayList<String>();
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
	public static Daisy202Book getDaisy202Book(String path) throws IOException {
		InputStream contents;
		Daisy202Book book = null;
		BookContext bookContext = openBook(path);
		contents = bookContext.getResource(Constants.FILE_NCC_NAME_NOT_CAPS);
		book = NccSpecification.readFromStream(contents);
		return book;
	}

	public static Daisy30Book getDaisy30Book(String path) throws IOException {
		InputStream contents;
		Daisy30Book book = null;
		String resourceFile = "";
		resourceFile = getFileNameOpf(path);
		BookContext bookContext = openBook(path + File.separator + resourceFile);
		contents = bookContext.getResource(resourceFile);
		book = OpfSpecification.readFromStream(contents, bookContext);
		return book;
	}

	public static String getFileNameOpf(String path) {
		String fileName = "";
		File folder = new File(path);
		if (folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();

			for (File file : listOfFiles) {
				if (file.isFile() && file.getName().endsWith(".opf")) {
					fileName = file.getName();
					break;
				}
			}
		}
		return fileName;
	}

	private static ArrayList<String> sResult;

	/**
	 * Gets the daisy book.
	 * @param path the path
	 * @param isLoop the is loop
	 * @return the daisy book
	 */
	public static ArrayList<String> getDaisyBook(File path, boolean isLoop) {
		if (!isLoop) {
			sResult = new ArrayList<String>();
		}
		if (folderContainsDaisy2_02Book(path) || folderContainsDaisy3Book(path)) {
			sResult.add(path.getAbsolutePath());
		} else if (path.listFiles() != null) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (folderContainsDaisy2_02Book(file) || folderContainsDaisy3Book(file)) {
					sResult.add(file.getAbsolutePath());
				} else if (file.isDirectory()) {
					getDaisyBook(file, true);
				}
			}
		}
		return sResult;
	}

	public static int findDaisyFormat(String path) {
		int result = 0;
		File file = new File(path);
		if (path.toLowerCase(Locale.getDefault()).contains(Constants.FILE_NCC_NAME_NOT_CAPS)) {
			result = Constants.DAISY_202_FORMAT;
		} else if (folderContainsDaisy3Book(file)) {
			result = Constants.DAISY_30_FORMAT;
		}
		return result;
	}
}
