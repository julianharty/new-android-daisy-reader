package org.androiddaisyreader.test.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.utils.Constants;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * The Class MetaDataHandlerTest.
 */
public class MetaDataHandlerTest extends AndroidTestCase {
	/** The context. */
	private Context mContext;
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

	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "");
		mContext = context;
		Constants.FOLDER_CONTAIN_METADATA = Environment.getExternalStorageDirectory().toString()
				+ "/" + Constants.FOLDER_NAME + "/";
		File directory = new File(Constants.FOLDER_CONTAIN_METADATA);
		// Create a File object for the parent directory
		directory.mkdirs();
		// Then run the method to copy the file.
		copyFileFromAssets();

	}

	/**
	 * Copy the file from the assets folder to the sdCard
	 **/
	private void copyFileFromAssets() {
		File file = new File(Constants.FOLDER_CONTAIN_METADATA + Constants.META_DATA_FILE_NAME);
		if (!file.exists()) {
			AssetManager assetManager = mContext.getAssets();
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(Constants.META_DATA_FILE_NAME);
				out = new FileOutputStream(Constants.FOLDER_CONTAIN_METADATA
						+ Constants.META_DATA_FILE_NAME);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, mContext);
				ex.writeLogException();
			}
		}
	}

	/**
	 * Copy file.
	 * 
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private ArrayList<DaisyBook> createDataForScanBook(int number) {
		ArrayList<DaisyBook> list = new ArrayList<DaisyBook>();
		// initial new daisy book
		for (int i = 0; i < number; i++) {
			DaisyBook daisyBook = new DaisyBook("", TITLE_DAISY_BOOK + String.valueOf(i),
					PATH_DAISY_BOOK, AUTHOR_DAISY_BOOK, PUBLISHER_DAISY_BOOK, DATE_DAISY_BOOK,
					SORT_DAISY_BOOK);
			list.add(daisyBook);
		}
		return list;
	}

	/**
	 * Test read data from xml file.
	 */
	public void testReadDataDownloadFromXmlFile() {
		readDataDownloadFromXmlFile(mContext.getString(R.string.web_site_url_htctu));
		readDataDownloadFromXmlFile(mContext.getString(R.string.web_site_url_daisy_org));

	}

	private void readDataDownloadFromXmlFile(String website) {
		/** The input Stream. */
		InputStream databaseInputStream = null;
		MetaDataHandler metadata = new MetaDataHandler();
		try {
			databaseInputStream = new FileInputStream(Constants.FOLDER_CONTAIN_METADATA
					+ Constants.META_DATA_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
		NodeList nlistDaisy = metadata.ReadDataDownloadFromXmlFile(databaseInputStream, website);
		assertEquals(true, nlistDaisy.getLength() > 0);
	}

	/**
	 * Test read data from xml file throw Illegal Argument Exception
	 */
	public void testReadDataDownloadFromXmlFileIllegalArgumentException() {
		readDataDownloadFromXmlFileIllegalArgumentException(mContext
				.getString(R.string.web_site_url_daisy_org));
		readDataDownloadFromXmlFileIllegalArgumentException(mContext
				.getString(R.string.web_site_url_htctu));
	}

	private void readDataDownloadFromXmlFileIllegalArgumentException(String website) {
		InputStream nullInputStream = null;
		MetaDataHandler metadata = new MetaDataHandler();
		try {
			metadata.ReadDataDownloadFromXmlFile(nullInputStream, website);
		} catch (IllegalArgumentException e) {
			assertEquals(true, e.getMessage().contains(IllegalArgumentException.class.toString()));
		}
	}

	/**
	 * Test write data to xml file.
	 */
	public void testWriteDataToXmlFile() {
		String localPath = Constants.FOLDER_CONTAIN_METADATA
				+ Constants.META_DATA_SCAN_BOOK_FILE_NAME;
		MetaDataHandler metadata = new MetaDataHandler();
		ArrayList<DaisyBook> list = createDataForScanBook(5);
		assertTrue(list.size() > 0);
		metadata.WriteDataToXmlFile(list, localPath);
		File file = new File(localPath);
		assertTrue(file.length() > 0);
	}

	/**
	 * Test read data scan from xml file.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public void testReadDataScanFromXmlFile() throws FileNotFoundException {
		String localPath = Constants.FOLDER_CONTAIN_METADATA
				+ Constants.META_DATA_SCAN_BOOK_FILE_NAME;
		MetaDataHandler metadata = new MetaDataHandler();
		ArrayList<DaisyBook> list = createDataForScanBook(5);
		assertTrue(list.size() > 0);
		metadata.WriteDataToXmlFile(list, localPath);

		InputStream databaseInputStream = null;
		databaseInputStream = new FileInputStream(localPath);
		NodeList nList = metadata.ReadDataScanFromXmlFile(databaseInputStream);
		assertTrue(nList.getLength() > 0);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		File directory = new File(Constants.FOLDER_CONTAIN_METADATA);
		directory.delete();
		Constants.FOLDER_CONTAIN_METADATA = "";
	}
}
