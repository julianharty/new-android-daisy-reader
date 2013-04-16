/**
 * This activity is simple mode which play audio.
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

import com.google.marvin.widget.GestureOverlay;
import com.google.marvin.widget.GestureOverlay.Gesture;
import com.google.marvin.widget.GestureOverlay.GestureListener;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

public class DaisyEbookReaderSimpleModeActivity extends Activity implements
		OnClickListener, TextToSpeech.OnInitListener {
	private boolean mIsFirstNext = false;
	private boolean mIsFirstPrevious = false;
	private TextToSpeech mTts;
	private BookContext mBookContext;
	private Daisy202Book mBook;
	private Navigator mNavigator;
	private Navigator mNavigatorOfTableContents;
	private NavigationListener mNavigationListener;
	private Controller mController;
	private AudioPlayerController mAudioPlayer;
	private AndroidAudioPlayer mAndroidAudioPlayer;
	private GestureOverlay mGestureOverlay;
	private MediaPlayer mPlayer;
	private ArrayList<Integer> mListEnd;
	private ArrayList<Integer> mListBegin;
	private IntentController mIntentController;
	private SharedPreferences mPreferences;
	private Window mWindow;
	private String mTime;
	private int mPositionSentence = 0;
	private boolean mIsRunable = true;
	private Runnable mRunnalbe;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_ebook_reader_simple_mode);
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mWindow = getWindow();
		mIntentController = new IntentController(
				DaisyEbookReaderSimpleModeActivity.this);
		mTts = new TextToSpeech(this, this);
		mNavigationListener = new NavigationListener();
		mController = new Controller(mNavigationListener);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.daisyReaderSimpleModeLayout);
		mGestureOverlay = new GestureOverlay(this, gestureListener);
		relativeLayout.addView(mGestureOverlay);
		setContentView(relativeLayout);
		mHandler = new Handler();
		openBook();
		try {
			String i = getIntent().getStringExtra(
					DaisyReaderConstants.POSITION_SECTION);
			mTime = getIntent().getStringExtra(DaisyReaderConstants.TIME);
			if (i != null) {
				mIsFirstNext = true;
				mIsFirstPrevious = true;
				Navigable n = null;
				int countLoop = Integer.valueOf(i);
				for (int j = 0; j < countLoop; j++) {
					n = mNavigator.next();
				}
				if (n instanceof Section) {
					mNavigationListener.onNext((Section) n);
				}
			} else {
				togglePlay();
			}

		} catch (Exception e) {
			mIntentController.pushToDialogError(
					getString(R.string.noPathFound), true);
		}
	}

	@Override
	public void onBackPressed() {
		mHandler.removeCallbacks(mRunnalbe);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.stop();
		}
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		mHandler.removeCallbacks(mRunnalbe);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		// get value of brightness from preference. Otherwise, get current
		// brightness from system.
		try {
			valueScreen = mPreferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		LayoutParams layoutpars = mWindow.getAttributes();
		layoutpars.screenBrightness = valueScreen / (float) 255;
		// apply attribute changes to this window
		mWindow.setAttributes(layoutpars);
		mNavigatorOfTableContents = new Navigator(mBook);
		super.onResume();
	}

	private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

		public void endOfAudio() {
			Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
			mController.next();
		}
	};

	/**
	 * open book from path
	 */
	private void openBook() {
		InputStream contents;
		try {
			String path = getIntent().getStringExtra(
					DaisyReaderConstants.DAISY_PATH);
			mBookContext = DaisyReaderUtils.openBook(path);
			String[] sp = path.split("/");
			contents = mBookContext.getResource(sp[sp.length - 1]);

			mAndroidAudioPlayer = new AndroidAudioPlayer(mBookContext);
			mAndroidAudioPlayer.addCallbackListener(audioCallbackListener);
			mAudioPlayer = new AudioPlayerController(mAndroidAudioPlayer);
			mPlayer = mAndroidAudioPlayer.getCurrentPlayer();

			mBook = NccSpecification.readFromStream(contents);
			mNavigator = new Navigator(mBook);
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
			Daisy202Section currentSection = new Daisy202Section.Builder()
					.setHref(section.getHref()).setContext(mBookContext)
					.build();

			StringBuilder audioListings = new StringBuilder();
			mListEnd = new ArrayList<Integer>();
			mListBegin = new ArrayList<Integer>();
			for (Part part : currentSection.getParts()) {
				for (int i = 0; i < part.getAudioElements().size(); i++) {
					Audio audioSegment = part.getAudioElements().get(i);
					mAudioPlayer.playFileSegment(audioSegment);
					audioListings.append(audioSegment.getAudioFilename() + ", "
							+ audioSegment.getClipBegin() + ":"
							+ audioSegment.getClipEnd() + "\n");
					mListBegin.add(audioSegment.getClipEnd());
					mListEnd.add(audioSegment.getClipEnd());
				}
			}
			// seek to time when user change from visual mode
			if (mTime != null) {
				mPlayer.seekTo(Integer.valueOf(mTime));
				mTime = null;
			}
			getCurrentPositionSentence();
		}

		public void atEndOfBook() {
			mTts.speak(getString(R.string.atEnd) + mBook.getTitle(),
					TextToSpeech.QUEUE_FLUSH, null);
		}

		public void atBeginOfBook() {
			mTts.speak(getString(R.string.atBegin) + mBook.getTitle(),
					TextToSpeech.QUEUE_FLUSH, null);
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
					mPositionSentence = 0;
					navigationListener.onNext((Section) n);
				}
			} else {
				navigationListener.atBeginOfBook();
			}
		}
	}

	@Override
	public void onClick(View v) {
	}

	private GestureListener gestureListener = new GestureListener() {
		private long startTime;

		@Override
		public void onGestureStart(int g) {
			startTime = java.lang.System.currentTimeMillis();
			Log.i("GESTURE", "onGestureStart" + startTime);
		}

		@Override
		public void onGestureFinish(int g) {
			long timeTaken = java.lang.System.currentTimeMillis() - startTime;
			Log.i("GESTURE", "onGestureTimeTaken" + timeTaken);
			// If user press and hold will go to table of contents.
			if (timeTaken > 1000) {
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
				}
				String path = getIntent().getStringExtra(
						DaisyReaderConstants.DAISY_PATH);
				mIntentController.pushToTableOfContentsIntent(path,
						mNavigatorOfTableContents,
						getString(R.string.simpleMode));
			} else {
				switch (g) {
				case Gesture.CENTER:
					Log.i("GESTURE", "Action: CENTER");
					togglePlay();
					break;
				case Gesture.DOWN:
					Log.i("GESTURE", "Action: DOWN");
					if (mNavigator.hasNext()) {
						mTts.speak(getString(R.string.nextSection),
								TextToSpeech.QUEUE_FLUSH, null);
					}
					nextSection();
					break;
				case Gesture.UP:
					Log.i("GESTURE", "Action: UP");

					if (mNavigator.hasPrevious()) {
						mTts.speak(getString(R.string.previousSection),
								TextToSpeech.QUEUE_FLUSH, null);
					}
					previousSection();
					break;
				case Gesture.LEFT:
					Log.i("GESTURE", "Action: LEFT");
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
					nextSentence();
					if (mPositionSentence < mListBegin.size() - 1) {
						mPositionSentence += 1;
						mHandler.removeCallbacks(mRunnalbe);
						mIsRunable = true;
						getCurrentPositionSentence();
						mHandler.postDelayed(mRunnalbe, 1000);

					}
					break;
				default:
					break;
				}
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
		int currentTime = mPlayer.getCurrentPosition();
		if (currentTime == 0 && !mNavigator.hasNext()) {
			mNavigationListener.atEndOfBook();
		} else if (mPositionSentence < mListBegin.size() - 1) {
			mTts.speak(getString(R.string.nextSentence),
					TextToSpeech.QUEUE_FLUSH, null);
			mPlayer.seekTo(mListBegin.get(mPositionSentence + 1));
		} else {
			nextSection();
			mPositionSentence -= 1;
		}
	}

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
		if (!mNavigator.hasPrevious() && mPositionSentence == 0) {
			mNavigationListener.atBeginOfBook();
		} else if (!mNavigator.hasNext()
				&& mPositionSentence == mListBegin.size() - 1) {
			// It is code to resolve previous sentence when the end
			// of the book.
			Navigable n = mNavigator.previous();
			n = mNavigator.next();
			mNavigationListener.onNext((Section) n);
			mTts.speak(getString(R.string.previousSentence),
					TextToSpeech.QUEUE_FLUSH, null);
			mPlayer.seekTo(mListEnd.get(mPositionSentence - 1));
		} else if (mPositionSentence > 0) {
			mTts.speak(getString(R.string.previousSentence),
					TextToSpeech.QUEUE_FLUSH, null);
			mPlayer.seekTo(mListBegin.get(mPositionSentence - 1));
		} else {
			mIsFirstPrevious = true;
			mController.previous();
			if (mListEnd.size() > 1) {
				mPlayer.seekTo(mListEnd.get(mListEnd.size() - 2));
			}
		}
	}

	private void previousSection() {
		boolean isPlaying = mPlayer.isPlaying();
		mController.previous();
		if (!isPlaying) {
			setMediaPause();
		}
	}

	private void setMediaPause() {
		mHandler.removeCallbacks(mRunnalbe);
		mTts.speak(getString(R.string.pause), TextToSpeech.QUEUE_FLUSH, null);
		mPlayer.pause();
		mIsRunable = false;
	}

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
		mHandler.post(mRunnalbe);
		mTts.speak(getString(R.string.play), TextToSpeech.QUEUE_FLUSH, null);
	}

	/**
	 * Toggles the Media Player between Play and Pause states.
	 */
	private void togglePlay() {
		if (mPlayer.isPlaying()) {
			setMediaPause();
		} else {
			try {
				setMediaPlay();
			} catch (Exception e) {
				mIntentController.pushToDialogError(
						getString(R.string.wrongFormat), true);
			}
		}
	}

	private void getCurrentPositionSentence() {
		mRunnalbe = new Runnable() {

			@Override
			public void run() {
				if (mIsRunable) {
					for (int i = mPositionSentence; i < mListBegin.size(); i++) {
						if (mListBegin.get(i) < mPlayer.getCurrentPosition() + 500
								&& mPlayer.getCurrentPosition() < mListEnd
										.get(i)) {
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
					mHandler.postDelayed(this, mListEnd.get(mPositionSentence)
							- mListBegin.get(mPositionSentence));
				}
			}
		};
		mHandler.post(mRunnalbe);
	}

	@Override
	public void onInit(int arg0) {
	}

}
