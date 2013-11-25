package org.androiddaisyreader.apps;

import org.androiddaisyreader.base.DaisyEbookReaderBaseActivity;
import org.androiddaisyreader.utils.Constants;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.MenuItem;

/**
 * This activity is setting. It have some functions such as: change text color,
 * change highlight color, etc.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

@SuppressWarnings("deprecation")
public class DaisyReaderSettingActivity extends DaisyEbookReaderBaseActivity {

    private Window mWindow;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private TextView mFontSize;
    private TextView mTextColor;
    private TextView mBackgroundColor;
    private TextView mHighlightColor;
    private int mFontsize;
    // some basic colors
    private static final int COLOR_TALBE[] = { 0xffffffff, 0xffc0c0c0, 0xff808080, 0xff000000, 0xffffc0c0,
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
    private static final int DEFAULT_FONT_SIZE = 6;
    private static final int DEFAULT_BRIGHTNESS = 20;
    private static final int BRIGHT_BAR = 255;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPreferences = PreferenceManager
                .getDefaultSharedPreferences(DaisyReaderSettingActivity.this);
        mEditor = mPreferences.edit();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case android.R.id.home:
            onBackPressed();
            break;

        default:
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    /**
     * Setting seekbar brightness.
     */
    private void settingBrightness() {
        SeekBar brightBar = (SeekBar) findViewById(R.id.barBrightness);
        brightBar.setMax(BRIGHT_BAR);
        brightBar.setKeyProgressIncrement(1);
        ContentResolver contentResolver = getContentResolver();
        mWindow = getWindow();
        layoutpars = mWindow.getAttributes();
        try {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(DaisyReaderSettingActivity.this);
            mBrightness = preferences.getInt(Constants.BRIGHTNESS,
                    System.getInt(contentResolver, System.SCREEN_BRIGHTNESS));
            // sets the progress of the seek bar based on the system's
            // brightness
            brightBar.setProgress(mBrightness - DEFAULT_BRIGHTNESS);
            // set the brightness of this window
            layoutpars.screenBrightness = mBrightness / (float) BRIGHT_BAR;
            // apply attribute changes to this window
            mWindow.setAttributes(layoutpars);
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyReaderSettingActivity.this);
            ex.writeLogException();
        }
        // register OnSeekBarChangeListener, so it can actually change values
        brightBar.setOnSeekBarChangeListener(seekBarBrightnessListener);
    }

    /**
     * Setting seekbar font size.
     */
    private void settingFontsize() {
        final int maxFontsizeBar = 30;
        SeekBar sizeBar = (SeekBar) findViewById(R.id.barFontSize);
        mFontsize = mPreferences.getInt(Constants.FONT_SIZE, Constants.FONTSIZE_DEFAULT);
        mFontSize.setTextSize(mFontsize);
        sizeBar.setMax(maxFontsizeBar);
        sizeBar.setProgress(mFontsize - DEFAULT_FONT_SIZE);
        sizeBar.setOnSeekBarChangeListener(seekBarSizeListener);
    }

    /**
     * Setting text color.
     */
    private void settingTextColor() {
        final int lightGray = 0xffc0c0c0;
        mCurrentTextColor = mPreferences.getInt(Constants.TEXT_COLOR, lightGray);
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

    /**
     * Setting background color.
     */
    private void settingBackgroundColor() {
        mCurrentBackgroundColor = mPreferences.getInt(Constants.BACKGROUND_COLOR, Color.BLACK);
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

    /**
     * Setting highlight color.
     */
    private void settingHighlightColor() {
        mCurrentHighlightColor = mPreferences.getInt(Constants.HIGHLIGHT_COLOR, Color.YELLOW);
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

    /**
     * Setting number of bookmark.
     */
    private void settingCurrentBookmark() {
        int currentNumberOfBookmarks = mPreferences.getInt(Constants.NUMBER_OF_BOOKMARKS,
                Constants.NUMBER_OF_BOOKMARK_DEFAULT);
        mNumberOfBookmarks.setText(String.valueOf(currentNumberOfBookmarks));
        mNumberOfBookmarks.addTextChangedListener(bookmarkTextWatcher);
    }

    /**
     * Setting number of recent book
     */
    private void settingCurrentRecentBook() {
        int currentNumberOfRecentBooks = mPreferences.getInt(Constants.NUMBER_OF_RECENT_BOOKS,
                Constants.NUMBER_OF_RECENTBOOK_DEFAULT);
        mNumberOfRecentBooks.setText(String.valueOf(currentNumberOfRecentBooks));
        mNumberOfRecentBooks.addTextChangedListener(recentBooksTextWatcher);
    }

    /**
     * Setting night mode.
     */
    private void settingNightmode() {
        final ToggleButton toogleNightMode = (ToggleButton) findViewById(R.id.toggleNightMode);
        boolean isCheckNightMode = mPreferences.getBoolean(Constants.NIGHT_MODE, false);
        toogleNightMode.setChecked(isCheckNightMode);
        toogleNightMode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditor.putBoolean(Constants.NIGHT_MODE, toogleNightMode.isChecked());
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
        super.onResume();
        speakText(getString(R.string.title_activity_daisy_reader_setting));
    }

    @Override
    protected void onDestroy() {
        try {
            mTts.stop();
            mTts.shutdown();
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyReaderSettingActivity.this);
            ex.writeLogException();
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
                    view.setBackgroundColor(COLOR_TALBE[position]);
                    return view;
                }
            };
            int sizeOfColorTable = COLOR_TALBE.length;
            for (int i = 0; i < sizeOfColorTable; i++) {
                adapter.add("");
            }
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    mCurrentTextColor = COLOR_TALBE[arg2];
                    mCurrentBackgroundColor = COLOR_TALBE[arg2];
                    mCurrentHighlightColor = COLOR_TALBE[arg2];
                    if (mChangeText) {
                        mTextColor.setBackgroundColor(mCurrentTextColor);
                        mEditor.putInt(Constants.TEXT_COLOR, mCurrentTextColor);
                    }
                    if (mChangeBackground) {
                        mBackgroundColor.setBackgroundColor(mCurrentBackgroundColor);
                        mEditor.putInt(Constants.BACKGROUND_COLOR, mCurrentBackgroundColor);
                    }
                    if (mChangeHighlight) {
                        mHighlightColor.setBackgroundColor(mCurrentHighlightColor);
                        mEditor.putInt(Constants.HIGHLIGHT_COLOR, mCurrentHighlightColor);
                    }
                    mEditor.commit();
                    dismissDialog(0);
                }
            });

            return new AlertDialog.Builder(this).setView(dialogView1)
                    .setNegativeButton(getString(R.string.cancel_bookmark), null).create();
        }
        return super.onCreateDialog(id);
    }

    /**
     * Handle input value for bookmark
     */
    private TextWatcher bookmarkTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String strEnteredVal = "";
            if (mNumberOfBookmarks.getText() != null) {
                strEnteredVal = mNumberOfBookmarks.getText().toString();
            }

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
            Editable numberOfBookmarks = mNumberOfBookmarks.getText();
            if (numberOfBookmarks != null && !numberOfBookmarks.toString().equals("")) {
                value = Integer.valueOf(mNumberOfBookmarks.getText().toString());
            }
            mEditor.putInt(Constants.NUMBER_OF_BOOKMARKS, value);
            mEditor.commit();
        }
    };

    /**
     * Handle input value for recent book
     */
    private TextWatcher recentBooksTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String strEnteredVal = "";
            if (mNumberOfRecentBooks.getText() != null) {
                strEnteredVal = mNumberOfRecentBooks.getText().toString();
            }
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
            Editable numberOfRecentBooks = mNumberOfRecentBooks.getText();
            if (numberOfRecentBooks != null && !numberOfRecentBooks.toString().equals("")) {
                value = Integer.valueOf(mNumberOfRecentBooks.getText().toString());
            }
            mEditor.putInt(Constants.NUMBER_OF_RECENT_BOOKS, value);
            mEditor.commit();
        }
    };

    /**
     * Handle value seek bar brightness.
     */
    private OnSeekBarChangeListener seekBarBrightnessListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // set the brightness of this window
            layoutpars.screenBrightness = mBrightness / (float) BRIGHT_BAR;
            mEditor.putInt(Constants.BRIGHTNESS, mBrightness);
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
            mBrightness = progress + DEFAULT_BRIGHTNESS;
        }
    };

    /**
     * Handle value seek bar size of text.
     */
    private OnSeekBarChangeListener seekBarSizeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mEditor.putInt(Constants.FONT_SIZE, mFontsize);
            mEditor.commit();
            mFontSize.setTextSize(mFontsize);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mFontsize = progress + DEFAULT_FONT_SIZE;
        }
    };

}
