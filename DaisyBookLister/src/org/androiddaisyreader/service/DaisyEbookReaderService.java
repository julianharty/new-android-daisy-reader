package org.androiddaisyreader.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

/**
 * The Class DaisyEbookReaderService.
 */
public class DaisyEbookReaderService extends IntentService {

	private MetaDataHandler mMetaData;
	private File mCurrentDirectory = Environment.getExternalStorageDirectory();

	public DaisyEbookReaderService() {
		super("DaisyEbookReaderService");
	}

	@Override
	public void onCreate() {
		mMetaData = new MetaDataHandler();
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onStart(Intent intent, int startid) {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getApplicationContext(), String.valueOf(hasStorage()), Toast.LENGTH_SHORT)
				.show();
		Runnable r = new Runnable() {
			public void run() {
				mMetaData.WriteDataToXmlFile(getData());
				// stopSelf();
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

							if (!daisyPath.getAbsolutePath().endsWith(Constants.SUFFIX_ZIP_FILE)) {
								result = result + File.separator
										+ DaisyBookUtil.getNccFileName(daisyPath);
							}
							Daisy202Book mBook = DaisyBookUtil.getDaisy202Book(result);
							if (mBook != null) {
								Date date = mBook.getDate();
								String sDate = "";
								if (date != null) {
									sDate = String.format(("%tB %te, %tY %n"), date, date, date,
											date);
								}
								DaisyBook daisyBook = new DaisyBook("", mBook.getTitle(), result,
										mBook.getAuthor(), mBook.getPublisher(), sDate, 1);
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

	private boolean hasStorage() {
		// TODO: After fix the bug, add "if (VERBOSE)" before logging errors.
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
	}

}
