package org.androiddaisyreader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.DaisyBookInfo;
import org.androiddaisyreader.model.FileSystemContext;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.OpfSpecification;
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
    public static ArrayList<DaisyBookInfo> searchBookWithText(CharSequence textSearch,
            ArrayList<DaisyBookInfo> listBook, ArrayList<DaisyBookInfo> listBookOriginal) {
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

    public static boolean folderContainsDaisy202Book(File folder) {
        boolean result = false;
        if (folder.getAbsolutePath().endsWith(".zip")) {
            result = zipFileContainsDaisy202Book(folder.getAbsolutePath());
        } else {

            if (!folder.isDirectory()) {
                result = false;
            }

            if (folder.getAbsolutePath().contains(Constants.FILE_NCC_NAME_NOT_CAPS)) {
                result = true;
            }

            if (new File(folder, Constants.FILE_NCC_NAME_NOT_CAPS).exists()) {
                result = true;
            }
            // Minor hack to cope with the potential of ALL CAPS filename, as
            // per
            // http://www.daisy.org/z3986/specifications/daisy_202.html#ncc
            if (new File(folder, Constants.FILE_NCC_NAME_CAPS).exists()) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Does the folder represent a DAISY 3.0 book?
     * 
     * @param folder the folder
     * @return true, if successful
     */
    public static boolean folderContainsDaisy30Book(File folder) {
        boolean result = false;
        if (!folder.isDirectory()) {
            result = false;
        }
        String fileName = getOpfFileName(folder.getAbsolutePath());
        if (folder.getAbsolutePath().endsWith(".zip")) {
            fileName = getOpfFileNameInZipFolder(folder.getAbsolutePath());
        }
        if (fileName != null) {
            result = true;
        }
        return result;
    }

    /**
     * 
     * @param filename
     * @return true if the uri has a zip file daisy book, else false.
     */
    private static boolean zipFileContainsDaisy202Book(String filename) {
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
     * Gets the opf file name in zip folder.
     * 
     * @param path the path
     * @return the opf file name in zip folder
     */
    public static String getOpfFileNameInZipFolder(String path) {
        String result = "";
        ZipEntry entry;
        try {
            ZipFile zipContents = new ZipFile(path);
            Enumeration<? extends ZipEntry> e = zipContents.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.getName().endsWith(".opf")) {
                    result = entry.getName();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
     * open book from path
     * 
     * @param path
     * @return Daisy202Book
     */
    public static DaisyBook getDaisy202Book(String path) throws IOException {
        InputStream contents;
        DaisyBook book = null;
        BookContext bookContext = openBook(path);
        contents = bookContext.getResource(Constants.FILE_NCC_NAME_NOT_CAPS);
        if (contents == null) {
            return null;
        }
        book = NccSpecification.readFromStream(contents);
        return book;
    }

    /**
     * Gets the daisy30 book.
     * 
     * @param path the path
     * @return the daisy30 book
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static DaisyBook getDaisy30Book(String path) throws IOException {
        InputStream contents = null;
        DaisyBook book = null;
        String filename = "";
        BookContext bookContext = null;
        if (path.endsWith(".zip")) {
            bookContext = openBook(path);
            contents = bookContext.getResource(getOpfFileNameInZipFolder(path));
        } else {
            filename = path + File.separator + getOpfFileName(path);
            bookContext = openBook(filename);
            contents = bookContext.getResource(getOpfFileName(path));
        }
        if (contents == null) {
            return null;
        }
        book = OpfSpecification.readFromStream(contents, bookContext);
        return book;
    }

    /**
     * Gets the opf file name.
     * 
     * @param path the folder contains file opf.
     * @return the opf file name
     */
    public static String getOpfFileName(String path) {
        String fileName = null;
        File folder = new File(path);
        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".opf")) {
                        fileName = file.getName();
                        break;
                    }
                }
            }
        }
        return fileName;
    }

    private static ArrayList<String> sResult;

    /**
     * Gets the daisy book.
     * 
     * @param path the path
     * @param isLoop the is loop
     * @return the daisy book
     */
    public static ArrayList<String> getDaisyBook(File path, boolean isLoop) {
        if (!isLoop) {
            sResult = new ArrayList<String>();
        }
        if (folderContainsDaisy202Book(path) || folderContainsDaisy30Book(path)) {
            sResult.add(path.getAbsolutePath());
        } else if (path.listFiles() != null) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (folderContainsDaisy202Book(file) || folderContainsDaisy30Book(file)) {
                    sResult.add(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    getDaisyBook(file, true);
                }
            }
        }
        return sResult;
    }

    /**
     * Find daisy format.
     * 
     * @param path the path
     * @return the int
     */
    public static int findDaisyFormat(String path) {
        int result = 0;
        File file = new File(path);
        if (folderContainsDaisy202Book(new File(path))) {
            result = Constants.DAISY_202_FORMAT;
        } else if (folderContainsDaisy30Book(file)) {
            result = Constants.DAISY_30_FORMAT;
        }
        return result;
    }
}
