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

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DaisyEbookReaderVisualModeActivity extends Activity implements
		TextToSpeech.OnInitListener {

	public static boolean isFirstNext = false;
	public static boolean isFirstPrevious = true;
	private BookContext bookContext;
	private Daisy202Book book;
	private Navigator navigator;
	private NavigationListener navigationListener = new NavigationListener();
	private Controller controller = new Controller(navigationListener);
	private AudioPlayerController audioPlayer;
	private AndroidAudioPlayer androidAudioPlayer;
	private MediaPlayer player;
	private Object[] sections;
	private TextView contents;
	private ImageButton imgButton;
	private IntentController intentController;
	private ScrollView scrollView;
	private int start;
	private Spannable wordtoSpan;
	private Runnable r;
	private Handler mHandler;
	private ArrayList<String> listStringText;
	private ArrayList<Integer> listBegin;
	private ArrayList<Integer> listEnd;
	private ArrayList<Integer> listIntEnd;
	private String bookTitle;
	private String hrefSection;
	private int time;
	private String path;
	private SharedPreferences preferences;
	private Window window;
	private int screenWidth;
	private int screenHeight;
	private int numberOfScroll;
	private int numberOfChar; 
	private int numberOfCharOnScreen;
	private int positionOfScrollView;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader_visual_mode);
		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		window = getWindow();
		window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		TextView tvBookTitle = (TextView) this.findViewById(R.id.bookTitle);
		path = getIntent().getStringExtra(
				DaisyReaderConstants.DAISY_PATH);
		String[] title = path.split("/");
		bookTitle = title[title.length - 2];
		tvBookTitle.setText(bookTitle);
		intentController = new IntentController(this);
		ImageView imgTableOfContents = (ImageView) this
				.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);

		ImageView imgBookmark = (ImageView) this.findViewById(R.id.imgBookmark);
		imgBookmark.setOnClickListener(imgBookmarkClick);

		openBook();
		contents = (TextView) this.findViewById(R.id.contents);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		imgButton = (ImageButton) this.findViewById(R.id.btnPlay);
		imgButton.setOnClickListener(imgButtonClick);
		
		//Event for buttons on navigation.
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
		//check if user play daisybook from table of contents or bookmark
		try {
			sections = book.getChildren().toArray();
			int i = getIntent().getIntExtra(
					DaisyReaderConstants.POSITION_SECTION, -1);
			time = getIntent().getIntExtra(DaisyReaderConstants.TIME, -1);

			if (i != -1) {
				navigationListener.onNext((Section) sections[i]);
				isFirstNext = true;
				isFirstPrevious = true;
			}

		} catch (Exception e) {
			intentController.pushToDialogError(getString(R.string.noPathFound));
		}
		togglePlay();
	}

	private OnClickListener imgButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			togglePlay();
		}
	};

	private OnClickListener btnNextSentenceClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			nextSentence();
		}
	};

	private OnClickListener btnNextSectionClick = new OnClickListener() {
		int i;

		@Override
		public void onClick(View v) {
			i = player.getCurrentPosition();
			if (isFirstNext && isFirstPrevious) {
				controller.next();
				player.seekTo(i);
			}
			controller.next();
		}
	};

	private OnClickListener btnPreviousSectionClick = new OnClickListener() {
		int i;

		@Override
		public void onClick(View v) {
			i = player.getCurrentPosition();
			if (isFirstNext && isFirstPrevious) {
				controller.previous();
				player.seekTo(i);
			}
			controller.previous();
		}
	};

	private OnClickListener btnPreviousSentenceClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			previousSentence();
		}
	};

	private OnClickListener imgTableOfContentsClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				if (player.isPlaying()) {
					player.pause();
				}
				intentController.pushToTableOfContentsIntent(path,
						getString(R.string.visualMode));

			} catch (Exception e) {
				intentController
						.pushToDialogError(getString(R.string.noPathFound));
			}
		}
	};

	private OnClickListener imgBookmarkClick = new OnClickListener() {

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			String sentence = null;
			int time = player.getCurrentPosition();
			int positionSection = 0;
			if (player.isPlaying()) {
				imgButton.setImageResource(R.drawable.media_play);
				player.pause();
			}
			if (listStringText != null) {
				for (int i = 0; i < listStringText.size(); i++) {
					if (listBegin.get(i) < player.getCurrentPosition() + 500
							&& time < listEnd.get(i)) {
						sentence = listStringText.get(i);
					}
				}
				for (positionSection = 0; positionSection < sections.length; positionSection++) {
					Section tmp = (Section) sections[positionSection];
					if (tmp.getHref().equals(hrefSection))
						break;
				}
			}
			Bookmark bookmark = new Bookmark(bookTitle, sentence, time,
					positionSection, 0, "");
			intentController
					.pushToDaisyReaderBookmarkIntent(bookmark, getIntent()
							.getStringExtra(DaisyReaderConstants.DAISY_PATH));
 		}

	};

	@Override
	public void onBackPressed() {
		mHandler.removeCallbacks(r);
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
		if (player.isPlaying()) {
			player.pause();
			imgButton.setImageResource(R.drawable.media_play);
		}
		switch (item.getItemId()) {
		// go to table of contents
		case R.id.menu_table:
			if (player.isPlaying()) {
				player.pause();
			}
			intentController.pushToTableOfContentsIntent(getIntent()
					.getStringExtra(DaisyReaderConstants.DAISY_PATH),
					getString(R.string.visualMode));
			return true;
			// go to simple mode
		case R.id.menu_simple:
			intentController.pushToDaisyEbookReaderSimpleModeIntent(getIntent()
					.getStringExtra(DaisyReaderConstants.DAISY_PATH));
			return true;
			// go to settings
		case R.id.menu_settings:
			intentController.pushToDaisyReaderSettingIntent();
			return true;
			// go to book marks
		case R.id.menu_bookmarks:
			// intentController.pushToDaisyReaderBookmarkIntent();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		if (player != null && player.isPlaying()) {
			player.stop();
		}
		mHandler.removeCallbacks(r);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		ContentResolver cResolver = getContentResolver();
		int valueBrightnessScreen = 0;
		//get value of brightness from preference. Otherwise, get current brightness from system.
		try {
			valueBrightnessScreen = preferences.getInt(DaisyReaderConstants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		LayoutParams layoutpars = window.getAttributes();
		layoutpars.screenBrightness = valueBrightnessScreen / (float) 255;
		// apply attribute changes to this window
		window.setAttributes(layoutpars);
		int fontSize = preferences.getInt(DaisyReaderConstants.FONT_SIZE, 12);
		contents.setTextSize(fontSize);
		// apply text color
		int textColor = preferences.getInt(DaisyReaderConstants.TEXT_COLOR, contents.getCurrentTextColor());
		contents.setTextColor(textColor);
		
		numberOfCharOnScreen = ((screenWidth/fontSize)*((screenHeight/fontSize)+screenHeight/(fontSize*10)))/2;
		numberOfScroll = 1;
		numberOfChar = 0;
		positionOfScrollView = 0;
		super.onResume();
	}
	
	private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

		public void endOfAudio() {
			Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
			controller.next();
		}
	};

	/**
	 * open book from path
	 */
	private void openBook() {
		InputStream contents;
		try {
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
			try {
				listStringText = new ArrayList<String>();
				listBegin = new ArrayList<Integer>();
				listEnd = new ArrayList<Integer>();
				Daisy202Section currentSection = new Daisy202Section.Builder()
						.setHref(section.getHref()).setContext(bookContext)
						.build();
				hrefSection = section.getHref();
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
					listBegin
							.add(part.getAudioElements().get(0).getClipBegin());
					listEnd.add(part.getAudioElements()
							.get(part.getAudioElements().size() - 1)
							.getClipEnd());
				}

				contents.setText(snippetText.toString(),
						TextView.BufferType.SPANNABLE);

				StringBuilder audioListings = new StringBuilder();
				listIntEnd = new ArrayList<Integer>();
				for (Part part : currentSection.getParts()) {
					for (int i = 0; i < part.getAudioElements().size(); i++) {
						Audio audioSegment = part.getAudioElements().get(i);
						audioPlayer.playFileSegment(audioSegment);
						audioListings.append(audioSegment.getAudioFilename()
								+ ", " + audioSegment.getClipBegin() + ":"
								+ audioSegment.getClipEnd() + "\n");
						listIntEnd.add(audioSegment.getClipEnd());
					}
				}
				imgButton.setImageResource(R.drawable.media_pause);
				start = 0;
				wordtoSpan = (Spannable) contents.getText();
				//seek to time when user loading from book mark.
				if (time != -1) {
					player.seekTo(time);
					time = -1;
					togglePlay();
				}
				r = new Runnable() {

					@Override
					public void run() {
						String fullText = contents.getText().toString();
						if(scrollView.getScrollY() != positionOfScrollView)
						{
							scrollView.scrollTo(0, positionOfScrollView);
						}
						for (int i = 0; i < listStringText.size(); i++) {
							if (listBegin.get(i) < player.getCurrentPosition() + 500
									&& player.getCurrentPosition() < listEnd
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
								contents.setText(wordtoSpan);
								numberOfChar = start + listStringText.get(i).length();
								if(numberOfChar>numberOfCharOnScreen*numberOfScroll)
								{
									positionOfScrollView = (screenHeight-300)*numberOfScroll + 100;
									scrollView.scrollTo(0, positionOfScrollView);
									numberOfScroll++;
								}
							}
						}
						mHandler.postDelayed(this, 5000);
					}
				};
				mHandler.post(r);
			} catch (Exception e) {
				intentController
						.pushToDialogError(getString(R.string.wrongFormat));
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

		// Go to next section
		public void next() {
			if (navigator.hasNext()) {
				if (isFirstNext) {
					// Make sure no repeat section is playing.
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

		/**
		 * Go to previous section
		 */
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

	/**
	 * Toggles the Media Player between Play and Pause states.
	 */
	public void togglePlay() {
		if (player.isPlaying()) {
			Toast.makeText(getBaseContext(), getString(R.string.pause),
					Toast.LENGTH_SHORT).show();
			player.pause();
			mHandler.removeCallbacks(r);
			imgButton.setImageResource(R.drawable.media_play);
		} else {
			Toast.makeText(getBaseContext(), getString(R.string.play),
					Toast.LENGTH_SHORT).show();
			try {
				player.start();
				imgButton.setImageResource(R.drawable.media_pause);
			} catch (Exception e) {
				intentController
						.pushToDialogError(getString(R.string.wrongFormat));
				onBackPressed();
			}

		}
	}

	/**
	 * Go to next sentence by seek to time of clip end nearest position.
	 */
	private void nextSentence() {
		int currentTime = player.getCurrentPosition();
		if (currentTime == 0) {
			navigationListener.atEndOfBook();
		} else {
			for (int i = 0; i < listEnd.size(); i++) {
				if (currentTime < listEnd.get(i)) {
					if (i == listEnd.size() - 2) {
						navigationListener.atEndOfBook();
					} else {
						player.seekTo(listEnd.get(i));
						break;
					}
				}
			}
		}
	}

	/**
	 * Go to previous sentence by seek to time of clip end before two units.
	 */
	private void previousSentence() {
		int currentTime = player.getCurrentPosition();
		if (currentTime == 0) {
			navigationListener.atEndOfBook();
		} else {
			for (int i = 0; i < listEnd.size(); i++) {
				if (currentTime < listEnd.get(i)) {
					if (i < 2) {
						navigationListener.atBeginOfBook();
					} else {
						player.seekTo(listEnd.get(i - 2));
						break;
					}
				}
			}
		}
	}

	@Override
	public void onInit(int arg0) {
	}
}
