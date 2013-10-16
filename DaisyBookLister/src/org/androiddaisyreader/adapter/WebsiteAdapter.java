/*
 * 
 */
package org.androiddaisyreader.adapter;

import java.util.List;

import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.Website;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * The Class WebsiteAdapter.
 * 
 * @author phuc.dang
 * @date Jul 8, 2013
 */

public class WebsiteAdapter extends BaseAdapter {

    /** The list website. */
    private List<Website> listWebsite;

    /** The m inflater. */
    private LayoutInflater mInflater;

    /**
     * Instantiates a new website adapter.
     * 
     * @param list the list
     * @param inflater the inflater
     */
    public WebsiteAdapter(final List<Website> list, LayoutInflater inflater) {
        mInflater = inflater;
        this.listWebsite = list;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return listWebsite.size();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewItem item;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_website, null);
            item = new ViewItem();
            item.txtWebsiteName = (TextView) convertView.findViewById(R.id.text_website_name);
            item.txtWebsiteURL = (TextView) convertView.findViewById(R.id.text_website_url);
            convertView.setTag(item);
        } else {
            item = (ViewItem) convertView.getTag();
        }

        final Website curWebsite = listWebsite.get(position);
        item.txtWebsiteName.setText(curWebsite.getSiteName());
        item.txtWebsiteURL.setText(curWebsite.getSiteURL());

        return convertView;
    }

    /**
     * The Class ViewItem.
     */
    private class ViewItem {

        private TextView txtWebsiteName;
        private TextView txtWebsiteURL;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.BaseAdapter#notifyDataSetChanged()
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
