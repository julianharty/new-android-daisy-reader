/**
 * This activity contains two mode "simple mode" and "visual mode".
 * @author LogiGear
 * @date 2013.03.05
 */

package org.androiddaisyreader.apps;

import java.io.InputStream;

import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DaisyEbookReaderActivity extends Activity implements
		TextToSpeech.OnInitListener {
	private IntentController mIntentController;
	private String mBookTitle;
	private String mPath;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private TextToSpeech mTts;
	private BookContext mBookContext;
	private Daisy202Book mBook;
	private Navigator mNavigator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		mIntentController = new IntentController(this);
		mTts = new TextToSpeech(this, this);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		mPath = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		String[] title = mPath.split("/");
		mBookTitle = title[title.length - 2].toString();
		tvBookTitle.setText(mBookTitle);
		RelativeLayout simpleMode = (RelativeLayout) this
				.findViewById(R.id.simpleMode);
		simpleMode.setOnClickListener(simpleModeClick);
		simpleMode.setOnLongClickListener(simpleModeLongClick);
		RelativeLayout visualMode = (RelativeLayout) this
				.findViewById(R.id.visualMode);
		visualMode.setOnClickListener(visualModeClick);
		visualMode.setOnLongClickListener(visualModeLongClick);
		ImageView imgTableOfContents = (ImageView) this
				.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);

		ImageView imgBookmarks = (ImageView) this
				.findViewById(R.id.imgBookmark);
		imgBookmarks.setOnClickListener(imgBookmarkClick);
	}

	private OnClickListener simpleModeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mTts.speak(getString(R.string.simpleMode), TextToSpeech.QUEUE_FLUSH,
					null);
		}
	};

	private OnLongClickListener simpleModeLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			mIntentController.pushToDaisyEbookReaderSimpleModeIntent(mPath, 0, 0);
			return false;
		}
	};

	private OnClickListener visualModeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mTts.speak(getString(R.string.visualMode), TextToSpeech.QUEUE_FLUSH,
					null);
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

		@Override
		public void onClick(View v) {
			try {
				InputStream contents;
				mBookContext = DaisyReaderUtils.openBook(mPath);
				String[] sp = mPath.split("/");
				contents = mBookContext.getResource(sp[sp.length - 1]);
				mBook = NccSpecification.readFromStream(contents);
				mNavigator = new Navigator(mBook);
				mIntentController.pushToTableOfContentsIntent(mPath, mNavigator,
						getString(R.string.visualMode));
			} catch (Exception e) {
				mIntentController
						.pushToDialogError(getString(R.string.noPathFound), true);
			}
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				Bookmark bookmark = new Bookmark();
				bookmark.setBook(mBookTitle);
				mIntentController
						.pushToDaisyReaderBookmarkIntent(bookmark, mPath);

			} catch (Exception e) {
				mIntentController
						.pushToDialogError(getString(R.string.noPathFound), true);
			}
		}
	};

	@Override
	protected void onDestroy() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
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
	public void onInit(int status) {
	}
}