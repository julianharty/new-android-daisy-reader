package org.androiddaisyreader.adapter;

import java.util.List;

import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.DaisyBookInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DaisyBookAdapter extends ArrayAdapter<DaisyBookInfo> {
    private List<DaisyBookInfo> mListDaisyBook;
    private LayoutInflater mVi;

    public DaisyBookAdapter(Context context, List<DaisyBookInfo> listDaisyBook) {
        super(context, 0, listDaisyBook);
        this.mListDaisyBook = listDaisyBook;
        mVi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewItem item;
        View view = convertView;
        if (view == null) {
            view = mVi.inflate(R.layout.item_book, null);
            item = new ViewItem();
            item.txtBookTitle = (TextView) view.findViewById(R.id.daisy_book_title);
            item.txtBookAuthor = (TextView) view.findViewById(R.id.daisy_book_author);
            item.txtBookDate = (TextView) view.findViewById(R.id.daisy_book_date);
            item.txtBookPublisher = (TextView) view.findViewById(R.id.daisy_book_publisher);
            item.viewBookAuthor = (View) view.findViewById(R.id.daisy_book_view_author);
            item.viewBookDate = (View) view.findViewById(R.id.daisy_book_view_date);
            item.viewBookPublisher = (View) view
                    .findViewById(R.id.daisy_book_view_publisher);
            view.setTag(item);
        } else {
            item = (ViewItem) view.getTag();
        }

        DaisyBookInfo curDaisyBook = mListDaisyBook.get(position);

        String title = curDaisyBook.getTitle();
        if (title != null && title.length() != 0) {
            item.txtBookTitle.setText(title);
        } else {
            item.txtBookTitle.setText("");
        }

        // display author if the book exists author
        String author = curDaisyBook.getAuthor();
        if (author != null && author.length() != 0) {
            item.txtBookAuthor.setText(author);
            item.viewBookAuthor.setVisibility(View.VISIBLE);

        } else {
            item.txtBookAuthor.setText("");
            item.viewBookAuthor.setVisibility(View.GONE);
        }

        // display produced date if the book exists author
        String date = mListDaisyBook.get(position).getDate();
        if (date != null && date.length() != 0) {
            item.viewBookDate.setVisibility(View.VISIBLE);
            item.txtBookDate.setText(date);
        } else {
            item.txtBookDate.setText("");
            item.viewBookDate.setVisibility(View.GONE);
        }

        // display produced date if the book exists author
        String publisher = curDaisyBook.getPublisher();
        if (publisher != null && publisher.length() != 0) {
            item.viewBookPublisher.setVisibility(View.VISIBLE);
            item.txtBookPublisher.setText(publisher);
        } else {
            item.txtBookPublisher.setText("");
            item.viewBookPublisher.setVisibility(View.GONE);
        }

        return view;
    }

    private class ViewItem {

        private TextView txtBookTitle;
        private TextView txtBookAuthor;
        private TextView txtBookDate;
        private TextView txtBookPublisher;
        private View viewBookAuthor, viewBookDate, viewBookPublisher;

    }
}
