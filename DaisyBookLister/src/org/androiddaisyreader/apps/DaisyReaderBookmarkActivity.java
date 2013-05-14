package org.androiddaisyreader.apps;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.androiddaisyreader.adapter.BookmarkListAdapter;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqllite.SqlLiteBookmarkHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This activity is bookmark which control all things about save bookmarks and
 * load bookmarks.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */
@SuppressLint("NewApi")
public class DaisyReaderBookmarkActivity extends Activity implements TextToSpeech.OnInitListener {
	private String TAG = "EbookReaderBookmark";
	private TextToSpeech mTts;
	private ListView mListBookmark;
	private ArrayList<Bookmark> mListItems;
	private Bookmark mBookmark;
	private String mPath;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private IntentController mIntentController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_reader_bookmark);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		mIntentController = new IntentController(this);
		startTts();
		ImageView imgBookmark = (ImageView) this.findViewById(R.id.imgBookmark);
		imgBookmark.setVisibility(View.INVISIBLE);
		ImageView imgTableOfContents = (ImageView) this.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);
		mListBookmark = (ListView) this.findViewById(R.id.listBookmark);
		String mBookTitle = getIntent().getStringExtra(DaisyReaderConstants.BOOK);
		mPath = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		Daisy202Book mBook = DaisyReaderUtils.getDaisy202Book(mPath);
		tvBookTitle.setText(mBook.getTitle());
		createNewBookmark(mBookTitle);
		SqlLiteBookmarkHelper mSql = new SqlLiteBookmarkHelper(getApplicationContext());
		mListItems = new ArrayList<Bookmark>();
		mListItems = mSql.getAllBookmark(mBookTitle);
		loadData();
	}

	private void startTts() {
		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	private void createNewBookmark(String bookTitle) {
		// create a bookmark
		mBookmark = new Bookmark();
		String sentence = getIntent().getStringExtra(DaisyReaderConstants.SENTENCE);
		String section = getIntent().getStringExtra(DaisyReaderConstants.SECTION);
		String time = getIntent().getStringExtra(DaisyReaderConstants.TIME);
		try {

			Preconditions.checkNotNull(sentence);
			mBookmark.setPath(bookTitle);
			mBookmark.setText(sentence);
			mBookmark.setTime(Integer.valueOf(time));
			mBookmark.setSection(Integer.valueOf(section));
			mBookmark.setId(UUID.randomUUID().toString());
		} catch (NullPointerException e) {
			Log.i(TAG, "sentence is null");
		}
	}
	
	private OnClickListener imgTableOfContentsClick = new OnClickListener() {
		Navigator navigator;

		@Override
		public void onClick(View v) {
			try {
				Daisy202Book mBook = DaisyReaderUtils.getDaisy202Book(mPath);
				navigator = new Navigator(mBook);
				mIntentController.pushToTableOfContentsIntent(mPath, navigator,
						getString(R.string.visualMode));
			} catch (Exception e) {
				mIntentController.pushToDialogError(getString(R.string.error_noPathFound), true);
			}
		}
	};
	
	class LoadingData extends AsyncTask<Void, Void, ArrayList<Bookmark>> {
		private ProgressDialog progressDialog;
		private int numberOfBookmarks = mPreferences.getInt(
				DaisyReaderConstants.NUMBER_OF_BOOKMARKS,
				DaisyReaderConstants.NUMBER_OF_BOOKMARK_DEFAULT);;

		@Override
		protected ArrayList<Bookmark> doInBackground(Void... params) {
			ArrayList<Bookmark> result = new ArrayList<Bookmark>();
			if (numberOfBookmarks < mListItems.size()) {
				for (int i = 0; i < numberOfBookmarks; i++) {
					Bookmark bookmark = mListItems.get(i);
					bookmark.setTextShow(bookmark.getText());
					result.add(bookmark);
				}
			} else {
				for (int i = 0; i < mListItems.size(); i++) {
					Bookmark bookmark = mListItems.get(i);
					bookmark.setTextShow(bookmark.getText());
					result.add(bookmark);
				}
				for (int i = 0; i < numberOfBookmarks - mListItems.size(); i++) {
					Bookmark bookmark = new Bookmark();
					bookmark.setTextShow(getString(R.string.empty_bookmark));
					result.add(bookmark);
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Bookmark> result) {
			BookmarkListAdapter mAdapter;
			mAdapter = new BookmarkListAdapter(DaisyReaderBookmarkActivity.this, result, mBookmark,
					mPath);
			mListBookmark.setAdapter(mAdapter);
			progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(DaisyReaderBookmarkActivity.this);
			progressDialog.setMessage(getString(R.string.waiting));
			progressDialog.show();
			super.onPreExecute();
		}
	}

	private void loadData() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new LoadingData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new LoadingData().execute();
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		try {
			Preconditions.checkNotNull(mTts);
			mTts.stop();
			mTts.shutdown();
		} catch (NullPointerException e) {
			Log.i(TAG, "tts is null");
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.title_activity_daisy_reader_bookmark),
				TextToSpeech.QUEUE_FLUSH, null);
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			valueScreen = mPreferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			Log.i(TAG, "can not get value of screen");
		}
		LayoutParams layoutpars = mWindow.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		super.onResume();
	}

	@Override
	public void onInit(int arg0) {
	}

}
