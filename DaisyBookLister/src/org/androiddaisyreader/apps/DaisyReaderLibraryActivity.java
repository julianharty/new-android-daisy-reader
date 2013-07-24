package org.androiddaisyreader.apps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.service.DaisyEbookReaderService;
import org.androiddaisyreader.utils.Constants;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * The Class DaisyReaderLibraryActivity.
 * 
 * @author LogiGear
 * @date Jul 5, 2013
 */

public class DaisyReaderLibraryActivity extends DaisyEbookReaderBaseActivity {

	private long mLastPressTime = 0;
	private boolean mIsExit = true;
	BroadcastReceiver mSDCardStateChangeListener = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		// set listener for view
		findViewById(R.id.btnRecentBooks).setOnClickListener(this);
		findViewById(R.id.btnScanBooks).setOnClickListener(this);
		findViewById(R.id.btnDownloadBooks).setOnClickListener(this);

		findViewById(R.id.btnRecentBooks).setOnLongClickListener(this);
		findViewById(R.id.btnScanBooks).setOnLongClickListener(this);
		findViewById(R.id.btnDownloadBooks).setOnLongClickListener(this);

		Constants.FOLDER_CONTAIN_METADATA = Environment.getExternalStorageDirectory().toString()
				+ "/" + Constants.FOLDER_NAME + "/";
		createFolderContainXml();
		deleteCurrentInformation();
		Intent serviceIntent = new Intent(DaisyReaderLibraryActivity.this,
				DaisyEbookReaderService.class);
		startService(serviceIntent);
	}

	/**
	 * Check to see if the sdCard is mounted and create a directory in it
	 **/
	private void createFolderContainXml() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			File directory = new File(Constants.FOLDER_CONTAIN_METADATA);
			// Create a File object for the parent directory
			directory.mkdirs();
			// Then run the method to copy the file.
			copyFileFromAssets();

		} else if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {
			IntentController mIntentController = new IntentController(this);
			mIntentController.pushToDialog(getString(R.string.sd_card_not_present),
					getString(R.string.error_title), R.drawable.error, false, false, null);
		}

	}

	/**
	 * Copy the file from the assets folder to the sdCard
	 **/
	private void copyFileFromAssets() {
		File file = new File(Constants.FOLDER_CONTAIN_METADATA + Constants.META_DATA_FILE_NAME);
		if (!file.exists()) {
			AssetManager assetManager = getAssets();
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(Constants.META_DATA_FILE_NAME);
				out = new FileOutputStream(Constants.FOLDER_CONTAIN_METADATA
						+ Constants.META_DATA_FILE_NAME);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, DaisyReaderLibraryActivity.this);
				ex.writeLogException();
			}
		}
	}

	/**
	 * Copy file.
	 * 
	 * @param in the in
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	@Override
	public void onClick(View v) {
		if (v instanceof Button) {
			Button button = (Button) v;
			String buttonText = button.getText().toString();
			mTts.speak(buttonText, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	/**
	 * Push to other screen.
	 * 
	 * @param activityID
	 *            the activity id
	 */
	private void pushToScreen(int activityID) {
		Intent intent = null;
		switch (activityID) {
		case R.id.btnRecentBooks: // push to Recent Books Screen.
			intent = new Intent(this, DaisyReaderRecentBooksActivity.class);
			break;
		case R.id.btnScanBooks: // push to Scan Books Screen.
			intent = new Intent(this, DaisyReaderScanBooksActivity.class);
			break;
		case R.id.btnDownloadBooks: // push to Download Books Screen.
			intent = new Intent(this, DaisyReaderDownloadSiteActivity.class);
			break;
		default:
			break;
		}
		this.startActivity(intent);
	}

	@Override
	public boolean onLongClick(View v) {
		pushToScreen(v.getId());
		return false;
	}

	@Override
	protected void onRestart() {
		deleteCurrentInformation();
		super.onRestart();
	}

	@Override
	public void onBackPressed() {
		// do not allow user press button many times at the same time.
		if (SystemClock.elapsedRealtime() - mLastPressTime < Constants.TIME_WAIT_TO_EXIT_APPLICATION
				&& mIsExit) {
			moveTaskToBack(true);
			mIsExit = false;
			Intent serviceIntent = new Intent(DaisyReaderLibraryActivity.this,
					DaisyEbookReaderService.class);
			stopService(serviceIntent);
		} else {
			Toast.makeText(DaisyReaderLibraryActivity.this,
					this.getString(R.string.message_exit_application), Toast.LENGTH_SHORT).show();
			mTts.speak(this.getString(R.string.message_exit_application), TextToSpeech.QUEUE_FLUSH,
					null);
			mIsExit = true;
		}
		mLastPressTime = SystemClock.elapsedRealtime();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTts.speak(getString(R.string.title_activity_daisy_reader_library),
				TextToSpeech.QUEUE_FLUSH, null);

		deleteCurrentInformation();

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
			PrivateException ex = new PrivateException(e, DaisyReaderLibraryActivity.this);
			ex.writeLogException();
		}

	}

}
