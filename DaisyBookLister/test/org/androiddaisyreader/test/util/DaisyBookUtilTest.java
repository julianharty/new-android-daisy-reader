package org.androiddaisyreader.test.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.androiddaisyreader.model.DaisyBookInfo;
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
	
	private static final String PATH_EBOOK_202 = "/storage/sdcard0/minidaisyaudiobook/minidaisyaudiobook";
	
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
		List<DaisyBookInfo> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(arrDaisyBook.size() == 5);
		List<DaisyBookInfo> arrDaisyBookOriginal = new ArrayList<DaisyBookInfo>(arrDaisyBook);
		List<DaisyBookInfo> arrResult = DaisyBookUtil.searchBookWithText("a", arrDaisyBook,
				arrDaisyBookOriginal);
		assertTrue(arrResult.size() == 2);
	}
	
	/**
	 * Test search book by title is not contain in list book.
	 */
	public void testSearchBookWhenNoContainText() {
		prepareDataToTest(Constants.TYPE_DOWNLOAD_BOOK);
		List<DaisyBookInfo> arrDaisyBook = helper.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		assertTrue(arrDaisyBook.size() == 5);
		List<DaisyBookInfo> arrDaisyBookOriginal = new ArrayList<DaisyBookInfo>(arrDaisyBook);
		List<DaisyBookInfo> arrResult = DaisyBookUtil.searchBookWithText("a3", arrDaisyBook,
				arrDaisyBookOriginal);
		assertTrue(arrResult.size() == 0);
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
		DaisyBookInfo daisyBook = new DaisyBookInfo("", title, PATH_DAISY_BOOK, AUTHOR_DAISY_BOOK,
				PUBLISHER_DAISY_BOOK, DATE_DAISY_BOOK, SORT_DAISY_BOOK);
		// add daisy book to local db
		boolean isOK = helper.addDaisyBook(daisyBook, type);
		assertTrue(isOK);
	}
	
	/**
	 * Test check ebook is daisy format202.
	 */
	public void testCheckEBookIsDaisyFormat202(){
		File daisyPath = new File(PATH_EBOOK_202);
		String pathNCC = PATH_EBOOK_202 + File.separator
				+ DaisyBookUtil.getNccFileName(daisyPath);
		int result = DaisyBookUtil.findDaisyFormat(pathNCC);
		assertEquals("Expected Daisy format 202", Constants.DAISY_202_FORMAT, result);
	}
	
	/**
	 * Test folder contains daisy202 return true when path correctly.
	 */
	public void testFolderContainsDaisy202ReturnTrueWhenPathCorrectly(){
		File folder = new File(PATH_EBOOK_202);
		boolean result = DaisyBookUtil.folderContainsDaisy202Book(folder);
		assertTrue(result);
	}
	
	/**
	 * Test folder contains daisy202 return false when path in correctly.
	 */
	public void testFolderContainsDaisy202ReturnFalseWhenPathInCorrectly(){
		File folder = new File("wrong path");
		boolean result = DaisyBookUtil.folderContainsDaisy202Book(folder);
		assertFalse(result);
	}
		
	@Override
	protected void tearDown() throws Exception {
		helper.close();
		super.tearDown();
	}
}
