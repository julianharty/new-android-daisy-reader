package org.androiddaisyreader.apps;

import java.util.ArrayList;

import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
    private DaisyBook mBook;
    private int order = 1;

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
        mIntentController = new IntentController(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getBookTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String targetActivity = getIntent().getStringExtra(Constants.TARGET_ACTIVITY);
        // invisible button book mark when you go to bookmark from simple mode.
        if (!targetActivity.equals(getString(R.string.simple_mode))) {
            SubMenu subMenu = menu.addSubMenu(0, Constants.SUBMENU_MENU, order++, R.string.menu_title);

            subMenu.add(0, Constants.SUBMENU_LIBRARY, order++, R.string.submenu_library).setIcon(
                    R.raw.library);

            subMenu.add(0, Constants.SUBMENU_BOOKMARKS, order++, R.string.submenu_bookmarks).setIcon(
                    R.raw.bookmark);

            subMenu.add(0, Constants.SUBMENU_SIMPLE_MODE, order++, R.string.submenu_simple_mode).setIcon(
                    R.raw.simple_mode);

            subMenu.add(0, Constants.SUBMENU_SETTINGS, order++, R.string.submenu_settings).setIcon(
                    R.raw.settings);

            MenuItem subMenuItem = subMenu.getItem();
            subMenuItem.setIcon(R.raw.ic_menu_32x32);
            subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
                if (DaisyBookUtil.findDaisyFormat(mPath) == Constants.DAISY_202_FORMAT) {
                    mBook = DaisyBookUtil.getDaisy202Book(mPath);
                    titleOfBook = mBook.getTitle() == null ? "" : mBook.getTitle();
                } else {
                    mBook = DaisyBookUtil.getDaisy30Book(mPath);
                    titleOfBook = mBook.getTitle() == null ? "" : mBook.getTitle();
                }
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
            boolean isDoubleTap = handleClickItem(position);
            if (isDoubleTap) {
                pushToDaisyEbookReaderModeIntent(position + 1);
            } else {
                speakTextOnHandler(mListResult.get(position).toString());
            }
        }
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
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (current != null) {
                current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
                sql.updateCurrentInformation(current);
            }
        } else if (targetActivity.equals(getString(R.string.visual_mode))) {
            i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (current != null) {
                current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
                sql.updateCurrentInformation(current);
            }
        }
        if (i != null) {
            i.putExtra(Constants.POSITION_SECTION, String.valueOf(position));
            // Make sure path of daisy book is correct.
            i.putExtra(Constants.DAISY_PATH, mPath);
            this.startActivity(i);
        }
    }

}