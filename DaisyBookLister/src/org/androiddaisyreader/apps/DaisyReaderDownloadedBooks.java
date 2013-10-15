package org.androiddaisyreader.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.androiddaisyreader.adapter.DaisyBookAdapter;
import org.androiddaisyreader.model.DaisyBookInfo;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.service.DaisyEbookReaderService;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
 * 
 * @author LogiGear
 * @date Jul 18, 2013
 */
public class DaisyReaderDownloadedBooks extends DaisyEbookReaderBaseActivity {

	private SQLiteDaisyBookHelper mSql;
	private ArrayList<DaisyBookInfo> mlistDaisyBook;
	private ArrayList<DaisyBookInfo> mListDaisyBookOriginal;
	private DaisyBookAdapter mDaisyBookAdapter;
	private EditText mTextSearch;
	private int mNumberOfRecentBooks;
	private SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_downloaded_books);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.downloaded_books);

		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyReaderDownloadedBooks.this);
		mNumberOfRecentBooks = mPreferences.getInt(Constants.NUMBER_OF_RECENT_BOOKS,
				Constants.NUMBER_OF_RECENTBOOK_DEFAULT);

		mTextSearch = (EditText) findViewById(R.id.edit_text_search);

		mSql = new SQLiteDaisyBookHelper(DaisyReaderDownloadedBooks.this);
		mlistDaisyBook = getActualDownloadedBooks();
		mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderDownloadedBooks.this, mlistDaisyBook);
		ListView listDownloaded = (ListView) findViewById(R.id.list_view_downloaded_books);
		listDownloaded.setAdapter(mDaisyBookAdapter);
		listDownloaded.setOnItemClickListener(onItemClick);
		deleteCurrentInformation();
		mListDaisyBookOriginal = new ArrayList<DaisyBookInfo>(mlistDaisyBook);
		// start service application when download completed
		Intent serviceIntent = new Intent(DaisyReaderDownloadedBooks.this,
				DaisyEbookReaderService.class);
		startService(serviceIntent);
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

	/**
	 * get all book is downloaded
	 * 
	 * @return List daisy book
	 */
	private ArrayList<DaisyBookInfo> getActualDownloadedBooks() {
		ArrayList<DaisyBookInfo> actualDownloadedBooks = new ArrayList<DaisyBookInfo>();
		ArrayList<DaisyBookInfo> listBooks = mSql.getAllDaisyBook(Constants.TYPE_DOWNLOADED_BOOK);
		for (DaisyBookInfo book : listBooks) {
			File file = new File(book.getPath());
			if (file.exists()) {
				actualDownloadedBooks.add(book);
			}
		}
		return actualDownloadedBooks;
	}

	private OnItemClickListener onItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			speakText(mlistDaisyBook.get(arg2).getTitle());

			final DaisyBookInfo daisyBook = mlistDaisyBook.get(arg2);
			boolean isDoubleTap = handleClickItem(arg2);
			if (isDoubleTap) {
				// add to sqlite
				addRecentBookToSQLite(daisyBook);

				// push to reader activity
				IntentController intentController = new IntentController(
						DaisyReaderDownloadedBooks.this);
				intentController.pushToDaisyEbookReaderIntent(daisyBook.getPath());
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
				mSql.deleteDaisyBook(mSql.getDaisyBookByTitle(daisyBook.getTitle(),
						Constants.TYPE_RECENT_BOOK).getId());
			}
			daisyBook.setSort(lastestIdRecentBooks + 1);
			mSql.addDaisyBook(daisyBook, Constants.TYPE_RECENT_BOOK);
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
					mlistDaisyBook = DaisyBookUtil.searchBookWithText(s, mlistDaisyBook,
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

	@Override
	protected void onRestart() {
		deleteCurrentInformation();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		handleSearchBook();
		deleteCurrentInformation();
	}

	@Override
	protected void onDestroy() {
		try {
			if (mTts != null) {
				mTts.shutdown();
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadedBooks.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}
}
