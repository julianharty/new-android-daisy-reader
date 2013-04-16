/**
 * This activity is visual mode which play audio and show full text.
 * @author LogiGear
 * @date 2013.03.05
 */
package org.androiddaisyreader.apps;

import java.io.InputStream;
import java.util.ArrayList;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Daisy202Section;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.player.AndroidAudioPlayer;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
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
import android.widget.ScrollView;
import android.widget.TextView;

public class DaisyEbookReaderVisualModeActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private boolean mIsFirstNext = false;
	private boolean mIsFirstPrevious = true;
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
	private ArrayList<Integer> mListBegin;
	private ArrayList<Integer> mListEnd;
	private ArrayList<Integer> mListIntEnd;
	private String mBookTitle;
	private String mPath;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private int mStartOfSentence;
	private int mTime;
	private int mNumberOfChar;
	private int mHighlightColor;
	private int mFontSize;
	private int mPositionSection = 0;
	private int mPositionSentence = 0;
	private boolean mIsRunable = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader_visual_mode);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		mPath = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		String[] title = mPath.split("/");
		mBookTitle = title[title.length - 2];
		tvBookTitle.setText(mBookTitle);
		mIntentController = new IntentController(this);
		ImageView imgTableOfContents = (ImageView) this
				.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);

		ImageView imgBookmark = (ImageView) this.findViewById(R.id.imgBookmark);
		imgBookmark.setOnClickListener(imgBookmarkClick);

		openBook();
		mContents = (TextView) this.findViewById(R.id.contents);
		mScrollView = (ScrollView) findViewById(R.id.scrollView);
		mImgButton = (ImageButton) this.findViewById(R.id.btnPlay);
		mImgButton.setOnClickListener(imgButtonClick);

		// Event for buttons on navigation.
		ImageButton btnNextSection = (ImageButton) this
				.findViewById(R.id.btnNextSection);
		btnNextSection.setOnClickListener(btnNextSectionClick);
		ImageButton btnNextSentence = (ImageButton) this
				.findViewById(R.id.btnNextSentence);
		btnNextSentence.setOnClickListener(btnNextSentenceClick);
		ImageButton btnPreviousSection = (ImageButton) this
				.findViewById(R.id.btnPreviousSection);
		btnPreviousSection.setOnClickListener(btnPreviousSectionClick);
		ImageButton btnPreviousSentence = (ImageButton) this
				.findViewById(R.id.btnPreviousSentence);
		btnPreviousSentence.setOnClickListener(btnPreviousSentenceClick);
		mHandler = new Handler();
		// check if user play daisybook from table of contents or bookmark
		try {
			String i = getIntent().getStringExtra(
					DaisyReaderConstants.POSITION_SECTION);
			mTime = getIntent().getIntExtra(DaisyReaderConstants.TIME, -1);

			if (i != null) {
				Navigable n = null;
				int countLoop = Integer.valueOf(i);
				for (int j = 0; j < countLoop; j++) {
					n = mNavigator.next();
					mPositionSection += 1;
				}
				if (n instanceof Section) {
					mNavigationListener.onNext((Section) n);
				}
			} else {
				togglePlay();
			}

		} catch (Exception e) {
			// If error the application will so dialog error.
			mIntentController.pushToDialogError(
					getString(R.string.noPathFound), true);
		}
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
			try {
				if (mPlayer.isPlaying()) {
					setMediaPause();
				}
				mIntentController.pushToTableOfContentsIntent(mPath,
						mNavigatorOfTableContents,
						getString(R.string.visualMode));

			} catch (Exception e) {
				mIntentController.pushToDialogError(
						getString(R.string.noPathFound), true);
			}
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mIntentController
					.pushToDaisyReaderBookmarkIntent(getBookmark(), getIntent()
							.getStringExtra(DaisyReaderConstants.DAISY_PATH));
		}
	};

	private Bookmark getBookmark() {
		String sentence = null;
		int time = mPlayer.getCurrentPosition();
		if (mPlayer.isPlaying()) {
			setMediaPause();
		}
		if (mListStringText != null) {
			for (int i = 0; i < mListStringText.size(); i++) {
				if (mListBegin.get(i) < mPlayer.getCurrentPosition() + 500
						&& time < mListEnd.get(i)) {
					sentence = mListStringText.get(i);
				}
			}
		}
		Bookmark bookmark = new Bookmark(mBookTitle, sentence, time,
				mPositionSection, 0, "");
		return bookmark;
	}

	@Override
	public void onBackPressed() {
		mHandler.removeCallbacks(mRunnalbe);
		finish();
		super.onBackPressed();
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
		if (mPlayer.isPlaying()) {
			setMediaPause();
		}
		switch (item.getItemId()) {
		// go to table of contents
		case R.id.menu_table:
			if (mPlayer.isPlaying()) {
				setMediaPause();
			}
			mIntentController.pushToTableOfContentsIntent(getIntent()
					.getStringExtra(DaisyReaderConstants.DAISY_PATH),
					mNavigatorOfTableContents, getString(R.string.visualMode));
			return true;
			// go to simple mode
		case R.id.menu_simple:
			Bookmark bookmark = getBookmark();
			mIntentController
					.pushToDaisyEbookReaderSimpleModeIntent(getIntent()
							.getStringExtra(DaisyReaderConstants.DAISY_PATH),
							bookmark.getSection(), bookmark.getTime());
			return true;
			// go to settings
		case R.id.menu_settings:
			mIntentController.pushToDaisyReaderSettingIntent();
			return true;
			// go to book marks
		case R.id.menu_bookmarks:
			mIntentController
					.pushToDaisyReaderBookmarkIntent(getBookmark(), getIntent()
							.getStringExtra(DaisyReaderConstants.DAISY_PATH));
			;
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
	
	// Show dialog when user choose function search of button settings.
	private void pushToDialogSearch() {
		final Dialog dialog = new Dialog(
				DaisyEbookReaderVisualModeActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_search);
		// set the custom dialog components - text, image and button
		final EditText searchText = (EditText) dialog
				.findViewById(R.id.searchText);
		Button dialogButton = (Button) dialog.findViewById(R.id.buttonSearch);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String ett = searchText.getText().toString();
				if (ett.trim().length() > 0) {
					String tvt = mContents.getText().toString();
					int ofe = tvt.indexOf(ett, 0);
					Spannable WordtoSpan = new SpannableString(mContents
							.getText());
					for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {

						ofe = tvt.indexOf(ett, ofs);
						if (ofe == -1)
							break;
						else {

							WordtoSpan.setSpan(new BackgroundColorSpan(
									mHighlightColor), ofe, ofe + ett.length(),
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							mContents.setText(WordtoSpan,
									TextView.BufferType.SPANNABLE);
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
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.stop();
		}
		mHandler.removeCallbacks(mRunnalbe);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		ContentResolver cResolver = getContentResolver();
		int valueBrightnessScreen = 0;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			valueBrightnessScreen = mPreferences.getInt(
					DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		LayoutParams layoutpars = mWindow.getAttributes();
		layoutpars.screenBrightness = valueBrightnessScreen / (float) 255;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		mFontSize = mPreferences.getInt(DaisyReaderConstants.FONT_SIZE, 12);
		mContents.setTextSize(mFontSize);
		boolean nightMode = mPreferences.getBoolean(
				DaisyReaderConstants.NIGHT_MODE, false);
		// apply visual mode when user chooose night mode
		if (nightMode) {
			mContents.setTextColor(Color.WHITE);
			mScrollView.setBackgroundColor(Color.BLACK);
			mHighlightColor = 0xff408000;
		} else {
			// apply text color
			int textColor = mPreferences.getInt(
					DaisyReaderConstants.TEXT_COLOR,
					mContents.getCurrentTextColor());
			mContents.setTextColor(textColor);

			// apply background color
			int backgroundColor = mPreferences.getInt(
					DaisyReaderConstants.BACKGROUND_COLOR, Color.BLACK);
			mScrollView.setBackgroundColor(backgroundColor);

			// apply highlight color
			mHighlightColor = mPreferences.getInt(
					DaisyReaderConstants.HIGHLIGHT_COLOR, Color.YELLOW);
		}
		mNavigatorOfTableContents = new Navigator(mBook);
		super.onResume();
	}

	private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

		public void endOfAudio() {
			Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
			mController.next();
		}
	};

	private void createValueToHightLightText() {
		mFontSize = mPreferences.getInt(DaisyReaderConstants.FONT_SIZE, 12);
		mListStringText = new ArrayList<String>();
		mListBegin = new ArrayList<Integer>();
		mListEnd = new ArrayList<Integer>();
	}

	/**
	 * open book from path
	 */
	private void openBook() {
		InputStream contents;
		try {
			mBookContext = DaisyReaderUtils.openBook(mPath);
			String[] sp = mPath.split("/");
			contents = mBookContext.getResource(sp[sp.length - 1]);

			mAndroidAudioPlayer = new AndroidAudioPlayer(mBookContext);
			mAndroidAudioPlayer.addCallbackListener(audioCallbackListener);
			mAudioPlayer = new AudioPlayerController(mAndroidAudioPlayer);
			mPlayer = mAndroidAudioPlayer.getCurrentPlayer();

			mBook = NccSpecification.readFromStream(contents);
			mNavigator = new Navigator(mBook);
			// get all navigator of book to push to table of contents.
			mNavigatorOfTableContents = new Navigator(mBook);

		} catch (Exception e) {
			// TODO 20120515 (jharty): Add test for SDCARD being available
			// so we can tell the user...
			e.printStackTrace();
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
				// create some value to support to highlight text.
				createValueToHightLightText();
				Daisy202Section currentSection = new Daisy202Section.Builder()
						.setHref(section.getHref()).setContext(mBookContext)
						.build();
				StringBuilder snippetText = new StringBuilder();
				for (Part part : currentSection.getParts()) {
					for (int i = 0; i < part.getSnippets().size(); i++) {
						if (i > 0) {
							snippetText.append(" ");
						}
						snippetText.append(part.getSnippets().get(i).getText());
						mListStringText
								.add(part.getSnippets().get(i).getText());
					}
					snippetText.append(" ");
					mListBegin.add(part.getAudioElements().get(0)
							.getClipBegin());
					mListEnd.add(part.getAudioElements()
							.get(part.getAudioElements().size() - 1)
							.getClipEnd());
				}
				mContents.setText(snippetText.toString(),
						TextView.BufferType.SPANNABLE);

				StringBuilder audioListings = new StringBuilder();
				mListIntEnd = new ArrayList<Integer>();
				for (Part part : currentSection.getParts()) {
					for (int i = 0; i < part.getAudioElements().size(); i++) {
						Audio audioSegment = part.getAudioElements().get(i);
						mAudioPlayer.playFileSegment(audioSegment);
						audioListings.append(audioSegment.getAudioFilename()
								+ ", " + audioSegment.getClipBegin() + ":"
								+ audioSegment.getClipEnd() + "\n");
						mListIntEnd.add(audioSegment.getClipEnd());
					}
				}
				mImgButton.setImageResource(R.drawable.media_pause);
				// seek to time when user loading from book mark.
				if (mTime != -1) {
					mPlayer.seekTo(mTime);
					mTime = -1;
				}
				highlightText();
			} catch (Exception e) {
				mIntentController.pushToDialogError(
						getString(R.string.wrongFormat), true);
			}
		}

		private void atEndOfBook() {
			if (mPlayer.getCurrentPosition() == 0) {
				mIsRunable = false;
				mWordtoSpan.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
						0, mContents.getText().length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mContents.setText(mWordtoSpan);
				mImgButton.setImageResource(R.drawable.media_play);
			}
			mIntentController.pushToDialogError(getString(R.string.atEnd)
					+ getString(R.string.space) + mBook.getTitle(), false);
		}

		public void atBeginOfBook() {
			mIntentController.pushToDialogError(getString(R.string.atBegin)
					+ getString(R.string.space) + mBook.getTitle(), false);
		}
	}

	private void highlightText() {
		mStartOfSentence = 0;
		mWordtoSpan = (Spannable) mContents.getText();
		mRunnalbe = new Runnable() {

			@Override
			public void run() {
				if (mIsRunable) {
					String fullText = mContents.getText().toString();
					for (int i = mPositionSentence; i < mListStringText.size(); i++) {
						if (mListBegin.get(i) < mPlayer.getCurrentPosition() + 500
								&& mPlayer.getCurrentPosition() < mListEnd
										.get(i)) {
							mStartOfSentence = fullText.indexOf(mListStringText
									.get(i));
							mNumberOfChar = mStartOfSentence
									+ mListStringText.get(i).length();
							// set color is transparent for all text before.
							if (i > 0) {
								mWordtoSpan.setSpan(new BackgroundColorSpan(
										Color.TRANSPARENT), 0,
										mStartOfSentence,
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
							mWordtoSpan.setSpan(new BackgroundColorSpan(
									mHighlightColor), mStartOfSentence,
									mNumberOfChar,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							// set color is transparent for all text after.
							mWordtoSpan.setSpan(new BackgroundColorSpan(
									Color.TRANSPARENT), mNumberOfChar,
									mContents.getText().length(),
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							mContents.setText(mWordtoSpan);
							mPositionSentence = i;
							break;
						}
					}
				}
				if (mTimePause != 0) {
					// If user choose pause and play. 400 is time delay when you
					// told on your phone.
					mHandler.postDelayed(this, mTimePause + 400);
				} else {
					// Default
					mHandler.postDelayed(this, mListEnd.get(mPositionSentence)
							- mListBegin.get(mPositionSentence));
				}
				mTimePause = 0;
			}
		};
		mHandler.post(mRunnalbe);
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

		// Go to next section
		public void next() {
			if (mNavigator.hasNext()) {
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
				mIntentController.pushToDialogError(
						getString(R.string.wrongFormat), true);
				onBackPressed();
			}

		}
	}

	private void setMediaPause() {
		// remove call backs when you touch button stop.
		mHandler.removeCallbacks(mRunnalbe);
		mPlayer.pause();
		mImgButton.setImageResource(R.drawable.media_play);
		mIsRunable = false;
	}
	
	// this variable will be handle the time when you change from pause to play.
	long mTimePause = 0;

	private void setMediaPlay() {
		mPlayer.start();
		mIsRunable = true;
		if (mPlayer.getCurrentPosition() != 0 && mListEnd != null) {
			// if you pause while audio playing. You need to know time pause to
			// high light text more correctly.
			mTimePause = mListEnd.get(mPositionSentence)
					- mPlayer.getCurrentPosition();
		}
		// create call backs when you touch button start.
		mHandler.post(mRunnalbe);
		mImgButton.setImageResource(R.drawable.media_pause);
	}

	private OnClickListener btnNextSentenceClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			nextSentence();
			if (mPositionSentence < mListBegin.size() - 1) {
				mPositionSentence += 1;
				mHandler.removeCallbacks(mRunnalbe);
				mIsRunable = true;
				highlightText();
				// when user choose next sentence. The application will updated
				// time to post delayed.
				mHandler.postDelayed(mRunnalbe, 1000);
			}
		}
	};

	/**
	 * Go to next sentence by seek to time of clip end nearest position.
	 */
	private void nextSentence() {
		int currentTime = mPlayer.getCurrentPosition();
		if (currentTime == 0 && !mNavigator.hasNext()) {
			mNavigationListener.atEndOfBook();
		} else if (mPositionSentence < mListBegin.size() - 1) {
			mPlayer.seekTo(mListBegin.get(mPositionSentence + 1));
		} else {
			nextSection();
			mPositionSentence -= 1;
		}
	}

	private OnClickListener btnPreviousSentenceClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			previousSentence();
			if (mPositionSentence > 0) {
				mPositionSentence -= 1;
				mHandler.removeCallbacks(mRunnalbe);
				mIsRunable = true;
				highlightText();
			}
		}
	};

	/**
	 * Go to previous sentence by seek to time of clip end before two units.
	 */
	private void previousSentence() {
		if (!mNavigator.hasPrevious() && mPositionSentence == 0) {
			mNavigationListener.atBeginOfBook();
		} else if (!mNavigator.hasNext()
				&& mPositionSentence == mListBegin.size() - 1) {
			// It is code to resolve previous sentence when the end
			// of the book.
			mIsRunable = true;
			Navigable n = mNavigator.previous();
			n = mNavigator.next();
			mNavigationListener.onNext((Section) n);
			mPlayer.seekTo(mListEnd.get(mPositionSentence - 1));
		} else if (mPositionSentence > 0) {
			mPlayer.seekTo(mListBegin.get(mPositionSentence - 1));
		} else {
			mIsFirstPrevious = true;
			mController.previous();
			if (mListEnd.size() > 1) {
				mPlayer.seekTo(mListEnd.get(mListEnd.size() - 2));
			}
		}
	}

	private void nextSection() {
		boolean isPlaying = mPlayer.isPlaying();
		mController.next();
		if (!isPlaying) {
			setMediaPause();
		}
	}

	private OnClickListener btnNextSectionClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			nextSection();
		}
	};

	private void previousSection() {
		boolean isPlaying = mPlayer.isPlaying();
		mController.previous();
		if (!isPlaying) {
			setMediaPause();
		}
	}

	private OnClickListener btnPreviousSectionClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			previousSection();
		}
	};

	@Override
	public void onInit(int arg0) {
	}
}
