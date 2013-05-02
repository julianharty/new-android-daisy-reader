/**
 * This activity is table of contents. It will so structure of book.
 * @author LogiGear
 * @date 2013.03.05
 */
package org.androiddaisyreader.apps;

import java.util.ArrayList;

import org.androiddaisyreader.utils.DaisyReaderConstants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class DaisyReaderTableOfContentsActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech mTts;
	private ArrayList<String> mListResult;
	private ListView mListContent;
	private String mTargetActivity;
	private SharedPreferences mPreferences;
	private Window mWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_table_of_contents);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mListContent = (ListView) this.findViewById(R.id.listContent);
		mListResult = getIntent().getStringArrayListExtra(
				DaisyReaderConstants.LIST_CONTENTS);
		mTts = new TextToSpeech(this, this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.listrow, R.id.rowTextView,
				mListResult);
		mListContent.setAdapter(adapter);
		mListContent.setOnItemClickListener(itemContentsClick);
		mListContent.setOnItemLongClickListener(itemContentsLongClick);
	}

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

	private OnItemClickListener itemContentsClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			mTts.speak(mListResult.get(position).toString(),
					TextToSpeech.QUEUE_FLUSH, null);
		}

	};

	private OnItemLongClickListener itemContentsLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v,
				int position, long id) {
			pushToDaisyEbookReaderModeIntent(position + 1);
			return false;
		};
	};

	private void pushToDaisyEbookReaderModeIntent(int position) {
		Intent i = null;
		mTargetActivity = getIntent().getStringExtra(
				DaisyReaderConstants.TARGET_ACTIVITY);
		if (mTargetActivity.equals(getString(R.string.simpleMode))) {
			i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
		} else if (mTargetActivity.equals(getString(R.string.visualMode))) {
			i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
		}
		i.putExtra(DaisyReaderConstants.POSITION_SECTION, String.valueOf(position));
		// Make sure path of daisy book is correct.
		i.putExtra(DaisyReaderConstants.DAISY_PATH,
				getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH));
		this.startActivity(i);
	}

	@Override
	public void onInit(int arg0) {
	}
}