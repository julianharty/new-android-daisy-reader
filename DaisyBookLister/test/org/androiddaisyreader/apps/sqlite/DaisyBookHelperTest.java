package org.androiddaisyreader.apps.sqlite;

import java.util.ArrayList;

import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.Constants;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * The Class DaisyBookHelperTest.
 */
public class DaisyBookHelperTest extends AndroidTestCase {
	/** The Constant TITLE_DAISY_BOOK. */
	private static final String TITLE_DAISY_BOOK = "title";

	/** The Constant PATH_DAISY_BOOK. */
	private static final String PATH_DAISY_BOOK = "path";

	/** The Constant AUTHOR_DAISY_BOOK. */
	private static final String AUTHOR_DAISY_BOOK = "author";

	/** The Constant PUBLISHER_DAISY_BOOK. */
	private static final String PUBLISHER_DAISY_BOOK = "publisher";

	/** The Constant DATE_DAISY_BOOK. */
	private static final String DATE_DAISY_BOOK = "date";

	/** The Constant SORT_DAISY_BOOK. */
	private static final int SORT_DAISY_BOOK = 1;

	/** The helper. */
	SQLiteDaisyBookHelper helper;

	/** The context. */
	Context context;

	@Override
	protected void setUp() throws Exception {
		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "");
		helper = new SQLiteDaisyBookHelper(context);
		super.setUp();
	}

	/**
	 * Test adding a daisy book into local db (with type is 'download').
	 */
	public void testDaisyBookToDatabase() {
		addDaisyBookToDatabase(Constants.TYPE_DOWNLOAD_BOOK);
		addDaisyBookToDatabase(Constants.TYPE_RECENT_BOOK);
		addDaisyBookToDatabase(Constants.TYPE_SCAN_BOOK);
		addDaisyBookToDatabase(Constants.TYPE_DOWNLOADED_BOOK);
	}

	/**
	 * Adds the daisy book to database.
	 * 
	 * @param type
	 *            the book (download, recent, scan, downloaded)
	 */
	public void addDaisyBookToDatabase(String type) {
		// initial new daisy book
		DaisyBook daisyBook = new DaisyBook("", TITLE_DAISY_BOOK, PATH_DAISY_BOOK,
				AUTHOR_DAISY_BOOK, PUBLISHER_DAISY_BOOK, DATE_DAISY_BOOK, SORT_DAISY_BOOK);
		// add daisy book to local db
		boolean isOK = helper.addDaisyBook(daisyBook, type);
		assertTrue(isOK);
	}

	/**
	 * Test get all daisy book from local db.
	 */
	public void testGetAllDaisyBook() {
		getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		getAllDaisyBook(Constants.TYPE_RECENT_BOOK);
		getAllDaisyBook(Constants.TYPE_SCAN_BOOK);
		getAllDaisyBook(Constants.TYPE_DOWNLOADED_BOOK);
	}

	/**
	 * Gets the all daisy book.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 * @return the all daisy book
	 */
	public void getAllDaisyBook(String type) {
		addDaisyBookToDatabase(type);
		ArrayList<DaisyBook> arrDaisyBook = helper.getAllDaisyBook(type);
		assertTrue(arrDaisyBook != null);
	}

	/**
	 * Test delete all daisy book from local db.
	 */
	public void testDeleteDaisyBook() {
		deleteDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		deleteDaisyBook(Constants.TYPE_RECENT_BOOK);
		deleteDaisyBook(Constants.TYPE_SCAN_BOOK);
		deleteDaisyBook(Constants.TYPE_DOWNLOADED_BOOK);
	}

	/**
	 * Delete daisy book.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 */
	public void deleteDaisyBook(String type) {
		addDaisyBookToDatabase(type);
		boolean result = helper.DeleteAllDaisyBook(type);
		assertTrue(result);
		ArrayList<DaisyBook> arrDaisyBook = helper.getAllDaisyBook(type);
		assertFalse(arrDaisyBook.size() > 0);
	}

	/**
	 * Test record exist.
	 */
	public void testRecordExist() {
		recordExist(Constants.TYPE_DOWNLOAD_BOOK);
		recordExist(Constants.TYPE_RECENT_BOOK);
		recordExist(Constants.TYPE_SCAN_BOOK);
		recordExist(Constants.TYPE_DOWNLOADED_BOOK);
	}

	/**
	 * Record exist.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 */
	public void recordExist(String type) {
		addDaisyBookToDatabase(type);
		boolean result = helper.isExists(TITLE_DAISY_BOOK, type);
		assertTrue(result);
	}

	/**
	 * Test get daisy book by title.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 */
	public void testGetDaisyBookByTitle(String type) {
		getDaisyBookByTitle(Constants.TYPE_DOWNLOAD_BOOK);
		getDaisyBookByTitle(Constants.TYPE_RECENT_BOOK);
		getDaisyBookByTitle(Constants.TYPE_SCAN_BOOK);
		getDaisyBookByTitle(Constants.TYPE_DOWNLOADED_BOOK);
	}

	/**
	 * Gets the daisy book by title.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 * @return the daisy book by title
	 */
	public void getDaisyBookByTitle(String type) {
		addDaisyBookToDatabase(type);
		DaisyBook daisyBook = helper.getDaisyBookByTitle(TITLE_DAISY_BOOK, type);
		assertTrue(daisyBook != null);
	}

	@Override
	protected void tearDown() throws Exception {
		helper.close();
		super.tearDown();
	}

}
