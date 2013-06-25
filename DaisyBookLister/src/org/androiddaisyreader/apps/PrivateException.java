package org.androiddaisyreader.apps;

import java.io.IOException;

import org.androiddaisyreader.player.IntentController;

import android.content.Context;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

/**
 * This class will handle all exception.
 * 
 * @author LogiGear
 * @date 2013.06.10
 */

public class PrivateException extends Exception {
	private static final long serialVersionUID = -3511134293183982787L;
	// constructor without parameters
	public Exception ex = new Exception();
	private Context mContext;
	private String mNameFolder;

	public PrivateException(Exception ex, Context context) {
		this.ex = ex;
		this.mContext = context;
	}

	public PrivateException(Exception ex, Context context, String path) {
		this.ex = ex;
		this.mContext = context;
		this.mNameFolder = getNameFolder(path);
	}

	private String getNameFolder(String path) {
		String nameFolder = "";
		String names[] = path.split("/");
		if (names.length > 1) {
			if (path.contains(DaisyReaderConstants.SUFFIX_ZIP_FILE)) {
				nameFolder = "\"" + names[names.length - 1] + "\"";
			} else {
				nameFolder = "\"" + names[names.length - 2] + "\"";
			}
		}
		return nameFolder;
	}

	/**
	 * This function will show a dialog error to user.
	 * 
	 * @param intent
	 */
	public void showDialogException(IntentController intent) {
		if (ex instanceof IOException) {
			intent.pushToDialog(
					String.format(mContext.getString(R.string.error_parse_file_ncc), mNameFolder),
					mContext.getString(R.string.error_title), R.drawable.error, true, false, null);
		} else if (ex instanceof IllegalStateException) {
			intent.pushToDialog(
					String.format(mContext.getString(R.string.error_no_path_found), mNameFolder),
					mContext.getString(R.string.error_title), R.drawable.error, true, false, null);
		} else if (ex instanceof NullPointerException) {
			intent.pushToDialog(mContext.getString(R.string.error_wrong_format),
					mContext.getString(R.string.error_title), R.drawable.error, true, false, null);
		} else if (ex instanceof RuntimeException) {
			intent.pushToDialog(mContext.getString(R.string.error_no_audio_found),
					mContext.getString(R.string.error_title), R.drawable.error, false, false, null);
		} else {
			intent.pushToDialog(
					String.format(mContext.getString(R.string.error_parse_file_ncc), mNameFolder),
					mContext.getString(R.string.error_title), R.drawable.error, true, false, null);
		}
	}

	/**
	 * This function will write error to log file.
	 */
	public void writeLogException() {
		if (ex instanceof IOException) {
			Log.i(mContext.getClass().toString(), IOException.class.toString());
		} else if (ex instanceof IllegalStateException) {
			Log.i(mContext.getClass().toString(), IllegalStateException.class.toString());
		} else if (ex instanceof NullPointerException) {
			Log.i(mContext.getClass().toString(), NullPointerException.class.toString());
		} else if (ex instanceof NumberFormatException) {
			Log.i(mContext.getClass().toString(), NumberFormatException.class.toString());
		} else if (ex instanceof SettingNotFoundException) {
			Log.i(mContext.getClass().toString(), SettingNotFoundException.class.toString());
		} else if (ex instanceof NumberFormatException) {
			Log.i(mContext.getClass().toString(), NumberFormatException.class.toString());
		} else {
			Log.i(mContext.getClass().toString(), Exception.class.toString());
		}

	}
}
