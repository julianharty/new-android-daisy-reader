package org.androiddaisyreader.apps;

import java.util.ArrayList;
import java.util.UUID;

import org.androiddaisyreader.adapter.BookmarkListAdapter;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteBookmarkHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * This activity is bookmark which control all things about save bookmarks and
 * load bookmarks.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */
@SuppressLint("NewApi")
public class DaisyReaderBookmarkActivity extends DaisyEbookReaderBaseActivity {
	private ListView mListBookmark;
	private ArrayList<Bookmark> mListItems;
	private Bookmark mBookmark;
	private String mPath;
	private SharedPreferences mPreferences;
	private IntentController mIntentController;
	private Daisy202Book mBook;
	private LoadingData mLoadingData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_bookmark);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyReaderBookmarkActivity.this);
		mIntentController = new IntentController(this);

		mListBookmark = (ListView) this.findViewById(R.id.listBookmark);
		mPath = getIntent().getStringExtra(Constants.DAISY_PATH);

		createNewBookmark();
		SQLiteBookmarkHelper mSql = new SQLiteBookmarkHelper(DaisyReaderBookmarkActivity.this);
		mListItems = new ArrayList<Bookmark>();
		mListItems = mSql.getAllBookmark(mPath);
		loadData();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getBookTitle());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, R.string.bookmarks).setIcon(R.drawable.table_of_contents)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case 1: // touch on table of content icon
			pushToTableOfContent();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return false;
	}

	private String getBookTitle() {
		String titleOfBook = "";
		mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
		try {
			try {
				mBook = DaisyBookUtil.getDaisy202Book(mPath);
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, getApplicationContext(), mPath);
				throw ex;
			}
			titleOfBook = mBook.getTitle() == null ? "" : mBook.getTitle();

		} catch (PrivateException e) {
			e.showDialogException(mIntentController);
		}
		return titleOfBook;
	}

	/**
	 * Create new bookmark.
	 */
	private void createNewBookmark() {
		// create a bookmark
		try {
			mBookmark = new Bookmark();
			String sentence = getIntent().getStringExtra(Constants.SENTENCE);
			String section = getIntent().getStringExtra(Constants.SECTION);
			String time = getIntent().getStringExtra(Constants.TIME);
			mBookmark.setPath(mPath);
			mBookmark.setText(sentence);
			mBookmark.setTime(Integer.valueOf(time));
			mBookmark.setSection(Integer.valueOf(section));
			mBookmark.setId(UUID.randomUUID().toString());
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderBookmarkActivity.this);
			ex.writeLogException();
		}
	}

	private void pushToTableOfContent() {
		Navigator navigator;
		try {
			try {
				Daisy202Book mBook = DaisyBookUtil.getDaisy202Book(mPath);
				navigator = new Navigator(mBook);
				mIntentController.pushToTableOfContentsIntent(mPath, navigator,
						getString(R.string.visual_mode));
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, DaisyReaderBookmarkActivity.this,
						mPath);
				throw ex;
			}
		} catch (PrivateException e) {
			e.showDialogException(mIntentController);
		}
	}

	/**
	 * Show dialog when data loading.
	 */
	class LoadingData extends AsyncTask<Void, Void, ArrayList<Bookmark>> {
		private ProgressDialog progressDialog;
		private int numberOfBookmarks = mPreferences.getInt(Constants.NUMBER_OF_BOOKMARKS,
				Constants.NUMBER_OF_BOOKMARK_DEFAULT);;

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
					mPath, mListItems.size());
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
		mLoadingData = new LoadingData();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mLoadingData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			mLoadingData.execute();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mLoadingData.cancel(true);
		finish();
	}

	@Override
	protected void onDestroy() {
		try {
			if (mTts != null) {
				mTts.shutdown();
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderBookmarkActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTts.speak(getString(R.string.title_activity_daisy_reader_bookmark),
				TextToSpeech.QUEUE_FLUSH, null);
	}
}
