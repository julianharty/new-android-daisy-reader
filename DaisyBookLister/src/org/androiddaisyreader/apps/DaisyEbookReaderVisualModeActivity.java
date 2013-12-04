package org.androiddaisyreader.apps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.base.DaisyEbookReaderBaseActivity;
import org.androiddaisyreader.base.DaisyEbookReaderBaseMode;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.player.AndroidAudioPlayer;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.common.base.Preconditions;

/**
 * This activity is visual mode which play audio and show full text.
 * 
 * @author LogiGear
 * @date 2013.03.05 s
 */

public class DaisyEbookReaderVisualModeActivity extends DaisyEbookReaderBaseActivity {

    private boolean mIsFirstNext = false;
    private boolean mIsFirstPrevious = true;
    private DaisyBook mBook;
    private Navigator mNavigator;
    private Navigator mNavigatorOfTableContents;
    private NavigationListener mNavigationListener = new NavigationListener();
    private Controller mController = new Controller(mNavigationListener);
    private AudioPlayerController mAudioPlayer;
    private MediaPlayer mPlayer;
    private TextView mContents;
    private ImageButton mImgButton;
    private IntentController mIntentController;
    private ScrollView mScrollView;
    private Spannable mWordtoSpan;
    private Runnable mRunnalbe;
    private Handler mHandler;
    private List<String> mListStringText;
    private List<Integer> mListTimeBegin;
    private List<Integer> mListTimeEnd;
    private List<Integer> mListValueScroll;
    private List<Integer> mListValueLine;
    private String mPath;
    private String mFullTextOfBook;
    private int mTime;
    private int mTotalLineOnScreen;
    private int mNumberOfChar;
    private int mPositionOfScrollView;
    private int mHighlightColor;
    private int mStartOfSentence = 0;
    private int mPositionSection = 0;
    private int mPositionSentence = 0;
    private static final int TIME_FOR_PROCESS = 400;
    private long mLastClickTime = 0;
    private boolean mIsRunable = true;
    // if audio is over, mIsEndOf will equal true.
    private boolean mIsEndOf = false;
    // This variable will help to find chapter has audio files or not.
    private boolean mIsFound = true;
    private boolean mIsPlaying = false;
    private SQLiteCurrentInformationHelper mSql;
    private CurrentInformation mCurrent;
    private boolean isFormat202 = false;
    private List<Audio> listAudio;
    private int countAudio = 0;

    private Map<String, List<Integer>> mHashMapBegin;
    private Map<String, List<Integer>> mHashMapEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daisy_ebook_reader_visual_mode);
        mIntentController = new IntentController(this);
        mSql = new SQLiteCurrentInformationHelper(DaisyEbookReaderVisualModeActivity.this);
        mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
        isFormat202 = DaisyBookUtil.findDaisyFormat(mPath) == Constants.DAISY_202_FORMAT;
        mContents = (TextView) this.findViewById(R.id.contents);
        openBook();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mBook != null) {
            getSupportActionBar().setTitle(mBook.getTitle());
            mScrollView = (ScrollView) findViewById(R.id.scrollView);
            mImgButton = (ImageButton) this.findViewById(R.id.btnPlay);
            mImgButton.setOnClickListener(imgButtonClick);
            mHandler = new Handler();
            setEventForNavigationButtons();
            // check if user play daisybook from table of contents or bookmark
            readBook();
        } else {
            mIntentController.pushToDialog(
                    String.format(getString(R.string.error_no_path_found), mPath),
                    getString(R.string.error_title), R.raw.error, true, false, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int order = 1;
        SubMenu subMenu = menu.addSubMenu(0, Constants.SUBMENU_MENU, order++, R.string.menu_title);

        subMenu.add(0, Constants.SUBMENU_LIBRARY, order++, R.string.submenu_library).setIcon(
                R.raw.library);

        subMenu.add(0, Constants.SUBMENU_BOOKMARKS, order++, R.string.submenu_bookmarks).setIcon(
                R.raw.bookmark);

        subMenu.add(0, Constants.SUBMENU_TABLE_OF_CONTENTS, order++,
                R.string.submenu_table_of_contents).setIcon(R.raw.table_of_contents);

        subMenu.add(0, Constants.SUBMENU_SIMPLE_MODE, order++, R.string.submenu_simple_mode)
                .setIcon(R.raw.simple_mode);

        subMenu.add(0, Constants.SUBMENU_SEARCH, order++, R.string.submenu_search).setIcon(
                R.raw.search);

        subMenu.add(0, Constants.SUBMENU_SETTINGS, order++, R.string.submenu_settings).setIcon(
                R.raw.settings);

        MenuItem subMenuItem = subMenu.getItem();
        subMenuItem.setIcon(R.raw.ic_menu_32x32);
        subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected Identify single menu
     * item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != Constants.SUBMENU_MENU) {
            mIsPlaying = mPlayer.isPlaying();
            if (mCurrent != null) {
                mCurrent.setPlaying(mIsPlaying);
            }
            if (mIsPlaying) {
                setMediaPause();
            }
        }

        switch (item.getItemId()) {
        // go to table of contents
        case Constants.SUBMENU_TABLE_OF_CONTENTS:
            pushToTableOfContents();
            return true;
            // go to simple mode
        case Constants.SUBMENU_SIMPLE_MODE:
            pushToSimpleMode();
            return true;
            // go to settings
        case Constants.SUBMENU_SETTINGS:
            pushToSettings();
            return true;
            // go to book marks
        case Constants.SUBMENU_BOOKMARKS:
            pushToBookmark();
            return true;
            // go to library
        case Constants.SUBMENU_LIBRARY:
            mIntentController.pushToLibraryIntent();
            return true;
            // go to search
        case Constants.SUBMENU_SEARCH:
            pushToDialogSearch();
            return true;
            // back to previous screen
        case android.R.id.home:
            onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Push to settings.
     */
    private void pushToSettings() {
        handleCurrentInformation(mCurrent);
        mIntentController.pushToDaisyReaderSettingIntent();
    }

    /**
     * Push to simple mode.
     */
    private void pushToSimpleMode() {
        handleCurrentInformation(mCurrent);
        mIntentController.pushToDaisyEbookReaderSimpleModeIntent(getIntent().getStringExtra(
                Constants.DAISY_PATH));
    }

    /**
     * Set event for bottom buttons (next sentence, next section, previous
     * sentence, previous section).
     */
    private void setEventForNavigationButtons() {
        // Event for buttons on navigation.
        ImageButton btnNextSection = (ImageButton) this.findViewById(R.id.btnNextSection);
        btnNextSection.setOnClickListener(btnNextSectionClick);
        ImageButton btnNextSentence = (ImageButton) this.findViewById(R.id.btnNextSentence);
        btnNextSentence.setOnClickListener(btnNextSentenceClick);
        ImageButton btnPreviousSection = (ImageButton) this.findViewById(R.id.btnPreviousSection);
        btnPreviousSection.setOnClickListener(btnPreviousSectionClick);
        ImageButton btnPreviousSentence = (ImageButton) this.findViewById(R.id.btnPreviousSentence);
        btnPreviousSentence.setOnClickListener(btnPreviousSentenceClick);
    }

    /**
     * Start reading book.
     */
    private void readBook() {
        String section;
        mCurrent = mSql.getCurrentInformation();
        String audioFileName = "";
        try {
            if (mCurrent != null
                    && mCurrent.getActivity().equals(
                            getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
                mCurrent.setAtTheEnd(false);
                mSql.updateCurrentInformation(mCurrent);
            }
            if (mCurrent != null
                    && !mCurrent.getActivity().equals(
                            getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
                section = String.valueOf(mCurrent.getSection());
                mTime = mCurrent.getTime();
                audioFileName = mCurrent.getAudioName();
                mPositionSentence = 0;
            } else {
                section = getIntent().getStringExtra(Constants.POSITION_SECTION);
                mTime = getIntent().getIntExtra(Constants.TIME, -1);
                if (!isFormat202) {
                    audioFileName = getIntent().getStringExtra(Constants.AUDIO_FILE_NAME);
                }
            }
            if (section != null) {
                int countLoop = Integer.valueOf(section) - mPositionSection;
                Navigable n = getNavigable(countLoop);
                if (n instanceof Section) {
                    mNavigationListener.onNext((Section) n);
                }
                // Bookmark for daisy 3.0
                if (!isFormat202 && listAudio != null) {
                    for (int i = 0; i < listAudio.size(); i++) {
                        Audio audio = listAudio.get(i);
                        if (audio.getAudioFilename().equals(audioFileName)) {
                            countAudio = i;
                            mAudioPlayer.playFileSegment(audio);
                            break;
                        }
                    }
                    // seek to time when user loading from book mark.
                    if (mTime != -1) {
                        mPlayer.seekTo(mTime);
                        mTime = -1;
                    }

                    // get status of audio
                    if (mCurrent != null) {
                        mSql.updateCurrentInformation(mCurrent);
                        if (mCurrent.getPlaying()) {
                            setMediaPlay();
                        } else {
                            setMediaPause();
                        }
                    }
                }

            } else {
                togglePlay();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
    }

    /**
     * Get current section which user want to play.
     * 
     * @param countLoop
     * @return Navigable
     */
    private Navigable getNavigable(int countLoop) {
        Navigable n = null;
        // case 1: variable > 0, user want to next section.
        if (countLoop > 0) {
            n = nextSectionByCountLoop(countLoop);
        }
        // case 2: variable > 0, user want to go to sentence.
        else if (countLoop == 0 && isFormat202) {
            // Fix bug: Audio is not read the contents after switch from simple
            // mode to visual mode
            n = mNavigator.previous();
            n = mNavigator.next();
        }
        // case 3: variable < 0, user want to previous section.
        else {
            n = previousSectionByCountLoop(-countLoop);
        }
        return n;
    }

    /**
     * Go to the exactly section when daisy book was loaded from bookmark or
     * table of contents.
     * 
     * @param countLoop
     * @return Navigable
     */
    private Navigable nextSectionByCountLoop(int countLoop) {
        Navigable n = null;
        try {
            for (int j = 0; j < countLoop; j++) {
                n = mNavigator.next();
                if (mCurrent != null
                        && mCurrent.getActivity().equals(
                                getString(R.string.title_activity_daisy_ebook_reader_simple_mode))
                        && mIsFirstNext) {
                    n = mNavigator.next();
                    mIsFirstPrevious = true;
                    mIsFirstNext = false;
                    mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
                    mSql.updateCurrentInformation(mCurrent);
                }
                mPositionSection += 1;
            }

        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
        return n;
    }

    /**
     * Go to the exactly section when daisy book was loaded from bookmark or
     * table of contents.
     * 
     * @param countLoop
     * @return Navigable
     */
    private Navigable previousSectionByCountLoop(int countLoop) {
        Navigable n = null;
        for (int j = 0; j < countLoop; j++) {
            n = mNavigator.previous();
            {
                if (mCurrent != null
                        && mCurrent.getActivity().equals(
                                getString(R.string.title_activity_daisy_ebook_reader_simple_mode))) {
                    n = mNavigator.previous();
                    mIsFirstPrevious = false;
                    mIsFirstNext = true;
                    mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
                    mSql.updateCurrentInformation(mCurrent);
                }
            }
            mPositionSection -= 1;
        }
        return n;
    }

    private OnClickListener imgButtonClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            togglePlay();
        }
    };

    /**
     * Push to table of contents.
     */
    private void pushToTableOfContents() {
        handleCurrentInformation(mCurrent);
        mIntentController.pushToTableOfContentsIntent(mPath, mNavigatorOfTableContents,
                getString(R.string.visual_mode));
    }

    /**
     * Push to bookmark.
     */
    private void pushToBookmark() {
        if (mIsEndOf) {
            mIntentController.pushToDialog(
                    String.format(this.getString(R.string.error_save_bookmark), mBook.getTitle()),
                    this.getString(R.string.error_title), R.raw.error, false, false, null);
        } else {
            handleCurrentInformation(mCurrent);
            mIntentController.pushToDaisyReaderBookmarkIntent(getBookmark(), getIntent()
                    .getStringExtra(Constants.DAISY_PATH));
        }
    }

    /**
     * get Bookmark to support for function save or load bookmark
     * 
     * @return Bookmark
     */
    private Bookmark getBookmark() {
        String sentence = null;
        int currentTime = mPlayer.getCurrentPosition();
        int i = 0;
        if (mPlayer.isPlaying()) {
            setMediaPause();
        }
        if (mListStringText != null) {
            int sizeOfStringText = mListStringText.size();
            for (; i < sizeOfStringText; i++) {
                if (mListTimeBegin.get(i) <= currentTime && currentTime < mListTimeEnd.get(i)) {
                    sentence = mListStringText.get(i);
                    break;
                }
            }
            // fix bug: chapter does not support audio and contents.
            if (sentence != null && sentence.length() <= 0 && mListStringText.size() > 1) {
                sentence = mListStringText.get(i + 1);
            } else if (sentence != null && sentence.length() <= 0) {
                sentence = " ";
            }
        }
        Bookmark bookmark = null;
        if (!isFormat202 && listAudio != null) {
            bookmark = new Bookmark(listAudio.get(countAudio).getAudioFilename(), mPath, sentence,
                    currentTime, mPositionSection, 0, "");
        } else {
            bookmark = new Bookmark("", mPath, sentence, currentTime, mPositionSection, 0, "");
        }
        return bookmark;
    }

    @Override
    public void onBackPressed() {
        if (mBook != null) {
            mIsPlaying = mPlayer.isPlaying();
            if (mIsPlaying) {
                setMediaPause();
            }
            super.onBackPressed();
            handleCurrentInformation(mCurrent);
            finish();
        } else {
            super.onBackPressed();
        }

    }

    private void handleCurrentInformation(CurrentInformation current) {
        DaisyEbookReaderBaseMode baseMode = new DaisyEbookReaderBaseMode(mPath,
                DaisyEbookReaderVisualModeActivity.this);
        CurrentInformation currentInformation;
        String audioName = "";
        if (!isFormat202 && listAudio != null) {
            audioName = listAudio.get(countAudio).getAudioFilename();
        }
        String activity = getString(R.string.title_activity_daisy_ebook_reader_visual_mode);
        if (current == null) {
            currentInformation = baseMode.createCurrentInformation(audioName, activity,
                    mPositionSection, mPlayer.getCurrentPosition(), mIsPlaying);
            mSql.addCurrentInformation(currentInformation);
        } else {
            currentInformation = baseMode.updateCurrentInformation(current, audioName, activity,
                    mPositionSection, mPositionSentence, mPlayer.getCurrentPosition(), mIsPlaying);
            mSql.updateCurrentInformation(currentInformation);
        }
    }

    /**
     * Show dialog when user choose function search of button settings.
     */
    private void pushToDialogSearch() {
        final Dialog dialog = new Dialog(DaisyEbookReaderVisualModeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search);
        // set the custom dialog components - text, image and button
        final EditText searchText = (EditText) dialog.findViewById(R.id.searchText);
        Button dialogButton = (Button) dialog.findViewById(R.id.buttonSearch);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String ett = searchText.getText().toString();
                if (ett.trim().length() > 0) {
                    String tvt = mContents.getText().toString();
                    int ofe = tvt.indexOf(ett, 0);
                    Spannable wordtoSpan = new SpannableString(mContents.getText());
                    for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                        ofe = tvt.indexOf(ett, ofs);
                        if (ofe == -1) {
                            break;
                        } else {
                            wordtoSpan.setSpan(new BackgroundColorSpan(mHighlightColor), ofe, ofe
                                    + ett.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            mContents.setText(wordtoSpan, TextView.BufferType.SPANNABLE);
                        }
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            mHandler.removeCallbacks(mRunnalbe);
            if (mTts != null) {
                mTts.shutdown();
            }

        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        speakText(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
        if (mBook != null) {
            getValueFromSetting();
            setNightMode();
            mNavigatorOfTableContents = new Navigator(mBook);
        }

        getStatusOfAudio();
    }

    /**
     * Handle current information.
     */
    private void getStatusOfAudio() {
        mCurrent = mSql.getCurrentInformation();
        if (mCurrent != null) {
            if (mCurrent.getPlaying()) {
                setMediaPlay();
            } else {
                setMediaPause();
            }
            if (!mCurrent.getActivity().equals(
                    getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
                readBook();
            }
        }
    }

    /**
     * get values from setting activity to apply.
     */
    private void getValueFromSetting() {
        final int numberToConvert = 255;
        int mFontSize;
        ContentResolver cResolver = getContentResolver();
        int valueScreen = 0;
        try {
            Window window = getWindow();
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(DaisyEbookReaderVisualModeActivity.this);
            valueScreen = preferences.getInt(Constants.BRIGHTNESS,
                    System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
            LayoutParams layoutpars = window.getAttributes();
            layoutpars.screenBrightness = valueScreen / (float) numberToConvert;
            // apply attribute changes to this window
            window.setAttributes(layoutpars);
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(DaisyEbookReaderVisualModeActivity.this);
        mFontSize = preferences.getInt(Constants.FONT_SIZE, Constants.FONTSIZE_DEFAULT);
        mContents.setTextSize(mFontSize);
    }

    /**
     * Apply night mode setting, if user turn on.
     */
    private void setNightMode() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(DaisyEbookReaderVisualModeActivity.this);
        boolean nightMode = preferences.getBoolean(Constants.NIGHT_MODE, false);
        final int nightModeColor = 0xff408000;
        final int nightModeText = 0xffc0c0c0;
        if (nightMode) {
            mContents.setTextColor(Color.WHITE);
            mScrollView.setBackgroundColor(Color.BLACK);
            mHighlightColor = nightModeColor;
        } else {
            // apply text color
            int textColor = preferences.getInt(Constants.TEXT_COLOR, nightModeText);
            mContents.setTextColor(textColor);

            // apply background color
            int backgroundColor = preferences.getInt(Constants.BACKGROUND_COLOR, Color.BLACK);
            mScrollView.setBackgroundColor(backgroundColor);

            // apply highlight color
            mHighlightColor = preferences.getInt(Constants.HIGHLIGHT_COLOR, Color.YELLOW);
        }

    }

    private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

        public void endOfAudio() {
            Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
            if (!isFormat202 && listAudio != null && listAudio.size() - 1 > countAudio) {
                countAudio = countAudio + 1;
                mAudioPlayer.playFileSegment(listAudio.get(countAudio));
            } else if (!mIsEndOf && mIsFound) {
                mController.next();
            }
        }
    };

    private List<String> listId;

    /**
     * open book from path.
     */
    private void openBook() {
        DaisyEbookReaderBaseMode baseMode = new DaisyEbookReaderBaseMode(mPath,
                DaisyEbookReaderVisualModeActivity.this);
        try {
            if (isFormat202) {
                mBook = baseMode.openBook202();
                if (!mBook.hasTotalTime()) {
                    mIntentController.pushToDialog(getString(R.string.error_wrong_format_audio),
                            getString(R.string.error_title), R.raw.error, false, false, null);
                }
            } else {
                mBook = baseMode.openBook30();
                mPath = baseMode.getPathExactlyDaisy30(mPath);
                Navigator temp = new Navigator(mBook);
                listId = new ArrayList<String>();
                while (temp.hasNext()) {
                    Section n = (Section) temp.next();
                    listId.add(splitHref(n.getHref())[1]);
                }
            }

            AndroidAudioPlayer androidAudioPlayer = new AndroidAudioPlayer(
                    baseMode.getBookContext(mPath));
            androidAudioPlayer.addCallbackListener(audioCallbackListener);
            mAudioPlayer = new AudioPlayerController(androidAudioPlayer);
            mPlayer = androidAudioPlayer.getCurrentPlayer();
            // get all navigator of book to push to table of contents.
            mNavigatorOfTableContents = new Navigator(mBook);
            mNavigator = mNavigatorOfTableContents;
        } catch (PrivateException e) {
            if (!isFinishing()) {
                e.showDialogException(mIntentController);
            }
        }
    }

    /**
     * Split href.
     * 
     * @param href the href
     * @return the string[]
     */
    private String[] splitHref(String href) {
        return href.split("#");
    }

    /**
     * Listens to Navigation Events.
     * 
     * @author Julian Harty
     */
    private class NavigationListener {
        public void onNext(Section section) {
            try {
                // create some values to support to highlight text.
                prepare();
                // If file or audio is not found, audio must to change status to
                // pause
                if (!mIsFound) {
                    setMediaPause();
                    mIsFound = true;
                }
                if (isFormat202) {
                    getSnippetAndAudioForDaisy202(section);
                } else {
                    getSnippetAndAudioForDaisy30(section);
                }

                // seek to time when user loading from book mark.
                if (isFormat202 && mTime != -1) {
                    mPlayer.seekTo(mTime);
                    mTime = -1;
                }

                if (mCurrent != null) {
                    mSql.updateCurrentInformation(mCurrent);
                    if (mCurrent.getPlaying()) {
                        setMediaPlay();
                    } else {
                        setMediaPause();
                    }
                } else {
                    setMediaPlay();
                }
                autoHighlightAndScroll();
            } catch (PrivateException e) {
                if (!isFinishing()) {
                    e.showDialogException(mIntentController);
                }
            }
        }

        /**
         * Gets the snippet and audio for daisy202.
         * 
         * @param section the section
         */
        private void getSnippetAndAudioForDaisy202(Section section) throws PrivateException {
            DaisyEbookReaderBaseMode baseMode = new DaisyEbookReaderBaseMode(mPath,
                    DaisyEbookReaderVisualModeActivity.this);
            try {
                Part[] parts = baseMode.getPartsFromSection(section, mPath, isFormat202);
                getSnippetsOfCurrentSection(parts);
                getAudioElementsOfCurrentSectionForDaisy202(parts);
            } catch (PrivateException e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this, mPath);
                throw ex;
            }
        }

        /**
         * Gets the snippet and audio for daisy30.
         * 
         * @param section the section
         */
        private void getSnippetAndAudioForDaisy30(Section section) throws PrivateException {
            Part[] parts = null;
            DaisyEbookReaderBaseMode baseMode = new DaisyEbookReaderBaseMode(mPath,
                    DaisyEbookReaderVisualModeActivity.this);
            try {
                parts = baseMode.getPartsFromSectionDaisy30(section, mPath, isFormat202, listId,
                        mPositionSection);
                getSnippetsOfCurrentSection(parts);
                getAudioElementsOfCurrentSectionForDaisy30(parts);
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this, mPath);
                throw ex;
            }
        }

        /**
         * Create some values to support to highlight text
         */
        private void prepare() {
            mTotalLineOnScreen = 0;
            mNumberOfChar = 0;
            mPositionOfScrollView = 0;
            mListStringText = new ArrayList<String>();
            mListTimeBegin = new ArrayList<Integer>();
            mListTimeEnd = new ArrayList<Integer>();
            mListValueScroll = new ArrayList<Integer>();
            mListValueScroll.add(mPositionOfScrollView);
            mListValueLine = new ArrayList<Integer>();
            mListValueLine.add(mTotalLineOnScreen);

            mHashMapBegin = new LinkedHashMap<String, List<Integer>>();
            mHashMapEnd = new LinkedHashMap<String, List<Integer>>();
        }

        /**
         * Get all text from parts.
         * 
         * @param parts
         */
        private void getSnippetsOfCurrentSection(Part[] parts) {
            StringBuilder snippetText = new StringBuilder();
            List<Integer> listClipBegin = new ArrayList<Integer>();
            List<Integer> listClipEnd = new ArrayList<Integer>();
            String fileName = null;
            try {
                for (Part part : parts) {
                    int sizeOfPart = part.getSnippets().size();
                    for (int i = 0; i < sizeOfPart; i++) {
                        if (i > 0) {
                            snippetText.append(getString(R.string.space));
                        }
                        String text = part.getSnippets().get(i).getText().toString();
                        snippetText.append(text);
                        mListStringText.add(text);
                    }
                    snippetText.append(getString(R.string.space));
                    List<Audio> audioElements = part.getAudioElements();
                    if (audioElements.size() > 0) {
                        mListTimeBegin.add(audioElements.get(0).getClipBegin());
                        mListTimeEnd.add(audioElements.get(audioElements.size() - 1).getClipEnd());
                    }

                    int audioElementsSize = audioElements.size();
                    if (audioElementsSize > 0) {
                        Audio audio = audioElements.get(0);
                        if (fileName == null || !fileName.equals(audio.getAudioFilename())) {
                            mHashMapBegin.put(fileName, listClipBegin);
                            mHashMapEnd.put(fileName, listClipEnd);
                            listClipBegin = new ArrayList<Integer>();
                            listClipEnd = new ArrayList<Integer>();
                            fileName = audio.getAudioFilename();
                        }
                        listClipBegin.add(audio.getClipBegin());
                        listClipEnd.add(audioElements.get(audioElementsSize - 1).getClipEnd());
                    }
                }
                mHashMapBegin.put(fileName, listClipBegin);
                mHashMapEnd.put(fileName, listClipEnd);
                mContents.setText(snippetText.toString(), TextView.BufferType.SPANNABLE);
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this);
                ex.writeLogException();
            }

        }

        /**
         * Get all audio from parts.
         * 
         * @param parts
         */
        private void getAudioElementsOfCurrentSectionForDaisy202(Part[] parts) {
            try {
                for (Part part : parts) {
                    for (Audio audioSegment : part.getAudioElements()) {
                        mAudioPlayer.playFileSegment(audioSegment);
                    }
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this, mPath);
                if (!isFinishing()) {
                    ex.showDialogException(mIntentController);
                }
                mIsFound = false;
                mImgButton.setImageResource(R.raw.media_play);
            }
        }

        /**
         * Gets the audio elements of current section for daisy30.
         * 
         * @param parts the parts
         * @return the audio elements of current section for daisy30
         */
        private void getAudioElementsOfCurrentSectionForDaisy30(final Part[] parts) {
            try {
                String audiFileName = "";
                countAudio = 0;
                listAudio = new ArrayList<Audio>();
                for (Part part : parts) {
                    if (part.getAudioElements().size() > 0) {
                        Audio audioSegment = part.getAudioElements().get(0);
                        if (!audioSegment.getAudioFilename().equals(audiFileName)) {
                            listAudio.add(audioSegment);
                            audiFileName = audioSegment.getAudioFilename();
                        }
                    }
                }
                if (listAudio.size() > 0) {
                    mAudioPlayer.playFileSegment(listAudio.get(0));
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this, mPath);
                if (!isFinishing()) {
                    ex.showDialogException(mIntentController);
                }
                mIsFound = false;
                mImgButton.setImageResource(R.raw.media_play);
            }
        }

        /**
         * Show message at the end of book.
         */
        private void atEndOfBook() {
            mIntentController.pushToDialog(getString(R.string.atEnd) + getString(R.string.space)
                    + mBook.getTitle(), getString(R.string.error_title), R.raw.error, false, false,
                    null);
            int currentTime = mPlayer.getCurrentPosition();
            if (currentTime == -1 || currentTime == mPlayer.getDuration() || currentTime == 0) {
                mIsRunable = false;
                mIsEndOf = true;
                mWordtoSpan.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 0, mContents
                        .getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mContents.setText(mWordtoSpan);
                mImgButton.setImageResource(R.raw.media_play);
            }

            if (mCurrent != null) {
                mCurrent.setAtTheEnd(mIsEndOf);
                mSql.updateCurrentInformation(mCurrent);
            }

        }

        /**
         * Show message at the begin of book.
         */
        public void atBeginOfBook() {
            mIntentController.pushToDialog(getString(R.string.atBegin) + getString(R.string.space)
                    + mBook.getTitle(), getString(R.string.error_title), R.raw.error, false, false,
                    null);
        }
    }

    /**
     * Auto highlight text while audio is playing. Auto scroll when the high
     * light at the end of screen
     */
    private void autoHighlightAndScroll() {
        mWordtoSpan = (Spannable) mContents.getText();
        mRunnalbe = new Runnable() {

            @Override
            public void run() {
                try {
                    if (mIsRunable) {
                        if (mScrollView.getScrollY() != mPositionOfScrollView) {
                            mScrollView.scrollTo(0, mPositionOfScrollView);
                        }
                        autoHighlight();
                        autoScroll();
                    }
                    setIsRunable();
                    if (mTimePause == 0) {
                        int timeReadSentence = mListTimeEnd.get(mPositionSentence)
                                - mListTimeBegin.get(mPositionSentence);
                        mHandler.postDelayed(this, timeReadSentence);
                    } else {
                        mHandler.postDelayed(this, mTimePause + TIME_FOR_PROCESS);
                    }
                    mTimePause = 0;
                } catch (Exception e) {
                    PrivateException ex = new PrivateException(e,
                            DaisyEbookReaderVisualModeActivity.this);
                    ex.writeLogException();
                }
            }
        };
        mHandler.post(mRunnalbe);
    }

    /**
     * set start/stop runable.
     */
    private void setIsRunable() {
        if (mPlayer.isPlaying()) {
            mIsRunable = true;
        } else {
            // Do not run runable while media player is pause
            mIsRunable = false;
        }
    }

    /**
     * this function support to highlight text while audio is playing.
     */
    private void autoHighlight() {
        try {
            mFullTextOfBook = mContents.getText().toString();
            int sizeOfStringText = mListStringText.size();
            for (int i = mPositionSentence; i < sizeOfStringText; i++) {
                int currentPosition = mPlayer.getCurrentPosition();
                if (mListTimeBegin.get(i) <= currentPosition + TIME_FOR_PROCESS
                        && currentPosition < mListTimeEnd.get(i)) {
                    mStartOfSentence = mFullTextOfBook.indexOf(mListStringText.get(i),
                            mStartOfSentence);
                    Preconditions.checkArgument(mStartOfSentence > -1);
                    mNumberOfChar = mStartOfSentence + mListStringText.get(i).length();
                    // set color is transparent for all text before.
                    if (i > 0) {
                        mWordtoSpan.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 0,
                                mStartOfSentence, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    // set color is transparent for all text after.
                    mWordtoSpan.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), mNumberOfChar,
                            mContents.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mWordtoSpan.setSpan(new BackgroundColorSpan(mHighlightColor), mStartOfSentence,
                            mNumberOfChar, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    mContents.setText(mWordtoSpan);
                    mPositionSentence = i;
                    break;
                }
                // This case for daisy 3.0. Some audio files won't play until it
                // finish, it was splitted and move to the next chapter
                else if (mPositionSentence + 1 >= sizeOfStringText && !mIsEndOf
                        && mNavigator.hasNext()) {
                    nextSection();
                }
            }

        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
    }

    /**
     * this function support to autoscroll when highlight text at the end of
     * screen.
     */
    private void autoScroll() {
        try {
            Layout contentLayout = mContents.getLayout();
            Preconditions.checkNotNull(contentLayout);
            // get height of visual mode activity.
            LinearLayout f = (LinearLayout) findViewById(R.id.layoutVisualMode);
            int heightOfViewVisualMode = f.getMeasuredHeight();

            // get height of navigator bar
            RelativeLayout r = (RelativeLayout) findViewById(R.id.layoutRelativeLayout);
            int heightOfNavigator = r.getMeasuredHeight();

            // exactly height show text.
            int heightView = heightOfViewVisualMode - heightOfNavigator;

            int lineEndCurrent = contentLayout.getLineForOffset(mNumberOfChar);
            int lineTopCurrent = contentLayout.getLineForOffset(mStartOfSentence);
            int lineOfScreen = heightView / mContents.getLineHeight();
            if (lineEndCurrent > mTotalLineOnScreen) {
                mPositionOfScrollView = contentLayout.getLineTop(lineTopCurrent);
                mScrollView.scrollTo(0, mPositionOfScrollView);
                addToListValueOfScroll(mPositionOfScrollView);
                mTotalLineOnScreen = lineTopCurrent + lineOfScreen - 1;
                addToListValueLine(mTotalLineOnScreen);
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
    }

    /**
     * This function help to add value scroll to list to support for auto scroll
     * previous sentence.
     */
    private void addToListValueOfScroll(int positionOfScrollView) {
        boolean isAdd = true;
        int sizeOfListValueScroll = mListValueScroll.size();
        for (int i = 0; i < sizeOfListValueScroll; i++) {
            int valueOfScroll = mListValueScroll.get(i);
            if (positionOfScrollView == valueOfScroll) {
                // Check add permission.
                isAdd = false;
                break;
            }
        }
        if (isAdd) {
            mListValueScroll.add(positionOfScrollView);
        }
    }

    /**
     * This function help to add value position of line to list to support for
     * auto scroll previous sentence.
     */
    private void addToListValueLine(int positionOfLine) {
        boolean isAdd = true;
        int sizeOfListValueLine = mListValueLine.size();
        for (int i = 0; i < sizeOfListValueLine; i++) {
            int currentLine = mListValueLine.get(i);
            if (positionOfLine == currentLine) {
                // Check add permission.
                isAdd = false;
                break;
            }
        }
        if (isAdd) {
            mListValueLine.add(positionOfLine);
        }
    }

    /**
     * Here is our nano-controller which calls methods on the Navigation
     * Listener. We could include a method to add additional listeners.
     * 
     * @author Julian Harty
     */
    private class Controller {
        private NavigationListener navigationListener;
        private Navigable n;

        Controller(NavigationListener navigationListener) {
            this.navigationListener = navigationListener;
        }

        /**
         * Go to next section
         */
        public void next() {
            if (mNavigator.hasNext()) {
                mIsFound = true;
                mStartOfSentence = 0;
                if (mIsFirstNext) {
                    // Make sure no repeat section is playing.
                    mNavigator.next();
                    mIsFirstPrevious = true;
                    mIsFirstNext = false;
                }
                n = mNavigator.next();
                if (n instanceof Section) {
                    mHandler.removeCallbacks(mRunnalbe);
                    mIsRunable = true;
                    mPositionSection += 1;
                    mPositionSentence = 0;
                    navigationListener.onNext((Section) n);
                }
            } else {
                navigationListener.atEndOfBook();
            }
        }

        /**
         * Go to previous section
         */
        public void previous() {
            if (mNavigator.hasPrevious()) {
                mIsFound = true;
                if (mIsFirstPrevious) {
                    // Make sure the section is playing no repeat.
                    mNavigator.previous();
                    mIsFirstPrevious = false;
                    mIsFirstNext = true;
                }
                n = mNavigator.previous();
                if (n instanceof Section) {
                    mHandler.removeCallbacks(mRunnalbe);
                    mIsRunable = true;
                    mPositionSection -= 1;
                    mPositionSentence = 0;
                    navigationListener.onNext((Section) n);
                }

            } else {
                navigationListener.atBeginOfBook();
            }
        }
    }

    /**
     * Toggles the Media Player between Play and Pause states.
     */
    public void togglePlay() {
        if (mPlayer.isPlaying()) {
            setMediaPause();
        } else {
            try {
                setMediaPlay();
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this, mPath);
                if (!isFinishing()) {
                    ex.showDialogException(mIntentController);
                }
            }
        }
    }

    /**
     * Set media pause and remove call back
     */
    private void setMediaPause() {
        // remove call backs when you touch button stop.
        mHandler.removeCallbacks(mRunnalbe);
        mPlayer.pause();
        if (mCurrent != null) {
            mCurrent.setPlaying(mPlayer.isPlaying());
            mSql.updateCurrentInformation(mCurrent);
        }
        mImgButton.setImageResource(R.raw.media_play);
        mIsRunable = false;
    }

    // this variable will be handle the time when you change from pause to play.
    private long mTimePause = 0;

    /**
     * Set media play and post runnable
     */
    private void setMediaPlay() {
        mCurrent = mSql.getCurrentInformation();
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        if (mIsEndOf) {
            mNavigationListener.atEndOfBook();
        } else {
            mPlayer.start();
            if (mCurrent != null) {
                mCurrent.setPlaying(true);
                mSql.updateCurrentInformation(mCurrent);
            }
            mIsRunable = true;
            if (mPlayer.getCurrentPosition() != 0 && mListTimeEnd != null) {
                // if you pause while audio playing. You need to know time
                // pause
                // to high light text more correctly.
                mTimePause = mListTimeEnd.get(mPositionSentence) - mPlayer.getCurrentPosition()
                        + TIME_FOR_PROCESS;
            }
            // create call backs when you touch button start.
            mHandler.post(mRunnalbe);
            mImgButton.setImageResource(R.raw.media_pause);
        }
    }

    private OnClickListener btnNextSentenceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // do not allow user press button many times at the same time.
            if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.TIME_WAIT_FOR_CLICK_SENTENCE) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            nextSentence();
            try {
                Preconditions.checkArgument(mPositionSentence < mListTimeBegin.size() - 1);
                mPositionSentence += 1;
                mHandler.removeCallbacks(mRunnalbe);
                mIsRunable = true;
                autoHighlightAndScroll();
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderVisualModeActivity.this);
                ex.writeLogException();
            }

        }
    };

    /**
     * Go to next sentence by seek to time of clip end nearest position.
     */
    private void nextSentence() {
        try {
            if (isFormat202) {
                nextSentenceDaisy202();
            }
            // For daisy format 3.0
            else {
                nextSentenceDaisy30();
            }

        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
    }

    /**
     * Next sentence daisy202.
     */
    private void nextSentenceDaisy202() {
        int currentTime = mPlayer.getCurrentPosition();
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        // this case for user press next sentence at the end of book.
        if (currentTime == 0 && !mNavigator.hasNext() && mPositionSentence == mListTimeBegin.size()
                || mIsEndOf) {
            mNavigationListener.atEndOfBook();
        }
        // this case for user press next sentence.
        else if (mPositionSentence < mListTimeBegin.size() - 1) {
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence + 1));
        }
        // this case for user press next sentence at the end of section.
        else {
            mStartOfSentence = 0;
            nextSection();
            mPositionSentence -= 1;
        }
    }

    /**
     * Next sentence daisy30.
     */
    private void nextSentenceDaisy30() {
        // this case for user press next sentence at the end of book
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        if (mPlayer.getCurrentPosition() == 0 && !mNavigator.hasNext()
                && mPositionSentence == mListTimeBegin.size() || mIsEndOf) {
            mNavigationListener.atEndOfBook();
        }
        // this case for user press next sentence.
        else if (mPositionSentence < mListTimeBegin.size() - 1) {
            boolean isBreak = false;
            int currentTimeBegin = mListTimeBegin.get(mPositionSentence + 1);
            int currentTimeEnd = mListTimeEnd.get(mPositionSentence + 1);
            // Find and play the next audio (If daisy book has many audio
            // files
            // on 1 chapter).
            for (Entry<String, List<Integer>> entry : mHashMapBegin.entrySet()) {
                List<Integer> listValue = entry.getValue();
                if (!isBreak) {
                    for (int value : listValue) {
                        if (value == currentTimeBegin) {
                            if (!entry.getKey()
                                    .equals(listAudio.get(countAudio).getAudioFilename())) {
                                List<Integer> listValueEnd = mHashMapEnd.get(entry.getKey());
                                if (listValueEnd.contains(currentTimeEnd)) {
                                    isBreak = true;
                                    countAudio = countAudio + 1;
                                    mAudioPlayer.playFileSegment(listAudio.get(countAudio));
                                }
                            }
                            if (isBreak) {
                                break;
                            }
                        }
                    }
                }
            }
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence + 1));
        }
        // this case for user press next sentence at the end of section.
        else {
            mStartOfSentence = 0;
            nextSection();
            mPositionSentence -= 1;
        }
    }

    private OnClickListener btnPreviousSentenceClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // do not allow user press button many times at the same time.
            if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.TIME_WAIT_FOR_CLICK_SENTENCE) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            previousSentence();
            if (mPositionSentence > 0) {
                mPositionSentence -= 1;
                mHandler.removeCallbacks(mRunnalbe);
                mIsRunable = true;
                autoHighlightAndScroll();
            }

            // auto scroll when user press previous sentence.
            int lineCurrent = mContents.getLayout().getLineForOffset(mStartOfSentence);
            int positionOfScrollView = mContents.getLayout().getLineTop(lineCurrent);
            int sizeOfValueScroll = mListValueScroll.size();

            for (int i = 0; i < sizeOfValueScroll; i++) {
                int valueOfScroll = mListValueScroll.get(i);
                if (positionOfScrollView < valueOfScroll) {
                    mPositionOfScrollView = mListValueScroll.get(i - 1);
                    mTotalLineOnScreen = mListValueLine.get(i - 1);
                    break;
                }
            }
        }
    };

    /**
     * Go to previous sentence by seek to time of clip end before two units.
     */
    private void previousSentence() {
        boolean isPlaying = mPlayer.isPlaying();
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        try {
            if (isFormat202) {
                previousSentenceDaisy202();
            }
            // For daisy format 3.0
            else {
                previousSentenceDaisy30();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
            ex.writeLogException();
        }
        // keep current state media player.
        if (!isPlaying) {
            setMediaPause();
        }
    }

    /**
     * Previous sentence daisy202.
     */
    private void previousSentenceDaisy202() {
        int lengthOfSpace = 2;
        // this case for user press previous sentence at the begin of
        // book.
        if (mPositionSection == 1 && mPositionSentence == 0) {
            mNavigationListener.atBeginOfBook();
        }
        // this case for user press previous sentence at the end of
        // book.
        else if (mIsEndOf) {
            // It is code to resolve previous sentence when the end
            // of the book.
            mCurrent = mSql.getCurrentInformation();
            if (mCurrent != null) {
                mCurrent.setAtTheEnd(false);
                mSql.updateCurrentInformation(mCurrent);
            }
            mIsRunable = true;
            Navigable n = mNavigator.previous();
            n = mNavigator.next();
            mNavigationListener.onNext((Section) n);
            mIsEndOf = false;

            mPlayer.seekTo(mListTimeBegin.get(mListTimeBegin.size() - 1));
            mPositionSentence = mListTimeBegin.size() - 1;
        }
        // this case for user press previous sentence.
        else if (mPositionSentence > 0) {
            int lengthOfCurrentSentence = mListStringText.get(mPositionSentence - 1).length();
            mStartOfSentence = mStartOfSentence - lengthOfCurrentSentence - lengthOfSpace;
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence - 1));
        }
        // this case for user press previous sentence at the begin of
        // section.
        else {
            mController.previous();
            mPositionSentence = mListTimeBegin.size() - 1;
            if (mListTimeEnd.size() > 1) {
                // get all text of text view
                mFullTextOfBook = mContents.getText().toString();
                int lengthOfCurrentSentence = mListStringText.get(mListTimeBegin.size() - 1)
                        .length();
                mStartOfSentence = mFullTextOfBook.length() - lengthOfCurrentSentence
                        - lengthOfSpace;
                mPlayer.seekTo(mListTimeEnd.get(mListTimeEnd.size() - 2));
            }
        }
    }

    /**
     * Previous sentence daisy30.
     */
    private void previousSentenceDaisy30() {
        int lengthOfSpace = 2;
        // this case for user press previous sentence at the begin of book.
        if (mPositionSection == 1 && mPositionSentence == 0) {
            mNavigationListener.atBeginOfBook();
        }
        // this case for user press previous sentence at the end of book.
        else if (mIsEndOf) {
            // It is code to resolve previous sentence when the end
            // of the book.
            mCurrent = mSql.getCurrentInformation();
            if (mCurrent != null) {
                mCurrent.setAtTheEnd(false);
                mSql.updateCurrentInformation(mCurrent);
            }
            mIsRunable = true;
            Navigable n = mNavigator.previous();
            n = mNavigator.next();
            mNavigationListener.onNext((Section) n);
            mIsEndOf = false;

            mAudioPlayer.playFileSegment(listAudio.get(listAudio.size() - 1));
            countAudio = listAudio.size() - 1;
            mPlayer.seekTo(mListTimeBegin.get(mListTimeBegin.size() - 1));
            mPositionSentence = mListTimeBegin.size() - 1;
        } else if (mPositionSentence > 0) {
            boolean isBreak = false;
            int currentTimeBegin = mListTimeBegin.get(mPositionSentence - 1);
            int currentTimeEnd = mListTimeEnd.get(mPositionSentence - 1);
            // Find and play the next audio (If daisy book has many audio files
            // on 1 chapter).
            for (Entry<String, List<Integer>> entry : mHashMapBegin.entrySet()) {
                List<Integer> listValue = entry.getValue();
                if (!isBreak) {
                    for (int value : listValue) {
                        if (value == currentTimeBegin) {
                            if (!entry.getKey()
                                    .equals(listAudio.get(countAudio).getAudioFilename())) {
                                List<Integer> listValueEnd = mHashMapEnd.get(entry.getKey());
                                if (listValueEnd.contains(currentTimeEnd)) {
                                    isBreak = true;
                                    countAudio = countAudio - 1;
                                    mAudioPlayer.playFileSegment(listAudio.get(countAudio));
                                }
                            }
                            if (isBreak) {
                                break;
                            }
                        }
                    }
                }
            }
            int lengthOfCurrentSentence = mListStringText.get(mPositionSentence - 1).length();
            mStartOfSentence = mStartOfSentence - lengthOfCurrentSentence - lengthOfSpace;
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence - 1));
        }
        // this case for user press previous sentence at the begin of section.
        else {
            mController.previous();
            mPositionSentence = mListTimeBegin.size() - 1;
            if (mListTimeEnd.size() > 1) {
                // get all text of text view
                mFullTextOfBook = mContents.getText().toString();
                int lengthOfCurrentSentence = mListStringText.get(mListTimeBegin.size() - 1)
                        .length();
                mStartOfSentence = mFullTextOfBook.length() - lengthOfCurrentSentence
                        - lengthOfSpace;
                mAudioPlayer.playFileSegment(listAudio.get(listAudio.size() - 1));
                countAudio = listAudio.size() - 1;
                mPlayer.seekTo(mListTimeEnd.get(mListTimeEnd.size() - 2));
            }
        }
    }

    /**
     * Go to next section.
     */
    private void nextSection() {
        mStartOfSentence = 0;
        boolean isPlaying = mPlayer.isPlaying();
        mController.next();
        if (!isPlaying && !mIsEndOf) {
            setMediaPause();
        }
    }

    private OnClickListener btnNextSectionClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // do not allow user press button many times at the same time.
            if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.TIME_WAIT_FOR_CLICK_SECTION) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            nextSection();
        }
    };

    /**
     * Go to next section.
     */
    private void previousSection() {
        mCurrent = mSql.getCurrentInformation();
        mStartOfSentence = 0;
        mIsEndOf = false;
        if (mCurrent != null) {
            mCurrent.setAtTheEnd(false);
            mSql.updateCurrentInformation(mCurrent);
        }
        boolean isPlaying = mPlayer.isPlaying();
        mController.previous();
        if (!isPlaying) {
            setMediaPause();
        }
    }

    private OnClickListener btnPreviousSectionClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // do not allow user press button many times at the same time.
            if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.TIME_WAIT_FOR_CLICK_SECTION) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            previousSection();
        }
    };

}
