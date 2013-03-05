package org.androiddaisyreader.apps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Daisy202Section;
import org.androiddaisyreader.model.FileSystemContext;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.model.ZippedBookContext;
import org.androiddaisyreader.player.AndroidAudioPlayer;
import org.androiddaisyreader.utils.DaisyReaderContants;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

public class DaisyBookListerActivity extends Activity implements
		TextToSpeech.OnInitListener {
	private static final boolean BENCHMARK_ACTIVITY = false;
	private BookContext bookContext;
	private EditText filename;
	private Button nextSection;
	private Daisy202Book book;
	private TextView sectionTitle;
	private Navigator navigator;
	private NavigationListener navigationListener = new NavigationListener();
	private Controller controller = new Controller(navigationListener);
	private TextView snippets;
	private AudioPlayerController audioPlayer;
	private AndroidAudioPlayer androidAudioPlayer;
	private TextToSpeech tts;

	int start;
	int maxScrollView;
	Spannable wordtoSpan;
	ScrollView scrollView;
	MediaPlayer player;
	Runnable r;
	Handler mHandler;
	private static final int MY_DATA_CHECK_CODE = 1234;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button open = (Button) findViewById(R.id.openbook);
		open.setOnClickListener(openListener);

		filename = (EditText) findViewById(R.id.filename);

		nextSection = (Button) findViewById(R.id.nextsection);
		nextSection.setOnClickListener(nextSectionListener);

		sectionTitle = (TextView) findViewById(R.id.sectiontitle);

		snippets = (TextView) findViewById(R.id.words);

		if (BENCHMARK_ACTIVITY) {
			Debug.startMethodTracing();
		}

		Button browserFile = (Button) findViewById(R.id.browserFile);
		browserFile.setOnClickListener(browserFileListener);
		browserFile.setOnLongClickListener(browserFileLongListener);

		tts = new TextToSpeech(this, this);

		String path = getIntent().getStringExtra("daisyPath");
		String nccPath = getIntent().getStringExtra("daisyNccFile");
		if (path != null || nccPath != null)
			filename.setText(path + nccPath);
		scrollView = (ScrollView) findViewById(R.id.scrollView);

		Button exit = (Button) findViewById(R.id.exit);
		exit.setOnClickListener(exitListener);
		exit.setOnLongClickListener(exitLongListener);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		if (DaisyReaderContants.isFirstRun) {
			tts.speak("Welcome to Daisy Reader Application.",
					TextToSpeech.QUEUE_FLUSH, null);
			DaisyReaderContants.isFirstRun = false;
		}
	}

	@Override
	protected void onPause() {
		if (BENCHMARK_ACTIVITY) {
			Debug.stopMethodTracing();
		}
		super.onPause();
	}

	private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

		public void endOfAudio() {
			Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
			controller.next();
		}
	};

	private void pushToBrowserFileIntent() {
		Intent daisyBookBrowserFile = new Intent(this,
				DaisyBookBrowserFileActivity.class);
		startActivity(daisyBookBrowserFile);
		finish();
	}

	private OnLongClickListener browserFileLongListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			if(player != null)
			{
				player.stop();
				finish();
			}
			pushToBrowserFileIntent();
			return false;
		}
	};

	private OnClickListener browserFileListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tts.speak("Browser file", TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	private OnClickListener exitListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tts.speak("Exit application", TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	private OnLongClickListener exitLongListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			int pid = android.os.Process.myPid(); 
			android.os.Process.killProcess(pid);
			return false;
		}
	};

	private OnClickListener nextSectionListener = new OnClickListener() {
		public void onClick(View v) {
			sectionTitle.setEnabled(true);
			controller.next();
		}
	};

	private OnClickListener openListener = new OnClickListener() {

		public void onClick(View v) {
			InputStream contents;
			try {
				bookContext = openBook(filename.getText().toString());
				contents = bookContext.getResource("ncc.html");

				androidAudioPlayer = new AndroidAudioPlayer(bookContext);
				androidAudioPlayer.addCallbackListener(audioCallbackListener);
				audioPlayer = new AudioPlayerController(androidAudioPlayer);

				book = NccSpecification.readFromStream(contents);
				Toast.makeText(getBaseContext(), book.getTitle(),
						Toast.LENGTH_LONG).show();

				nextSection.setEnabled(true);
				navigator = new Navigator(book);

			} catch (Exception e) {
				// TODO 20120515 (jharty): Add test for SDCARD being available
				// so we can tell the user...
				e.printStackTrace();
			}
		}
	};

	private static BookContext openBook(String filename) throws IOException {
		BookContext bookContext;

		if (filename.endsWith(".zip")) {
			bookContext = new ZippedBookContext(filename);
		} else {
			File directory = new File(filename);
			bookContext = new FileSystemContext(directory.getParent());
			directory = null;
		}
		return bookContext;
	}

	/**
	 * Listens to Navigation Events.
	 * 
	 * @author Julian Harty
	 */
	ArrayList<String> listStringText;
	ArrayList<Integer> listIntBegin;
	ArrayList<Integer> listIntEnd;

	private class NavigationListener {
		public void onNext(Section section) {
			listStringText = new ArrayList<String>();
			listIntBegin = new ArrayList<Integer>();
			listIntEnd = new ArrayList<Integer>();
			sectionTitle.setText(section.getTitle());

			Daisy202Section currentSection = new Daisy202Section.Builder()
					.setHref(section.getHref()).setContext(bookContext).build();

			StringBuilder snippetText = new StringBuilder();

			for (Part part : currentSection.getParts()) {
				for (int i = 0; i < part.getSnippets().size(); i++) {
					if (i > 0) {
						snippetText.append(" ");
					}
					snippetText.append(part.getSnippets().get(i).getText());
					listStringText.add(part.getSnippets().get(i).getText());
				}
				snippetText.append(" ");
				listIntBegin.add(part.getAudioElements().get(0).getClipBegin());
				listIntEnd.add(part.getAudioElements()
						.get(part.getAudioElements().size() - 1).getClipEnd());
			}

			snippets.setText(snippetText.toString(),
					TextView.BufferType.SPANNABLE);
			StringBuilder audioListings = new StringBuilder();

			for (Part part : currentSection.getParts()) {
				for (int i = 0; i < part.getAudioElements().size(); i++) {
					Audio audioSegment = part.getAudioElements().get(i);

					audioPlayer.playFileSegment(audioSegment);
					audioListings.append(audioSegment.getAudioFilename() + ", "
							+ audioSegment.getClipBegin() + ":"
							+ audioSegment.getClipEnd() + "\n");
				}
			}

			
			start = 0;
			maxScrollView = scrollView.getMaxScrollAmount();
			wordtoSpan = (Spannable) snippets.getText();
			player = androidAudioPlayer.getCurrentPlayer();
			player.start();
			r = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String fullText = snippets.getText().toString();
					for (int i = 0; i < listStringText.size(); i++) {
						if (listIntBegin.get(i) < player.getCurrentPosition() + 500
								&& player.getCurrentPosition() < listIntEnd
										.get(i)) {
							start = fullText.indexOf(listStringText.get(i));
							if (i > 0) {
								wordtoSpan.setSpan(new BackgroundColorSpan(
										0x00000000), 0, start,
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
							wordtoSpan.setSpan(new BackgroundColorSpan(
									0xFFFFFF00), start, start
									+ listStringText.get(i).length(),
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							snippets.setText(wordtoSpan);
							int positionLine = snippets.getLayout().getLineTop(
									i);
							int positionScrollView = scrollView.getScrollY();
							if (i > 0 && i < listStringText.size() - 1) {
								if (positionScrollView > positionLine) {
									scrollView.scrollTo(0, snippets.getLayout()
											.getLineTop(i - 1));
								}
								if (positionScrollView
										+ scrollView.getMaxScrollAmount() < maxScrollView) {
									scrollView.scrollTo(0, snippets.getLayout()
											.getLineTop(i - 1));
								}

								if (positionLine > maxScrollView) {
									scrollView.scrollTo(0, snippets.getLayout()
											.getLineTop(i - 1));
									maxScrollView = maxScrollView
											+ scrollView.getMaxScrollAmount();
								}
							}
						}
					}
					if(!navigator.hasNext())
					{
						mHandler.removeCallbacks(this);
					}
					mHandler.postDelayed(this, 5000);
				}
			};
			mHandler.post(r);
		}

		public void atEndOfBook() {
			Toast.makeText(getBaseContext(), "At end of " + book.getTitle(),
					Toast.LENGTH_LONG).show();
			nextSection.setEnabled(false);
			sectionTitle.setEnabled(false);
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

		public void next() {
			if (navigator.hasNext()) {
				mHandler = new Handler();
				mHandler.removeCallbacks(r);
				n = navigator.next();
				if (n instanceof Section) {
					navigationListener.onNext((Section) n);
				}

				if (n instanceof Part) {
					String msg = "Part, id: " + ((Part) n).id;
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_SHORT);
				}
			} else {
				navigationListener.atEndOfBook();
			}
		}
	}
}