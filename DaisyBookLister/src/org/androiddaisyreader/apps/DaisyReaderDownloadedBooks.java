package org.androiddaisyreader.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.androiddaisyreader.adapter.DaisyBookAdapter;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.androiddaisyreader.utils.Constants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class DaisyReaderDownloadedBooks extends Activity implements OnClickListener,
		TextToSpeech.OnInitListener {
	private Window mWindow;
	private SQLiteDaisyBookHelper mSql;
	private ArrayList<DaisyBook> mlistDaisyBook;
	private ArrayList<DaisyBook> mListDaisyBookOriginal;
	private DaisyBookAdapter mDaisyBookAdapter;
	private EditText mTextSearch;
	private int mNumberOfRecentBooks;
	private SharedPreferences mPreferences;
	private TextToSpeech mTts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_downloaded_books);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_navigation_bar);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyReaderDownloadedBooks.this);
		mNumberOfRecentBooks = mPreferences.getInt(Constants.NUMBER_OF_RECENT_BOOKS,
				Constants.NUMBER_OF_RECENTBOOK_DEFAULT);
		startTts();
		// initial back button
		mTextSearch = (EditText) findViewById(R.id.edit_text_search);
		findViewById(R.id.imgBack).setOnClickListener(this);

		// set title of this screen
		setScreenTitle();
		mSql = new SQLiteDaisyBookHelper(DaisyReaderDownloadedBooks.this);
		mlistDaisyBook = getActualDownloadedBooks();
		mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderDownloadedBooks.this, mlistDaisyBook);
		ListView listDownloaded = (ListView) findViewById(R.id.list_view_downloaded_books);
		listDownloaded.setAdapter(mDaisyBookAdapter);
		listDownloaded.setOnItemClickListener(onItemClick);
		listDownloaded.setOnItemLongClickListener(onItemLongClick);
		handleSearchBook();
		mListDaisyBookOriginal = new ArrayList<DaisyBook>(mlistDaisyBook);
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

	/**
	 * Sets the screen title.
	 */
	private void setScreenTitle() {
		TextView tvScreenTitle = (TextView) this.findViewById(R.id.screenTitle);
		tvScreenTitle.setOnClickListener(this);
		tvScreenTitle.setText(R.string.downloaded_books);
	}
	
	/**
	 * get all book is downloaded
	 * @return List daisy book
	 */ 
	private ArrayList<DaisyBook> getActualDownloadedBooks() {
		ArrayList<DaisyBook> actualDownloadedBooks = new ArrayList<DaisyBook>();
		ArrayList<DaisyBook> listBooks = mSql
				.getAllDaisyBook(Constants.TYPE_DOWNLOADED_BOOK);
		for (DaisyBook book : listBooks) {
			File file = new File(book.getPath());
			if (file.exists()) {
				actualDownloadedBooks.add(book);
			}
		}
		return actualDownloadedBooks;
	}

	private OnItemClickListener onItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mTts.speak(mlistDaisyBook.get(arg2).getTitle(), TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	private OnItemLongClickListener onItemLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			DaisyBook daisyBook = mlistDaisyBook.get(position);
			// add to sqlite
			addRecentBookToSQLite(daisyBook);

			// push to reader activity
			IntentController intentController = new IntentController(
					DaisyReaderDownloadedBooks.this);
			intentController.pushToDaisyEbookReaderIntent(daisyBook.getPath());
			return false;
		}
	};

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
	 * Back to top screen.
	 */
	private void backToTopScreen() {
		Intent intent = new Intent(this, DaisyReaderLibraryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// Removes other Activities from stack
		startActivity(intent);
	}

	/**
	 * Adds the recent book to sql lite.
	 * 
	 * @param daisyBook
	 *            the daisy book
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
	 * handle search book when text changed.
	 */
	private void handleSearchBook() {
		mTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mListDaisyBookOriginal.size() != 0) {
					mlistDaisyBook =  DaisyBookUtil.searchBookWithText(s, mlistDaisyBook, mListDaisyBookOriginal);
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

	@Override
	public void onInit(int arg0) {
	}

	@Override
	protected void onResume() {
		// set screen bright when user change it in setting
		Window window = getWindow();
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyReaderDownloadedBooks.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = window.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadedBooks.this);
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
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadedBooks.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}
}
