package org.androiddaisyreader.adapter;

import java.util.ArrayList;

import org.androiddaisyreader.apps.DaisyEbookReaderVisualModeActivity;
import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.sqllite.SqlLiteBookmarkHelper;
import org.androiddaisyreader.utils.DaisyReaderConstants;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * This adapter to handle bookmark
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class BookmarkListAdapter extends ArrayAdapter<Bookmark> {
	private Context mContext;
	private ArrayList<Bookmark> mListBookmark;
	private LayoutInflater mVi;
	private RadioButton mRbt;
	private View mV;
	private Bookmark mBookmark;
	private Bookmark mBookmarkTmp;
	private int mSelectedPosition = -1;;
	private RadioButton mSelectedRB;
	private SqlLiteBookmarkHelper mSql;
	private Dialog mDialog;
	private boolean mOnlyLoad = false;
	private boolean mOnlySave = false;
	private String mPath;
	private int mTotalNumberBookmark;

	public BookmarkListAdapter(Context context, ArrayList<Bookmark> listBookmark,
			Bookmark bookmark, String path, int totalNumberBookmark) {
		super(context, 0, listBookmark);
		this.mContext = context;
		this.mListBookmark = listBookmark;
		this.mBookmarkTmp = bookmark;
		this.mPath = path;
		this.mTotalNumberBookmark = totalNumberBookmark;
		mSql = new SqlLiteBookmarkHelper(getContext());
		mVi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (bookmark.getId() == null) {
			mOnlyLoad = true;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		mV = convertView;
		mV = mVi.inflate(R.layout.item_bookmark, null);
		mBookmark = mListBookmark.get(position);
		mRbt = (RadioButton) mV.findViewById(R.id.itemBookmark);
		mRbt.setText(mBookmark.getTextShow());

		mRbt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBookmark = mListBookmark.get(position);
				if (mBookmark.getTextShow().equals(mContext.getString(R.string.empty_bookmark))) {
					mBookmark.setSort(mTotalNumberBookmark);
				} else {
					mBookmark.setSort(position);
				}
				if (position != mSelectedPosition && mSelectedRB != null) {
					mSelectedRB.setChecked(false);
				}
				mSelectedPosition = position;
				mSelectedRB = (RadioButton) v;
				pushToDialogOptions(mContext.getString(R.string.message_bookmark));
			}
		});
		return mV;
	}
	
	/**
	 * Show dialog for user choose save, load or cancel.
	 * @param message
	 */
	public void pushToDialogOptions(String message) {
		mDialog = new Dialog(mContext);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.dialog_options);
		// set the custom dialog components - text, image and button
		TextView text = (TextView) mDialog.findViewById(R.id.text);
		text.setText(message);

		Button buttonSave = (Button) mDialog.findViewById(R.id.buttonSave);
		buttonSave.setText(mContext.getString(R.string.save_bookmark));
		buttonSave.setOnClickListener(buttonSaveClick);
		buttonSave.setEnabled(!mOnlyLoad);

		Button buttonLoad = (Button) mDialog.findViewById(R.id.buttonLoad);
		buttonLoad.setText(mContext.getString(R.string.load_bookmark));
		buttonLoad.setOnClickListener(buttonLoadClick);

		if (mBookmark.getTextShow().equals(mContext.getString(R.string.empty_bookmark))) {
			mOnlySave = true;
		}
		buttonLoad.setEnabled(!mOnlySave);
		mOnlySave = false;

		Button buttonCancel = (Button) mDialog.findViewById(R.id.buttonCancel);
		buttonCancel.setText(mContext.getString(R.string.cancel_bookmark));
		buttonCancel.setOnClickListener(buttonCancelClick);
		mDialog.show();
	}
	
	/**
	 * Handle save bookmark
	 */
	OnClickListener buttonSaveClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mBookmarkTmp.setSort(mBookmark.getSort());
			// delete old bookmark
			mSql.deleteBookmark(mBookmark.getId());
			// add new bookmark
			mBookmarkTmp.setTextShow(mBookmarkTmp.getText());
			mSql.addBookmark(mBookmarkTmp);
			mDialog.dismiss();
			Activity a = (Activity) mContext;
			a.onBackPressed();
		}
	};
	
	/**
	 * Handle cancel bookmark
	 */
	OnClickListener buttonCancelClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
		}
	};
	
	/**
	 * Handle load bookmark
	 */
	OnClickListener buttonLoadClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			pushToDaisyEbookReaderVisualModeIntent(mPath, mBookmark.getSection(),
					mBookmark.getTime());
			mDialog.dismiss();
		}
	};
	
	/**
	 * Push to visual mode when user load from bookmark
	 * @param path
	 * @param section
	 * @param time
	 */
	private void pushToDaisyEbookReaderVisualModeIntent(String path, int section, int time) {
		Intent i = new Intent(mContext, DaisyEbookReaderVisualModeActivity.class);
		i.putExtra(DaisyReaderConstants.POSITION_SECTION, String.valueOf(section));
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		i.putExtra(DaisyReaderConstants.TIME, time);
		mContext.startActivity(i);
	}
}
