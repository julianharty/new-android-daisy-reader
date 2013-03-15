/**
* This activity contains two mode "simple mode" and "visual mode".
* @author LogiGear
* @date 2013.03.05
*/

package org.androiddaisyreader.apps;

import java.util.ArrayList;

import org.androiddaisyreader.utils.DaisyReaderConstants;
import org.androiddaisyreader.utils.DaisyReaderUtils;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DaisyEbookReaderActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_daisy_ebook_reader);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		TextView bookTitle = (TextView) this.findViewById(R.id.bookTitle);
		String path = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		String[] title = path.split("/");
		bookTitle.setText(title[title.length - 2]);
		TextView simpleMode = (TextView) this.findViewById(R.id.simpleMode);
		simpleMode.setOnClickListener(simpleModeClick);
		
		ImageView imgTableOfContents = (ImageView)this.findViewById(R.id.imgTableOfContents);
		imgTableOfContents.setOnClickListener(imgTableOfContentsClick);
	}

	private OnClickListener simpleModeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			pushToDaisyEbookReaderSimpleModeIntent();
		}
	};
	
	private OnClickListener imgTableOfContentsClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try {
				pushToTableOfContentsIntent();

			} catch (Exception e) {
				showDialogError(getString(R.string.noPathFound));
			}
		}
	};
	
	private void pushToDaisyEbookReaderSimpleModeIntent() {
		Intent i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH));
		this.startActivity(i);
	}

	/**
	 * Push to activity table of content when user press and hold.
	 */
	private void pushToTableOfContentsIntent() {
		Intent i = new Intent(this, DaisyReaderTableOfContentsActivity.class);
		String path = getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH);
		ArrayList<String> listContents = DaisyReaderUtils.getContents(path);
		i.putStringArrayListExtra(DaisyReaderConstants.LIST_CONTENTS, listContents);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, path);
		this.startActivity(i);
	}
	
	public void showDialogError(String message) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog); 
		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				onBackPressed();
			}
		});
		dialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_daisy_ebook_reader, menu);
		return true;
	}

}