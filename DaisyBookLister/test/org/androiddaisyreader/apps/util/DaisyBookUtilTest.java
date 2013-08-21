package org.androiddaisyreader.test.util;

import java.util.ArrayList;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

// TODO: Auto-generated Javadoc
/**
 * The Class DaisyBookUtilTest.
 */
public class DaisyBookUtilTest extends AndroidTestCase {
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
	/** The context. */
	Context mContext;
	/** The helper. */
	SQLiteDaisyBookHelper helper;

	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "");
		mContext = context;
		helper = new SQLiteDaisyBookHelper(context);

	}
	
	/**
	 * Test search book by title contains in list book.
	 */
	public void testSearchBookReturnTrueIfSearchCorrectly() {
		prepareDataToTest(Constants.TYPE_DOWNLOAD_BOOK);
		ArrayList<DaisyBook> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(arrDaisyBook.size() == 5);
		ArrayList<DaisyBook> arrDaisyBookOriginal = new ArrayList<DaisyBook>(arrDaisyBook);
		ArrayList<DaisyBook> arrResult = DaisyBookUtil.searchBookWithText("a", arrDaisyBook,
				arrDaisyBookOriginal);
		assertTrue(arrResult.size() == 2);
	}
	
	public void testSearchBookReturnFalseIfSearchInCorrectly() {
		prepareDataToTest(Constants.TYPE_DOWNLOAD_BOOK);
		ArrayList<DaisyBook> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(arrDaisyBook.size() == 5);
		ArrayList<DaisyBook> arrDaisyBookOriginal = new ArrayList<DaisyBook>(arrDaisyBook);
		ArrayList<DaisyBook> arrResult = DaisyBookUtil.searchBookWithText("a", arrDaisyBook,
				arrDaisyBookOriginal);
		assertFalse(arrResult.size() != 2);
	}

	/**
	 * Test search book by title is not contain in list book.
	 */
	public void testSearchBookReturnTrueWhenNoContainTextAndResultCorrectly() {
		prepareDataToTest(Constants.TYPE_DOWNLOAD_BOOK);
		ArrayList<DaisyBook> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(arrDaisyBook.size() == 5);
		ArrayList<DaisyBook> arrDaisyBookOriginal = new ArrayList<DaisyBook>(arrDaisyBook);
		ArrayList<DaisyBook> arrResult = DaisyBookUtil.searchBookWithText("a3", arrDaisyBook,
				arrDaisyBookOriginal);
		assertTrue(arrResult.size() == 0);
	}
	
	public void testSearchBookReturnTrueWhenNoContainTextAndResultInCorrectly() {
		prepareDataToTest(Constants.TYPE_DOWNLOAD_BOOK);
		ArrayList<DaisyBook> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(arrDaisyBook.size() == 5);
		ArrayList<DaisyBook> arrDaisyBookOriginal = new ArrayList<DaisyBook>(arrDaisyBook);
		ArrayList<DaisyBook> arrResult = DaisyBookUtil.searchBookWithText("a3", arrDaisyBook,
				arrDaisyBookOriginal);
		assertFalse(arrResult.size() != 0);
	}

	/**
	 * Prepare data to test.
	 *
	 * @param type the (download, recent, scan, downloaded)
	 */
	private void prepareDataToTest(String type) {
		addDaisyBookToDatabase(type, TITLE_DAISY_BOOK + "a");
		addDaisyBookToDatabase(type, TITLE_DAISY_BOOK + "a1");
		addDaisyBookToDatabase(type, TITLE_DAISY_BOOK + "c");
		addDaisyBookToDatabase(type, TITLE_DAISY_BOOK + "c1");
		addDaisyBookToDatabase(type, TITLE_DAISY_BOOK + "d");
	}

	/**
	 * Adds the daisy book to database.
	 *
	 * @param type the (download, recent, scan, downloaded)
	 * @param title the title
	 */
	private void addDaisyBookToDatabase(String type, String title) {
		// initial new daisy book
		DaisyBook daisyBook = new DaisyBook("", title, PATH_DAISY_BOOK, AUTHOR_DAISY_BOOK,
				PUBLISHER_DAISY_BOOK, DATE_DAISY_BOOK, SORT_DAISY_BOOK);
		// add daisy book to local db
		boolean isOK = helper.addDaisyBook(daisyBook, type);
		assertTrue(isOK);
	}

	@Override
	protected void tearDown() throws Exception {
		helper.close();
		super.tearDown();
	}
}
