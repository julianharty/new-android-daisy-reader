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

public class BookmarkListAdapter extends ArrayAdapter<Bookmark> {
	private Context context;
	private ArrayList<Bookmark> listBookmark;
	private LayoutInflater vi;
	private RadioButton rbt;
	private View v;
	private Bookmark bookmark;
	private Bookmark tmp;
	private int mSelectedPosition = -1;;
	private RadioButton mSelectedRB;
	private SqlLiteBookmarkHelper sql;
	private Dialog dialog;
	private boolean onlyLoad = false;
	private boolean onlySave = false;
	private String path;

	public BookmarkListAdapter(Context context,
			ArrayList<Bookmark> listBookmark, Bookmark bookmark, String path) {
		super(context, 0, listBookmark);
		this.context = context;
		this.listBookmark = listBookmark;
		this.tmp = bookmark;
		this.path = path;
		sql = new SqlLiteBookmarkHelper(getContext());
		vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (bookmark.getId() == null) {
			onlyLoad = true;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		v = convertView;
		v = vi.inflate(R.layout.item_bookmark, null);
		bookmark = listBookmark.get(position);
		rbt = (RadioButton) v.findViewById(R.id.itemBookmark);
		rbt.setText(bookmark.getTextShow());

		rbt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bookmark = listBookmark.get(position);
				bookmark.setSort(position);
				if (position != mSelectedPosition && mSelectedRB != null) {
					mSelectedRB.setChecked(false);
				}
				mSelectedPosition = position;
				mSelectedRB = (RadioButton) v;
				pushToDialogOptions(context
						.getString(R.string.message_bookmark));
			}
		});
		return v;
	}

	public void pushToDialogOptions(String message) {
		dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_options);
		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);

		Button buttonSave = (Button) dialog.findViewById(R.id.buttonSave);
		buttonSave.setText(context.getString(R.string.save_bookmark));
		buttonSave.setOnClickListener(buttonSaveClick);
		buttonSave.setEnabled(!onlyLoad);

		Button buttonLoad = (Button) dialog.findViewById(R.id.buttonLoad);
		buttonLoad.setText(context.getString(R.string.load_bookmark));
		buttonLoad.setOnClickListener(buttonLoadClick);
		
		if (bookmark.getTextShow().equals(
				context.getString(R.string.empty_bookmark))) {
			onlySave = true;
		}
		buttonLoad.setEnabled(!onlySave);
		onlySave = false;

		Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
		buttonCancel.setText(context.getString(R.string.cancel_bookmark));
		buttonCancel.setOnClickListener(buttonCancelClick);
		dialog.show();
	}

	OnClickListener buttonSaveClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			tmp.setSort(bookmark.getSort());
			// delete old bookmark
			sql.deleteBookmark(bookmark.getId());
			// add new bookmark
			tmp.setTextShow(tmp.getText());
			sql.addBookmark(tmp);
			dialog.dismiss();
			Activity a = (Activity) context;
			a.onBackPressed();
		}
	};

	OnClickListener buttonCancelClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			dialog.dismiss();
		}
	};

	OnClickListener buttonLoadClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			pushToDaisyEbookReaderVisualModeIntent(path, bookmark.getSection(),
					bookmark.getTime());
			dialog.dismiss();
		}
	};

	private void pushToDaisyEbookReaderVisualModeIntent(String path,
			int section, int time) {
		Intent i = new Intent(context, DaisyEbookReaderVisualModeActivity.class);
		i.putExtra(DaisyReaderConstants.POSITION_SECTION, section);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		i.putExtra(DaisyReaderConstants.TIME, time);
		context.startActivity(i);
	}
}
