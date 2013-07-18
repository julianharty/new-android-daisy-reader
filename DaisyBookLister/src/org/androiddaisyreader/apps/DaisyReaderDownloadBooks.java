package org.androiddaisyreader.apps;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;

import org.androiddaisyreader.adapter.DaisyBookAdapter;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.androiddaisyreader.utils.Constants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The Class DaisyReaderDownloadBooks.
 */
@SuppressLint("NewApi")
public class DaisyReaderDownloadBooks extends Activity implements OnClickListener,
		TextToSpeech.OnInitListener {

	/** The m window. */
	private Window mWindow;
	private String mLink;
	private String mWebsiteName;
	private SQLiteDaisyBookHelper mSql;
	private DaisyBookAdapter mDaisyBookAdapter;
	private MetaDataHandler mMetadata;
	public String mName;
	private DownloadFileFromURL mTask;
	private ArrayList<DaisyBook> mlistDaisyBook;
	private ArrayList<DaisyBook> mListDaisyBookOriginal;
	private DaisyBook mDaisyBook;
	private EditText mTextSearch;
	public final static String mPath = Environment.getExternalStorageDirectory().toString()
			+ Constants.FOLDER_DOWNLOADED + "/";
	private TextToSpeech mTts;
	private ProgressDialog mProgressDialog;
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_download_books);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_navigation_bar);
		startTts();
		// initial back button
		mTextSearch = (EditText) findViewById(R.id.edit_text_search);
		findViewById(R.id.imgBack).setOnClickListener(this);

		mLink = getIntent().getStringExtra(Constants.LINK_WEBSITE);
		mWebsiteName = getIntent().getStringExtra(Constants.NAME_WEBSITE);

		// set title of this screen
		setScreenTitle();

		mSql = new SQLiteDaisyBookHelper(DaisyReaderDownloadBooks.this);
		mSql.DeleteAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		createDownloadData();
		mlistDaisyBook = mSql.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
		mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderDownloadBooks.this, mlistDaisyBook);
		ListView listDownload = (ListView) findViewById(R.id.list_view_download_books);
		listDownload.setAdapter(mDaisyBookAdapter);
		listDownload.setOnItemClickListener(onItemClick);
		listDownload.setOnItemLongClickListener(onItemLongClick);
		handleSearchBook();
		mListDaisyBookOriginal = new ArrayList<DaisyBook>(mlistDaisyBook);
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
	 * Sets the screen title.
	 */
	private void setScreenTitle() {
		TextView tvScreenTitle = (TextView) this.findViewById(R.id.screenTitle);
		tvScreenTitle.setOnClickListener(this);
		tvScreenTitle.setText(mWebsiteName.length() != 0 ? mWebsiteName : "");
	}

	/**
	 * Wirte data to sqlite from metadata
	 */
	private void createDownloadData() {
		try {
			InputStream databaseInputStream = new FileInputStream(
					Constants.FOLDER_CONTAIN_METADATA
							+ Constants.META_DATA_FILE_NAME);
			mMetadata = new MetaDataHandler();
			NodeList nList = mMetadata.ReadDataDownloadFromXmlFile(databaseInputStream, mLink);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String author = eElement.getElementsByTagName(Constants.ATT_AUTHOR)
							.item(0).getTextContent();
					String publisher = eElement
							.getElementsByTagName(Constants.ATT_PUBLISHER).item(0)
							.getTextContent();
					String path = eElement.getAttribute(Constants.ATT_LINK);
					String title = eElement.getElementsByTagName(Constants.ATT_TITLE)
							.item(0).getTextContent();
					String date = eElement.getElementsByTagName(Constants.ATT_DATE)
							.item(0).getTextContent();
					DaisyBook daisyBook = new DaisyBook("", title, path, author, publisher, date, 1);
					mSql.addDaisyBook(daisyBook, Constants.TYPE_DOWNLOAD_BOOK);
				}
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
			ex.writeLogException();
		}
	}

	private OnItemClickListener onItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			mTts.speak(mlistDaisyBook.get(arg2).getTitle(), TextToSpeech.QUEUE_FLUSH, null);
		}
	};

	private OnItemLongClickListener onItemLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			boolean isConnected = DaisyBookUtil.getConnectivityStatus(DaisyReaderDownloadBooks.this) != Constants.TYPE_NOT_CONNECTED;
			if (isConnected) {
				if (checkFolderIsExist()) {
					mDaisyBook = mlistDaisyBook.get(position);
					String params[] = { mDaisyBook.getPath() };
					mTask = new DownloadFileFromURL();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
					} else {
						mTask.execute(params);
					}
				}
			} else {
				IntentController intent = new IntentController(DaisyReaderDownloadBooks.this);
				intent.pushToDialog(
						DaisyReaderDownloadBooks.this.getString(R.string.error_connect_internet),
						DaisyReaderDownloadBooks.this.getString(R.string.error_title),
						R.drawable.error, false, false, null);
			}
			return false;
		}
	};

	/**
	 * Create folder if not exists
	 * 
	 * @return
	 */
	private boolean checkFolderIsExist() {
		boolean result = false;
		File folder = new File(mPath);
		result = folder.exists();
		if (!result) {
			result = folder.mkdir();
		}
		return result;
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
	 * handle search book when text changed.
	 */
	private void handleSearchBook() {
		mTextSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mListDaisyBookOriginal.size() != 0) {
					mlistDaisyBook = DaisyBookUtil.searchBookWithText(s, mlistDaisyBook, mListDaisyBookOriginal);
					mDaisyBookAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, Integer, Boolean> {
		/**
		 * Before starting background thread Show Progress Bar Dialog
		 * */

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(DaisyReaderDownloadBooks.this);
			mProgressDialog.setMessage(DaisyReaderDownloadBooks.this
					.getString(R.string.message_downloading_file));
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setProgress(0);
			mProgressDialog.setMax(100);
			mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					runOnUiThread(new Runnable() {
						public void run() {
							pushToDialogOptions(DaisyReaderDownloadBooks.this
									.getString(R.string.message_confirm_exit_download));
						}
					});
				}
			});
			mProgressDialog.show();
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected Boolean doInBackground(String... params) {
			int count;
			boolean result = false;
			try {
				String link = params[0];
				java.net.URL url = new java.net.URL(link);
				URLConnection conection = url.openConnection();
				conection.connect();
				// this will be useful so that you can show a tipical 0-100%
				// progress bar
				int lenghtOfFile = conection.getContentLength();
				// download the file
				InputStream input = new BufferedInputStream(url.openStream(), 8192);
				// Output stream
				String splitString[] = link.split("/");
				mName = splitString[splitString.length - 1];
				OutputStream output = new FileOutputStream(mPath + mName);
				byte data[] = new byte[1024];
				long total = 0;
				while ((count = input.read(data)) != -1) {
					if (isCancelled()) {
						File file = new File(mPath + mName);
						file.delete();
						break;
					} else {
						total += count;
						// publishing the progress....
						// After this onProgressUpdate will be called
						publishProgress((int) ((total * 100) / lenghtOfFile));
						// writing data to file
						output.write(data, 0, count);
					}
				}
				// flushing output
				output.flush();
				// closing streams
				output.close();
				input.close();
				result = true;
			} catch (Exception e) {
				result = false;
				mTask.cancel(true);
				mProgressDialog.dismiss();
				// show error message if an error occurs while connecting to the
				// resource
				final PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
				runOnUiThread(new Runnable() {
					public void run() {
						IntentController intent = new IntentController(
								DaisyReaderDownloadBooks.this);
						ex.showDialogDowloadException(intent);
					}
				});
			}

			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mProgressDialog.setProgress(values[0]);
		}

		/**
		 * After completing background task Dismiss the progress dialogs
		 * **/
		@Override
		protected void onPostExecute(Boolean result) {
			if (alertDialog != null) {
				alertDialog.dismiss();
			}
			mProgressDialog.dismiss();
			try {
				if (result == true) {
					DaisyBook daisyBook = new DaisyBook();
					daisyBook.setAuthor(mDaisyBook.getAuthor());
					daisyBook.setDate(mDaisyBook.getDate());
					daisyBook.setPath(mPath + mName);
					daisyBook.setPublisher(mDaisyBook.getPublisher());
					daisyBook.setSort(mDaisyBook.getSort());
					daisyBook.setTitle(mDaisyBook.getTitle());
					if (mSql.addDaisyBook(daisyBook, Constants.TYPE_DOWNLOADED_BOOK) == true) {
						Intent intent = new Intent(DaisyReaderDownloadBooks.this,
								DaisyReaderDownloadedBooks.class);
						DaisyReaderDownloadBooks.this.startActivity(intent);
					}
				}
			} catch (Exception e) {
				PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
				ex.writeLogException();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (mTask != null) {
			mTask.cancel(false);
		}
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		// set screen bright when user change it in setting
		Window window = getWindow();
		ContentResolver cResolver = getContentResolver();
		int valueScreen = 0;
		try {
			SharedPreferences mPreferences = PreferenceManager
					.getDefaultSharedPreferences(DaisyReaderDownloadBooks.this);
			valueScreen = mPreferences.getInt(Constants.BRIGHTNESS,
					System.getInt(cResolver, System.SCREEN_BRIGHTNESS));
			LayoutParams layoutpars = window.getAttributes();
			layoutpars.screenBrightness = valueScreen / (float) 255;
			// apply attribute changes to this window
			window.setAttributes(layoutpars);
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
			ex.writeLogException();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		try {
			mTts.stop();
			mTts.shutdown();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
			ex.writeLogException();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
	}

	/**
	 * Show a dialog to confirm exit download.
	 * 
	 * @param message
	 */
	private void pushToDialogOptions(String message) {
		alertDialog = new AlertDialog.Builder(DaisyReaderDownloadBooks.this).create();
		// Setting Dialog Title
		alertDialog.setTitle(R.string.error_title);
		// Setting Dialog Message
		alertDialog.setMessage(message);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setCancelable(false);
		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.error);
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
				DaisyReaderDownloadBooks.this.getString(R.string.no),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mProgressDialog.show();
					}
				});
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
				DaisyReaderDownloadBooks.this.getString(R.string.yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mTask.cancel(true);
					}
				});
		alertDialog.show();
	}
}