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
import org.androiddaisyreader.sqllite.SqlLiteHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

@SuppressLint("NewApi")
public class DaisyReaderLibraryActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private ProgressDialog mProgressDialog;
	private List<String> files;
	private ArrayList<String> filesResult;
	private ArrayList<String> filesResultRecent;
	private File currentDirectory = new File(DaisyReaderConstants.SDCARD);
	private LinkedHashMap<String, HeaderInfo> myDepts = new LinkedHashMap<String, HeaderInfo>();
	private ArrayList<HeaderInfo> bookList = new ArrayList<HeaderInfo>();
	private LibraryListAdapter listAdapter;
	private ExpandableListView myList;
	private SqlLiteHelper sqlLite;
	private int NumberOfRecentBooks = DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS;
	private boolean isLoadScanBook = true;
	private int groupPos = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_library);
		try {
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent,
					DaisyReaderConstants.MY_DATA_CHECK_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tts = new TextToSpeech(this, this);
		sqlLite = new SqlLiteHelper(getApplicationContext());
		addProduct(getString(R.string.recentBooks), null);
		addProduct(getString(R.string.scanBooks), null);
		// get all recent books from sql lite
		LoadRecentBooks();
		// get reference to the ExpandableListView
		myList = (ExpandableListView) findViewById(R.id.myList);
		// create the adapter by passing your ArrayList data
		listAdapter = new LibraryListAdapter(DaisyReaderLibraryActivity.this,
				bookList);
		// attach the adapter to the list
		myList.setAdapter(listAdapter);
		// listener for child row click
		myList.setOnChildClickListener(myListItemClicked);
		myList.setOnItemLongClickListener(listItemLongClick);
		myList.setOnGroupExpandListener(onGroupExpandListener);
	}

	/**
	 * Make sure TTS installed on your device.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DaisyReaderConstants.MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);
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
		if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
		finish();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
		super.onPause();
	}
	
	/**
	 * Load some initial data into out list
	 */
	private void loadScanBooks() {
		Boolean isSDPresent = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (isSDPresent) {
			// fix bug "HONEYCOMB cannot be resolved or is not a field". Please change library android to version 3.0 or higher.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				new loadingData()
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new loadingData().execute();
			}
			isLoadScanBook = false;
		} else {
			tts.speak(getString(R.string.sdCardNotPresent),
					TextToSpeech.QUEUE_FLUSH, null);
			Toast.makeText(getBaseContext(),
					getString(R.string.sdCardNotPresent), Toast.LENGTH_SHORT)
					.show();
		}

	}

	private OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {

		@Override
		public void onGroupExpand(int groupPosition) {
			HeaderInfo headerInfo = bookList.get(groupPosition);
			if (isLoadScanBook) {
				if (headerInfo.getName().equals(getString(R.string.scanBooks))) {
					// get all books form sd card
					groupPos = groupPosition;
					loadScanBooks();
				}
			}
		}
	};

	private OnChildClickListener myListItemClicked = new OnChildClickListener() {

		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// get the group header
			HeaderInfo headerInfo = bookList.get(groupPosition);
			// get the child info
			DetailInfo detailInfo = headerInfo.getBookList().get(childPosition);
			// Text to speech name of item.
			tts.speak(detailInfo.getName(), TextToSpeech.QUEUE_FLUSH, null);
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
				HeaderInfo headerInfo = bookList.get(parentPosition);
				int childPosition = ExpandableListView
						.getPackedPositionChild(id);
				String item = "";
				if (headerInfo.getName()
						.equals(getString(R.string.recentBooks))) {
					item = filesResultRecent.get(childPosition);
				} else if (headerInfo.getName().equals(
						getString(R.string.scanBooks))) {
					item = filesResult.get(childPosition);
				}
				File daisyPath = new File(currentDirectory, item);
				String path = daisyPath.getAbsolutePath() + "/";
				String nccPath = DaisyReaderUtils.getNccFileName(daisyPath);
				addRecentBookToSqlLite(item, path + nccPath);
				pushToDaisyEbookReaderIntent(path + nccPath);
				return true;
			}
			return false;
		};
	};

	/**
	 * Here we add my book into recent books or scan books
	 * @param dept
	 * @param book
	 * @return
	 */
	private int addProduct(String dept, String book) {
		int groupPosition = 0;

		// check the hash map if the group already exists
		HeaderInfo headerInfo = myDepts.get(dept);
		// add the group if doesn't exists
		if (headerInfo == null) {
			headerInfo = new HeaderInfo();
			headerInfo.setName(dept);
			myDepts.put(dept, headerInfo);
			bookList.add(headerInfo);
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

	private void LoadRecentBooks() {
		filesResultRecent = new ArrayList<String>();
		List<RecentBooks> recentBooks = sqlLite.getAllRecentBooks();
		if (recentBooks.size() >= NumberOfRecentBooks) {
			for (int i = 0; i < NumberOfRecentBooks; i++) {
				RecentBooks re = recentBooks.get(i);
				filesResultRecent.add(re.getName());
			}
		} else {
			for (int i = 0; i < recentBooks.size(); i++) {
				RecentBooks re = recentBooks.get(i);
				filesResultRecent.add(re.getName());
			}
		}
		for (int j = 0; j < filesResultRecent.size(); j++) {
			addProduct(getString(R.string.recentBooks),
					filesResultRecent.get(j));
		}
	}

	/**
	 * Add a recent book to database.
	 * @param name
	 * @param path
	 */
	private void addRecentBookToSqlLite(String name, String path) {
		List<RecentBooks> recentBooks = sqlLite.getAllRecentBooks();
		if (!sqlLite.isExists(name)) {
			if (recentBooks.size() == NumberOfRecentBooks) {

				sqlLite.deleteRecentBook(recentBooks
						.get(NumberOfRecentBooks - 1));
				for (int i = 0; i < NumberOfRecentBooks - 1; i++) {
					RecentBooks recentBook = recentBooks.get(i);
					recentBook.setSort(recentBook.getSort() + 1);
					sqlLite.updateRecentBook(recentBook);
				}
				sqlLite.addRecentBook(new RecentBooks(name, path, 1));
			} else {
				sqlLite.addRecentBook(new RecentBooks(name, path, recentBooks
						.size() + 1));
			}
		}
	}

	private void pushToDaisyEbookReaderIntent(String path) {
		Intent i = new Intent(this, DaisyEbookReaderActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		this.startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_daisy_reader_library, menu);
		return true;
	}

	/**
	 * Show dialog when data loading.
	 * @author nguyen.le
	 *
	 */
	class loadingData extends AsyncTask<Void, Void, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			FilenameFilter dirFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			};
			String[] listOfFiles = currentDirectory.list(dirFilter);
			if (listOfFiles != null) {
				files = new ArrayList<String>(Arrays.asList(listOfFiles));
				Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
				filesResult = new ArrayList<String>();
				for (int i = 0; i < files.size(); i++) {
					String item = files.get(i);
					File daisyPath = new File(currentDirectory, item);
					if (DaisyReaderUtils.folderContainsDaisy2_02Book(daisyPath)) {
						filesResult.add(item);
					}
				}
			} else {
				files = new ArrayList<String>();
			}
			return filesResult;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			for (int i = 0; i < result.size(); i++) {
				addProduct(getString(R.string.scanBooks), result.get(i));
				myList.collapseGroup(groupPos);
				myList.expandGroup(groupPos);
			}
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