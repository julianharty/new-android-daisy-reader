/*
 * 
 */
package org.androiddaisyreader.test.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.annotation.SuppressLint;
import android.test.AndroidTestCase;

public class DaisyEbookServiceTest extends AndroidTestCase {
	private static final String PATH_EBOOK_202 = "/storage/sdcard0/light-man/light-man";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * Test get ebook202 from path
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressLint("DefaultLocale")
	public void testGetEbook202FromPath() throws IOException {
		String path = PATH_EBOOK_202;
		File daisyPath = new File(path);
		Daisy202Book mBook202 = null;
		DaisyBook daisyBook = null;
		try {
			if (!daisyPath.getAbsolutePath().endsWith(Constants.SUFFIX_ZIP_FILE)) {
				if (DaisyBookUtil.getNccFileName(daisyPath) != null) {
					path = path + File.separator
							+ DaisyBookUtil.getNccFileName(daisyPath);
					mBook202 = DaisyBookUtil.getDaisy202Book(path);
				}
			}
			if (mBook202 != null) {
				Date date = mBook202.getDate();
				String sDate = "";
				if (date != null) {
					sDate = String.format(("%tB %te, %tY %n"), date, date, date, date);
				}
				daisyBook = new DaisyBook("", mBook202.getTitle(), path, mBook202.getAuthor(),
						mBook202.getPublisher(), sDate, 1);
				assertNotNull(daisyBook);
			}else{
				assertNull(daisyBook);
			}
		} catch (Exception e) {
			assertNull(daisyBook);
		}
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
}
