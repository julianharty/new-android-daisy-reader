package org.androiddaisyreader.apps;

import java.util.ArrayList;
import java.util.Locale;

import org.androiddaisyreader.utils.DaisyReaderConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class DaisyReaderTableOfContentsActivity extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private ArrayList<String> listResult;
	private ListView listContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daisy_reader_table_of_contents);
		listContent = (ListView) this.findViewById(R.id.listContent);
		listResult = getIntent().getStringArrayListExtra("listContent");
		tts = new TextToSpeech(this, this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.listrow, R.id.rowTextView,
				listResult);
		listContent.setAdapter(adapter);
		listContent.setOnItemClickListener(itemContentsClick);
		listContent.setOnItemLongClickListener(itemContentsLongClick);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		tts.shutdown();
		tts.stop();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private OnItemClickListener itemContentsClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			tts.setLanguage(Locale.JAPANESE);
			tts.isLanguageAvailable(Locale.JAPAN);
			Toast.makeText(getBaseContext(), listResult.get(position).toString(),
					Toast.LENGTH_SHORT).show();
			tts.speak(listResult.get(position).toString(), TextToSpeech.QUEUE_FLUSH, null);
		}
		
	};
	
	private OnItemLongClickListener itemContentsLongClick = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int position,
				long id) {
			pushToDaisyEbookReaderSimpleModeIntent(position);
			return false;
		};
	};
	
	private void pushToDaisyEbookReaderSimpleModeIntent(int position)
	{
		Intent i = new Intent(this, DaisyEbookReaderSimpleModeActivity.class);
        i.putExtra(DaisyReaderConstants.POSITION_SECTION, position);
        
        /**
         * Make sure path of daisy book is correct.
         */
        i.putExtra(DaisyReaderConstants.DAISY_PATH, getIntent().getStringExtra(DaisyReaderConstants.DAISY_PATH));
        this.startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(
				R.menu.activity_daisy_reader_table_of_contents, menu);
		return true;
	}

	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
