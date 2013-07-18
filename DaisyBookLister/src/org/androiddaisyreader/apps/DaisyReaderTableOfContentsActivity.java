package org.androiddaisyreader.apps;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
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
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.androiddaisyreader.utils.Constants;
import java.util.ArrayList;

/**
 * This activity is table of contents. It will so structure of book.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */
public class DaisyReaderTableOfContentsActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech mTts;
	private ArrayList<String> mListResult;
	private String mPath;
	private Window mWindow;
	private IntentController mIntentController;
	Daisy202Book mBook;

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
		String targetActivity = getIntent().getStringExtra(Constants.TARGET_ACTIVITY);
		// invisible button book mark when you go to bookmark from simple mode.
		if (targetActivity.equals(getString(R.string.simple_mode))) {
			imgBookmark.setVisibility(View.INVISIBLE);
		}
		startTts();
		mListResult = getIntent().getStringArrayListExtra(Constants.LIST_CONTENTS);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				DaisyReaderTableOfContentsActivity.this, R.layout.listrow, R.id.rowTextView,
				mListResult);
		ListView listContent = (ListView) this.findViewById(R.id.listContent);
		listContent.setAdapter(adapter);
		listContent.setOnItemClickListener(itemContentsClick);
		listContent.setOnItemLongClickListener(itemContentsLongClick);
		mIntentController = new IntentController(this);
		setBookTitle();
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
				PrivateException ex = new PrivateException(e,
						DaisyReaderTableOfContentsActivity.this, mPath);
				throw ex;
			}
			tvBookTitle.setText(mBook.getTitle());

		} catch (PrivateException e) {
			e.showDialogException(mIntentController);
		}

	}

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
			PrivateException ex = new PrivateException(e, DaisyReaderTableOfContentsActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.title_activity_daisy_reader_table_of_contents),
				TextToSpeech.QUEUE_FLUSH, null);
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyReaderTableOfContentsActivity.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = mWindow.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			mWindow.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderTableOfContentsActivity.this);
			ex.writeLogException();
		}
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
			Bookmark bookmark = new Bookmark();
			bookmark.setPath(mPath);
			mIntentController.pushToDaisyReaderBookmarkIntent(bookmark, mPath);
		}
	};

	/**
	 * Handle push to simple mode or visual mode
	 * 
	 * @param position
	 */
	private void pushToDaisyEbookReaderModeIntent(int position) {
		Intent i = null;
		String targetActivity = getIntent().getStringExtra(Constants.TARGET_ACTIVITY);
		SQLiteCurrentInformationHelper sql = new SQLiteCurrentInformationHelper(
				DaisyReaderTableOfContentsActivity.this);
		CurrentInformation current = sql.getCurrentInformation();
		if (targetActivity.equals(getString(R.string.simple_mode))) {
			i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
			if (current != null) {
				current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
				sql.updateCurrentInformation(current);
			}
		} else if (targetActivity.equals(getString(R.string.visual_mode))) {
			i = new Intent(this, DaisyEbookReaderVisualModeActivity.class);
			if (current != null) {
				current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
				sql.updateCurrentInformation(current);
			}
		}
		i.putExtra(Constants.POSITION_SECTION, String.valueOf(position));
		// Make sure path of daisy book is correct.
		i.putExtra(Constants.DAISY_PATH, mPath);
		this.startActivity(i);
	}

	@Override
	public void onInit(int arg0) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener
	}
}