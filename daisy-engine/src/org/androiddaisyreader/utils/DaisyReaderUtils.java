package org.androiddaisyreader.utils;

/**
 * DaisyBookUtils contains helper methods for DaisyBooks.
 * 
 * Currently it's limited to a couple of static methods, and as such doesn't
 * really justify being a class. So, expect things to change :)
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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

		if (new File(folder, DaisyReaderConstants.FILE_NAME_NOT_CAPS).exists()) {
			result = true;
		}

		// Minor hack to cope with the potential of ALL CAPS filename, as per
		// http://www.daisy.org/z3986/specifications/daisy_202.html#ncc
		if (new File(folder, DaisyReaderConstants.FILE_NAME_CAPS).exists()) {
			result = true;
		}

		if (folder.getAbsolutePath().endsWith(".zip")) {
			result = zipFileContainsDaisy2_02Book(folder.getAbsolutePath());
		}
		return result;
	}
	
    /**
     * Does the uri represent a DAISY 2.02 book?
     * @param uri textual identifier e.g. a filename or path
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
				if (entry.getName().contains(DaisyReaderConstants.FILE_NAME_NOT_CAPS)
						|| entry.getName().contains(DaisyReaderConstants.FILE_NAME_CAPS)) {
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
		if (new File(currentDirectory, DaisyReaderConstants.FILE_NAME_NOT_CAPS).exists()) {
			return DaisyReaderConstants.FILE_NAME_NOT_CAPS;
		}

		if (new File(currentDirectory, DaisyReaderConstants.FILE_NAME_CAPS).exists()) {
			return DaisyReaderConstants.FILE_NAME_CAPS;
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
		if (filename.endsWith(".zip")) {
			bookContext = new ZippedBookContext(filename);
		} else {
			File directory = new File(filename);
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
	public static ArrayList<String> getContents(String path) {
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
	public static Daisy202Book getDaisy202Book(String path) {
		InputStream contents;
		Daisy202Book book = null;
		try {
			BookContext bookContext;
			bookContext = DaisyReaderUtils.openBook(path);
			String[] sp = path.split("/");
			contents = bookContext.getResource(sp[sp.length - 1]);
			book = NccSpecification.readFromStream(contents);
		} catch (Exception e) {
			// TODO 20120515 (jharty): Add test for SDCARD being available
			// so we can tell the user...
			e.printStackTrace();
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

	public static String unzip(String zipFile, String location) {
		String nameOfFolder = "";
		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			File folder = new File(location);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(location + File.separator + fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());
				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				File f = new File(newFile.getParent());
				if (nameOfFolder == "") {
					nameOfFolder = f.getName();
				}

				if (!f.exists()) {
					f.mkdir();
				}

				if (newFile.getName().contains(".")) {

					FileOutputStream fos = new FileOutputStream(newFile);

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
		} catch (Exception e) {
			System.out.println("Can not Unzip");
		}
		return nameOfFolder;

	}
}
