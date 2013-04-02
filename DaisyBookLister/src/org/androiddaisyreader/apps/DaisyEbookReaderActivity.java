/**
 * This activity contains two mode "simple mode" and "visual mode".
 * @author LogiGear
 * @date 2013.03.05
 */

package org.androiddaisyreader.apps;

import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.utils.DaisyReaderConstants;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
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

public class DaisyEbookReaderActivity extends Activity implements
		TextToSpeech.OnInitListener {
	private IntentController intentController;
	private String bookTitle;
	private String path;
	private SharedPreferences preferences;
	private Window window;
	private TextToSpeech tts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		window = getWindow();
		window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		intentController = new IntentController(this);
		tts = new TextToSpeech(this, this);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		path = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		String[] title = path.split("/");
		bookTitle = title[title.length - 2].toString();
		tvBookTitle.setText(bookTitle);
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
			tts.speak(getString(R.string.simpleMode), TextToSpeech.QUEUE_FLUSH,
					null);
		}
	};

	private OnLongClickListener simpleModeLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			pushToDaisyEbookReaderSimpleModeIntent();
			return false;
		}
	};

	private void pushToDaisyEbookReaderSimpleModeIntent() {
		Intent i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		this.startActivity(i);
	}

	private OnClickListener visualModeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			tts.speak(getString(R.string.visualMode), TextToSpeech.QUEUE_FLUSH,
					null);
		}
	};

	private OnLongClickListener visualModeLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			pushToDaisyEbookReaderVisualModeIntent();
			return false;
		}
	};

	private void pushToDaisyEbookReaderVisualModeIntent() {
		Intent i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		this.startActivity(i);
	}

	private OnClickListener imgTableOfContentsClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				intentController.pushToTableOfContentsIntent(path,
						getString(R.string.visualMode));
			} catch (Exception e) {
				intentController
						.pushToDialogError(getString(R.string.noPathFound));
			}
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				Bookmark bookmark = new Bookmark();
				bookmark.setBook(bookTitle);
				intentController
						.pushToDaisyReaderBookmarkIntent(bookmark, path);

			} catch (Exception e) {
				intentController
						.pushToDialogError(getString(R.string.noPathFound));
			}
		}
	};

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
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
			valueScreen = preferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		LayoutParams layoutpars = window.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		window.setAttributes(layoutpars);
		super.onResume();
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub

	}
}