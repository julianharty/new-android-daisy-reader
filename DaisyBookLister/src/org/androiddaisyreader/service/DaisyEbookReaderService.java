package org.androiddaisyreader.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.daisy30.Daisy30Book;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.DaisyBook;
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
	private ArrayList<DaisyBook> getData() {
		ArrayList<DaisyBook> filesResult = new ArrayList<DaisyBook>();
		File[] files = mCurrentDirectory.listFiles();
		try {
			if (files != null) {
				int lengthOfFile = files.length;
				for (int i = 0; i < lengthOfFile; i++) {
					ArrayList<String> listResult = DaisyBookUtil.getDaisyBook(files[i], false);
					for (String result : listResult) {
						try {
							File daisyPath = new File(result);
							Daisy202Book mBook202 = null;
							Daisy30Book mBook30 = null;
							if (!daisyPath.getAbsolutePath().endsWith(Constants.SUFFIX_ZIP_FILE)) {
								DaisyBook daisyBook;
								if (DaisyBookUtil.getNccFileName(daisyPath) != null) {
									result = result + File.separator
											+ DaisyBookUtil.getNccFileName(daisyPath);
									mBook202 = DaisyBookUtil.getDaisy202Book(result);
									 daisyBook = getDataFromDaisyBook(mBook202, result);
								} else {
									mBook30 = DaisyBookUtil.getDaisy30Book(result);
									 daisyBook = getDataFromDaisyBook(mBook30, result);
								}
								filesResult.add(daisyBook);
							}

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

	private DaisyBook getDataFromDaisyBook(Daisy30Book daisy30, String result) {
		DaisyBook daisyBook = null;

		Date date = daisy30.getDate();
		String sDate = formatDateOrReturnEmptyString(date);
		daisyBook = new DaisyBook("", daisy30.getTitle(), result, daisy30.getAuthor(),
				daisy30.getPublisher(), sDate, 1);
		return daisyBook;
	}
	
	private DaisyBook getDataFromDaisyBook(Daisy202Book daisy202, String result) {
		DaisyBook daisyBook = null;

		Date date = daisy202.getDate();
		String sDate = formatDateOrReturnEmptyString(date);
		daisyBook = new DaisyBook("", daisy202.getTitle(), result, daisy202.getAuthor(),
				daisy202.getPublisher(), sDate, 1);
		return daisyBook;
	}

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
