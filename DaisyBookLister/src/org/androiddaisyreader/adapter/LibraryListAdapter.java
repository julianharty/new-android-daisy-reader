package org.androiddaisyreader.adapter;

import java.util.ArrayList;

import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.DetailInfo;
import org.androiddaisyreader.model.HeaderInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class LibraryListAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private ArrayList<HeaderInfo> mHeaderList;

	public LibraryListAdapter(Context context, ArrayList<HeaderInfo> deptList) {
		this.mContext = context;
		this.mHeaderList = deptList;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		ArrayList<DetailInfo> bookList = mHeaderList.get(groupPosition).getBookList();
		return bookList.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
			ViewGroup parent) {

		DetailInfo detailInfo = (DetailInfo) getChild(groupPosition, childPosition);
		if (view == null) {
			LayoutInflater infalInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = infalInflater.inflate(R.layout.child_row, null);
		}

		TextView sequence = (TextView) view.findViewById(R.id.sequence);
		sequence.setText(detailInfo.getSequence().trim() + ") ");
		TextView childItem = (TextView) view.findViewById(R.id.childItem);
		childItem.setText(detailInfo.getName().trim());
		
		view.setTag(childPosition);

		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		ArrayList<DetailInfo> bookList = mHeaderList.get(groupPosition).getBookList();
		return bookList.size();

	}

	@Override
	public Object getGroup(int groupPosition) {
		return mHeaderList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mHeaderList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {

		HeaderInfo headerInfo = (HeaderInfo) getGroup(groupPosition);
		if (view == null) {
			LayoutInflater inf = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.group_heading, null);
		}

		TextView heading = (TextView) view.findViewById(R.id.heading);
		heading.setText(headerInfo.getName().trim());

		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
