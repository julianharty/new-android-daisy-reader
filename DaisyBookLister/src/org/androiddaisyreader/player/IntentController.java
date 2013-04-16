/**
 * This is controller. It will help to change intents.
 * @author LogiGear
 * @date 2013.03.05
 */

package org.androiddaisyreader.player;

import java.util.ArrayList;

import org.androiddaisyreader.apps.DaisyEbookReaderSimpleModeActivity;
import org.androiddaisyreader.apps.DaisyEbookReaderVisualModeActivity;
import org.androiddaisyreader.apps.DaisyReaderBookmarkActivity;
import org.androiddaisyreader.apps.DaisyReaderLibraryActivity;
import org.androiddaisyreader.apps.DaisyReaderSettingActivity;
import org.androiddaisyreader.apps.DaisyReaderTableOfContentsActivity;
import org.androiddaisyreader.apps.R;
import org.androiddaisyreader.model.Bookmark;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.utils.DaisyReaderConstants;

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
	
	/**
	 * push to activity setting
	 */
	public void pushToDaisyReaderSettingIntent() {
		Intent i = new Intent(context, DaisyReaderSettingActivity.class);
		context.startActivity(i);
	}
	
	/**
	 * push to activity table of contents
	 * @param path
	 * @param navigator
	 * @param targetActivity
	 */
	public void pushToTableOfContentsIntent(String path, Navigator navigator,
			String targetActivity) {
		Intent i = new Intent(context, DaisyReaderTableOfContentsActivity.class);
		ArrayList<String> listContents = new ArrayList<String>();
		String chapter;
		int numOfChapter = 0;
		int numOfSection = 0;
		while (navigator.hasNext()) {
			Navigable n = navigator.next();
			if (n instanceof Section) {
				Section section = (Section) n;
				if (section.getLevel() == 1) {
					numOfSection = 0;
					chapter = "Chapter";
					numOfChapter++;
					listContents.add(String.format("%s %s: %s", chapter,
							numOfChapter, section.getTitle()));
				} else {
					chapter = "Section";
					numOfSection++;
					listContents.add(String.format("\t %s %s: %s", chapter,
							numOfSection, section.getTitle()));
				}
			}
		}
		i.putStringArrayListExtra(DaisyReaderConstants.LIST_CONTENTS,
				listContents);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		i.putExtra(DaisyReaderConstants.TARGET_ACTIVITY, targetActivity);
		context.startActivity(i);
	}

	/**
	 * push to activity bookmark
	 * 
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
	
	/**
	 * push to activity simple mode
	 * @param path
	 * @param section
	 * @param currentTime
	 */
	public void pushToDaisyEbookReaderSimpleModeIntent(String path,
			int section, int currentTime) {
		Intent i = new Intent(context, DaisyEbookReaderSimpleModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		i.putExtra(DaisyReaderConstants.TIME, String.valueOf(currentTime));
		i.putExtra(DaisyReaderConstants.POSITION_SECTION,
				String.valueOf(section));
		context.startActivity(i);
	}
	
	/**
	 * push to activity visual mode
	 * @param path
	 */
	public void pushToDaisyEbookReaderVisualModeIntent(String path) {
		Intent i = new Intent(context, DaisyEbookReaderVisualModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		context.startActivity(i);
	}
	
	/**
	 * handle show/hide dialog error
	 * @param message
	 * @param isBack
	 */
	public void pushToDialogError(String message, final boolean isBack) {
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
				if (isBack) {
					Activity a = (Activity) context;
					a.onBackPressed();
				}
			}
		});
		dialog.show();
	}
	
	/**
	 * push to activity library
	 */
	public void pushToLibraryIntent() {
		Intent i = new Intent(context, DaisyReaderLibraryActivity.class);
		context.startActivity(i);
	}
}
