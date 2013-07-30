package org.androiddaisyreader.apps;

import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
	private Daisy202Book mBook;
	private SQLiteCurrentInformationHelper mSql;
	private CurrentInformation mCurrent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_ebook_reader);

		mSql = new SQLiteCurrentInformationHelper(getApplicationContext());
		mIntentController = new IntentController(this);

		RelativeLayout simpleMode = (RelativeLayout) this.findViewById(R.id.simpleMode);
		simpleMode.setOnClickListener(simpleModeClick);
		simpleMode.setOnLongClickListener(simpleModeLongClick);
		RelativeLayout visualMode = (RelativeLayout) this.findViewById(R.id.visualMode);
		visualMode.setOnClickListener(visualModeClick);
		visualMode.setOnLongClickListener(visualModeLongClick);

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
	protected void onStart() {
		super.onStart();
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
		mCurrent = mSql.getCurrentInformation();
		if (mCurrent != null) {
			mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader));
			mSql.updateCurrentInformation(mCurrent);
		}
	}

	/**
	 * get book title on the top activity
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

	private OnClickListener simpleModeClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			speakOut(Constants.SIMPLE_MODE);
		}
	};

	private OnLongClickListener simpleModeLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			mIntentController.pushToDaisyEbookReaderSimpleModeIntent(mPath);
			return false;
		}
	};

	private OnClickListener visualModeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			speakOut(Constants.VISUAL_MODE);
		}
	};

	private OnLongClickListener visualModeLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			mIntentController.pushToDaisyEbookReaderVisualModeIntent(mPath);
			return false;
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
		speakOut(Constants.READER_ACTIVITY);
	}

	private void speakOut(int message) {
		switch (message) {
		case Constants.SIMPLE_MODE:
			speakText(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
			break;
		case Constants.VISUAL_MODE:
			speakText(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
			break;
		case Constants.READER_ACTIVITY:
			speakText(getString(R.string.title_activity_daisy_ebook_reader));
			break;
		default:
			break;
		}
	}

}