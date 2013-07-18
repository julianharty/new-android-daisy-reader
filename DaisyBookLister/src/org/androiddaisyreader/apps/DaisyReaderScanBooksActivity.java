package org.androiddaisyreader.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.androiddaisyreader.adapter.DaisyBookAdapter;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.androiddaisyreader.utils.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The Class DaisyReaderScanBooksActivity.
 * 
 * @author phuc.dang
 * @date Jul 8, 2013
 */

@SuppressLint({ "DefaultLocale", "NewApi" })
public class DaisyReaderScanBooksActivity extends Activity implements OnClickListener,
		TextToSpeech.OnInitListener {

	private Window mWindow;
	private ListView mlistViewScanBooks;
	private File mCurrentDirectory = Environment.getExternalStorageDirectory();
	private ProgressDialog mProgressDialog;
	private ArrayList<DaisyBook> mListScanBook;
	private ArrayList<DaisyBook> mListDaisyBookOriginal;
	private DaisyBookAdapter mDaisyBookAdapter;
	private int mNumberOfRecentBooks;
	private SharedPreferences mPreferences;
	private SQLiteDaisyBookHelper mSql;
	private EditText mTextSearch;
	private TextToSpeech mTts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_scan_books);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_navigation_bar);

		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyReaderScanBooksActivity.this);
		mNumberOfRecentBooks = mPreferences.getInt(Constants.NUMBER_OF_RECENT_BOOKS,
				Constants.NUMBER_OF_RECENTBOOK_DEFAULT);

		mSql = new SQLiteDaisyBookHelper(DaisyReaderScanBooksActivity.this);
		startTts();
		// initial view
		mTextSearch = (EditText) findViewById(R.id.edit_text_search);
		mlistViewScanBooks = (ListView) findViewById(R.id.list_view_scan_books);
		mlistViewScanBooks.setOnItemClickListener(onItemBookClick);
		mlistViewScanBooks.setOnItemLongClickListener(onItemBookLongClick);

		// initial back button
		findViewById(R.id.imgBack).setOnClickListener(this);

		// set title of this screen
		setScreenTitle();

		mListScanBook = new ArrayList<DaisyBook>();

		loadScanBooks();
		handleSearchBook();
	}

	/**
	 * Start text to speech
	 */
	private void startTts() {
		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	@Override
	protected void onResume() {
		// set screen bright when user change it in setting
		Window window = getWindow();
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyReaderScanBooksActivity.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = window.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderScanBooksActivity.this);
			ex.writeLogException();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		try {
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderScanBooksActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	/**
	 * Sets the screen title.
	 */
	private void setScreenTitle() {
		TextView tvScreenTitle = (TextView) this.findViewById(R.id.screenTitle);
		tvScreenTitle.setOnClickListener(this);
		tvScreenTitle.setText(R.string.title_activity_daisy_reader_scan_book);

	}

	/**
	 * handle search book when text changed.
	 */
	private void handleSearchBook() {
		mTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mListDaisyBookOriginal.size() != 0) {
					mListScanBook = DaisyBookUtil.searchBookWithText(s, mListScanBook, mListDaisyBookOriginal);
					mDaisyBookAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	/**
	 * Scan books on SD card.
	 */
	private void loadScanBooks() {
		Boolean isSDPresent = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (isSDPresent) {
			// fix bug "HONEYCOMB cannot be resolved or is not a field". Please
			// change library android to version 3.0 or higher.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				new LoadingData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new LoadingData().execute();
			}
		} else {
			IntentController mIntentController = new IntentController(this);
			mIntentController.pushToDialog(getString(R.string.sd_card_not_present),
					getString(R.string.error_title), R.drawable.error, false, false, null);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgBack:
			backToTopScreen();
			break;
		case R.id.screenTitle:
			backToTopScreen();
			break;
		default:
			break;
		}

	}

	/**
	 * Make sure TTS installed on your device.
	 * 
	 * @param requestCode the request code
	 * @param resultCode the result code
	 * @param data the data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.MY_DATA_CHECK_CODE) {
			if (!(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)) {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	/** The on item book long click. */
	private OnItemLongClickListener onItemBookLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			addRecentBookToSQLite(mListScanBook.get(arg2));

			// push to reader activity
			itemScanBookClick(mListScanBook.get(arg2));
			return false;
		}

	};

	/** The on item book click. */
	private OnItemClickListener onItemBookClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mTts.speak(mListScanBook.get(arg2).getTitle(), TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	/**
	 * Adds the recent book to sql lite.
	 * 
	 * @param daisyBook the daisy book
	 */
	private void addRecentBookToSQLite(DaisyBook daisyBook) {
		if (mNumberOfRecentBooks > 0) {
			int lastestIdRecentBooks = 0;
			List<DaisyBook> recentBooks = mSql
					.getAllDaisyBook(Constants.TYPE_RECENT_BOOK);
			if (recentBooks.size() > 0) {
				lastestIdRecentBooks = recentBooks.get(0).getSort();
			}
			if (mSql.isExists(daisyBook.getTitle(), Constants.TYPE_RECENT_BOOK)) {
				mSql.DeleteDaisyBook(mSql.getDaisyBookByTitle(daisyBook.getTitle(),
						Constants.TYPE_RECENT_BOOK).getId());
			}
			daisyBook.setSort(lastestIdRecentBooks + 1);
			mSql.addDaisyBook(daisyBook, Constants.TYPE_RECENT_BOOK);
		}
	}

	/**
	 * Item scan book click.
	 * 
	 * @param daisyBook the daisy book
	 */
	private void itemScanBookClick(DaisyBook daisyBook) {
		IntentController intentController = new IntentController(this);
		intentController.pushToDaisyEbookReaderIntent(daisyBook.getPath());
	}

	/**
	 * Back to top screen.
	 */
	private void backToTopScreen() {
		Intent intent = new Intent(this, DaisyReaderLibraryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// Removes other Activities from stack
		startActivity(intent);
	}

	/**
	 * Show dialog when data loading.
	 * 
	 * @author nguyen.le
	 * 
	 */
	class LoadingData extends AsyncTask<Void, Void, ArrayList<DaisyBook>> {

		/** The list files. */
		List<String> listFiles;

		@Override
		protected ArrayList<DaisyBook> doInBackground(Void... params) {
			ArrayList<DaisyBook> filesResult = new ArrayList<DaisyBook>();
			File[] files = mCurrentDirectory.listFiles();
			try {
				if (files != null) {
					int lengthOfFile = files.length;
					for (int i = 0; i < lengthOfFile; i++) {
						ArrayList<String> listResult = DaisyBookUtil.getDaisyBook(files[i],
								false);
						for (String result : listResult) {
							try {
								File daisyPath = new File(result);

								if (!daisyPath.getAbsolutePath().endsWith(
										Constants.SUFFIX_ZIP_FILE)) {
									result = result + File.separator
											+ DaisyBookUtil.getNccFileName(daisyPath);
								}
								Daisy202Book mBook = DaisyBookUtil.getDaisy202Book(result);
								if (mBook != null) {
									Date date = mBook.getDate();
									String sDate = "";
									if (date != null) {
										sDate = String.format(("%tB %te, %tY %n"), date, date,
												date, date);
									}
									DaisyBook daisyBook = new DaisyBook("", mBook.getTitle(),
											result, mBook.getAuthor(), mBook.getPublisher(), sDate,
											1);
									filesResult.add(daisyBook);
								}
							} catch (Exception e) {
								PrivateException ex = new PrivateException(e,
										DaisyReaderScanBooksActivity.this);
								ex.writeLogException();
							}
						}
					}
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, DaisyReaderScanBooksActivity.this);
				ex.writeLogException();
			}
			return filesResult;
		}

		@Override
		protected void onPostExecute(ArrayList<DaisyBook> result) {
			if (result != null) {
				mListScanBook = result;
				mListDaisyBookOriginal = new ArrayList<DaisyBook>(result);
				mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderScanBooksActivity.this,
						mListScanBook);

				mlistViewScanBooks.setAdapter(mDaisyBookAdapter);

			}

			mProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(DaisyReaderScanBooksActivity.this);
			mProgressDialog.setMessage(getString(R.string.waiting));
			mProgressDialog.show();
			super.onPreExecute();
		}
	}

	@Override
	public void onInit(int status) {

	}

}
