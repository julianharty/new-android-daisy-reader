package org.androiddaisyreader.apps;

import java.util.ArrayList;
import java.util.List;

import org.androiddaisyreader.adapter.WebsiteAdapter;
import org.androiddaisyreader.base.DaisyEbookReaderBaseActivity;
import org.androiddaisyreader.model.Website;
import org.androiddaisyreader.utils.Constants;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

/**
 * The Class DaisyReaderDownloadSiteActivity.
 */
public class DaisyReaderDownloadSiteActivity extends DaisyEbookReaderBaseActivity {

    private ListView mListViewWebsite;
    private List<Website> listWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_site);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.download_sites);

        mListViewWebsite = (ListView) findViewById(R.id.list_view_website);
        initListWebsite();
        WebsiteAdapter websiteAdapter = new WebsiteAdapter(listWebsite, getLayoutInflater());
        mListViewWebsite.setAdapter(websiteAdapter);

        // set listener while touch on website
        mListViewWebsite.setOnItemClickListener(onItemWebsiteClick);

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

    private OnItemClickListener onItemWebsiteClick = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Website website = listWebsite.get(arg2);
            boolean isDoubleTap = handleClickItem(arg2);
            if (isDoubleTap) {
                String websiteUrl = website.getSiteURL();
                String websiteName = website.getSiteName();
                pushToWebsite(websiteUrl, websiteName);
            } else {
                speakTextOnHandler(website.getSiteName());
            }
        }
    };

    /**
     * Inits the list website.
     */
    private void initListWebsite() {
        listWebsite = new ArrayList<Website>();
        Website website = null;
        website = new Website(this.getString(R.string.web_site_name_daisy_org),
                this.getString(R.string.web_site_url_daisy_org), 1);
        listWebsite.add(website);
        website = new Website(this.getString(R.string.web_site_name_htctu),
                this.getString(R.string.web_site_url_htctu), 2);
        listWebsite.add(website);

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
        super.onResume();
        speakText(getString(R.string.download_sites));

    }

    @Override
    protected void onDestroy() {
        try {
            if (mTts != null) {
                mTts.shutdown();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyReaderDownloadSiteActivity.this);
            ex.writeLogException();
        }
        super.onDestroy();
    }

}
