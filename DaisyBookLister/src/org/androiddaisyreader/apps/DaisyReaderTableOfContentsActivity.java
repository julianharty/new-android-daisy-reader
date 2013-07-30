package org.androiddaisyreader.apps;

import java.util.ArrayList;

import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

/**
 * This activity is table of contents. It will so structure of book.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */
public class DaisyReaderTableOfContentsActivity extends DaisyEbookReaderBaseActivity {

	private ArrayList<String> mListResult;
	private String mPath;
	private IntentController mIntentController;
	Daisy202Book mBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_table_of_contents);

		mListResult = getIntent().getStringArrayListExtra(Constants.LIST_CONTENTS);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				DaisyReaderTableOfContentsActivity.this, R.layout.listrow, R.id.rowTextView,
				mListResult);
		ListView listContent = (ListView) this.findViewById(R.id.listContent);
		listContent.setAdapter(adapter);
		listContent.setOnItemClickListener(itemContentsClick);
		listContent.setOnItemLongClickListener(itemContentsLongClick);
		mIntentController = new IntentController(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getBookTitle());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String targetActivity = getIntent().getStringExtra(Constants.TARGET_ACTIVITY);
		// invisible button book mark when you go to bookmark from simple mode.
		if (!targetActivity.equals(getString(R.string.simple_mode))) {
			SubMenu subMenu = menu.addSubMenu(0, Constants.SUBMENU_MENU, 1, R.string.menu_title);

			subMenu.add(0, Constants.SUBMENU_LIBRARY, 2, R.string.submenu_library).setIcon(R.drawable.library);

			subMenu.add(0, Constants.SUBMENU_BOOKMARKS, 3, R.string.submenu_bookmarks).setIcon(
					R.drawable.bookmark);

			subMenu.add(0, Constants.SUBMENU_SIMPLE_MODE, 5, R.string.submenu_simple_mode).setIcon(
					R.drawable.simple_mode);

			subMenu.add(0, Constants.SUBMENU_SETTINGS, 7, R.string.submenu_settings).setIcon(R.drawable.settings);

			MenuItem subMenuItem = subMenu.getItem();
			subMenuItem.setIcon(R.drawable.ic_menu_32x32);
			subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		return true;
	}

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// go to simple mode
		case Constants.SUBMENU_SIMPLE_MODE:
			mIntentController.pushToDaisyEbookReaderSimpleModeIntent(getIntent().getStringExtra(
					Constants.DAISY_PATH));
			return true;
			// go to settings
		case Constants.SUBMENU_SETTINGS:
			mIntentController.pushToDaisyReaderSettingIntent();
			return true;
			// go to book marks
		case Constants.SUBMENU_BOOKMARKS:
			pushToBookmark();
			return true;
			// go to library
		case Constants.SUBMENU_LIBRARY:
			mIntentController.pushToLibraryIntent();
			return true;
			// back to previous screen
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Gets the book title.
	 * 
	 * @return the book title
	 */
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onDestroy() {
		try {
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderTableOfContentsActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		speakText(getString(R.string.title_activity_daisy_reader_table_of_contents));
	}

	private OnItemClickListener itemContentsClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
			speakText(mListResult.get(position).toString());
		}
	};

	private OnItemLongClickListener itemContentsLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
			pushToDaisyEbookReaderModeIntent(position + 1);
			return false;
		};
	};

	/**
	 * Push to bookmark.
	 */
	private void pushToBookmark() {
		Bookmark bookmark = new Bookmark();
		bookmark.setPath(mPath);
		mIntentController.pushToDaisyReaderBookmarkIntent(bookmark, mPath);
		finish();
	}

	/**
	 * Handle push to simple mode or visual mode
	 * 
	 * @param position
	 */
	private void pushToDaisyEbookReaderModeIntent(int position) {
		Intent i = null;
		String targetActivity = getIntent().getStringExtra(Constants.TARGET_ACTIVITY);
		SQLiteCurrentInformationHelper sql = new SQLiteCurrentInformationHelper(
				DaisyReaderTableOfContentsActivity.this);
		CurrentInformation current = sql.getCurrentInformation();
		if (targetActivity.equals(getString(R.string.simple_mode))) {
			i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
			if (current != null) {
				current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
				sql.updateCurrentInformation(current);
			}
		} else if (targetActivity.equals(getString(R.string.visual_mode))) {
			i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
			if (current != null) {
				current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
				sql.updateCurrentInformation(current);
			}
		}
		i.putExtra(Constants.POSITION_SECTION, String.valueOf(position));
		// Make sure path of daisy book is correct.
		i.putExtra(Constants.DAISY_PATH, mPath);
		this.startActivity(i);
	}

}