package org.androiddaisyreader.apps;

import java.util.List;
import java.util.Vector;

import org.androiddaisyreader.adapter.WebsiteAdapter;
import org.androiddaisyreader.model.Website;
import org.androiddaisyreader.utils.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The Class DaisyReaderDownloadSiteActivity.
 */
public class DaisyReaderDownloadSiteActivity extends Activity implements OnClickListener,
		TextToSpeech.OnInitListener {

	private Window mWindow;
	private ListView mListViewWebsite;
	private List<Website> listWebsite;
	private WebsiteAdapter websiteAdapter;
	private TextToSpeech mTts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_download_site);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_navigation_bar);
		startTts();
		mListViewWebsite = (ListView) findViewById(R.id.list_view_website);
		initListWebsite();
		websiteAdapter = new WebsiteAdapter(listWebsite, getLayoutInflater());
		mListViewWebsite.setAdapter(websiteAdapter);

		// set listener back button
		findViewById(R.id.imgBack).setOnClickListener(this);

		// set listener while touch on website
		mListViewWebsite.setOnItemClickListener(onItemWebsiteClick);
		mListViewWebsite.setOnItemLongClickListener(onItemWebsiteLongClick);

		// set title of this screen
		setScreenTitle();
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

	private OnItemClickListener onItemWebsiteClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mTts.speak(listWebsite.get(arg2).getSiteName(), TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	private OnItemLongClickListener onItemWebsiteLongClick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			String websiteUrl = listWebsite.get(arg2).getSiteURL();
			String websiteName = listWebsite.get(arg2).getSiteName();
			pushToWebsite(websiteUrl, websiteName);
			return false;
		}
	};

	/**
	 * Inits the list website.
	 */
	private void initListWebsite() {
		listWebsite = new Vector<Website>();
		Website website = null;
		website = new Website(this.getString(R.string.web_site_name_daisy_org),
				this.getString(R.string.web_site_url_daisy_org), 1);
		listWebsite.add(website);
		website = new Website(this.getString(R.string.web_site_name_htctu),
				this.getString(R.string.web_site_url_htctu), 2);
		listWebsite.add(website);

	}

	/**
	 * Sets the screen title.
	 */
	private void setScreenTitle() {
		TextView tvScreenTitle = (TextView) this.findViewById(R.id.screenTitle);
		tvScreenTitle.setOnClickListener(this);
		tvScreenTitle.setText(R.string.download_sites);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgBack:
			backToTopScreen();
			break;
		case R.id.screenTitle:
			backToTopScreen();
			break;
		default:
			break;
		}

	}

	/**
	 * Back to top screen.
	 */
	private void backToTopScreen() {
		Intent intent = new Intent(this, DaisyReaderLibraryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// Removes other Activities from stack
		startActivity(intent);
	}

	/**
	 * Push to list book of website.
	 */
	private void pushToWebsite(String websiteURL, String websiteName) {
		Intent intent = new Intent(this, DaisyReaderDownloadBooks.class);
		intent.putExtra(Constants.LINK_WEBSITE, websiteURL);
		intent.putExtra(Constants.NAME_WEBSITE, websiteName);
		this.startActivity(intent);
	}

	@Override
	protected void onResume() {
		mTts.speak(getString(R.string.download_sites), TextToSpeech.QUEUE_FLUSH, null);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		try {
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadSiteActivity.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
	}
}
