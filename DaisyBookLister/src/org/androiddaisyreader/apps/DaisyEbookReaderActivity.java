/**
* This activity contains two mode "simple mode" and "visual mode".
* @author LogiGear
* @date 2013.03.05
*/

package org.androiddaisyreader.apps;

import org.androiddaisyreader.utils.DaisyReaderConstants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class DaisyEbookReaderActivity extends Activity {

	private TextView simpleMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_ebook_reader);
		simpleMode = (TextView) this.findViewById(R.id.simpleMode);
		simpleMode.setOnClickListener(simpleModeClick);
	}

	private OnClickListener simpleModeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			pushToDaisyEbookReaderSimpleModeIntent();
		}
	};

	private void pushToDaisyEbookReaderSimpleModeIntent() {
		Intent i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
		i.putExtra(DaisyReaderConstants.DAISY_PATH, getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH));
		this.startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_daisy_ebook_reader, menu);
		return true;
	}

}
