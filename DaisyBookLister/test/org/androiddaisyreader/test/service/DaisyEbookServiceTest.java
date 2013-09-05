/*
 * 
 */
package org.androiddaisyreader.test.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.DaisyBookInfo;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.annotation.SuppressLint;
import android.test.AndroidTestCase;

public class DaisyEbookServiceTest extends AndroidTestCase {
	private static final String PATH_EBOOK_202 = "/storage/sdcard0/minidaisyaudiobook/minidaisyaudiobook";

	/**
	 * Test get ebook202 from path
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressLint("DefaultLocale")
	public void testGetEbook202FromPath() throws IOException {
		// fixture setup
		String path = PATH_EBOOK_202;
		File daisyPath = new File(path);
		DaisyBook mBook202 = null;
		DaisyBookInfo daisyBook = null;
		if (!daisyPath.getAbsolutePath().endsWith(Constants.SUFFIX_ZIP_FILE)) {
			// exercise
			if (DaisyBookUtil.getNccFileName(daisyPath) != null) {
				path = path + File.separator + DaisyBookUtil.getNccFileName(daisyPath);
				mBook202 = DaisyBookUtil.getDaisy202Book(path);
			}
			
			if (mBook202 != null) {
				Date date = mBook202.getDate();
				String sDate = "";
				if (date != null) {
					sDate = String.format(("%tB %te, %tY %n"), date, date, date, date);
				}
				daisyBook = new DaisyBookInfo("", mBook202.getTitle(), path, mBook202.getAuthor(),
						mBook202.getPublisher(), sDate, 1);
				// verify
				assertNotNull(daisyBook);
				assertEquals("title", daisyBook.getTitle(), "A mini DAISY book for testing");
				assertEquals("author", daisyBook.getAuthor(), "Julian Harty");
				assertEquals("publisher", daisyBook.getPublisher(), "Julian Harty");
				assertEquals("date", daisyBook.getDate().trim(), "August 28, 2011");
				
			} else {
				fail("wrong path");
			}
		}
	}
}
