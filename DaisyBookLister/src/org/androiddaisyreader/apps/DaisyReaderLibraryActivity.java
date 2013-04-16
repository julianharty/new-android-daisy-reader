/**
 * This is library activity which have contains recent books, scan books, etc.
 * @author LogiGear
 * @date 2013.03.05
 */

package org.androiddaisyreader.apps;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.androiddaisyreader.adapter.LibraryListAdapter;
import org.androiddaisyreader.model.DetailInfo;
import org.androiddaisyreader.model.HeaderInfo;
import org.androiddaisyreader.model.RecentBooks;
import org.androiddaisyreader.sqllite.SqlLiteRecentBookHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

@SuppressLint("NewApi")
public class DaisyReaderLibraryActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech mTts;
	private ProgressDialog mProgressDialog;
	private List<String> mListFiles;
	private ArrayList<String> mFilesResultRecent;
	private ArrayList<ArrayList<String>> mFilesResultScan;
	private File mCurrentDirectory = Environment.getExternalStorageDirectory();;
	private LinkedHashMap<String, HeaderInfo> mHashMapHeaderInfo;
	private ArrayList<HeaderInfo> mBookList;
	private LibraryListAdapter mListAdapter;
	private ExpandableListView mExpandableListView;
	private SqlLiteRecentBookHelper mSqlLite;
	private int mNumberOfRecentBooks;
	private boolean mIsLoadScanBook = true;
	private int mGroupPos = 0;
	private SharedPreferences mPreferences;
	private Window mWindow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_library);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mNumberOfRecentBooks = mPreferences.getInt(
				DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS, 3);
		mWindow = getWindow();
		try {
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent,
					DaisyReaderConstants.MY_DATA_CHECK_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSqlLite = new SqlLiteRecentBookHelper(getApplicationContext());
		mFilesResultScan = new ArrayList<ArrayList<String>>();
		mHashMapHeaderInfo = new LinkedHashMap<String, HeaderInfo>();
		mBookList = new ArrayList<HeaderInfo>();
		addProduct(getString(R.string.recentBooks), null);
		addProduct(getString(R.string.scanBooks), null);
		// get all recent books from sql lite
		loadRecentBooks();
		// get reference to the ExpandableListView
		mExpandableListView = (ExpandableListView) findViewById(R.id.myList);
		// create the adapter by passing your ArrayList data
		mListAdapter = new LibraryListAdapter(DaisyReaderLibraryActivity.this,
				mBookList);
		// attach the adapter to the list
		mExpandableListView.setAdapter(mListAdapter);
		// listener for child row click
		mExpandableListView.setOnChildClickListener(myListItemClicked);
		mExpandableListView.setOnItemLongClickListener(listItemLongClick);
		mExpandableListView.setOnGroupExpandListener(onGroupExpandListener);
		mExpandableListView.setOnGroupCollapseListener(onGroupCollapseListener);

	}

	/**
	 * Make sure TTS installed on your device.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DaisyReaderConstants.MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@Override
	public void onInit(int arg0) {
	}

	@Override
	protected void onDestroy() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(DaisyReaderConstants.NIGHT_MODE, false);
		editor.commit();
		super.onDestroy();
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
				new LoadingData()
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new LoadingData().execute();
			}
			mIsLoadScanBook = false;
		} else {
			mTts.speak(getString(R.string.sdCardNotPresent),
					TextToSpeech.QUEUE_FLUSH, null);
			Toast.makeText(getBaseContext(),
					getString(R.string.sdCardNotPresent), Toast.LENGTH_SHORT)
					.show();
		}

	}

	private OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {

		@Override
		public void onGroupExpand(int groupPosition) {
			HeaderInfo headerInfo = mBookList.get(groupPosition);
			if (!mIsLoadScanBook) {
				mTts.speak(getString(R.string.expand) + headerInfo.getName(), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				if (headerInfo.getName().equals(getString(R.string.scanBooks))) {
					// get all books form sd card
					mGroupPos = groupPosition;
					loadScanBooks();
				} else {
					mTts.speak(getString(R.string.expand) + getString(R.string.recentBooks),
							TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		}
	};

	private OnGroupCollapseListener onGroupCollapseListener = new OnGroupCollapseListener() {

		@Override
		public void onGroupCollapse(int groupPosition) {
			HeaderInfo headerInfo = mBookList.get(groupPosition);
			if (!mIsLoadScanBook) {
				mTts.speak(getString(R.string.collapse) + headerInfo.getName(), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				if (!headerInfo.getName().equals(getString(R.string.scanBooks))) {
					mTts.speak(getString(R.string.collapse) + getString(R.string.recentBooks),
							TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		}
	};

	private OnChildClickListener myListItemClicked = new OnChildClickListener() {

		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// get the group header
			HeaderInfo headerInfo = mBookList.get(groupPosition);
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
		public boolean onItemLongClick(AdapterView<?> arg0, View v,
				int position, long id) {
			if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				int parentPosition = ExpandableListView
						.getPackedPositionGroup(id);
				HeaderInfo headerInfo = mBookList.get(parentPosition);
				int childPosition = ExpandableListView
						.getPackedPositionChild(id);
				String item = null;
				String path = null;
				if (headerInfo.getName()
						.equals(getString(R.string.recentBooks))) {
					item = mFilesResultRecent.get(childPosition);
					RecentBooks recentBook = mSqlLite.getInfoRecentBook(item);
					path = recentBook.getPath();
				} else if (headerInfo.getName().equals(
						getString(R.string.scanBooks))) {
					item = mFilesResultScan.get(childPosition).get(0);
					File daisyPath = new File(mFilesResultScan
							.get(childPosition).get(1));
					path = daisyPath.getAbsolutePath() + "/"
							+ DaisyReaderUtils.getNccFileName(daisyPath);
					addRecentBookToSqlLite(item, path);
				}
				pushToDaisyEbookReaderIntent(path);
				return true;
			}
			return false;
		};
	};

	/**
	 * Here we add my book into recent books or scan books
	 * 
	 * @param dept
	 * @param book
	 * @return
	 */
	private int addProduct(String dept, String book) {
		int groupPosition = 0;

		// check the hash map if the group already exists
		HeaderInfo headerInfo = mHashMapHeaderInfo.get(dept);
		// add the group if doesn't exists
		if (headerInfo == null) {
			headerInfo = new HeaderInfo();
			headerInfo.setName(dept);
			mHashMapHeaderInfo.put(dept, headerInfo);
			mBookList.add(headerInfo);
		}

		// get the children for the group
		ArrayList<DetailInfo> bookList = headerInfo.getBookList();
		// size of the children list
		int listSize = bookList.size();
		// add to the counter
		listSize++;

		// create a new child and add that to the group
		if (book != null) {
			DetailInfo detailInfo = new DetailInfo();
			detailInfo.setSequence(String.valueOf(listSize));
			detailInfo.setName(book);
			bookList.add(detailInfo);
		}
		headerInfo.setBookList(bookList);

		// find the group position inside the list
		groupPosition = bookList.indexOf(headerInfo);
		return groupPosition;
	}

	private void loadRecentBooks() {
		mFilesResultRecent = new ArrayList<String>();
		List<RecentBooks> recentBooks = mSqlLite.getAllRecentBooks();
		if (recentBooks.size() >= mNumberOfRecentBooks) {
			for (int i = 0; i < mNumberOfRecentBooks; i++) {
				RecentBooks re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists())
					mFilesResultRecent.add(re.getName());
			}
			for (int i = mNumberOfRecentBooks; i < recentBooks.size(); i++) {
				RecentBooks re = recentBooks.get(i);
				mSqlLite.deleteRecentBook(re);

			}
		} else {
			for (int i = 0; i < recentBooks.size(); i++) {
				RecentBooks re = recentBooks.get(i);
				File f = new File(re.getPath());
				if (f.exists())
					mFilesResultRecent.add(re.getName());
			}
		}
		for (int j = 0; j < mFilesResultRecent.size(); j++) {
			addProduct(getString(R.string.recentBooks),
					mFilesResultRecent.get(j));
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
			List<RecentBooks> recentBooks = mSqlLite.getAllRecentBooks();
			if (!mSqlLite.isExists(name)) {
				if (recentBooks.size() == mNumberOfRecentBooks) {

					mSqlLite.deleteRecentBook(recentBooks
							.get(mNumberOfRecentBooks - 1));
					for (int i = 0; i < mNumberOfRecentBooks - 1; i++) {
						RecentBooks recentBook = recentBooks.get(i);
						recentBook.setSort(recentBook.getSort() + 1);
						mSqlLite.updateRecentBook(recentBook);
					}
					mSqlLite.addRecentBook(new RecentBooks(name, path, 1));
				} else {
					mSqlLite.addRecentBook(new RecentBooks(name, path,
							recentBooks.size() + 1));
				}
			}
		}
	}

	private void pushToDaisyEbookReaderIntent(String path) {
		Intent i = new Intent(this, DaisyEbookReaderActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		this.startActivity(i);
	}

	/**
	 * Show dialog when data loading.
	 * 
	 * @author nguyen.le
	 * 
	 */
	class LoadingData extends AsyncTask<Void, Void, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			FilenameFilter dirFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			};
			int count = 0;
			ArrayList<String> filesResult = new ArrayList<String>();
			String[] listOfFiles = mCurrentDirectory.list(dirFilter);
			if (listOfFiles != null) {
				mListFiles = new ArrayList<String>(Arrays.asList(listOfFiles));
				Collections.sort(mListFiles, String.CASE_INSENSITIVE_ORDER);

				for (int i = 0; i < mListFiles.size(); i++) {
					String item = mListFiles.get(i);
					File daisyPath = new File(mCurrentDirectory, item);

					if (DaisyReaderUtils.folderContainsDaisy2_02Book(daisyPath)) {
						mFilesResultScan.add(new ArrayList<String>());
						filesResult.add(item);
						mFilesResultScan.get(count).add(item);
						mFilesResultScan.get(count).add(
								daisyPath.getAbsolutePath());
						count++;
					} else {
						if (daisyPath.listFiles() != null) {
							for (File f : daisyPath.listFiles()) {
								if (DaisyReaderUtils
										.folderContainsDaisy2_02Book(f)) {
									mFilesResultScan
											.add(new ArrayList<String>());
									filesResult.add(f.getName());
									mFilesResultScan.get(count).add(f.getName());
									mFilesResultScan.get(count).add(
											f.getAbsolutePath());
									count++;
								}
							}
						}
						;
					}
				}
			} else {
				mListFiles = new ArrayList<String>();
			}
			return filesResult;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			for (int i = 0; i < result.size(); i++) {
				addProduct(getString(R.string.scanBooks), result.get(i));
			}
			mExpandableListView.collapseGroup(mGroupPos);
			mExpandableListView.expandGroup(mGroupPos);
			mProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(
					DaisyReaderLibraryActivity.this);
			mProgressDialog.setMessage(getString(R.string.waiting));
			mProgressDialog.show();
			super.onPreExecute();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		return mProgressDialog;
	}
}