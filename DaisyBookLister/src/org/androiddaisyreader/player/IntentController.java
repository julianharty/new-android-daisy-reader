package org.androiddaisyreader.player;

import java.util.ArrayList;

import org.androiddaisyreader.apps.DaisyEbookReaderSimpleModeActivity;
import org.androiddaisyreader.apps.DaisyReaderBookmarkActivity;
import org.androiddaisyreader.apps.DaisyReaderLibraryActivity;
import org.androiddaisyreader.apps.DaisyReaderSettingActivity;
import org.androiddaisyreader.apps.DaisyReaderTableOfContentsActivity;
import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class IntentController {
	private Context context;

	public IntentController(Context context) {
		this.context = context;
	}

	public void pushToDaisyReaderSettingIntent() {
		Intent i = new Intent(context, DaisyReaderSettingActivity.class);
		context.startActivity(i);
	}

	/**
	 * Push to activity table of content when user press and hold.
	 */
	public void pushToTableOfContentsIntent(String path, String targetActivity) {
		Intent i = new Intent(context, DaisyReaderTableOfContentsActivity.class);
		ArrayList<String> listContents = DaisyReaderUtils.getContents(path);
		i.putStringArrayListExtra(DaisyReaderConstants.LIST_CONTENTS,
				listContents);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		i.putExtra(DaisyReaderConstants.TARGET_ACTIVITY, targetActivity);
		context.startActivity(i);
	}
	/**
	 * push to activity bookmark
	 * @param bookmark
	 * @param path
	 */
	
	public void pushToDaisyReaderBookmarkIntent(Bookmark bookmark, String path) {
		Intent i = new Intent(context, DaisyReaderBookmarkActivity.class);
		i.putExtra(DaisyReaderConstants.BOOK, bookmark.getBook());
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		if (bookmark.getText() != null) {
			i.putExtra(DaisyReaderConstants.SENTENCE, bookmark.getText());
			i.putExtra(DaisyReaderConstants.TIME,
					String.valueOf(bookmark.getTime()));
			i.putExtra(DaisyReaderConstants.SECTION,
					String.valueOf(bookmark.getSection()));
		}
		context.startActivity(i);
	}

	public void pushToDaisyEbookReaderSimpleModeIntent(String path) {
		Intent i = new Intent(context, DaisyEbookReaderSimpleModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		context.startActivity(i);
	}

	public void pushToDialogError(String message) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_error);
		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);

		Button dialogButton = (Button) dialog.findViewById(R.id.buttonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Activity a = (Activity) context;
				a.onBackPressed();
			}
		});
		dialog.show();
	}
	
	public void pushToLibraryIntent()
	{
		Intent i = new Intent(context, DaisyReaderLibraryActivity.class);
		context.startActivity(i);
	}
}
