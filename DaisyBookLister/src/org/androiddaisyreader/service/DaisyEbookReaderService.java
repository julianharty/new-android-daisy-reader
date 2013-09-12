package org.androiddaisyreader.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.DaisyBookInfo;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * The Class DaisyEbookReaderService.
 */
public class DaisyEbookReaderService extends IntentService {

	private MetaDataHandler mMetaData;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;

	private File mCurrentDirectory = Environment.getExternalStorageDirectory();

	public DaisyEbookReaderService() {
		super(DaisyEbookReaderService.class.toString());
	}

	@Override
	public void onCreate() {
		mMetaData = new MetaDataHandler();
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mEditor = mPreferences.edit();
		mEditor.putBoolean(Constants.SERVICE_DONE, false);
		mEditor.commit();
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onStart(Intent intent, int startid) {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Runnable r = new Runnable() {
			public void run() {
				String localPath = Constants.FOLDER_CONTAIN_METADATA
						+ Constants.META_DATA_SCAN_BOOK_FILE_NAME;
				mMetaData.WriteDataToXmlFile(getData(), localPath);
				mEditor.putBoolean(Constants.SERVICE_DONE, true);
				mEditor.commit();
				stopSelf();
			}
		};

		Thread t = new Thread(r);
		t.start();
		return Service.START_STICKY;
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	private ArrayList<DaisyBookInfo> getData() {
		ArrayList<DaisyBookInfo> filesResult = new ArrayList<DaisyBookInfo>();
		File[] files = mCurrentDirectory.listFiles();
		try {
			if (files != null) {
				int lengthOfFile = files.length;
				for (int i = 0; i < lengthOfFile; i++) {
					ArrayList<String> listResult = DaisyBookUtil.getDaisyBook(files[i], false);
					for (String result : listResult) {
						try {
							File daisyPath = new File(result);
							DaisyBookInfo daisyBook;
							DaisyBook mBook202 = null;
							// Check zip files.
							if (!daisyPath.getAbsolutePath().endsWith(Constants.SUFFIX_ZIP_FILE)) {
								if (DaisyBookUtil.getNccFileName(daisyPath) != null) {
									// We think we have a DAISY 2.02 book as
									// these include an NCC file.
									result = result + File.separator
											+ DaisyBookUtil.getNccFileName(daisyPath);
									mBook202 = DaisyBookUtil.getDaisy202Book(result);
								}
							} else {
								mBook202 = DaisyBookUtil.getDaisy202Book(result);
							}
							// If book is not daisy 2.02, go to function daisy
							// 3.0 to read it.
							if (mBook202 == null) {
								DaisyBook mBook30 = DaisyBookUtil.getDaisy30Book(result);
								daisyBook = getDataFromDaisyBook(mBook30, result);
							} else {
								daisyBook = getDataFromDaisyBook(mBook202, result);
							}
							filesResult.add(daisyBook);

						} catch (Exception e) {
							PrivateException ex = new PrivateException(e,
									DaisyEbookReaderService.this);
							ex.writeLogException();
						}
					}
				}
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderService.this);
			ex.writeLogException();
		}
		return filesResult;
	}

	/**
	 * Gets the data from daisy30 book.
	 * 
	 * @param daisy30 the daisy30
	 * @param result the result
	 * @return the data from daisy book
	 */
	private DaisyBookInfo getDataFromDaisyBook(DaisyBook daisybook, String result) {
		DaisyBookInfo daisyBook = null;

		Date date = daisybook.getDate();
		String sDate = formatDateOrReturnEmptyString(date);
		daisyBook = new DaisyBookInfo("", daisybook.getTitle(), result, daisybook.getAuthor(),
				daisybook.getPublisher(), sDate, 1);
		return daisyBook;
	}


	/**
	 * Format date or return empty string.
	 * 
	 * @param date the date
	 * @return the string
	 */
	private String formatDateOrReturnEmptyString(Date date) {
		String sDate = "";
		if (date != null) {
			sDate = String.format(("%tB %te, %tY %n"), date, date, date, date);
		}
		return sDate;
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
	}
}
