package org.androiddaisyreader.apps;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.androiddaisyreader.adapter.DaisyBookAdapter;
import org.androiddaisyreader.metadata.MetaDataHandler;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.DaisyBookInfo;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteDaisyBookHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.Countly;
import org.androiddaisyreader.utils.DaisyBookUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

/**
 * The Class DaisyReaderDownloadBooks.
 */
@SuppressLint("NewApi")
public class DaisyReaderDownloadBooks extends DaisyEbookReaderBaseActivity {

    private String mLink;
    private SQLiteDaisyBookHelper mSql;
    private DaisyBookAdapter mDaisyBookAdapter;
    private String mName;
    private DownloadFileFromURL mTask;
    private List<DaisyBookInfo> mlistDaisyBook;
    private List<DaisyBookInfo> mListDaisyBookOriginal;
    private DaisyBookInfo mDaisyBook;
    private EditText mTextSearch;
    public static final String PATH = Environment.getExternalStorageDirectory().toString()
            + Constants.FOLDER_DOWNLOADED + "/";
    private ProgressDialog mProgressDialog;
    private AlertDialog alertDialog;

    private static final int MAX_PROGRESS = 100;
    private static final int SIZE = 8192;
    private static final int BYTE_VALUE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_books);

        mTextSearch = (EditText) findViewById(R.id.edit_text_search);
        mLink = getIntent().getStringExtra(Constants.LINK_WEBSITE);
        String websiteName = getIntent().getStringExtra(Constants.NAME_WEBSITE);

        mSql = new SQLiteDaisyBookHelper(DaisyReaderDownloadBooks.this);
        mSql.deleteAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
        createDownloadData();
        mlistDaisyBook = mSql.getAllDaisyBook(Constants.TYPE_DOWNLOAD_BOOK);
        mDaisyBookAdapter = new DaisyBookAdapter(DaisyReaderDownloadBooks.this, mlistDaisyBook);
        ListView listDownload = (ListView) findViewById(R.id.list_view_download_books);
        listDownload.setAdapter(mDaisyBookAdapter);
        listDownload.setOnItemClickListener(onItemClick);
        mListDaisyBookOriginal = new ArrayList<DaisyBookInfo>(mlistDaisyBook);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(websiteName.length() != 0 ? websiteName : "");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case android.R.id.home:
            backToTopScreen();
            break;

        default:
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    /**
     * Wirte data to sqlite from metadata
     */
    private void createDownloadData() {
        try {
            InputStream databaseInputStream = new FileInputStream(Constants.folderContainMetadata
                    + Constants.META_DATA_FILE_NAME);
            MetaDataHandler metadata = new MetaDataHandler();
            NodeList nList = metadata.readDataDownloadFromXmlFile(databaseInputStream, mLink);
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    String author = eElement.getElementsByTagName(Constants.ATT_AUTHOR).item(0)
                            .getTextContent();
                    String publisher = eElement.getElementsByTagName(Constants.ATT_PUBLISHER)
                            .item(0).getTextContent();
                    String path = eElement.getAttribute(Constants.ATT_LINK);
                    String title = eElement.getElementsByTagName(Constants.ATT_TITLE).item(0)
                            .getTextContent();
                    String date = eElement.getElementsByTagName(Constants.ATT_DATE).item(0)
                            .getTextContent();
                    DaisyBookInfo daisyBook = new DaisyBookInfo("", title, path, author, publisher,
                            date, 1);
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
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            final DaisyBookInfo daisyBook = mlistDaisyBook.get(position);
            boolean isDoubleTap = handleClickItem(position);
            if (isDoubleTap) {
                downloadABook(position);
            } else {
                speakTextOnHandler(daisyBook.getTitle());
            }
        }
    };

    /**
     * Run asyn task.
     * 
     * @param params the params
     */
    private void runAsynTask(String params[]) {
        mTask = new DownloadFileFromURL();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            mTask.execute(params);
        }
    }

    /**
     * Check storage.
     * 
     * @param link the link
     * @return true, if successful
     */
    private int checkStorage(String link) {
        int result = 0;
        try {
            java.net.URL url = new java.net.URL(link);
            URLConnection conection = url.openConnection();
            conection.connect();
            int lenghtOfFile = conection.getContentLength();

            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = statFs.getBlockSize();
            long freeSize = statFs.getFreeBlocks() * blockSize;

            if (freeSize > lenghtOfFile) {
                result = 1;
            }
        } catch (Exception e) {
            result = 2;
            PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
            ex.writeLogException();
        }
        return result;
    }

    /**
     * Create folder if not exists
     * 
     * @return
     */
    private boolean checkFolderIsExist() {
        boolean result = false;
        File folder = new File(PATH);
        result = folder.exists();
        if (!result) {
            result = folder.mkdir();
        }
        return result;
    }

    /**
     * handle search book when text changed.
     */
    private void handleSearchBook() {
        mTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mListDaisyBookOriginal != null && mListDaisyBookOriginal.size() != 0) {
                    mlistDaisyBook = DaisyBookUtil.searchBookWithText(s, mlistDaisyBook,
                            mListDaisyBookOriginal);
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
            mProgressDialog.setMax(MAX_PROGRESS);
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
            String link = params[0];
            try {
                java.net.URL url = new java.net.URL(link);
                URLConnection conection = url.openConnection();
                conection.connect();
                long startTime = System.currentTimeMillis();
                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), SIZE);
                // Output stream
                String splitString[] = link.split("/");
                mName = splitString[splitString.length - 1];
                OutputStream output = new FileOutputStream(PATH + mName);
                byte data[] = new byte[BYTE_VALUE];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        File file = new File(PATH + mName);
                        file.delete();
                        break;
                    } else {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress((int) ((total * MAX_PROGRESS) / lenghtOfFile));
                        // writing data to file
                        output.write(data, 0, count);
                    }
                }
                // Record the time taken for the download excluding local cleanup.
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                String timeTaken = Long.toString(elapsedTime);
                
                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();
                
                // Record the book download completed successfully 
                HashMap<String, String> results = new HashMap<String, String> ();
                results.put("URL", link);
                results.put("FileSize", Integer.toString(count));
                results.put("DurationIn(ms)", timeTaken);
                Countly.sharedInstance().recordEvent(Constants.RECORD_BOOK_DOWNLOAD_COMPLETED, results, 1);
                result = true;
            } catch (Exception e) {
            	HashMap<String, String> results = new HashMap<String, String> ();
            	results.put("URL", link);
            	results.put("Exception", e.getMessage());
            	Countly.sharedInstance().recordEvent(Constants.RECORD_BOOK_DOWNLOAD_FAILED, results, 1);
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
                if (result) {
                    DaisyBook daisyBook = new DaisyBook();
                    String path = PATH + mName;
                    daisyBook = DaisyBookUtil.getDaisy202Book(path);

                    DaisyBookInfo daisyBookInfo = new DaisyBookInfo();
                    daisyBookInfo.setAuthor(daisyBook.getAuthor());
                    Date date = daisyBook.getDate();
                    String sDate = formatDateOrReturnEmptyString(date);
                    daisyBookInfo.setDate(sDate);
                    daisyBookInfo.setPath(path);
                    daisyBookInfo.setPublisher(daisyBook.getPublisher());
                    daisyBookInfo.setSort(mDaisyBook.getSort());
                    daisyBookInfo.setTitle(daisyBook.getTitle());
                    if (mSql.addDaisyBook(daisyBookInfo, Constants.TYPE_DOWNLOADED_BOOK)) {
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

    /**
     * Format date or return empty string.
     * 
     * @param date the date
     * @return the string
     */
    private String formatDateOrReturnEmptyString(Date date) {
        String sDate = "";
        if (date != null) {
            sDate = String.format(Locale.getDefault(), ("%tB %te, %tY %n"), date, date, date);
        }
        return sDate;
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
        super.onResume();
        handleSearchBook();
    }

    @Override
    protected void onDestroy() {
        try {
            if (mTts != null) {
                mTts.shutdown();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyReaderDownloadBooks.this);
            ex.writeLogException();
        }
        super.onDestroy();
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
        alertDialog.setIcon(R.raw.error);
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

    private void downloadABook(int position) {
        boolean isConnected = DaisyBookUtil.getConnectivityStatus(DaisyReaderDownloadBooks.this) != Constants.TYPE_NOT_CONNECTED;
        IntentController intent = new IntentController(DaisyReaderDownloadBooks.this);
        if (isConnected) {
            if (checkFolderIsExist()) {
                mDaisyBook = mlistDaisyBook.get(position);
                String link = mDaisyBook.getPath();

                if (checkStorage(link) != 0) {
                    String params[] = { link };
                    runAsynTask(params);
                } else {
                    intent.pushToDialog(DaisyReaderDownloadBooks.this
                            .getString(R.string.error_not_enough_space),
                            DaisyReaderDownloadBooks.this.getString(R.string.error_title),
                            R.raw.error, false, false, null);
                }
            }
        } else {
            intent.pushToDialog(
                    DaisyReaderDownloadBooks.this.getString(R.string.error_connect_internet),
                    DaisyReaderDownloadBooks.this.getString(R.string.error_title), R.raw.error,
                    false, false, null);
        }
    }
}
