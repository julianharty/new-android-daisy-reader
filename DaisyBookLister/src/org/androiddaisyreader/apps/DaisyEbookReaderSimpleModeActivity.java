package org.androiddaisyreader.apps;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.BookContext;
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

import com.google.marvin.widget.GestureOverlay;
import com.google.marvin.widget.GestureOverlay.Gesture;
import com.google.marvin.widget.GestureOverlay.GestureListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This activity is simple mode which play audio.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class DaisyEbookReaderSimpleModeActivity extends Activity implements OnClickListener,
		TextToSpeech.OnInitListener {
	private boolean mIsFirstNext = false;
	private boolean mIsFirstPrevious = true;
	private TextToSpeech mTts;
	private BookContext mBookContext;
	private Daisy202Book mBook;
	private Navigator mNavigator;
	private Navigator mNavigatorOfTableContents;
	private NavigationListener mNavigationListener;
	private Controller mController;
	private AudioPlayerController mAudioPlayer;
	private AndroidAudioPlayer mAndroidAudioPlayer;
	private MediaPlayer mPlayer;
	private ArrayList<String> mListStringText;
	private ArrayList<Integer> mListTimeEnd;
	private ArrayList<Integer> mListTimeBegin;
	private IntentController mIntentController;
	private Window mWindow;
	private String mTime;
	private int mPositionSentence = 0;
	private boolean mIsRunable = true;
	private Runnable mRunnalbe;
	private Handler mHandler;
	// if audio is over, mIsEndOf will equal true;
	private boolean mIsEndOf = false;
	private int mTimeForProcess = 400;
	private boolean mIsFound = true;
	private int mOldMessage;
	private CurrentInformation mCurrent;
	private SQLiteCurrentInformationHelper mSql;
	private int mPositionSection = 0;
	private boolean mIsPlaying = false;
	private String mPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_ebook_reader_simple_mode);
		mWindow = getWindow();
		mIntentController = new IntentController(DaisyEbookReaderSimpleModeActivity.this);
		mSql = new SQLiteCurrentInformationHelper(DaisyEbookReaderSimpleModeActivity.this);
		startTts();
		mNavigationListener = new NavigationListener();
		mController = new Controller(mNavigationListener);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.daisyReaderSimpleModeLayout);
		GestureOverlay mGestureOverlay = new GestureOverlay(this, gestureListener);
		relativeLayout.addView(mGestureOverlay);
		setContentView(relativeLayout);
		mHandler = new Handler();
		mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
		openBook();
		readBook();
	}

	/**
	 * Start text to speech
	 */
	private void startTts() {
		mTts = new TextToSpeech(DaisyEbookReaderSimpleModeActivity.this, this);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_OK);
	}

	/**
	 * Start reading book.
	 */
	private void readBook() {
		try {
			String section;
			mCurrent = mSql.getCurrentInformation();
			if (mCurrent != null
					&& !mCurrent.getActivity().equals(
							getString(R.string.title_activity_daisy_ebook_reader_simple_mode))) {
				section = String.valueOf(mCurrent.getSection());
				mTime = String.valueOf(mCurrent.getTime());
				mPositionSentence = 0;
				mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
				mSql.updateCurrentInformation(mCurrent);
			} else {
				section = getIntent().getStringExtra(Constants.POSITION_SECTION);
				mTime = getIntent().getStringExtra(Constants.TIME);
			}
			if (section != null) {
				int countLoop = Integer.valueOf(section) - mPositionSection;
				Navigable n = getNavigable(countLoop);
				if (n instanceof Section) {
					mNavigationListener.onNext((Section) n);
				}
			} else {
				// if user do not load from table of contents, play reading book
				// at normal.
				togglePlay();
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this,
					mPath);
			ex.showDialogException(mIntentController);
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
			mPlayer.seekTo(Integer.valueOf(mTime));
			mTime = null;
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
		for (int j = 0; j < countLoop; j++) {
			n = mNavigator.next();
			if (mCurrent != null
					&& mCurrent.getActivity().equals(
							getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
				n = mNavigator.next();
				mIsFirstPrevious = true;
				mIsFirstNext = false;
				;
			}
			mPositionSection += 1;
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
								getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
					n = mNavigator.previous();
					mIsFirstPrevious = false;
					mIsFirstNext = true;
				}
			}
			mPositionSection -= 1;
		}
		return n;
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

	/**
	 * Create current information.
	 */
	private void createCurrentInformation() {
		// create a current information
		CurrentInformation current = new CurrentInformation();
		try {
			current.setPath(getIntent().getStringExtra(Constants.DAISY_PATH));
			current.setSection(mPositionSection);
			current.setTime(mPlayer.getCurrentPosition());
			current.setPlaying(mIsPlaying);
			current.setSentence(1);
			current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
			current.setAtTheEnd(false);
			current.setId(UUID.randomUUID().toString());
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
			ex.writeLogException();
		}
		mSql.addCurrentInformation(current);
	}

	/**
	 * Update current information.
	 */
	private void updateCurrentInformation() {
		mCurrent.setPlaying(mIsPlaying);
		mCurrent.setTime(mPlayer.getCurrentPosition());
		mCurrent.setSection(mPositionSection);
		mCurrent.setSentence(mPositionSentence);
		mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
		mSql.updateCurrentInformation(mCurrent);
	}

	@Override
	protected void onDestroy() {
		try {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
			ex.writeLogException();
		}
		mHandler.removeCallbacks(mRunnalbe);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		speakOut(Constants.SIMPLE_MODE);
		speakOut(mOldMessage);
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyEbookReaderSimpleModeActivity.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = mWindow.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			mWindow.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
			ex.writeLogException();
		}
		if (mBook != null) {
			mNavigatorOfTableContents = new Navigator(mBook);
		}
		super.onResume();
	}

	@Override
	protected void onRestart() {
		mCurrent = mSql.getCurrentInformation();
		if (mCurrent != null) {
			if (mCurrent.getPlaying()) {
				setMediaPlay();
			} else {
				setMediaPause();
			}
			if (!mCurrent.getActivity().equals(
					getString(R.string.title_activity_daisy_ebook_reader_simple_mode))) {
				readBook();
			}
		}
		super.onRestart();
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
				mNavigator = new Navigator(mBook);
				mNavigatorOfTableContents = new Navigator(mBook);

				if (!mBook.hasTotalTime()) {
					mIntentController.pushToDialog(getString(R.string.error_wrong_format_audio),
							getString(R.string.error_title), R.drawable.error, true, false, null);
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderSimpleModeActivity.this, mPath);
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
				Daisy202Section currentSection = new Daisy202Section.Builder()
						.setHref(section.getHref()).setContext(mBookContext).build();
				Part[] parts = currentSection.getParts();
				getSnippetsOfCurrentSection(parts);
				getAudioElementsOfCurrentSection(parts);
				// seek to time when user change from visual mode
				if (mListTimeEnd.size() > 0) {
					if (mTime != null) {
						mPlayer.seekTo(Integer.valueOf(mTime));
						mTime = null;
					}
					if (mCurrent != null) {
						mSql.updateCurrentInformation(mCurrent);
						if (mCurrent.getPlaying()) {
							setMediaPlay();
						} else {
							setMediaPause();
						}
					}
					getCurrentPositionSentence();
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderSimpleModeActivity.this);
				ex.writeLogException();
			}
		}

		/**
		 * Get all text from parts.
		 * 
		 * @param parts
		 */
		private void getSnippetsOfCurrentSection(Part[] parts) {
			mListStringText = new ArrayList<String>();
			mListTimeEnd = new ArrayList<Integer>();
			mListTimeBegin = new ArrayList<Integer>();
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
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderSimpleModeActivity.this);
				ex.writeLogException();
			}

		}

		/**
		 * 
		 * Get all audio from current section.
		 * 
		 * @param currentSection
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
						DaisyEbookReaderSimpleModeActivity.this, mPath);
				ex.showDialogException(mIntentController);
				mOldMessage = Constants.ERROR_NO_AUDIO_FOUND;
				speakOut(Constants.ERROR_NO_AUDIO_FOUND);
				mIsFound = false;
			}
		}

		/**
		 * Speak message at the end of book.
		 */
		public void atEndOfBook() {
			speakOut(Constants.AT_THE_END);
			int currentTime = mPlayer.getCurrentPosition();
			if (currentTime == -1 || currentTime == mPlayer.getDuration() || currentTime == 0) {
				mIsRunable = false;
				mIsEndOf = true;
			}
			if (mCurrent != null) {
				mCurrent.setAtTheEnd(true);
				mSql.updateCurrentInformation(mCurrent);
			}
		}

		/**
		 * Speak message at the begin of book.
		 */
		public void atBeginOfBook() {
			speakOut(Constants.AT_THE_BEGIN);
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

				if (mCurrent != null) {
					mCurrent.setFirstNext(true);
					mCurrent.setFirstPrevious(false);
					mSql.updateCurrentInformation(mCurrent);
				}
			} else {
				navigationListener.atBeginOfBook();
			}

		}
	}

	@Override
	public void onClick(View v) {
	}

	/**
	 * Handle all gesture actions.
	 */
	private GestureListener gestureListener = new GestureListener() {
		private long startTime;

		@Override
		public void onGestureStart(int g) {
			startTime = java.lang.System.currentTimeMillis();
			Log.i("GESTURE", "onGestureStart" + startTime);
		}

		@Override
		public void onGestureFinish(int g) {
			try {
				long timeTaken = java.lang.System.currentTimeMillis() - startTime;
				Log.i("GESTURE", "onGestureTimeTaken" + timeTaken);
				// If user press and hold will go to table of contents.
				if (timeTaken > 1000) {
					mIsPlaying = mPlayer.isPlaying();
					if (mIsPlaying) {
						mPlayer.pause();
					}
					if (mCurrent != null) {
						updateCurrentInformation();
					} else {
						createCurrentInformation();
					}
					String path = getIntent().getStringExtra(Constants.DAISY_PATH);
					mIntentController.pushToTableOfContentsIntent(path, mNavigatorOfTableContents,
							getString(R.string.simple_mode));
				} else {
					switch (g) {
					case Gesture.CENTER:
						Log.i("GESTURE", "Action: CENTER");
						togglePlay();
						break;
					case Gesture.DOWN:
						Log.i("GESTURE", "Action: DOWN");
						if (mNavigator.hasNext()) {
							speakOut(Constants.NEXT_SECTION);
						}
						nextSection();
						break;
					case Gesture.UP:
						Log.i("GESTURE", "Action: UP");

						if (mNavigator.hasPrevious()) {
							speakOut(Constants.PREVIOUS_SECTION);
						}
						previousSection();
						break;
					case Gesture.LEFT:
						Log.i("GESTURE", "Action: LEFT");
						speakOut(Constants.PREVIOUS_SENTENCE);
						previousSentence();
						if (mPositionSentence > 0) {
							mPositionSentence -= 1;
							mHandler.removeCallbacks(mRunnalbe);
							mIsRunable = true;
							getCurrentPositionSentence();
						}
						break;
					case Gesture.RIGHT:
						Log.i("GESTURE", "Action: RIGHT");
						speakOut(Constants.NEXT_SENTENCE);
						nextSentence();
						if (mPositionSentence < mListTimeBegin.size() - 1) {
							mPositionSentence += 1;
							mHandler.removeCallbacks(mRunnalbe);
							mIsRunable = true;
							getCurrentPositionSentence();
						}
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderSimpleModeActivity.this);
				ex.writeLogException();
			}
		}

		@Override
		public void onGestureChange(int g) {
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
				nextSection();
				mPositionSentence -= 1;
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
			ex.writeLogException();
		}
	}

	/**
	 * Go to next section.
	 */
	private void nextSection() {
		boolean isPlaying = mPlayer.isPlaying();
		mController.next();
		if (!isPlaying) {
			setMediaPause();
		}
	}

	/**
	 * Go to previous sentence by seek to time of clip end before two units.
	 */
	private void previousSentence() {
		boolean isPlaying = mPlayer.isPlaying();
		try {
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
				Navigable n = mNavigator.previous();
				n = mNavigator.next();
				mNavigationListener.onNext((Section) n);
				mIsEndOf = false;
				mPlayer.seekTo(mListTimeBegin.get(mListTimeBegin.size() - 1));
				mPositionSentence = mListTimeBegin.size() - 1;
			}
			// this case for user press previous sentence.
			else if (mPositionSentence > 0) {
				mPlayer.seekTo(mListTimeBegin.get(mPositionSentence - 1));
			}
			// this case for user press previous sentence at the begin of
			// section.
			else {
				mController.previous();
				int sizeOfListEnd = mListTimeEnd.size();
				mPositionSentence = sizeOfListEnd - 1;
				if (sizeOfListEnd > 1) {
					mPlayer.seekTo(mListTimeEnd.get(sizeOfListEnd - 2));
				}
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
			ex.writeLogException();
		}
		// keep current state media player.
		if (!isPlaying) {
			setMediaPause();
		}
	}

	/**
	 * Go to previous section.
	 */
	private void previousSection() {
		mCurrent = mSql.getCurrentInformation();
		boolean isPlaying = mPlayer.isPlaying();
		mIsEndOf = false;
		if (mCurrent != null) {
			mCurrent.setAtTheEnd(false);
			mSql.updateCurrentInformation(mCurrent);
		}
		mController.previous();
		if (!isPlaying) {
			setMediaPause();
		}
	}

	/**
	 * Set media pause and remove call back
	 */
	private void setMediaPause() {
		mHandler.removeCallbacks(mRunnalbe);
		mPlayer.pause();
		mIsRunable = false;
	}

	long mTimePause = 0;

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
			mIsRunable = true;
			if (mPlayer.getCurrentPosition() != 0 && mListTimeEnd != null) {
				// if you pause while audio playing. You need to know time pause
				// to
				// high light text more correctly.
				mTimePause = mListTimeEnd.get(mPositionSentence) - mPlayer.getCurrentPosition();
			}
			mHandler.post(mRunnalbe);
		}
	}

	/**
	 * Toggles the Media Player between Play and Pause states.
	 */
	private void togglePlay() {
		if (mPlayer.isPlaying()) {
			setMediaPause();
			speakOut(Constants.PAUSE);
		} else {
			try {
				setMediaPlay();
				speakOut(Constants.PLAY);
			} catch (Exception e) {
				speakOut(Constants.ERROR_WRONG_FORMAT_AUDIO);
				PrivateException ex = new PrivateException(e,
						DaisyEbookReaderSimpleModeActivity.this);
				ex.writeLogException();

			}
		}
	}

	/**
	 * This function help to get current position sentence to support next
	 * sentence, previous sentence.
	 */
	private void getCurrentPositionSentence() {
		try {
			mRunnalbe = new Runnable() {
				@Override
				public void run() {
					if (mIsRunable) {
						int sizeOfStringText = mListStringText.size();
						for (int i = mPositionSentence; i < sizeOfStringText; i++) {
							if (mListTimeBegin.get(i) <= mPlayer.getCurrentPosition()
									+ mTimeForProcess
									&& mPlayer.getCurrentPosition() < mListTimeEnd.get(i)) {
								mPositionSentence = i;
								break;
							}
						}
					}
					if (mTimePause == 0) {
						int timeReadSentence = mListTimeEnd.get(mPositionSentence)
								- mListTimeBegin.get(mPositionSentence);
						mHandler.postDelayed(this, timeReadSentence);
					} else {
						// If user choose pause and play. 400 is time delay
						// when
						// you told on your phone.
						mHandler.postDelayed(this, mTimePause + mTimeForProcess);
					}
					mTimePause = 0;
				}
			};
			mHandler.post(mRunnalbe);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
			ex.writeLogException();
		}
	}

	/**
	 * This function will speak out message.
	 * 
	 * @param message
	 */
	private void speakOut(int message) {
		switch (message) {
		case Constants.ERROR_NO_AUDIO_FOUND:
			mTts.speak(getString(R.string.error_no_audio_found), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.SIMPLE_MODE:
			mTts.speak(getString(R.string.title_activity_daisy_ebook_reader_simple_mode),
					TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.ERROR_WRONG_FORMAT_AUDIO:
			mTts.speak(getString(R.string.error_wrong_format_audio), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.AT_THE_END:
			mTts.speak(getString(R.string.atEnd) + mBook.getTitle(), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.AT_THE_BEGIN:
			mTts.speak(getString(R.string.atBegin) + mBook.getTitle(), TextToSpeech.QUEUE_FLUSH,
					null);
			break;
		case Constants.NEXT_SECTION:
			mTts.speak(getString(R.string.next_section), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.PREVIOUS_SECTION:
			mTts.speak(getString(R.string.previous_section), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.NEXT_SENTENCE:
			mTts.speak(getString(R.string.next_sentence), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.PREVIOUS_SENTENCE:
			mTts.speak(getString(R.string.previous_sentence), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.PLAY:
			mTts.speak(getString(R.string.play), TextToSpeech.QUEUE_FLUSH, null);
			break;
		case Constants.PAUSE:
			mTts.speak(getString(R.string.pause), TextToSpeech.QUEUE_FLUSH, null);
			break;
		default:
			break;
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Must import because this activity implements
		// TextToSpeech.OnInitListener
	}

}