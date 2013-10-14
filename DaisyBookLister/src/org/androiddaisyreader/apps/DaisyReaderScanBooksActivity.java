package org.androiddaisyreader.apps;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.androiddaisyreader.adapter.DaisyBookAdapter;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.DaisyBookInfo;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

/**
 * The Class DaisyReaderScanBooksActivity.
 * 
 * @author LogiGear
 * @date Jul 8, 2013
 */

@SuppressLint({ "DefaultLocale", "NewApi" })
public class DaisyReaderScanBooksActivity extends DaisyEbookReaderBaseActivity {

	private ListView mlistViewScanBooks;
	private ProgressDialog mProgressDialog;
	private ArrayList<DaisyBookInfo> mListScanBook;
	private ArrayList<DaisyBookInfo> mListDaisyBookOriginal;
	private DaisyBookAdapter mDaisyBookAdapter;
	private int mNumberOfRecentBooks;
	private SharedPreferences mPreferences;
	private SQLiteDaisyBookHelper mSql;
	private EditText mTextSearch;
	private MetaDataHandler mMetadata;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_books);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// set title of this screen
		getSupportActionBar().setTitle(R.string.title_activity_daisy_reader_scan_book);

		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyReaderScanBooksActivity.this);
		mNumberOfRecentBooks = mPreferences.getInt(Constants.NUMBER_OF_RECENT_BOOKS,
				Constants.NUMBER_OF_RECENTBOOK_DEFAULT);

		mSql = new SQLiteDaisyBookHelper(DaisyReaderScanBooksActivity.this);
		// initial view
		mTextSearch = (EditText) findViewById(R.id.edit_text_search);
		mlistViewScanBooks = (ListView) findViewById(R.id.list_view_scan_books);
		mlistViewScanBooks.setOnItemClickListener(onItemBookClick);

		mListScanBook = new ArrayList<DaisyBookInfo>();
		mMetadata = new MetaDataHandler();
		deleteCurrentInformation();
		loadScanBooks();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			backToTopScreen();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		speakText(getString(R.string.title_activity_daisy_reader_scan_book));
		handleSearchBook();
		deleteCurrentInformation();

	}

	@Override
	protected void onDestroy() {
		try {
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderScanBooksActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		deleteCurrentInformation();
		super.onRestart();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mListScanBook != null && mDaisyBookAdapter != null) {
			mDaisyBookAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * handle search book when text changed.
	 */
	private void handleSearchBook() {
		mTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mListDaisyBookOriginal != null && mListDaisyBookOriginal.size() != 0) {
					mListScanBook = DaisyBookUtil.searchBookWithText(s, mListScanBook,
							mListDaisyBookOriginal);
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

	/**
	 * Scan books on SD card.
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
			mIntentController.pushToDialog(getString(R.string.sd_card_not_present),
					getString(R.string.error_title), R.raw.error, false, false, null);
		}

	}

	/** The on item book click. */
	private OnItemClickListener onItemBookClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			final DaisyBookInfo daisyBook = mListScanBook.get(arg2);
			boolean isDoubleTap = handleClickItem(arg2);
			if (isDoubleTap) {
				addRecentBookToSQLite(mListScanBook.get(arg2));
				itemScanBookClick(daisyBook);
			} else {
				speakTextOnHandler(daisyBook.getTitle());
			}
		}
	};

	/**
	 * Adds the recent book to sql lite.
	 * 
	 * @param daisyBook the daisy book
	 */
	private void addRecentBookToSQLite(DaisyBookInfo daisyBook) {
		if (mNumberOfRecentBooks > 0) {
			int lastestIdRecentBooks = 0;
			List<DaisyBookInfo> recentBooks = mSql.getAllDaisyBook(Constants.TYPE_RECENT_BOOK);
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
	 * Item scan book click.
	 * 
	 * @param daisyBook the daisy book
	 */
	private void itemScanBookClick(DaisyBookInfo daisyBook) {
		IntentController intentController = new IntentController(this);
		intentController.pushToDaisyEbookReaderIntent(daisyBook.getPath());
	}

	/**
	 * Show dialog when data loading.
	 * 
	 * @author nguyen.le
	 * 
	 */
	class LoadingData extends AsyncTask<Void, Void, ArrayList<DaisyBookInfo>> {

		/** The list files. */
		List<String> listFiles;

		@Override
		protected ArrayList<DaisyBookInfo> doInBackground(Void... params) {
			ArrayList<DaisyBookInfo> filesResult = new ArrayList<DaisyBookInfo>();
			try {
				while (!mPreferences.getBoolean(Constants.SERVICE_DONE, false)) {
					Thread.sleep(1000);
				}
				if (mPreferences.getBoolean(Constants.SERVICE_DONE, false)) {
					InputStream databaseInputStream = new FileInputStream(
							Constants.folderContainMetadata
									+ Constants.META_DATA_SCAN_BOOK_FILE_NAME);
					NodeList nList = mMetadata.ReadDataScanFromXmlFile(databaseInputStream);
					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {

							Element eElement = (Element) nNode;
							String author = eElement.getElementsByTagName(Constants.ATT_AUTHOR)
									.item(0).getTextContent();
							String publisher = eElement
									.getElementsByTagName(Constants.ATT_PUBLISHER).item(0)
									.getTextContent();
							String path = eElement.getAttribute(Constants.ATT_PATH);
							String title = eElement.getElementsByTagName(Constants.ATT_TITLE)
									.item(0).getTextContent();
							String date = eElement.getElementsByTagName(Constants.ATT_DATE).item(0)
									.getTextContent();
							DaisyBookInfo daisyBook = new DaisyBookInfo("", title, path, author,
									publisher, date, 1);
							filesResult.add(daisyBook);
						}
					}
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, getApplicationContext());
				ex.writeLogException();
			}
			return filesResult;
		}

		@Override
		protected void onPostExecute(ArrayList<DaisyBookInfo> result) {
			if (result != null) {
				mListScanBook = result;
				mListDaisyBookOriginal = new ArrayList<DaisyBookInfo>(result);
				mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderScanBooksActivity.this,
						mListScanBook);

				mlistViewScanBooks.setAdapter(mDaisyBookAdapter);

			}
			mProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(DaisyReaderScanBooksActivity.this);
			mProgressDialog.setMessage(getString(R.string.waiting));
			mProgressDialog.show();
			super.onPreExecute();
		}
	}

}
