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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class DaisyReaderTableOfContentsActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private ArrayList<String> listResult;
	private ListView listContent;
	private String targetActivity;
	private SharedPreferences preferences;
	private Window window;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_table_of_contents);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		window = getWindow();
		listContent = (ListView) this.findViewById(R.id.listContent);
		listResult = getIntent().getStringArrayListExtra(
				DaisyReaderConstants.LIST_CONTENTS);
		tts = new TextToSpeech(this, this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.listrow, R.id.rowTextView,
				listResult);
		listContent.setAdapter(adapter);
		listContent.setOnItemClickListener(itemContentsClick);
		listContent.setOnItemLongClickListener(itemContentsLongClick);
	}

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

	private OnItemClickListener itemContentsClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			Toast.makeText(getBaseContext(),
					listResult.get(position).toString(), Toast.LENGTH_SHORT)
					.show();
			tts.speak(listResult.get(position).toString(),
					TextToSpeech.QUEUE_FLUSH, null);
		}

	};

	private OnItemLongClickListener itemContentsLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v,
				int position, long id) {
			pushToDaisyEbookReaderModeIntent(position);
			return false;
		};
	};

	private void pushToDaisyEbookReaderModeIntent(int position) {
		Intent i = null;
		targetActivity = getIntent().getStringExtra(
				DaisyReaderConstants.TARGET_ACTIVITY);
		if (targetActivity.equals(getString(R.string.simpleMode))) {
			i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
		} else if (targetActivity.equals(getString(R.string.visualMode))) {
			i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
		}
		i.putExtra(DaisyReaderConstants.POSITION_SECTION, position);
		// Make sure path of daisy book is correct.
		i.putExtra(DaisyReaderConstants.DAISY_PATH,
				getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH));
		this.startActivity(i);
	}

	@Override
	public void onInit(int arg0) {
	}
}