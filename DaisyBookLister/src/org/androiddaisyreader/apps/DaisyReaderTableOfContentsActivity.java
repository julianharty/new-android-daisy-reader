package org.androiddaisyreader.apps;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import com.google.common.base.Preconditions;

import java.util.ArrayList;

/**
 * This activity is table of contents. It will so structure of book.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */
public class DaisyReaderTableOfContentsActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private String TAG = "DaisyReaderTableOfContents";
	private TextToSpeech mTts;
	private ArrayList<String> mListResult;
	private String mPath;
	private Window mWindow;
	private IntentController mIntentController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_reader_table_of_contents);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		ImageView imgBookmark = (ImageView) this.findViewById(R.id.imgBookmark);
		imgBookmark.setOnClickListener(imgBookmarkClick);
		ImageView imgTableOfContents = (ImageView) this.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setVisibility(View.INVISIBLE);
		startTts();
		mListResult = getIntent().getStringArrayListExtra(DaisyReaderConstants.LIST_CONTENTS);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.listrow, R.id.rowTextView, mListResult);
		ListView listContent = (ListView) this.findViewById(R.id.listContent);
		listContent.setAdapter(adapter);
		listContent.setOnItemClickListener(itemContentsClick);
		listContent.setOnItemLongClickListener(itemContentsLongClick);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		mIntentController = new IntentController(this);
		mPath = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		Daisy202Book book = DaisyReaderUtils.getDaisy202Book(mPath);
		tvBookTitle.setText(book.getTitle());
	}

	private void startTts() {
		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
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
		mTts.speak(getString(R.string.title_activity_daisy_reader_table_of_contents),
				TextToSpeech.QUEUE_FLUSH, null);
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			valueScreen = preferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			Log.i(TAG, "can not get value of screen");
		}
		LayoutParams layoutpars = mWindow.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		super.onResume();
	}

	private OnItemClickListener itemContentsClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
			mTts.speak(mListResult.get(position).toString(), TextToSpeech.QUEUE_FLUSH, null);
		}

	};

	private OnItemLongClickListener itemContentsLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
			pushToDaisyEbookReaderModeIntent(position + 1);
			return false;
		};
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

	private void pushToDaisyEbookReaderModeIntent(int position) {
		Intent i = null;
		String targetActivity = getIntent().getStringExtra(DaisyReaderConstants.TARGET_ACTIVITY);
		if (targetActivity.equals(getString(R.string.simpleMode))) {
			i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
		} else if (targetActivity.equals(getString(R.string.visualMode))) {
			i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
		}
		i.putExtra(DaisyReaderConstants.POSITION_SECTION, String.valueOf(position));
		// Make sure path of daisy book is correct.
		i.putExtra(DaisyReaderConstants.DAISY_PATH, mPath);
		this.startActivity(i);
	}

	@Override
	public void onInit(int arg0) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener
	}
}