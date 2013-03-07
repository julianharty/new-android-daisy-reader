//package org.androiddaisyreader.apps;
//
//import java.io.File;
//import java.io.FilenameFilter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import org.androiddaisyreader.utils.DaisyReaderUtils;
//
//import android.os.Bundle;
//import android.os.Environment;
//import android.app.AlertDialog;
//import android.app.ListActivity;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//
//public class DaisyBookBrowserFileActivity extends ListActivity {
//	private File currentDirectory = new File("/sdcard/");
//	private List<String> files;
//	private List<String> filesResult;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_daisy_book_browser_file);
//		generateBrowserData();
//	}
//	
//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		// TODO Auto-generated method stub
//		super.onListItemClick(l, v, position, id);
//		String item = filesResult.get(position);
//		File daisyPath = new File(currentDirectory, item);
//		
//		if (DaisyReaderUtils.folderContainsDaisy2_02Book(daisyPath)) {
//            Intent i = new Intent(this, DaisyBookListerActivity.class);
// 
//            i.putExtra("daisyPath", daisyPath.getAbsolutePath() + "/");
//            i.putExtra("daisyNccFile", DaisyReaderUtils.getNccFileName(daisyPath));
//            this.startActivity(i);
//            finish();
//            return;
//        }
//		if (item.equals(this.getString(R.string.up_1_level))) {
//			currentDirectory = new File(currentDirectory.getParent());
//			generateBrowserData();
//			return;
//		}
//
//		File temp = daisyPath;
//		if (temp.isDirectory()) {
//			currentDirectory = temp;
//			generateBrowserData();
//		}
//	}
//	
//	private void generateBrowserData() {
//		String storagestate = Environment.getExternalStorageState();
//		if (!storagestate.equals(Environment.MEDIA_MOUNTED)) {
//			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//			alertDialog.setTitle(R.string.sdcard_title);
//			alertDialog.setMessage(this.getString(R.string.sdcard_mounted));
//			alertDialog.setButton(this.getString(R.string.close_instructions),
//					new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							finish();
//							return;
//						}
//					});
//			alertDialog.show();
//		}
//
//		FilenameFilter dirFilter = new FilenameFilter() {
//			public boolean accept(File dir, String name) {
//				return new File(dir, name).isDirectory();
//			}
//		};
//
//		String[] listOfFiles = currentDirectory.list(dirFilter);
//		if (listOfFiles != null) {
//			files = new ArrayList<String>(Arrays.asList(listOfFiles));
//			Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
//			filesResult = new ArrayList<String>();
//			for(int i = 0;i<files.size();i++)
//			{
//				String item = files.get(i);
//				File daisyPath = new File(currentDirectory, item);
//				if(DaisyReaderUtils.folderContainsDaisy2_02Book(daisyPath))
//				{
//					filesResult.add(item);
//				}
//			}
//			
//			if (!currentDirectory.getParent().equals("/")) {
//				filesResult.add(this.getString(R.string.up_1_level));
//			}
//		} else {
//			files = new ArrayList<String>();
//		}
//		setListAdapter(new ArrayAdapter<String>(this, R.layout.listrow,
//				R.id.rowTextView, filesResult));
//		return;
//	}
//
//}
