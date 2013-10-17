package org.androiddaisyreader.apps;

import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.actionbarsherlock.view.MenuItem;

/**
 * This activity contains two mode "simple mode" and "visual mode".
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class DaisyEbookReaderModeChoiceActivity extends DaisyEbookReaderBaseActivity {
    private IntentController mIntentController;
    private String mPath;
    private SQLiteCurrentInformationHelper mSql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daisy_ebook_reader);

        mSql = new SQLiteCurrentInformationHelper(getApplicationContext());
        mIntentController = new IntentController(this);

        RelativeLayout simpleMode = (RelativeLayout) this.findViewById(R.id.simpleMode);
        simpleMode.setOnClickListener(modeClick);
        RelativeLayout visualMode = (RelativeLayout) this.findViewById(R.id.visualMode);
        visualMode.setOnClickListener(modeClick);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getBookTitle());
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
    protected void onRestart() {
        updateCurrentInformation();
        super.onRestart();
    }

    /**
     * Update current information.
     */
    private void updateCurrentInformation() {
        CurrentInformation current = mSql.getCurrentInformation();
        if (current != null) {
            current.setActivity(getString(R.string.title_activity_daisy_ebook_reader));
            mSql.updateCurrentInformation(current);
        }
    }

    /**
     * get book title on the top activity
     */
    private String getBookTitle() {
        DaisyBook daisyBook;
        String titleOfBook = "";
        mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
        try {
            try {
                if (DaisyBookUtil.findDaisyFormat(mPath) == Constants.DAISY_202_FORMAT) {
                    daisyBook = DaisyBookUtil.getDaisy202Book(mPath);
                    titleOfBook = daisyBook.getTitle() == null ? "" : daisyBook.getTitle();
                } else {
                    daisyBook = DaisyBookUtil.getDaisy30Book(mPath);
                    titleOfBook = daisyBook.getTitle() == null ? "" : daisyBook.getTitle();
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e, getApplicationContext(), mPath);
                throw ex;
            }

        } catch (PrivateException e) {
            e.showDialogException(mIntentController);
        }
        return titleOfBook;
    }

    private OnClickListener modeClick = new OnClickListener() {
        @SuppressLint("HandlerLeak")
        @Override
        public void onClick(final View v) {
            boolean isDoubleTap = handleClickItem(v.getId());
            if (isDoubleTap) {
                if (v.getId() == R.id.simpleMode) {
                    mIntentController.pushToDaisyEbookReaderSimpleModeIntent(mPath);
                } else {
                    mIntentController.pushToDaisyEbookReaderVisualModeIntent(mPath);
                }
            } else {
                if (v.getId() == R.id.simpleMode) {
                    speakTextOnHandler(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
                } else {
                    speakTextOnHandler(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mTts != null) {
                mTts.shutdown();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderModeChoiceActivity.this);
            ex.writeLogException();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        speakText(getString(R.string.title_activity_daisy_ebook_reader));
    }

}