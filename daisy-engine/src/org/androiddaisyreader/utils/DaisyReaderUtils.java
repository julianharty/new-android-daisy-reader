package org.androiddaisyreader.utils;

/**
 * DaisyBookUtils contains helper methods for DaisyBooks.
 * 
 * Currently it's limited to a couple of static methods, and as such doesn't
 * really justify being a class. So, expect things to change :)
 */
import java.io.File;

public final class DaisyReaderUtils {
	public static final String LAST_BOOK = "last_book_open";
	public static final String PREFS_FILE = "DaisyReaderPreferences";
	public static final String DEFAULT_ROOT_FOLDER = "/sdcard/";
	public static final String OPT_ROOT_FOLDER = "rootfolder";
	
	// Don't allow anyone to create this utility class.
	private DaisyReaderUtils() {};
	
	/**
	 * Tests if the directory contains the essential root file for a Daisy book
	 * 
	 * Currently it's limited to checking for Daisy 2.02 books.
	 * @param folder for the directory to check
	 * @return true if the directory is deemed to contain a Daisy Book, else
	 * false.
	 */
    public static boolean folderContainsDaisy2_02Book(File folder) {
        if (!folder.isDirectory()) {
            return false;
        }

        if (new File(folder, "ncc.html").exists()) {
        	return true;
        }
        
        // Minor hack to cope with the potential of ALL CAPS filename, as per
        // http://www.daisy.org/z3986/specifications/daisy_202.html#ncc
        if (new File(folder, "NCC.HTML").exists()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * returns the NccFileName for a given book's root folder.
     * @param currentDirectory
     * @return the filename as a string if it exists, else null.
     */
    public static String getNccFileName(File currentDirectory) {
    	if (new File(currentDirectory, "ncc.html").exists()) {
    		return "ncc.html";
    	}

    	if (new File(currentDirectory, "NCC.HTML").exists()) {
    		return "NCC.HTML";
    	}

		return null;
	}
}
