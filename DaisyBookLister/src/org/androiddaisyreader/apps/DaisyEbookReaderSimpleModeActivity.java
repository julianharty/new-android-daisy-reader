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
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import com.google.marvin.widget.GestureOverlay;
import com.google.marvin.widget.GestureOverlay.Gesture;
import com.google.marvin.widget.GestureOverlay.GestureListener;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DaisyEbookReaderSimpleModeActivity extends Activity implements
		OnClickListener, TextToSpeech.OnInitListener {
	public static boolean isFirstNext = false;
	public static boolean isFirstPrevious = true;
	private static boolean BENCHMARK_ACTIVITY = false;
	private TextToSpeech tts;
	private BookContext bookContext;
	private Daisy202Book book;
	private Navigator navigator;
	private NavigationListener navigationListener = new NavigationListener();
	private Controller controller = new Controller(navigationListener);
	private AudioPlayerController audioPlayer;
	private AndroidAudioPlayer androidAudioPlayer;
	private GestureOverlay gestureOverlay;
	private MediaPlayer player;
	private ArrayList<Integer> listIntEnd;
	private Object[] sections;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_ebook_reader_simple_mode);
		if (BENCHMARK_ACTIVITY) {
			Debug.startMethodTracing();
		}
		tts = new TextToSpeech(this, this);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.daisyReaderSimpleModeLayout);
		gestureOverlay = new GestureOverlay(this, gestureListener);
		relativeLayout.addView(gestureOverlay);
		setContentView(relativeLayout);
		openBook();
		try {
			sections = book.getChildren().toArray();
			int i = getIntent().getIntExtra(
					DaisyReaderConstants.POSITION_SECTION, -1);

			if (i != -1) {
				navigationListener.onNext((Section) sections[i]);
				isFirstNext = true;
				isFirstPrevious = true;
			}

		} catch (Exception e) {
			showDialogError(getString(R.string.noPathFound));
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(
				R.menu.activity_daisy_ebook_reader_simple_mode, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		if (player != null && player.isPlaying()) {
			player.stop();
		}
		tts.stop();
		tts.shutdown();
		super.onDestroy();
	}

	private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

		public void endOfAudio() {
			Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
			controller.next();
		}
	};

	// Push to activity table of content when user press and hold.
	private void pushToTableOfContentsIntent() {
		if (player.isPlaying()) {
			player.pause();
		}
		Intent i = new Intent(this, DaisyReaderTableOfContentsActivity.class);
		String path = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		ArrayList<String> listContents = DaisyReaderUtils.getContents(path);
		i.putStringArrayListExtra(DaisyReaderConstants.LIST_CONTENTS, listContents);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		this.startActivity(i);
	}

	// open book from path
	private void openBook() {
		InputStream contents;
		try {
			String path = getIntent().getStringExtra(
					DaisyReaderConstants.DAISY_PATH);
			bookContext = DaisyReaderUtils.openBook(path);
			String[] sp = path.split("/");
			contents = bookContext.getResource(sp[sp.length - 1]);

			androidAudioPlayer = new AndroidAudioPlayer(bookContext);
			androidAudioPlayer.addCallbackListener(audioCallbackListener);
			audioPlayer = new AudioPlayerController(androidAudioPlayer);
			player = androidAudioPlayer.getCurrentPlayer();

			book = NccSpecification.readFromStream(contents);
			Toast.makeText(getBaseContext(), book.getTitle(),
					Toast.LENGTH_SHORT).show();
			navigator = new Navigator(book);

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
					.setHref(section.getHref()).setContext(bookContext).build();

			StringBuilder audioListings = new StringBuilder();
			listIntEnd = new ArrayList<Integer>();
			for (Part part : currentSection.getParts()) {
				for (int i = 0; i < part.getAudioElements().size(); i++) {
					Audio audioSegment = part.getAudioElements().get(i);
					audioPlayer.playFileSegment(audioSegment);
					audioListings.append(audioSegment.getAudioFilename() + ", "
							+ audioSegment.getClipBegin() + ":"
							+ audioSegment.getClipEnd() + "\n");
					listIntEnd.add(audioSegment.getClipEnd());
				}
			}
		}

		public void atEndOfBook() {
			Toast.makeText(getBaseContext(),
					getString(R.string.atEnd) + book.getTitle(),
					Toast.LENGTH_SHORT).show();
		}

		public void atBeginOfBook() {
			Toast.makeText(getBaseContext(),
					getString(R.string.atBegin) + book.getTitle(),
					Toast.LENGTH_SHORT).show();
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

		//Go to next section
		public void next() {
			if (navigator.hasNext()) {
				if (isFirstNext) {
					/**
					 * Make sure no repeat section is playing.
					 */
					navigator.next();
					isFirstPrevious = true;
					isFirstNext = false;
				}
				n = navigator.next();
				if (n instanceof Section) {
					navigationListener.onNext((Section) n);
				}
				if (n instanceof Part) {
					String msg = "Part, id: " + ((Part) n).id;
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_SHORT).show();
				}

			} else {
				navigationListener.atEndOfBook();
			}
		}

		// Go to previous section
		public void previous() {
			if (navigator.hasPrevious()) {
				if (isFirstPrevious) {
					// Make sure the section is playing no repeat.
					navigator.previous();
					isFirstPrevious = false;
					isFirstNext = true;
				}
				n = navigator.previous();
				if (n instanceof Section) {
					navigationListener.onNext((Section) n);
				}
				if (n instanceof Part) {
					String msg = "Part, id: " + ((Part) n).id;
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_SHORT).show();
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
			startTime = System.currentTimeMillis();
			Log.i("GESTURE", "onGestureStart" + startTime);
		}

		@Override
		public void onGestureFinish(int g) {
			long timeTaken = System.currentTimeMillis() - startTime;
			int i;
			Log.i("GESTURE", "onGestureTimeTaken" + timeTaken);
			// If user press and hold will go to table of contents.
			if (timeTaken > 1000) {
				pushToTableOfContentsIntent();
			} else {
				switch (g) {
				case Gesture.CENTER:
					Log.i("GESTURE", "Action: CENTER");
					togglePlay();
					break;
				case Gesture.DOWN:
					Log.i("GESTURE", "Action: DOWN");
					tts.speak(getString(R.string.nextSection),
							TextToSpeech.QUEUE_FLUSH, null);
					Toast.makeText(getBaseContext(),
							getString(R.string.nextSection),
							Toast.LENGTH_SHORT).show();
					i = player.getCurrentPosition();
					if (isFirstNext && isFirstPrevious) {
						controller.next();
						player.seekTo(i);
					}
					controller.next();
					break;
				case Gesture.UP:
					Log.i("GESTURE", "Action: UP");
					tts.speak(getString(R.string.previousSection),
							TextToSpeech.QUEUE_FLUSH, null);
					Toast.makeText(getBaseContext(),
							getString(R.string.previousSection),
							Toast.LENGTH_SHORT).show();
					i = player.getCurrentPosition();
					if (isFirstNext && isFirstPrevious) {
						controller.previous();
						player.seekTo(i);
					}
					controller.previous();
					break;
				case Gesture.LEFT:
					Log.i("GESTURE", "Action: LEFT");
					tts.speak(getString(R.string.previousSentence),
							TextToSpeech.QUEUE_FLUSH, null);
					Toast.makeText(getBaseContext(),
							getString(R.string.previousSentence),
							Toast.LENGTH_SHORT).show();
					previousSentence();
					break;
				case Gesture.RIGHT:
					Log.i("GESTURE", "Action: RIGHT");
					tts.speak(getString(R.string.nextSentence),
							TextToSpeech.QUEUE_FLUSH, null);
					Toast.makeText(getBaseContext(),
							getString(R.string.nextSentence),
							Toast.LENGTH_SHORT).show();
					nextSentence();
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

	// Toggles the Media Player between Play and Pause states.
	public void togglePlay() {
		if (player.isPlaying()) {
			tts.speak(getString(R.string.pause),
					TextToSpeech.QUEUE_FLUSH, null);
			Toast.makeText(getBaseContext(), getString(R.string.pause),
					Toast.LENGTH_SHORT).show();
			player.pause();
		} else {
			tts.speak(getString(R.string.play),
					TextToSpeech.QUEUE_FLUSH, null);
			Toast.makeText(getBaseContext(), getString(R.string.play),
					Toast.LENGTH_SHORT).show();
			try {
				player.start();
			} catch (Exception e) {
				showDialogError(getString(R.string.wrongFormat));
			}
		}
	}

	// Go to next sentence by seek to time of clip end nearest position.
	private void nextSentence() {
		int currentTime = player.getCurrentPosition();
		if (currentTime == 0) {
			navigationListener.atEndOfBook();
		} else {
			for (int i = 0; i < listIntEnd.size(); i++) {
				if (currentTime < listIntEnd.get(i)) {
					if (i == listIntEnd.size() - 2) {
						navigationListener.atEndOfBook();
					} else {
						player.seekTo(listIntEnd.get(i));
						break;
					}
				}
			}
		}
	}
	
	//Go to previous sentence by seek to time of clip end before two units.
	private void previousSentence() {
		int currentTime = player.getCurrentPosition();
		if (currentTime == 0) {
			navigationListener.atEndOfBook();
		} else {
			for (int i = 0; i < listIntEnd.size(); i++) {
				if (currentTime < listIntEnd.get(i)) {
					if (i < 2) {
						navigationListener.atBeginOfBook();
					} else {
						player.seekTo(listIntEnd.get(i - 2));
						break;
					}
				}
			}
		}
	}

	@Override
	public void onInit(int arg0) {
	}

	public void showDialogError(String message) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog);
		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				onBackPressed();
			}
		});
		dialog.show();
	}

}