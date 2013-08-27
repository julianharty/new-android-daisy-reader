package org.androiddaisyreader.adapter;

import java.util.ArrayList;

import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.DaisyBookInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DaisyBookAdapter extends ArrayAdapter<DaisyBookInfo> {
	private ArrayList<DaisyBookInfo> mListDaisyBook;
	private LayoutInflater mVi;

	public DaisyBookAdapter(Context context, ArrayList<DaisyBookInfo> listDaisyBook) {
		super(context, 0, listDaisyBook);
		this.mListDaisyBook = listDaisyBook;
		mVi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewItem item;
		if (convertView == null) {
			convertView = mVi.inflate(R.layout.item_book, null);
			item = new ViewItem();
			item.txtBookTitle = (TextView) convertView.findViewById(R.id.daisy_book_title);
			item.txtBookAuthor = (TextView) convertView.findViewById(R.id.daisy_book_author);
			item.txtBookDate = (TextView) convertView.findViewById(R.id.daisy_book_date);
			item.txtBookPublisher = (TextView) convertView.findViewById(R.id.daisy_book_publisher);
			item.viewBookAuthor = (View) convertView.findViewById(R.id.daisy_book_view_author);
			item.viewBookDate = (View) convertView.findViewById(R.id.daisy_book_view_date);
			item.viewBookPublisher = (View) convertView
					.findViewById(R.id.daisy_book_view_publisher);
			convertView.setTag(item);
		} else {
			item = (ViewItem) convertView.getTag();
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

		return convertView;
	}

	private class ViewItem {

		TextView txtBookTitle;
		TextView txtBookAuthor;
		TextView txtBookDate;
		TextView txtBookPublisher;
		View viewBookAuthor, viewBookDate, viewBookPublisher;

	}
}
