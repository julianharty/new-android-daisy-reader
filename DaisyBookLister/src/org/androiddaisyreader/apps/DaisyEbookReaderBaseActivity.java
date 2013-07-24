package org.androiddaisyreader.apps;

import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.actionbarsherlock.app.SherlockActivity;
import com.bugsense.trace.BugSenseHandler;

/**
 * 
 * @author LogiGear
 * @date Jul 19, 2013
 */

public class DaisyEbookReaderBaseActivity extends SherlockActivity implements OnClickListener,
		OnLongClickListener, TextToSpeech.OnInitListener {
	protected TextToSpeech mTts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// start the session
		BugSenseHandler.initAndStartSession(getApplicationContext(), Constants.BUGSENSE_API_KEY);

		// initial TTS
		startTts();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Window window = getWindow();
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = window.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, getApplicationContext());
			ex.writeLogException();
		}
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		try {
			if (mTts != null) {
				if (mTts.isSpeaking()) {
					mTts.stop();
				}
				mTts.shutdown();
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderBaseActivity.this);
			ex.writeLogException();
		}
	}

	/**
	 * Make sure TTS installed on your device.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.MY_DATA_CHECK_CODE) {
			if (!(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)) {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onInit(int status) {
		
	}

	@Override
	public boolean onLongClick(View arg0) {
		return false;
	}

	@Override
	public void onClick(View arg0) {

	}

	/*
	 * Start text to speech
	 */
	private void startTts() {
		if (mTts == null) {
			mTts = new TextToSpeech(this, this);
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, RESULT_OK);
		}
	}

	/**
	 * Back to top screen.
	 */
	public void backToTopScreen() {
		Intent intent = new Intent(this, DaisyReaderLibraryActivity.class);
		// Removes other Activities from stack
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * Delete current information.
	 */
	public void deleteCurrentInformation() {
		SQLiteCurrentInformationHelper sql = new SQLiteCurrentInformationHelper(
				getApplicationContext());
		CurrentInformation current = sql.getCurrentInformation();
		if (current != null) {
			sql.deleteCurrentInformation(current.getId());
		}
	}

}
