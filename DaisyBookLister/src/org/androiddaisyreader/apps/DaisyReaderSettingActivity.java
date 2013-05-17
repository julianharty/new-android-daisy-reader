package org.androiddaisyreader.apps;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.provider.Settings.System;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

import org.androiddaisyreader.utils.DaisyReaderConstants;

import com.google.common.base.Preconditions;

/**
 * This activity is setting. It have some functions such as: change text color,
 * change highlight color, etc.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */
@SuppressWarnings("deprecation")
public class DaisyReaderSettingActivity extends Activity implements TextToSpeech.OnInitListener {

	private String TAG = "DaisyReaderSetting";
	private TextToSpeech mTts;
	private Window mWindow;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;
	private TextView mFontSize;
	private TextView mTextColor;
	private TextView mBackgroundColor;
	private TextView mHighlightColor;
	private int mFontsize;
	// some basic colors
	private int mColorTable[] = { 0xffffffff, 0xffc0c0c0, 0xff808080, 0xff000000, 0xffffc0c0,
			0xffff6060, 0xffff0000, 0xff800000, 0xffffe0c0, 0xffffb060, 0xffff8000, 0xff804000,
			0xffffffc0, 0xffffff60, 0xffffff00, 0xff808000, 0xffe0ffc0, 0xffb0ff60, 0xff80ff00,
			0xff408000, 0xffc0ffc0, 0xff60ff60, 0xff00ff00, 0xff008000, 0xffc0ffe0, 0xff60ffb0,
			0xff00ff80, 0xff008040, 0xffc0ffff, 0xff60ffff, 0xff00ffff, 0xff008080, 0xffc0e0ff,
			0xff60b0ff, 0xff0080ff, 0xff004480, 0xffc0c0ff, 0xff6060ff, 0xff0000ff, 0xff000080,
			0xffe0c0ff, 0xffb060ff, 0xff8000ff, 0xff400080, 0xffffc0ff, 0xffff60ff, 0xffff00ff,
			0xff800080, 0xffffc0e0, 0xffff60b0, 0xffff0080, 0xff800040 };

	private int mBrightness;
	private int mCurrentTextColor;
	private int mCurrentBackgroundColor;
	private int mCurrentHighlightColor;
	private int mCurrentNumberOfRecentBooks;
	private int mCurrentNumberOfBookmarks;
	private int mDefaultFontsize = 6;
	private int mDefaultBrightness = 20;
	private Boolean mChangeText;
	private Boolean mChangeBackground;
	private Boolean mChangeHighlight;
	private EditText mNumberOfRecentBooks;
	private EditText mNumberOfBookmarks;
	private LayoutParams layoutpars;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_setting);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mEditor = mPreferences.edit();
		startTts();
		settingBrightness();

		mFontSize = (TextView) findViewById(R.id.tvFontSize);
		settingFontsize();
		// setting text color
		mTextColor = (TextView) findViewById(R.id.tvTextColor);
		settingTextColor();
		// setting background color
		mBackgroundColor = (TextView) findViewById(R.id.tvBackgroundColor);
		settingBackgroundColor();
		// setting current highlight color
		mHighlightColor = (TextView) findViewById(R.id.tvHighlightColor);
		settingHighlightColor();
		// setting current number of rencent books
		mNumberOfRecentBooks = (EditText) findViewById(R.id.edtNumberOfRecentBooks);
		settingCurrentRecentBook();
		// setting current number of bookmarks
		mNumberOfBookmarks = (EditText) findViewById(R.id.edtNumberOfBookmarks);
		settingCurrentBookmark();
		// setting night mode
		settingNightmode();
	}

	private void startTts() {
		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	private void settingBrightness() {
		SeekBar brightBar = (SeekBar) findViewById(R.id.barBrightness);
		brightBar.setMax(255);
		brightBar.setKeyProgressIncrement(1);
		ContentResolver contentResolver = getContentResolver();
		mWindow = getWindow();
		try {
			// get the current system brightness
			mBrightness = mPreferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(contentResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			Log.i(TAG, "can not get value of brightness");
		}
		// sets the progress of the seek bar based on the system's brightness
		brightBar.setProgress(mBrightness - mDefaultBrightness);
		layoutpars = mWindow.getAttributes();
		// set the brightness of this window
		layoutpars.screenBrightness = mBrightness / (float) 255;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		// register OnSeekBarChangeListener, so it can actually change values
		brightBar.setOnSeekBarChangeListener(seekBarBrightnessListener);
	}

	private void settingFontsize() {
		SeekBar sizeBar = (SeekBar) findViewById(R.id.barFontSize);
		mFontsize = mPreferences.getInt(DaisyReaderConstants.FONT_SIZE,
				DaisyReaderConstants.FONTSIZE_DEFAULT);
		mFontSize.setTextSize(mFontsize);
		sizeBar.setMax(30);
		sizeBar.setProgress(mFontsize - mDefaultFontsize);
		sizeBar.setOnSeekBarChangeListener(seekBarSizeListener);
	}

	private void settingTextColor() {
		mCurrentTextColor = mPreferences.getInt(DaisyReaderConstants.TEXT_COLOR, 0xffc0c0c0);
		mTextColor.setBackgroundColor(mCurrentTextColor);
		mTextColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mChangeHighlight = false;
				mChangeBackground = false;
				mChangeText = true;
				showDialog(0);
			}
		});
	}

	private void settingBackgroundColor() {
		mCurrentBackgroundColor = mPreferences.getInt(DaisyReaderConstants.BACKGROUND_COLOR,
				Color.BLACK);
		mBackgroundColor.setBackgroundColor(mCurrentBackgroundColor);
		mBackgroundColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mChangeBackground = true;
				mChangeText = false;
				mChangeHighlight = false;
				showDialog(0);
			}
		});
	}

	private void settingHighlightColor() {
		mCurrentHighlightColor = mPreferences.getInt(DaisyReaderConstants.HIGHLIGHT_COLOR,
				Color.YELLOW);
		mHighlightColor.setBackgroundColor(mCurrentHighlightColor);
		mHighlightColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mChangeBackground = false;
				mChangeText = false;
				mChangeHighlight = true;
				showDialog(0);
			}
		});

	}

	private void settingCurrentBookmark() {
		mCurrentNumberOfBookmarks = mPreferences.getInt(DaisyReaderConstants.NUMBER_OF_BOOKMARKS,
				DaisyReaderConstants.NUMBER_OF_BOOKMARK_DEFAULT);
		mNumberOfBookmarks.setText(String.valueOf(mCurrentNumberOfBookmarks));
		mNumberOfBookmarks.addTextChangedListener(bookmarkTextWatcher);
	}

	private void settingCurrentRecentBook() {
		mCurrentNumberOfRecentBooks = mPreferences.getInt(
				DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS,
				DaisyReaderConstants.NUMBER_OF_RECENTBOOK_DEFAULT);
		mNumberOfRecentBooks.setText(String.valueOf(mCurrentNumberOfRecentBooks));
		mNumberOfRecentBooks.addTextChangedListener(recentBooksTextWatcher);
	}

	private void settingNightmode() {
		final ToggleButton toogleNightMode = (ToggleButton) findViewById(R.id.toggleNightMode);
		boolean isCheckNightMode = mPreferences.getBoolean(DaisyReaderConstants.NIGHT_MODE, false);
		toogleNightMode.setChecked(isCheckNightMode);
		toogleNightMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditor.putBoolean(DaisyReaderConstants.NIGHT_MODE, toogleNightMode.isChecked());
				mEditor.commit();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.title_activity_daisy_reader_setting),
				TextToSpeech.QUEUE_FLUSH, null);
		super.onResume();
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

	/**
	 * Show table of colors.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			LayoutInflater inflater = LayoutInflater.from(this);
			View dialogView1 = inflater.inflate(R.layout.dialog_selecter, null);

			GridView gridView = (GridView) dialogView1.findViewById(R.id.colorSelectGridView);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					view.setBackgroundColor(mColorTable[position]);
					return view;
				}
			};
			for (int i = 0; i < mColorTable.length; i++) {
				adapter.add("");
			}
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					mCurrentTextColor = mColorTable[arg2];
					mCurrentBackgroundColor = mColorTable[arg2];
					mCurrentHighlightColor = mColorTable[arg2];
					if (mChangeText) {
						mTextColor.setBackgroundColor(mCurrentTextColor);
						mEditor.putInt(DaisyReaderConstants.TEXT_COLOR, mCurrentTextColor);
					}
					if (mChangeBackground) {
						mBackgroundColor.setBackgroundColor(mCurrentBackgroundColor);
						mEditor.putInt(DaisyReaderConstants.BACKGROUND_COLOR,
								mCurrentBackgroundColor);
					}
					if (mChangeHighlight) {
						mHighlightColor.setBackgroundColor(mCurrentHighlightColor);
						mEditor.putInt(DaisyReaderConstants.HIGHLIGHT_COLOR, mCurrentHighlightColor);
					}
					mEditor.commit();
					dismissDialog(0);
				}
			});

			return new AlertDialog.Builder(this).setView(dialogView1)
					.setNegativeButton("Cancel", null).create();
		}
		return super.onCreateDialog(id);
	}

	private TextWatcher bookmarkTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String strEnteredVal = mNumberOfBookmarks.getText().toString();

			if (!strEnteredVal.equals("")) {
				int num = Integer.parseInt(strEnteredVal);
				if (num <= 0) {
					mNumberOfBookmarks.setText("");
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			int value = 1;
			if (mNumberOfBookmarks.getText() != null) {
				if (!mNumberOfBookmarks.getText().toString().equals("")) {
					value = Integer.valueOf(mNumberOfBookmarks.getText().toString());
				}
			}
			mEditor.putInt(DaisyReaderConstants.NUMBER_OF_BOOKMARKS, value);
			mEditor.commit();
		}
	};

	private TextWatcher recentBooksTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String strEnteredVal = mNumberOfRecentBooks.getText().toString();
			if (!strEnteredVal.equals("")) {
				int num = Integer.parseInt(strEnteredVal);
				if (num <= 0) {
					mNumberOfRecentBooks.setText("");
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			int value = 1;
			if (mNumberOfRecentBooks.getText() != null) {
				if (!mNumberOfRecentBooks.getText().toString().equals("")) {
					value = Integer.valueOf(mNumberOfRecentBooks.getText().toString());
				}
			}
			mEditor.putInt(DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS, value);
			mEditor.commit();
		}
	};

	private OnSeekBarChangeListener seekBarBrightnessListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// set the brightness of this window
			layoutpars.screenBrightness = mBrightness / (float) 255;
			mEditor.putInt(DaisyReaderConstants.BRIGHTNESS, mBrightness);
			mEditor.commit();
			// apply attribute changes to this window
			mWindow.setAttributes(layoutpars);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// sets brightness variable based on the progress bar
			mBrightness = progress + mDefaultBrightness;
		}
	};

	private OnSeekBarChangeListener seekBarSizeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mEditor.putInt(DaisyReaderConstants.FONT_SIZE, mFontsize);
			mEditor.commit();
			mFontSize.setTextSize(mFontsize);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			mFontsize = progress + mDefaultFontsize;
		}
	};

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub

	}
}
