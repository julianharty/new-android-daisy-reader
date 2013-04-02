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
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;

@SuppressLint("NewApi")
public class DaisyReaderBookmarkActivity extends Activity {
	private BookmarkListAdapter adapter;
	private ProgressDialog mProgressDialog;
	private ListView listBookmark;
	private String bookTitle;
	private String sentence;
	private String section;
	private String time;
	private SqlLiteBookmarkHelper sql;
	private ArrayList<Bookmark> listItems;
	private Bookmark bookmark;
	private String path;
	private SharedPreferences preferences;
	private Window window;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_bookmark);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		window = getWindow();
		listBookmark = (ListView) this.findViewById(R.id.listBookmark);
		bookTitle = getIntent().getStringExtra(DaisyReaderConstants.BOOK);
		sentence = getIntent().getStringExtra(DaisyReaderConstants.SENTENCE);
		section = getIntent().getStringExtra(DaisyReaderConstants.SECTION);
		time = getIntent().getStringExtra(DaisyReaderConstants.TIME);
		path = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		// create a bookmark
		bookmark = new Bookmark();
		if (sentence != null) {
			bookmark.setBook(bookTitle);
			bookmark.setText(sentence);
			bookmark.setTime(Integer.valueOf(time));
			bookmark.setSection(Integer.valueOf(section));
			bookmark.setId(UUID.randomUUID().toString());
		}
		sql = new SqlLiteBookmarkHelper(getApplicationContext());
		listItems = new ArrayList<Bookmark>();
		listItems = sql.getAllBookmark(bookTitle);
		loadData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_daisy_reader_bookmark, menu);
		return true;
	}

	class LoadingData extends AsyncTask<Void, Void, ArrayList<Bookmark>> {

		@Override
		protected ArrayList<Bookmark> doInBackground(Void... params) {
			ArrayList<Bookmark> result = new ArrayList<Bookmark>();
			for (int i = 0; i < listItems.size(); i++) {
				Bookmark bookmark = listItems.get(i);
				bookmark.setTextShow(bookmark.getText());
				result.add(bookmark);
			}
			for (int i = 0; i < 10 - listItems.size(); i++) {
				Bookmark bookmark = new Bookmark();
				bookmark.setTextShow(getString(R.string.empty_bookmark));
				result.add(bookmark);
			}
			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Bookmark> result) {
			adapter = new BookmarkListAdapter(DaisyReaderBookmarkActivity.this,
					result, bookmark, path);
			listBookmark.setAdapter(adapter);
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
		//get value of brightness from preference. Otherwise, get current brightness from system.
		try {
			valueScreen = preferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		LayoutParams layoutpars = window.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		window.setAttributes(layoutpars);
		super.onResume();
	}

}
