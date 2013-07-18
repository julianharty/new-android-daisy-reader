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

/**
 * The Class DaisyReaderRecentBooksActivity.
 * 
 * @author LogiGear
 * @date Jul 5, 2013
 */

public class DaisyReaderRecentBooksActivity extends Activity implements OnClickListener,
		TextToSpeech.OnInitListener {

	private Window mWindow;
	private ListView mListViewRecentBooks;
	private EditText mTextSearch;
	private SQLiteDaisyBookHelper mSql;
	private DaisyBookAdapter mDaisyBookAdapter;
	private ArrayList<DaisyBook> mListRecentBooks;
	private ArrayList<DaisyBook> mListRecentBookOriginal;
	private int mNumberOfRecentBooks;
	private SharedPreferences mPreferences;
	private TextToSpeech mTts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_recent_books);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_navigation_bar);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mNumberOfRecentBooks = mPreferences.getInt(Constants.NUMBER_OF_RECENT_BOOKS,
				Constants.NUMBER_OF_RECENTBOOK_DEFAULT);
		startTts();
		mListViewRecentBooks = (ListView) findViewById(R.id.list_view_recent_books);
		mTextSearch = (EditText) findViewById(R.id.edit_text_search);

		// init SQLite Recent Book
		mSql = new SQLiteDaisyBookHelper(this);

		// initial back button
		findViewById(R.id.imgBack).setOnClickListener(this);

		// set title of this screen
		setScreenTitle();

		mListViewRecentBooks.setOnItemClickListener(onItemBookClick);
		mListViewRecentBooks.setOnItemLongClickListener(onItemBookLongClick);

		// add listener search text changed
		handleSearchBook();
	}

	/*
	 * ======= /** Start text to speech
	 */
	private void startTts() {
		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mListRecentBooks = loadRecentBooks();
		mListRecentBookOriginal = new ArrayList<DaisyBook>(loadRecentBooks());
		mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderRecentBooksActivity.this,
				mListRecentBooks);

		mListViewRecentBooks.setAdapter(mDaisyBookAdapter);

		// set screen bright when user change it in setting
		Window window = getWindow();
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyReaderRecentBooksActivity.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = window.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderRecentBooksActivity.this);
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
			PrivateException ex = new PrivateException(e, DaisyReaderRecentBooksActivity.this);
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
		tvScreenTitle.setText(R.string.recent_books);

	}

	/**
	 * Load recent books.
	 * 
	 * @return the array list recent books
	 */
	private ArrayList<DaisyBook> loadRecentBooks() {
		ArrayList<DaisyBook> daisyBookList = new ArrayList<DaisyBook>();
		// get all recent books from sqlite.
		List<DaisyBook> recentBooks = mSql.getAllDaisyBook(Constants.TYPE_RECENT_BOOK);
		// if size of recent books > number of recent books in setting.
		int sizeOfRecentBooks = recentBooks.size();
		if (sizeOfRecentBooks >= mNumberOfRecentBooks) {
			// get all items from 0 to number of recent books
			for (int i = 0; i < mNumberOfRecentBooks; i++) {
				DaisyBook re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists()) {
					daisyBookList.add(re);
				}
			}
		} else {
			for (int i = 0; i < sizeOfRecentBooks; i++) {
				DaisyBook re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists()) {
					daisyBookList.add(re);
				}
			}
		}
		return daisyBookList;
	}

	/**
	 * handle search book when text changed.
	 */
	private void handleSearchBook() {
		mTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mListRecentBookOriginal.size() != 0) {
					mListRecentBooks = DaisyBookUtil.searchBookWithText(s, mListRecentBooks, mListRecentBookOriginal);
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

	/** The on item book long click. */
	private OnItemLongClickListener onItemBookLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// push to reader activity
			itemScanBookClick(mListRecentBooks.get(arg2));
			return false;
		}

	};

	/** The on item book click. */
	private OnItemClickListener onItemBookClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mTts.speak(mListRecentBooks.get(arg2).getTitle(), TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	/**
	 * Item scan book click.
	 * 
	 * @param daisyBook the daisy book
	 */
	private void itemScanBookClick(DaisyBook daisyBook) {
		IntentController intentController = new IntentController(this);
		intentController.pushToDaisyEbookReaderIntent(daisyBook.getPath());
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
	 * Back to top screen.
	 */
	private void backToTopScreen() {
		Intent intent = new Intent(this, DaisyReaderLibraryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		// Removes other Activities from stack
		startActivity(intent);
	}

	@Override
	public void onInit(int status) {

	}

}
