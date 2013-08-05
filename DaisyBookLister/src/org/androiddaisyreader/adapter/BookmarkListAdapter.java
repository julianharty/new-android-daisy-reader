package org.androiddaisyreader.adapter;

import java.util.ArrayList;

import org.androiddaisyreader.apps.DaisyEbookReaderVisualModeActivity;
import org.androiddaisyreader.apps.DaisyReaderBookmarkActivity;
import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.sqlite.SQLiteBookmarkHelper;
import org.androiddaisyreader.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

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
	private SQLiteBookmarkHelper mSql;
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
		mSql = new SQLiteBookmarkHelper(getContext());
		mVi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				DaisyReaderBookmarkActivity activity = (DaisyReaderBookmarkActivity) mContext;
				if (activity != null) {
					boolean isDoubleTap = activity.handleClickItem(position);
					if (isDoubleTap) {
						handleTouchOnItem(position, v);
				} else {
				if (position != mSelectedPosition && mSelectedRB != null) {
					mSelectedRB.setChecked(false);
				}
				mSelectedPosition = position;
				mSelectedRB = (RadioButton) v;
						activity.speakTextOnHandler(mListBookmark.get(position).getTextShow());
					}
				}
			}
		});
		return mV;
	}

	/**
	 * Show dialog for user choose save, load or cancel.
	 * 
	 * @param message
	 * @param onlyLoad
	 * @param onlySave
	 * @param loadAndSave
	 */

	public void pushToDialogOptions(String message, boolean onlyLoad, boolean onlySave,
			boolean loadAndSave) {
		AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
		// Setting Dialog Title
		alertDialog.setTitle(R.string.error_title);
		// Setting Dialog Message
		alertDialog.setMessage(message);
		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.error);
		// Setting OK Button
		if (onlyLoad) {
			buttonLoad(alertDialog);
		} else if (onlySave) {
			buttonSave(alertDialog);
		} else if (loadAndSave) {
			buttonLoad(alertDialog);
			buttonSave(alertDialog);
		}
		buttonCancel(alertDialog);
		alertDialog.show();
	}

	/**
	 * Create button Save for AlertDialog
	 * 
	 * @param alertDialog
	 */
	private void buttonSave(AlertDialog alertDialog) {
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
				mContext.getString(R.string.save_bookmark), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mBookmarkTmp.setSort(mBookmark.getSort());
						// delete old bookmark
						mSql.deleteBookmark(mBookmark.getId());
						// add new bookmark
						mBookmarkTmp.setTextShow(mBookmarkTmp.getText());
						mSql.addBookmark(mBookmarkTmp);
						dialog.cancel();
						Activity a = (Activity) mContext;
						a.onBackPressed();
					}
				});
	}

	/**
	 * Create button Load for AlertDialog
	 * 
	 * @param alertDialog
	 */
	private void buttonLoad(AlertDialog alertDialog) {
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
				mContext.getString(R.string.load_bookmark), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						pushToDaisyEbookReaderVisualModeIntent(mPath, mBookmark.getSection(),
								mBookmark.getTime());
					}
				});
	}

	/**
	 * Create button Cancel for AlertDialog
	 * 
	 * @param alertDialog
	 */
	private void buttonCancel(AlertDialog alertDialog) {
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
				mContext.getString(R.string.cancel_bookmark),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}

	/**
	 * Push to visual mode when user load from bookmark
	 * 
	 * @param path
	 * @param section
	 * @param time
	 */
	private void pushToDaisyEbookReaderVisualModeIntent(String path, int section, int time) {
		Intent i = new Intent(mContext, DaisyEbookReaderVisualModeActivity.class);
		i.putExtra(Constants.POSITION_SECTION, String.valueOf(section));
		i.putExtra(Constants.DAISY_PATH, path);
		i.putExtra(Constants.TIME, time);
		mContext.startActivity(i);
	}

	private void handleTouchOnItem(final int position, View v) {
		mBookmark = mListBookmark.get(position);
		boolean isEmptyText = mBookmark.getTextShow().equals(
				mContext.getString(R.string.empty_bookmark));
		if (isEmptyText) {
			mBookmark.setSort(mTotalNumberBookmark);
		} else {
			mBookmark.setSort(position);
		}
		if (position != mSelectedPosition && mSelectedRB != null) {
			mSelectedRB.setChecked(false);
		}
		mSelectedPosition = position;
		mSelectedRB = (RadioButton) v;
		// Set enable for buttons
		boolean onlyLoad = false;
		boolean onlySave = false;
		boolean loadAndSave = false;
		if (mBookmark.getId() != null && mBookmarkTmp.getId() == null && !isEmptyText) {
			onlyLoad = true;
		} else if (mBookmark.getId() != null && mBookmarkTmp.getId() != null && !isEmptyText) {
			loadAndSave = true;
		} else if (mBookmark.getId() == null && mBookmarkTmp.getId() != null && isEmptyText) {
			onlySave = true;
		}
		pushToDialogOptions(mContext.getString(R.string.message_bookmark), onlyLoad, onlySave,
				loadAndSave);
	}
}
