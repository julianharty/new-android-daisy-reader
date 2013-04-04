package org.androiddaisyreader.apps;

import org.androiddaisyreader.utils.DaisyReaderConstants;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Color;
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

@SuppressWarnings("deprecation")
public class DaisyReaderSettingActivity extends Activity {
	private SeekBar brightbar;
	private SeekBar sizebar;
	private int brightness;
	private ContentResolver cResolver;
	private Window window;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private TextView tv;
	private int fontsize;
	// some basic colors
	private int COLOR_TABLE[] = { 0xffffffff, 0xffc0c0c0, 0xff808080,
			0xff000000, 0xffffc0c0, 0xffff6060, 0xffff0000, 0xff800000,
			0xffffe0c0, 0xffffb060, 0xffff8000, 0xff804000, 0xffffffc0,
			0xffffff60, 0xffffff00, 0xff808000, 0xffe0ffc0, 0xffb0ff60,
			0xff80ff00, 0xff408000, 0xffc0ffc0, 0xff60ff60, 0xff00ff00,
			0xff008000, 0xffc0ffe0, 0xff60ffb0, 0xff00ff80, 0xff008040,
			0xffc0ffff, 0xff60ffff, 0xff00ffff, 0xff008080, 0xffc0e0ff,
			0xff60b0ff, 0xff0080ff, 0xff004480, 0xffc0c0ff, 0xff6060ff,
			0xff0000ff, 0xff000080, 0xffe0c0ff, 0xffb060ff, 0xff8000ff,
			0xff400080, 0xffffc0ff, 0xffff60ff, 0xffff00ff, 0xff800080,
			0xffffc0e0, 0xffff60b0, 0xffff0080, 0xff800040 };
	private TextView tvTextColor;
	private int currentTextColor;
	private TextView tvBackgroundColor;
	private int currentBackgroundColor;
	private TextView tvHighlightColor;
	private int currentHighlightColor;
	private Boolean changeText;
	private Boolean changeBackground;
	private Boolean changeHighlight;
	private EditText edtNumberOfRecentBooks;
	private int currentNumberOfRecentBooks;
	private EditText edtNumberOfBookmarks;
	private int currentNumberOfBookmarks;
	private ToggleButton toogleNightMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_setting);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = preferences.edit();
		brightbar = (SeekBar) findViewById(R.id.barBrightness);
		cResolver = getContentResolver();
		window = getWindow();
		brightbar.setMax(255);
		brightbar.setKeyProgressIncrement(1);
		try {
			// get the current system brightness
			brightness = preferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		// sets the progress of the seek bar based on the system's brightness
		brightbar.setProgress(brightness);
		// register OnSeekBarChangeListener, so it can actually change values
		brightbar.setOnSeekBarChangeListener(seekBarBrightnessListener);
		tv = (TextView) findViewById(R.id.tvFontSize);
		sizebar = (SeekBar) findViewById(R.id.barFontSize);
		fontsize = preferences.getInt(DaisyReaderConstants.FONT_SIZE, 12);
		tv.setTextSize(fontsize);
		sizebar.setMax(30);
		sizebar.setKeyProgressIncrement(1);
		sizebar.setProgress(fontsize);
		sizebar.setOnSeekBarChangeListener(seekBarSizeListener);

		tvTextColor = (TextView) findViewById(R.id.tvTextColor);

		// setting text color
		currentTextColor = preferences.getInt(DaisyReaderConstants.TEXT_COLOR,
				tvTextColor.getCurrentTextColor());
		tvTextColor.setBackgroundColor(currentTextColor);
		tvTextColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeHighlight = false;
				changeBackground = false;
				changeText = true;
				showDialog(0);
			}
		});

		// setting background color
		tvBackgroundColor = (TextView) findViewById(R.id.tvBackgroundColor);
		currentBackgroundColor = preferences.getInt(
				DaisyReaderConstants.BACKGROUND_COLOR, Color.BLACK);
		tvBackgroundColor.setBackgroundColor(currentBackgroundColor);
		tvBackgroundColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeBackground = true;
				changeText = false;
				changeHighlight = false;
				showDialog(0);
			}
		});

		// setting current highlight color
		tvHighlightColor = (TextView) findViewById(R.id.tvHighlightColor);
		currentHighlightColor = preferences.getInt(
				DaisyReaderConstants.HIGHLIGHT_COLOR, Color.YELLOW);
		tvHighlightColor.setBackgroundColor(currentHighlightColor);
		tvHighlightColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeBackground = false;
				changeText = false;
				changeHighlight = true;
				showDialog(0);
			}
		});

		// setting current number of rencent books
		edtNumberOfRecentBooks = (EditText) findViewById(R.id.edtNumberOfRecentBooks);
		currentNumberOfRecentBooks = preferences.getInt(
				DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS, 3);
		edtNumberOfRecentBooks.setText(String
				.valueOf(currentNumberOfRecentBooks));
		
		// setting current number of bookmarks
		edtNumberOfBookmarks= (EditText) findViewById(R.id.edtNumberOfBookmarks);
		currentNumberOfBookmarks = preferences.getInt(
				DaisyReaderConstants.NUMBER_OF_BOOKMARKS, 3);
		edtNumberOfBookmarks.setText(String
				.valueOf(currentNumberOfBookmarks));
		
		// setting night mode
		toogleNightMode = (ToggleButton)findViewById(R.id.toggleNightMode);
		toogleNightMode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(toogleNightMode.isChecked())
				{
					editor.putBoolean(DaisyReaderConstants.NIGHT_MODE,
							true);
					editor.commit();
				}
				
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		if (edtNumberOfRecentBooks.getText() != null) {
			editor.putInt(DaisyReaderConstants.NUMBER_OF_RECENT_BOOKS, Integer
					.valueOf(edtNumberOfRecentBooks.getText().toString()));
			editor.commit();
		}
		
		if (edtNumberOfBookmarks.getText() != null) {
			editor.putInt(DaisyReaderConstants.NUMBER_OF_BOOKMARKS, Integer
					.valueOf(edtNumberOfBookmarks.getText().toString()));
			editor.commit();
		}
		super.onBackPressed();
	}

	/**
	 * Show table of colors.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			LayoutInflater inflater1 = LayoutInflater.from(this);
			final View dialogView1 = inflater1.inflate(
					R.layout.dialog_selecter, null);

			final GridView gridView = (GridView) dialogView1
					.findViewById(R.id.colorSelectGridView);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					view.setBackgroundColor(COLOR_TABLE[position]);
					return view;
				}
			};
			for (int i = 0; i < COLOR_TABLE.length; i++) {
				adapter.add("");
			}
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					currentTextColor = COLOR_TABLE[arg2];
					currentBackgroundColor = COLOR_TABLE[arg2];
					currentHighlightColor = COLOR_TABLE[arg2];
					if (changeText) {
						tvTextColor.setBackgroundColor(currentTextColor);
						editor.putInt(DaisyReaderConstants.TEXT_COLOR,
								currentTextColor);
					}
					if (changeBackground) {
						tvBackgroundColor
								.setBackgroundColor(currentBackgroundColor);
						editor.putInt(DaisyReaderConstants.BACKGROUND_COLOR,
								currentBackgroundColor);
					}
					if (changeHighlight) {
						tvHighlightColor
								.setBackgroundColor(currentHighlightColor);
						editor.putInt(DaisyReaderConstants.HIGHLIGHT_COLOR,
								currentHighlightColor);
					}
					editor.commit();
					dismissDialog(0);
				}
			});

			return new AlertDialog.Builder(this).setView(dialogView1)
					.setNegativeButton("Cancel", null).create();
		}
		return super.onCreateDialog(id);
	}

	OnSeekBarChangeListener seekBarBrightnessListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			LayoutParams layoutpars = window.getAttributes();
			// set the brightness of this window
			layoutpars.screenBrightness = brightness / (float) 255;
			editor.putInt(DaisyReaderConstants.BRIGHTNESS, brightness);
			editor.commit();
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (progress <= 20) {
				// set the brightness to 20
				brightness = 20;
			} else {
				// sets brightness variable based on the progress bar
				brightness = progress;
			}

		}
	};

	OnSeekBarChangeListener seekBarSizeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			fontsize = progress;
			tv.setTextSize(fontsize);
			editor.putInt(DaisyReaderConstants.FONT_SIZE, progress);
			editor.commit();
		}
	};
}
