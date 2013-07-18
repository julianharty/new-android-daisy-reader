package org.androiddaisyreader.apps;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.androiddaisyreader.utils.Constants;

/**
 * This activity contains two mode "simple mode" and "visual mode".
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class DaisyEbookReaderActivity extends Activity implements TextToSpeech.OnInitListener {
	private IntentController mIntentController;
	private String mPath;
	private Window mWindow;
	private TextToSpeech mTts;
	private Daisy202Book mBook;
	private SQLiteCurrentInformationHelper mSql;
	private CurrentInformation mCurrent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		mSql = new SQLiteCurrentInformationHelper(getApplicationContext());
		mIntentController = new IntentController(this);
		startTts();
		setBookTitle();
		RelativeLayout simpleMode = (RelativeLayout) this.findViewById(R.id.simpleMode);
		simpleMode.setOnClickListener(simpleModeClick);
		simpleMode.setOnLongClickListener(simpleModeLongClick);
		RelativeLayout visualMode = (RelativeLayout) this.findViewById(R.id.visualMode);
		visualMode.setOnClickListener(visualModeClick);
		visualMode.setOnLongClickListener(visualModeLongClick);
		ImageView imgTableOfContents = (ImageView) this.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);
		ImageView imgBookmarks = (ImageView) this.findViewById(R.id.imgBookmark);
		imgBookmarks.setOnClickListener(imgBookmarkClick);
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
	 * Start text to speech
	 */
	private void startTts() {
		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	/**
	 * Set book title on the top activity (between icon table of content and
	 * bookmark
	 */
	private void setBookTitle() {
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
		try {
			try {
				mBook = DaisyBookUtil.getDaisy202Book(mPath);
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, getApplicationContext(), mPath);
				throw ex;
			}
			tvBookTitle.setText(mBook.getTitle());

		} catch (PrivateException e) {
			e.showDialogException(mIntentController);
		}

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

	private OnClickListener imgTableOfContentsClick = new OnClickListener() {
		Navigator navigator;

		@Override
		public void onClick(View v) {
			navigator = new Navigator(mBook);
			mIntentController.pushToTableOfContentsIntent(mPath, navigator,
					getString(R.string.visual_mode));
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Bookmark bookmark = new Bookmark();
			bookmark.setPath(mPath);
			mIntentController.pushToDaisyReaderBookmarkIntent(bookmark, mPath);
		}
	};

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
			PrivateException ex = new PrivateException(e, DaisyEbookReaderActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		speakOut(Constants.READER_ACTIVITY);
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = mWindow.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			mWindow.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderActivity.this);
			ex.writeLogException();
		}
		super.onResume();
	}

	private void speakOut(int message) {
		switch (message) {
		case Constants.SIMPLE_MODE:
			mTts.speak(getString(R.string.title_activity_daisy_ebook_reader_simple_mode),
					TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.VISUAL_MODE:
			mTts.speak(getString(R.string.title_activity_daisy_ebook_reader_visual_mode),
					TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.READER_ACTIVITY:
			mTts.speak(getString(R.string.title_activity_daisy_ebook_reader),
					TextToSpeech.QUEUE_FLUSH, null);
			break;
		default:
			break;
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener
	}
}