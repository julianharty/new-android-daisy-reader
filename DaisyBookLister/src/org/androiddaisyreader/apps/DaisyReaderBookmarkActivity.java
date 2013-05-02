/**
 * This activity is bookmark which control all things about save bookmarks and load bookmarks.
 * @author LogiGear
 * @date 2013.03.05
 */

package org.androiddaisyreader.apps;

import java.util.ArrayList;
import java.util.UUID;

import org.androiddaisyreader.adapter.BookmarkListAdapter;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.sqllite.SqlLiteBookmarkHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;

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
import android.content.SharedPreferences;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class DaisyReaderBookmarkActivity extends Activity {
	private BookmarkListAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	private ListView mListBookmark;
	private String mBookTitle;
	private String mSentence;
	private String mSection;
	private String mTime;
	private SqlLiteBookmarkHelper mSql;
	private ArrayList<Bookmark> mListItems;
	private Bookmark mBookmark;
	private String mPath;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private int mNumberOfBookmarks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_reader_bookmark);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		mNumberOfBookmarks = mPreferences.getInt(
				DaisyReaderConstants.NUMBER_OF_BOOKMARKS, 3);		
		mListBookmark = (ListView) this.findViewById(R.id.listBookmark);
		mBookTitle = getIntent().getStringExtra(DaisyReaderConstants.BOOK);
		mSentence = getIntent().getStringExtra(DaisyReaderConstants.SENTENCE);
		mSection = getIntent().getStringExtra(DaisyReaderConstants.SECTION);
		mTime = getIntent().getStringExtra(DaisyReaderConstants.TIME);
		mPath = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		String[] title = mPath.split("/");
		mBookTitle = title[title.length - 2];
		tvBookTitle.setText(mBookTitle);
		// create a bookmark
		mBookmark = new Bookmark();
		if (mSentence != null) {
			mBookmark.setBook(mBookTitle);
			mBookmark.setText(mSentence);
			mBookmark.setTime(Integer.valueOf(mTime));
			mBookmark.setSection(Integer.valueOf(mSection));
			mBookmark.setId(UUID.randomUUID().toString());
		}
		mSql = new SqlLiteBookmarkHelper(getApplicationContext());
		mListItems = new ArrayList<Bookmark>();
		mListItems = mSql.getAllBookmark(mBookTitle);
		loadData();
	}

	class LoadingData extends AsyncTask<Void, Void, ArrayList<Bookmark>> {

		@Override
		protected ArrayList<Bookmark> doInBackground(Void... params) {
			ArrayList<Bookmark> result = new ArrayList<Bookmark>();
			if (mNumberOfBookmarks < mListItems.size()) {
				for (int i = 0; i < mNumberOfBookmarks; i++) {
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
				for (int i = 0; i < mNumberOfBookmarks - mListItems.size(); i++) {
					Bookmark bookmark = new Bookmark();
					bookmark.setTextShow(getString(R.string.empty_bookmark));
					result.add(bookmark);
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Bookmark> result) {
			mAdapter = new BookmarkListAdapter(DaisyReaderBookmarkActivity.this,
					result, mBookmark, mPath);
			mListBookmark.setAdapter(mAdapter);
			mProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(
					DaisyReaderBookmarkActivity.this);
			mProgressDialog.setMessage(getString(R.string.waiting));
			mProgressDialog.show();
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
	protected void onResume() {
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			valueScreen = mPreferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		LayoutParams layoutpars = mWindow.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		super.onResume();
	}

}
