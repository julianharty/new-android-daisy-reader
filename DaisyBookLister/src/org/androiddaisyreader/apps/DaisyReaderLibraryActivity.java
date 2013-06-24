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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

import org.androiddaisyreader.adapter.LibraryListAdapter;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DetailInfo;
import org.androiddaisyreader.model.HeaderInfo;
import org.androiddaisyreader.model.RecentBooks;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqllite.SqlLiteCurrentInformationHelper;
import org.androiddaisyreader.sqllite.SqlLiteRecentBookHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

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
	private SqlLiteRecentBookHelper mSqlLiteRecentBook;
	private int mNumberOfRecentBooks;
	private int mGroupPositionExpand;
	private SharedPreferences mPreferences;
	private long mLastPressTime = 0;
	private boolean mIsExit = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_library);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyReaderLibraryActivity.this);
		mNumberOfRecentBooks = mPreferences.getInt(DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS,
				DaisyReaderConstants.NUMBER_OF_RECENTBOOK_DEFAULT);
		try {
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, DaisyReaderConstants.MY_DATA_CHECK_CODE);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderLibraryActivity.this);
			ex.writeLogException();
		}
		mTts = new TextToSpeech(this, this);
		deleteCurrentInformation();
		handleExpandableList();
	}

	private void handleExpandableList() {
		mSqlLiteRecentBook = new SqlLiteRecentBookHelper(DaisyReaderLibraryActivity.this);
		mFilesResultScan = new ArrayList<ArrayList<String>>();
		mHashMapHeaderInfo = new LinkedHashMap<String, HeaderInfo>();
		mBookListHeader = new ArrayList<HeaderInfo>();
		// create the adapter by passing your ArrayList data
		mListAdapter = new LibraryListAdapter(DaisyReaderLibraryActivity.this, mBookListHeader);
		addBookToExpandableList(getString(R.string.recent_books), null);
		addBookToExpandableList(getString(R.string.scan_books), null);
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
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderLibraryActivity.this);
			ex.writeLogException();
		}
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(DaisyReaderConstants.NIGHT_MODE, false);
		editor.commit();
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		deleteCurrentInformation();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.title_activity_daisy_reader_library),
				TextToSpeech.QUEUE_FLUSH, null);
		Window window = getWindow();
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyReaderLibraryActivity.this);
			valueScreen = mPreferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = window.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderLibraryActivity.this);
			ex.writeLogException();
		}
		super.onResume();
		deleteCurrentInformation();
	}

	@Override
	public void onBackPressed() {
		// do not allow user press button many times at the same time.
		if (SystemClock.elapsedRealtime() - mLastPressTime < DaisyReaderConstants.TIME_WAIT_TO_EXIT_APPLICATION
				&& mIsExit) {
			moveTaskToBack(true);
			mIsExit = false;
		} else {
			Toast.makeText(DaisyReaderLibraryActivity.this,
					this.getString(R.string.message_exit_application), Toast.LENGTH_SHORT).show();
			mTts.speak(this.getString(R.string.message_exit_application), TextToSpeech.QUEUE_FLUSH,
					null);
			mIsExit = true;
		}
		mLastPressTime = SystemClock.elapsedRealtime();
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
			mTts.speak(getString(R.string.sd_card_not_present), TextToSpeech.QUEUE_FLUSH, null);
			mIntentController.pushToDialog(getString(R.string.sd_card_not_present),
					getString(R.string.error_title), R.drawable.error, false, false, null);
		}

	}

	private OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {
		int groupPositionRecentBooks = 0;
		int groupPositionScanBooks = 1;

		@Override
		public void onGroupExpand(int groupPosition) {
			HeaderInfo headerInfo = mBookListHeader.get(groupPosition);
			String header = getString(R.string.scan_books);
			if (headerInfo.getName().equals(header)) {
				mTts.speak(getString(R.string.expand) + header, TextToSpeech.QUEUE_FLUSH, null);
				mGroupPositionExpand = groupPositionScanBooks;
				mExpandableListView.collapseGroup(groupPositionRecentBooks);
				// get all books form sd card
				loadScanBooks();
			} else {
				header = getString(R.string.recent_books);
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
			if (mGroupPositionExpand == groupPosition) {
				mTts.speak(getString(R.string.collapse) + headerInfo.getName(),
						TextToSpeech.QUEUE_FLUSH, null);
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
			if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int parentPosition = ExpandableListView.getPackedPositionGroup(id);
				HeaderInfo headerInfo = mBookListHeader.get(parentPosition);
				int childPosition = ExpandableListView.getPackedPositionChild(id);
				if (headerInfo.getName().equals(getString(R.string.recent_books))) {
					itemRecentBookClick(mFilesResultRecent.get(childPosition));
				} else if (headerInfo.getName().equals(getString(R.string.scan_books))) {
					File daisyPath = new File(mFilesResultScan.get(childPosition).get(1));
					itemScanBookClick(mFilesResultScan.get(childPosition).get(0), daisyPath);
				}
				return true;
			} else {
				return false;
			}
		};
	};

	/**
	 * Handle click on list recent book.
	 * 
	 * @param item
	 */
	private void itemRecentBookClick(String item) {
		IntentController intentController = new IntentController(this);
		String path = null;
		RecentBooks recentBook = mSqlLiteRecentBook.getInfoRecentBook(item);
		path = recentBook.getPath();
		intentController.pushToDaisyEbookReaderIntent(path);
	}

	/**
	 * Handle click on list scan book.
	 * 
	 * @param item
	 */
	private void itemScanBookClick(String item, File daisyPath) {
		IntentController intentController = new IntentController(this);
		String path = daisyPath.getAbsolutePath();
		if (!daisyPath.getAbsolutePath().endsWith(DaisyReaderConstants.SUFFIX_ZIP_FILE)) {
			path = path + File.separator + DaisyReaderUtils.getNccFileName(daisyPath);
		}
		addRecentBookToSqlLite(item, path);
		intentController.pushToDaisyEbookReaderIntent(path);
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

		if (book != null) {
			DetailInfo detailInfo = new DetailInfo();
			detailInfo.setSequence(String.valueOf(listSize));
			detailInfo.setName(book);
			mBookListDetail.add(detailInfo);
		}
		headerInfo.setBookList(mBookListDetail);
		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * Remove books from list by header (Scan book or Recent book)
	 * 
	 * @param header
	 */
	private void removeBooksFromExpandableList(String header) {
		// check the hash map if the group already exists
		HeaderInfo headerInfo = mHashMapHeaderInfo.get(header);
		// get the children for the group
		mBookListDetail = headerInfo.getBookList();
		mBookListDetail.removeAll(mBookListDetail);
		headerInfo.setBookList(mBookListDetail);
		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * Load recent books
	 */
	private void loadRecentBooks() {
		mFilesResultRecent = new ArrayList<String>();
		// get all recent books from sqlite.
		List<RecentBooks> recentBooks = mSqlLiteRecentBook.getAllRecentBooks();
		// if size of recent books > number of recent books in setting.
		int sizeOfRecentBooks = recentBooks.size();
		if (sizeOfRecentBooks >= mNumberOfRecentBooks) {
			// get all items from 0 to number of recent books
			for (int i = 0; i < mNumberOfRecentBooks; i++) {
				RecentBooks re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists()) {
					mFilesResultRecent.add(re.getName());
				}
			}
		} else {
			for (int i = 0; i < sizeOfRecentBooks; i++) {
				RecentBooks re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists()) {
					mFilesResultRecent.add(re.getName());
				}
			}
		}
		int sizeOfFilesResultRecent = mFilesResultRecent.size();
		for (int j = 0; j < sizeOfFilesResultRecent; j++) {
			addBookToExpandableList(getString(R.string.recent_books), mFilesResultRecent.get(j));
		}
	}

	/**
	 * Add a recent book to database.
	 * 
	 * @param name
	 * @param path
	 */
	private void addRecentBookToSqlLite(String name, String path) {
		if (mNumberOfRecentBooks > 0) {
			int lastestIdRecentBooks = 0;
			List<RecentBooks> recentBooks = mSqlLiteRecentBook.getAllRecentBooks();
			if (recentBooks.size() > 0) {
				lastestIdRecentBooks = recentBooks.get(0).getSort();
			}
			if (mSqlLiteRecentBook.isExists(name)) {
				mSqlLiteRecentBook.deleteRecentBook(mSqlLiteRecentBook.getInfoRecentBook(name));
			}
			mSqlLiteRecentBook.addRecentBook(new RecentBooks(name, path, lastestIdRecentBooks + 1));
		}
	}

	private void deleteCurrentInformation() {
		SqlLiteCurrentInformationHelper sql = new SqlLiteCurrentInformationHelper(
				DaisyReaderLibraryActivity.this);
		CurrentInformation current = sql.getCurrentInformation();
		if (current != null) {
			sql.deleteCurrentInformation(current.getId());
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
				try {
					if (files != null) {
						int lengthOfFile = files.length;
						for (int i = 0; i < lengthOfFile; i++) {
							ArrayList<String> listResult = DaisyReaderUtils.getDaisyBook(files[i],
									false);
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
					}
				} catch (Exception e) {
					PrivateException ex = new PrivateException(e, DaisyReaderLibraryActivity.this);
					throw ex;
				}
			} catch (PrivateException e) {
				e.writeLogException();
			}
			return filesResult;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			String header = getString(R.string.scan_books);
			removeBooksFromExpandableList(header);
			int sizeOfResult = result.size();
			for (int i = 0; i < sizeOfResult; i++) {
				addBookToExpandableList(getString(R.string.scan_books), result.get(i));
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