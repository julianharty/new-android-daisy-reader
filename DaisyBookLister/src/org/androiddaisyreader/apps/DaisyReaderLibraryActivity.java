package org.androiddaisyreader.apps;

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
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import org.androiddaisyreader.adapter.LibraryListAdapter;
import org.androiddaisyreader.model.DetailInfo;
import org.androiddaisyreader.model.HeaderInfo;
import org.androiddaisyreader.model.RecentBooks;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqllite.SqlLiteRecentBookHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is library activity which have contains recent books, scan books, etc.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

@SuppressLint("NewApi")
public class DaisyReaderLibraryActivity extends Activity implements TextToSpeech.OnInitListener {

	private String TAG = "EbookReaderLibrary";
	private TextToSpeech mTts;
	private ProgressDialog mProgressDialog;
	private ArrayList<String> mFilesResultRecent;
	private ArrayList<ArrayList<String>> mFilesResultScan;
	private ArrayList<DetailInfo> mBookListDetail;
	private ArrayList<HeaderInfo> mBookListHeader;
	private File mCurrentDirectory = Environment.getExternalStorageDirectory();
	private LinkedHashMap<String, HeaderInfo> mHashMapHeaderInfo;
	private LibraryListAdapter mListAdapter;
	private ExpandableListView mExpandableListView;
	private SqlLiteRecentBookHelper mSqlLite;
	private int mNumberOfRecentBooks;
	private int mGroupPositionExpand;
	private SharedPreferences mPreferences;
	private IntentController mIntentController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_library);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mNumberOfRecentBooks = mPreferences.getInt(DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS,
				DaisyReaderConstants.NUMBER_OF_RECENTBOOK_DEFAULT);
		try {
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, DaisyReaderConstants.MY_DATA_CHECK_CODE);
		} catch (Exception e) {
			Log.i(TAG, "can not get intent");
		}
		mTts = new TextToSpeech(this, this);
		mIntentController = new IntentController(this);
		mSqlLite = new SqlLiteRecentBookHelper(getApplicationContext());
		mFilesResultScan = new ArrayList<ArrayList<String>>();
		mHashMapHeaderInfo = new LinkedHashMap<String, HeaderInfo>();
		mBookListHeader = new ArrayList<HeaderInfo>();
		// create the adapter by passing your ArrayList data
		mListAdapter = new LibraryListAdapter(DaisyReaderLibraryActivity.this, mBookListHeader);
		addBookToExpandableList(getString(R.string.recentBooks), null);
		addBookToExpandableList(getString(R.string.scanBooks), null);
		// get reference to the ExpandableListView
		mExpandableListView = (ExpandableListView) findViewById(R.id.myList);
		// attach the adapter to the list
		mExpandableListView.setAdapter(mListAdapter);
		// listener for child row click
		mExpandableListView.setOnChildClickListener(listItemClick);
		mExpandableListView.setOnItemLongClickListener(listItemLongClick);
		mExpandableListView.setOnGroupExpandListener(onGroupExpandListener);
		mExpandableListView.setOnGroupCollapseListener(onCollapseListener);
	}

	/**
	 * Make sure TTS installed on your device.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DaisyReaderConstants.MY_DATA_CHECK_CODE) {
			if (!(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)) {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@Override
	public void onInit(int arg0) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener

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
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(DaisyReaderConstants.NIGHT_MODE, false);
		editor.commit();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Window window = getWindow();
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
		LayoutParams layoutpars = window.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		window.setAttributes(layoutpars);
		mTts.speak(getString(R.string.title_activity_daisy_reader_library),
				TextToSpeech.QUEUE_FLUSH, null);
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}

	/**
	 * Load some initial data into out list
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
			mTts.speak(getString(R.string.sdCardNotPresent), TextToSpeech.QUEUE_FLUSH, null);
			mIntentController.pushToDialogError(getString(R.string.sdCardNotPresent), false);
		}

	}

	private OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {
		int groupPositionRecentBooks = 0;
		int groupPositionScanBooks = 1;

		@Override
		public void onGroupExpand(int groupPosition) {
			HeaderInfo headerInfo = mBookListHeader.get(groupPosition);
			String header = getString(R.string.scanBooks);
			if (headerInfo.getName().equals(header)) {
				mTts.speak(getString(R.string.expand) + header, TextToSpeech.QUEUE_FLUSH, null);
				mGroupPositionExpand = groupPositionScanBooks;
				mExpandableListView.collapseGroup(groupPositionRecentBooks);
				// get all books form sd card
				loadScanBooks();
			} else {
				header = getString(R.string.recentBooks);
				mTts.speak(getString(R.string.expand) + header, TextToSpeech.QUEUE_FLUSH, null);
				mGroupPositionExpand = groupPositionRecentBooks;
				mExpandableListView.collapseGroup(groupPositionScanBooks);
				removeBooksFromExpandableList(header);
				// get all recent books from sqlite
				loadRecentBooks();
			}
		}
	};

	private OnGroupCollapseListener onCollapseListener = new OnGroupCollapseListener() {

		@Override
		public void onGroupCollapse(int groupPosition) {
			HeaderInfo headerInfo = mBookListHeader.get(groupPosition);
			try {
				Preconditions.checkArgument(mGroupPositionExpand == groupPosition);
				mTts.speak(getString(R.string.collapse) + headerInfo.getName(),
						TextToSpeech.QUEUE_FLUSH, null);
			} catch (IllegalArgumentException e) {
				Log.i(TAG, "can not check argument");
			}
		}
	};

	private OnChildClickListener listItemClick = new OnChildClickListener() {

		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				int childPosition, long id) {
			// get the group header
			HeaderInfo headerInfo = mBookListHeader.get(groupPosition);
			// get the child info
			DetailInfo detailInfo = headerInfo.getBookList().get(childPosition);
			// Text to speech name of item.
			mTts.speak(detailInfo.getName(), TextToSpeech.QUEUE_FLUSH, null);
			return false;
		}

	};

	/**
	 * Text to speech name of item.
	 */
	private OnItemLongClickListener listItemLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
			try {
				Preconditions
						.checkArgument(ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD);
				int parentPosition = ExpandableListView.getPackedPositionGroup(id);
				HeaderInfo headerInfo = mBookListHeader.get(parentPosition);
				int childPosition = ExpandableListView.getPackedPositionChild(id);
				if (headerInfo.getName().equals(getString(R.string.recentBooks))) {
					itemRecentBookClick(mFilesResultRecent.get(childPosition));
				} else if (headerInfo.getName().equals(getString(R.string.scanBooks))) {
					File daisyPath = new File(mFilesResultScan.get(childPosition).get(1));
					itemScanBookClick(mFilesResultScan.get(childPosition).get(0), daisyPath);
				}
				return true;
			} catch (IllegalArgumentException e) {
				Log.i(TAG, "can not check argument");
				return false;
			}
		};
	};

	private void itemRecentBookClick(String item) {
		String path = null;
		RecentBooks recentBook = mSqlLite.getInfoRecentBook(item);
		path = recentBook.getPath();
		mIntentController.pushToDaisyEbookReaderIntent(path);
	}

	private void itemScanBookClick(String item, File daisyPath) {
		String path = daisyPath.getAbsolutePath();
		if (!daisyPath.getAbsolutePath().endsWith(".zip")) {
			path = path + File.separator + DaisyReaderUtils.getNccFileName(daisyPath);
		}
		addRecentBookToSqlLite(item, path);
		mIntentController.pushToDaisyEbookReaderIntent(path);
	}

	/**
	 * Here we add my book into recent books or scan books
	 * 
	 * @param header
	 * @param book
	 * @return
	 */
	private void addBookToExpandableList(String header, String book) {
		// check the hash map if the group already exists
		HeaderInfo headerInfo = mHashMapHeaderInfo.get(header);
		// add the group if doesn't exists
		if (headerInfo == null) {
			headerInfo = new HeaderInfo();
			headerInfo.setName(header);
			mHashMapHeaderInfo.put(header, headerInfo);
			mBookListHeader.add(headerInfo);
		}

		// get the children for the group
		mBookListDetail = headerInfo.getBookList();
		// size of the children list
		int listSize = mBookListDetail.size();
		// add to the counter
		listSize++;

		// create a new child and add that to the group
		try {
			Preconditions.checkNotNull(book);
			DetailInfo detailInfo = new DetailInfo();
			detailInfo.setSequence(String.valueOf(listSize));
			detailInfo.setName(book);
			mBookListDetail.add(detailInfo);
		} catch (NullPointerException e) {
			Log.i(TAG, "book is null");
		}
		headerInfo.setBookList(mBookListDetail);
		mListAdapter.notifyDataSetChanged();
	}

	private void removeBooksFromExpandableList(String header) {
		// check the hash map if the group already exists
		HeaderInfo headerInfo = mHashMapHeaderInfo.get(header);
		// get the children for the group
		mBookListDetail = headerInfo.getBookList();
		mBookListDetail.removeAll(mBookListDetail);
		headerInfo.setBookList(mBookListDetail);
		mListAdapter.notifyDataSetChanged();
	}

	private void loadRecentBooks() {
		mFilesResultRecent = new ArrayList<String>();
		// get all recent books from sqlite.
		List<RecentBooks> recentBooks = mSqlLite.getAllRecentBooks();
		// if size of recent books > number of recent books in setting.
		if (recentBooks.size() >= mNumberOfRecentBooks) {
			// get all items from 0 to number of recent books
			for (int i = 0; i < mNumberOfRecentBooks; i++) {
				RecentBooks re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists()) {
					mFilesResultRecent.add(re.getName());
				}
			}
		} else {
			for (int i = 0; i < recentBooks.size(); i++) {
				RecentBooks re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists()) {
					mFilesResultRecent.add(re.getName());
				}
			}
		}
		for (int j = 0; j < mFilesResultRecent.size(); j++) {
			addBookToExpandableList(getString(R.string.recentBooks), mFilesResultRecent.get(j));
		}
	}

	/**
	 * Add a recent book to database.
	 * 
	 * @param name
	 * @param path
	 */
	private void addRecentBookToSqlLite(String name, String path) {
		try {
			Preconditions.checkArgument(mNumberOfRecentBooks > 0);
			int lastestIdRecentBooks = 0;
			List<RecentBooks> recentBooks = mSqlLite.getAllRecentBooks();
			if (recentBooks.size() > 0) {
				lastestIdRecentBooks = recentBooks.get(0).getSort();
			}
			if (mSqlLite.isExists(name)) {
				mSqlLite.deleteRecentBook(mSqlLite.getInfoRecentBook(name));
			}
			mSqlLite.addRecentBook(new RecentBooks(name, path, lastestIdRecentBooks + 1));
		} catch (IllegalArgumentException e) {
			Log.i(TAG, "can not check argument");
		}
	}

	/**
	 * Show dialog when data loading.
	 * 
	 * @author nguyen.le
	 * 
	 */
	class LoadingData extends AsyncTask<Void, Void, ArrayList<String>> {
		List<String> listFiles;

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			int count = 0;
			ArrayList<String> filesResult = new ArrayList<String>();
			File[] files = mCurrentDirectory.listFiles();
			try {
				Preconditions.checkNotNull(files);
				for (int i = 0; i < files.length; i++) {
					ArrayList<String> listResult = DaisyReaderUtils.getDaisyBook(files[i], false);
					for (String result : listResult) {
						String[] title = result.split(File.separator);
						String item = title[title.length - 1];
						mFilesResultScan.add(new ArrayList<String>());
						filesResult.add(item);
						mFilesResultScan.get(count).add(item);
						mFilesResultScan.get(count).add(result);
						count++;
					}
				}
			} catch (NullPointerException e) {
				Log.i(TAG, "list of files is null");
			}
			return filesResult;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			String header = getString(R.string.scanBooks);
			removeBooksFromExpandableList(header);
			for (int i = 0; i < result.size(); i++) {
				addBookToExpandableList(getString(R.string.scanBooks), result.get(i));
			}
			mProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(DaisyReaderLibraryActivity.this);
			mProgressDialog.setMessage(getString(R.string.waiting));
			mProgressDialog.show();
			super.onPreExecute();
		}
	}
}