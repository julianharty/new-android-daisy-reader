package org.androiddaisyreader.test.sqlite;

import java.util.ArrayList;

import org.androiddaisyreader.model.DaisyBookInfo;
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
	public void testAddDaisyBookToDatabaseReturnTrueWhenAllConventionsMet() {
		assertEquals(addDaisyBookValidToDatabase(Constants.TYPE_DOWNLOAD_BOOK), true);
		assertEquals(addDaisyBookValidToDatabase(Constants.TYPE_RECENT_BOOK), true);
		assertEquals(addDaisyBookValidToDatabase(Constants.TYPE_SCAN_BOOK), true);
		assertEquals(addDaisyBookValidToDatabase(Constants.TYPE_DOWNLOADED_BOOK), true);
	}
	
	/**
	 * Test add daisy book to database return false when title is null.
	 */
	public void testAddDaisyBookToDatabaseReturnFalseWhenTitleIsNull(){
		// initial new daisy book
		DaisyBookInfo daisyBook = new DaisyBookInfo("", null, PATH_DAISY_BOOK, AUTHOR_DAISY_BOOK,
				PUBLISHER_DAISY_BOOK, DATE_DAISY_BOOK, SORT_DAISY_BOOK);
				// add daisy book to local db
				boolean result = helper.addDaisyBook(daisyBook, Constants.TYPE_DOWNLOAD_BOOK);
				assertFalse(result);
	}
	
	/**
	 * Adds the daisy book to database.
	 * 
	 * @param type the book (download, recent, scan, downloaded)
	 * @return true, if successful
	 */
	public boolean addDaisyBookValidToDatabase(String type) {
		// initial new daisy book
		DaisyBookInfo daisyBook = new DaisyBookInfo("", TITLE_DAISY_BOOK, PATH_DAISY_BOOK,
				AUTHOR_DAISY_BOOK, PUBLISHER_DAISY_BOOK, DATE_DAISY_BOOK, SORT_DAISY_BOOK);
		// add daisy book to local db
		return helper.addDaisyBook(daisyBook, type);
	}

	/**
	 * Test get all daisy book from local db.
	 */
	public void testDaisyBookReturnTrueWhenTypeIsCorrect() {
		//exercise
		ArrayList<DaisyBookInfo> daisyInfoList = getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		DaisyBookInfo daisyInfo = daisyInfoList.get(0);
		//verify
		assertNotNull(daisyInfo);
		assertEquals("title", daisyInfo.getTitle(), TITLE_DAISY_BOOK);
		assertEquals("author", daisyInfo.getAuthor(), AUTHOR_DAISY_BOOK);
		assertEquals("publisher", daisyInfo.getPublisher(), PUBLISHER_DAISY_BOOK);
		assertEquals("date", daisyInfo.getDate(), DATE_DAISY_BOOK);
	}
	
	/**
	 * Gets the all daisy book.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 * @return the all daisy book
	 */
	public ArrayList<DaisyBookInfo> getAllDaisyBook(String type) {
		addDaisyBookValidToDatabase(type);
		ArrayList<DaisyBookInfo> arrDaisyBook = helper.getAllDaisyBook(type);
		return arrDaisyBook;
	}

	/**
	 * Test delete all daisy book from local db.
	 */
	public void testDeleteDaisyBookReturnTrueWhenTypeIsCorrect() {
		addDaisyBookValidToDatabase(Constants.TYPE_DOWNLOAD_BOOK);
		boolean result = helper.DeleteAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(result);
		ArrayList<DaisyBookInfo> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertFalse(arrDaisyBook.size() > 0);
	}

	/**
	 * Test delete daisy book return false when type is wrong.
	 */
	public void testDeleteDaisyBookReturnFalseWhenTypeIsWrong() {
		addDaisyBookValidToDatabase("download");
		boolean result = helper.DeleteAllDaisyBook("delete");
		assertFalse(result);
	}
	
	/**
	 * Test record exist return true when exist.
	 */
	public void testValidReturnTrueWhenRecordExist() {
		addDaisyBookValidToDatabase(Constants.TYPE_DOWNLOAD_BOOK);
		boolean result = helper.isExists(TITLE_DAISY_BOOK, Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(result);
	}

	/**
	 * Test valid return false when record not exist.
	 */
	public void testValidReturnFalseWhenRecordNotExist() {
		addDaisyBookValidToDatabase("type1");
		boolean result = helper.isExists(TITLE_DAISY_BOOK, "type2");
		assertFalse(result);
	}

	/**
	 * Test get daisy book by title.
	 * 
	 * @param type
	 *            the (download, recent, scan, downloaded)
	 */
	public void testGetDaisyBookByTitle() {
		addDaisyBookValidToDatabase(Constants.TYPE_DOWNLOAD_BOOK);
		DaisyBookInfo daisyBook = helper.getDaisyBookByTitle(TITLE_DAISY_BOOK, Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(daisyBook != null);
	}

	@Override
	protected void tearDown() throws Exception {
		helper.close();
		super.tearDown();
	}
}
