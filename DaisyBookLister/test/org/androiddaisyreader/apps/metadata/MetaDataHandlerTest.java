package org.androiddaisyreader.apps.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.metadata.MetaDataHandler;
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
	Context mContext;
	
	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "");
		mContext = context;
		Constants.FOLDER_CONTAIN_METADATA = Environment.getExternalStorageDirectory().toString()
				+ "/" + mContext.getString(R.string.app_name) + "/";
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
	 * @param in the in
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	/**
	 * Test read data from xml file.
	 */
	public void testReadDataDownloadFromXmlFile() {
		ReadDataDownloadFromXmlFile(mContext.getString(R.string.web_site_url_htctu));
		ReadDataDownloadFromXmlFile(mContext.getString(R.string.web_site_url_daisy_org));

	}

	/**
	 * Read data download from xml file.
	 *
	 * @param website the website
	 */
	public void ReadDataDownloadFromXmlFile(String website) {
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

	/**
	 * Read data download from xml file illegal argument exception.
	 *
	 * @param website the website
	 */
	public void readDataDownloadFromXmlFileIllegalArgumentException(String website) {
		InputStream nullInputStream = null;
		MetaDataHandler metadata = new MetaDataHandler();
		try {
			metadata.ReadDataDownloadFromXmlFile(nullInputStream, website);
		} catch (IllegalArgumentException e) {
			assertEquals(true, e.getMessage().contains(IllegalArgumentException.class.toString()));
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		File directory = new File(Constants.FOLDER_CONTAIN_METADATA);
		directory.delete();
		Constants.FOLDER_CONTAIN_METADATA = "";
	}
}
