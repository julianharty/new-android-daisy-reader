package org.androiddaisyreader.apps;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

/**
 * This activity contains two mode "simple mode" and "visual mode".
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class DaisyEbookReaderActivity extends Activity implements TextToSpeech.OnInitListener {
	private String TAG = "DaisyEbookReader";
	private IntentController mIntentController;
	private String mPath;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private TextToSpeech mTts;
	private Daisy202Book mBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
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
	 * Set book title on the top activity (between icon table of content and bookmark) 
	 */
	private void setBookTitle() {
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		mPath = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		try {
			mBook = DaisyReaderUtils.getDaisy202Book(mPath);
			Preconditions.checkNotNull(mBook);
			tvBookTitle.setText(mBook.getTitle());
		} catch (NullPointerException e) {
			mIntentController.pushToDialogError(getString(R.string.error_wrongFormat), true);
		}

	}

	private OnClickListener simpleModeClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mTts.speak(getString(R.string.simpleMode), TextToSpeech.QUEUE_FLUSH, null);
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
			mTts.speak(getString(R.string.visualMode), TextToSpeech.QUEUE_FLUSH, null);
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
			try {
				navigator = new Navigator(mBook);
				mIntentController.pushToTableOfContentsIntent(mPath, navigator,
						getString(R.string.visualMode));
			} catch (Exception e) {
				mIntentController.pushToDialogError(getString(R.string.error_noPathFound), true);
			}
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				Bookmark bookmark = new Bookmark();
				bookmark.setPath(mPath);
				mIntentController.pushToDaisyReaderBookmarkIntent(bookmark, mPath);

			} catch (Exception e) {
				mIntentController.pushToDialogError(getString(R.string.error_noPathFound), true);
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
		try {
			Preconditions.checkNotNull(mTts);
			mTts.stop();
			mTts.shutdown();
		} catch (NullPointerException e) {
			Log.i(TAG, "tts is null");
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.title_activity_daisy_ebook_reader), TextToSpeech.QUEUE_FLUSH,
				null);
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		float valueConvert = 255;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			valueScreen = mPreferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			Log.i(TAG, "can not get value screnn");
		}
		LayoutParams layoutpars = mWindow.getAttributes();
		layoutpars.screenBrightness = valueScreen / valueConvert;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		super.onResume();
	}

	@Override
	public void onInit(int arg0) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener
	}
}