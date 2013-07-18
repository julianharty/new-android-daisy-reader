package org.androiddaisyreader.apps;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Daisy202Section;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.player.AndroidAudioPlayer;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.androiddaisyreader.utils.Constants;

import com.google.common.base.Preconditions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This activity is visual mode which play audio and show full text.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class DaisyEbookReaderVisualModeActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private boolean mIsFirstNext = false;
	private boolean mIsFirstPrevious = true;
	private TextToSpeech mTts;
	private BookContext mBookContext;
	private Daisy202Book mBook;
	private Navigator mNavigator;
	private Navigator mNavigatorOfTableContents;
	private NavigationListener mNavigationListener = new NavigationListener();
	private Controller mController = new Controller(mNavigationListener);
	private AudioPlayerController mAudioPlayer;
	private AndroidAudioPlayer mAndroidAudioPlayer;
	private MediaPlayer mPlayer;
	private TextView mContents;
	private ImageButton mImgButton;
	private IntentController mIntentController;
	private ScrollView mScrollView;
	private Spannable mWordtoSpan;
	private Runnable mRunnalbe;
	private Handler mHandler;
	private ArrayList<String> mListStringText;
	private ArrayList<Integer> mListTimeBegin;
	private ArrayList<Integer> mListTimeEnd;
	private ArrayList<Integer> mListValueScroll;
	private ArrayList<Integer> mListValueLine;
	private String mPath;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private String mFullTextOfBook;
	private int mTime;
	private int mTotalLineOnScreen;
	private int mNumberOfChar;
	private int mPositionOfScrollView;
	private int mHighlightColor;
	private int mStartOfSentence = 0;
	private int mPositionSection = 0;
	private int mPositionSentence = 0;
	private int mTimeForProcess = 400;
	private long mLastClickTime = 0;
	// if audio is over, mIsEndOf will equal true;
	private boolean mIsRunable = true;
	private boolean mIsEndOf = false;
	private boolean mIsFound = true;
	private boolean mIsPlaying = false;
	private SQLiteCurrentInformationHelper mSql;
	private CurrentInformation mCurrent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader_visual_mode);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(DaisyEbookReaderVisualModeActivity.this);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		mIntentController = new IntentController(this);
		mSql = new SQLiteCurrentInformationHelper(DaisyEbookReaderVisualModeActivity.this);
		mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
		startTts();
		setEventForTopButtons();
		openBook();
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		if (mBook != null) {
			tvBookTitle.setText(mBook.getTitle());
			mContents = (TextView) this.findViewById(R.id.contents);
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
					getString(R.string.error_title), R.drawable.error, true, false, null);
		}
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
	 * Set event for top buttons (table of contents, bookmark).
	 */
	private void setEventForTopButtons() {
		ImageView imgTableOfContents = (ImageView) this.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);

		ImageView imgBookmark = (ImageView) this.findViewById(R.id.imgBookmark);
		imgBookmark.setOnClickListener(imgBookmarkClick);
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
		if (mCurrent != null
				&& !mCurrent.getActivity().equals(
						getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
			section = String.valueOf(mCurrent.getSection());
			mTime = mCurrent.getTime();
			mPositionSentence = 0;
		} else {
			section = getIntent().getStringExtra(Constants.POSITION_SECTION);
			mTime = getIntent().getIntExtra(Constants.TIME, -1);
		}
		try {
			if (section != null) {
				int countLoop = Integer.valueOf(section) - mPositionSection;
				Navigable n = getNavigable(countLoop);
				if (n instanceof Section) {
					mNavigationListener.onNext((Section) n);
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
		else if (countLoop == 0) {
			mPlayer.seekTo(mTime);
			mTime = -1;
		}
		// case 3: variable < 0, user want to previous section.
		else {
			n = previousSectionByCountLoop(-countLoop);
		}
		return n;
	}

	/**
	 * Get next section.
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
	 * Get previous section
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

	private OnClickListener imgTableOfContentsClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mIsPlaying = mPlayer.isPlaying();
			if (mIsPlaying) {
				setMediaPause();
			}
			if (mCurrent != null) {
				updateCurrentInformation();
			} else {
				createCurrentInformation();
			}
			mIntentController.pushToTableOfContentsIntent(mPath, mNavigatorOfTableContents,
					getString(R.string.visual_mode));
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mIsPlaying = mPlayer.isPlaying();
			if (mIsPlaying) {
				setMediaPause();
			}
			if (mCurrent != null) {
				updateCurrentInformation();
			} else {
				createCurrentInformation();
			}
			mIntentController.pushToDaisyReaderBookmarkIntent(getBookmark(), getIntent()
					.getStringExtra(Constants.DAISY_PATH));
		}
	};

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
			if (sentence.length() <= 0) {
				sentence = mListStringText.get(i + 1);
			}
		}
		Bookmark bookmark = new Bookmark(mPath, sentence, currentTime, mPositionSection, 0, "");
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
			if (mCurrent == null) {
				createCurrentInformation();
			} else {
				updateCurrentInformation();
			}
			finish();
		} else {
			super.onBackPressed();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu, menu);
		return true;
	}

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mIsPlaying = mPlayer.isPlaying();
		if (mIsPlaying) {
			setMediaPause();
		}
		switch (item.getItemId()) {
		// go to table of contents
		case R.id.menu_table:
			if (mCurrent != null) {
				mCurrent.setFirstNext(false);
				mCurrent.setFirstPrevious(false);
				updateCurrentInformation();
			} else {
				createCurrentInformation();
			}
			mIntentController.pushToTableOfContentsIntent(
					getIntent().getStringExtra(Constants.DAISY_PATH),
					mNavigatorOfTableContents, getString(R.string.visual_mode));
			return true;
			// go to simple mode
		case R.id.menu_simple:
			if (mCurrent != null) {
				updateCurrentInformation();
			} else {
				createCurrentInformation();
			}
			mIntentController.pushToDaisyEbookReaderSimpleModeIntent(getIntent().getStringExtra(
					Constants.DAISY_PATH));
			return true;
			// go to settings
		case R.id.menu_settings:
			if (mCurrent != null) {
				updateCurrentInformation();
			} else {
				createCurrentInformation();
			}
			mIntentController.pushToDaisyReaderSettingIntent();
			return true;
			// go to book marks
		case R.id.menu_bookmarks:
			if (mCurrent != null) {
				updateCurrentInformation();
			} else {
				createCurrentInformation();
			}
			mIntentController.pushToDaisyReaderBookmarkIntent(getBookmark(), getIntent()
					.getStringExtra(Constants.DAISY_PATH));
			return true;
			// go to library
		case R.id.menu_library:
			mIntentController.pushToLibraryIntent();
			return true;
		case R.id.menu_search:
			pushToDialogSearch();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Create current information.
	 */
	private void createCurrentInformation() {
		// create a current information
		CurrentInformation current = new CurrentInformation();
		try {
			current.setPath(mPath);
			current.setSection(mPositionSection);
			current.setTime(mPlayer.getCurrentPosition());
			current.setPlaying(mIsPlaying);
			current.setSentence(1);
			current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
			current.setFirstNext(false);
			current.setFirstPrevious(true);
			current.setAtTheEnd(false);
			current.setId(UUID.randomUUID().toString());
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
			ex.writeLogException();
		}
		mSql.addCurrentInformation(current);
	}

	/**
	 * Update current information.
	 */
	private void updateCurrentInformation() {
		mCurrent.setTime(mPlayer.getCurrentPosition());
		mCurrent.setSection(mPositionSection);
		mCurrent.setSentence(mPositionSentence);
		mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_visual_mode));
		mCurrent.setPlaying(mIsPlaying);
		mSql.updateCurrentInformation(mCurrent);
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
					Spannable WordtoSpan = new SpannableString(mContents.getText());
					for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
						ofe = tvt.indexOf(ett, ofs);
						if (ofe == -1)
							break;
						else {

							WordtoSpan.setSpan(new BackgroundColorSpan(mHighlightColor), ofe, ofe
									+ ett.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							mContents.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
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
		try {
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
			ex.writeLogException();
		}
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.stop();
		}
		mHandler.removeCallbacks(mRunnalbe);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.title_activity_daisy_ebook_reader_visual_mode),
				TextToSpeech.QUEUE_FLUSH, null);
		if (mBook != null) {
			getValueFromSetting();
			setNightMode();
			mNavigatorOfTableContents = new Navigator(mBook);
		}
		super.onResume();
		handleCurrentInformation();
	}

	private void handleCurrentInformation() {
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
		int mFontSize;
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyEbookReaderVisualModeActivity.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = mWindow.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			mWindow.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
			ex.writeLogException();
		}
		mFontSize = mPreferences.getInt(Constants.FONT_SIZE,
				Constants.FONTSIZE_DEFAULT);
		mContents.setTextSize(mFontSize);
	}

	/**
	 * Apply night mode setting, if user turn on.
	 */
	private void setNightMode() {
		boolean nightMode = mPreferences.getBoolean(Constants.NIGHT_MODE, false);
		if (nightMode) {
			mContents.setTextColor(Color.WHITE);
			mScrollView.setBackgroundColor(Color.BLACK);
			mHighlightColor = 0xff408000;
		} else {
			// apply text color
			int textColor = mPreferences.getInt(Constants.TEXT_COLOR, 0xffc0c0c0);
			mContents.setTextColor(textColor);

			// apply background color
			int backgroundColor = mPreferences.getInt(Constants.BACKGROUND_COLOR,
					Color.BLACK);
			mScrollView.setBackgroundColor(backgroundColor);

			// apply highlight color
			mHighlightColor = mPreferences.getInt(Constants.HIGHLIGHT_COLOR,
					Color.YELLOW);
		}

	}

	private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

		public void endOfAudio() {
			Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
			if (!mIsEndOf && mIsFound) {
				mController.next();
			}
		}
	};

	/**
	 * open book from path
	 */
	private void openBook() {
		InputStream contents;
		try {
			try {
				mBookContext = DaisyBookUtil.openBook(mPath);
				contents = mBookContext.getResource(Constants.FILE_NCC_NAME_NOT_CAPS);
				mAndroidAudioPlayer = new AndroidAudioPlayer(mBookContext);
				mAndroidAudioPlayer.addCallbackListener(audioCallbackListener);
				mAudioPlayer = new AudioPlayerController(mAndroidAudioPlayer);
				mPlayer = mAndroidAudioPlayer.getCurrentPlayer();

				mBook = NccSpecification.readFromStream(contents);
				// get all navigator of book to push to table of contents.
				mNavigatorOfTableContents = new Navigator(mBook);
				mNavigator = mNavigatorOfTableContents;

				if (!mBook.hasTotalTime()) {
					mIntentController.pushToDialog(getString(R.string.error_wrong_format_audio),
							getString(R.string.error_title), R.drawable.error, false, false, null);
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderVisualModeActivity.this, mPath);
				throw ex;
			}
		} catch (PrivateException e) {
			e.showDialogException(mIntentController);
		}
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
				// If file or audio is not found, audio must to change
				// status to
				// pause
				if (!mIsFound) {
					setMediaPause();
					mIsFound = true;
				}
				Daisy202Section currentSection = new Daisy202Section.Builder()
						.setHref(section.getHref()).setContext(mBookContext).build();
				Part[] parts = currentSection.getParts();
				getSnippetsOfCurrentSection(parts);
				getAudioElementsOfCurrentSection(parts);
				// seek to time when user loading from book mark.
				if (mTime != -1) {
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
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderVisualModeActivity.this);
				ex.writeLogException();
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
		}

		/**
		 * Get all text from parts.
		 * 
		 * @param parts
		 */
		private void getSnippetsOfCurrentSection(Part[] parts) {
			StringBuilder snippetText = new StringBuilder();
			try {
				for (Part part : parts) {
					int sizeOfPart = part.getSnippets().size();
					for (int i = 0; i < sizeOfPart; i++) {
						if (i > 0) {
							snippetText.append(getString(R.string.space));
						}
						String text = part.getSnippets().get(i).getText();
						snippetText.append(text);
						mListStringText.add(text);
					}
					snippetText.append(getString(R.string.space));
					List<Audio> audioElements = part.getAudioElements();
					mListTimeBegin.add(audioElements.get(0).getClipBegin());
					mListTimeEnd.add(audioElements.get(audioElements.size() - 1).getClipEnd());
				}
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
		private void getAudioElementsOfCurrentSection(Part[] parts) {
			try {
				StringBuilder audioListings = new StringBuilder();
				for (Part part : parts) {
					for (Audio audioSegment : part.getAudioElements()) {
						mAudioPlayer.playFileSegment(audioSegment);
						audioListings.append(audioSegment.getAudioFilename() + ", "
								+ audioSegment.getClipBegin() + ":" + audioSegment.getClipEnd()
								+ "\n");
					}
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderVisualModeActivity.this, mPath);
				ex.showDialogException(mIntentController);
				mIsFound = false;
				mImgButton.setImageResource(R.drawable.media_play);
			}
		}

		/**
		 * Show message at the end of book.
		 */
		private void atEndOfBook() {
			mIntentController.pushToDialog(getString(R.string.atEnd) + getString(R.string.space)
					+ mBook.getTitle(), getString(R.string.error_title), R.drawable.error, false,
					false, null);
			int currentTime = mPlayer.getCurrentPosition();
			if (currentTime == -1 || currentTime == mPlayer.getDuration() || currentTime == 0) {
				mIsRunable = false;
				mIsEndOf = true;
				mWordtoSpan.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 0, mContents
						.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mContents.setText(mWordtoSpan);
				mImgButton.setImageResource(R.drawable.media_play);
			}

			if (mCurrent != null) {
				mCurrent.setAtTheEnd(true);
				mSql.updateCurrentInformation(mCurrent);
			}

		}

		/**
		 * Show message at the begin of book.
		 */
		public void atBeginOfBook() {
			mIntentController.pushToDialog(getString(R.string.atBegin) + getString(R.string.space)
					+ mBook.getTitle(), getString(R.string.error_title), R.drawable.error, false,
					false, null);
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
						mHandler.postDelayed(this, mTimePause);
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
				if (mListTimeBegin.get(i) <= mPlayer.getCurrentPosition() + mTimeForProcess
						&& mPlayer.getCurrentPosition() < mListTimeEnd.get(i)) {
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
				ex.showDialogException(mIntentController);
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
		mImgButton.setImageResource(R.drawable.media_play);
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
						+ mTimeForProcess;
			}
			// create call backs when you touch button start.
			mHandler.post(mRunnalbe);
			mImgButton.setImageResource(R.drawable.media_pause);
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
			int currentTime = mPlayer.getCurrentPosition();
			// this case for user press next sentence at the end of book.
			if (currentTime == 0 && !mNavigator.hasNext()
					&& mPositionSentence == mListTimeBegin.size()) {
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
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderVisualModeActivity.this);
			ex.writeLogException();
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
		try {
			int lengthOfSpace = 2;
			// this case for user press previous sentence at the begin of
			// book.
			if (!mNavigator.hasPrevious() && mPositionSentence == 0) {
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
	 * Go to next section.
	 */
	private void nextSection() {
		mStartOfSentence = 0;
		boolean isPlaying = mPlayer.isPlaying();
		mController.next();
		if (!isPlaying) {
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

	@Override
	public void onInit(int arg0) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener
	}
}
