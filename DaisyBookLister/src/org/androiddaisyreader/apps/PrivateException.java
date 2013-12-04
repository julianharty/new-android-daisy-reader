package org.androiddaisyreader.apps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.utils.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
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
    private Exception ex = new Exception();
    private Context mContext;
    private String path;
    private String message;

    public PrivateException(String message) {
        this.message = message;
    }

    public PrivateException(Exception ex, Context context, String... path) {
        this.ex = ex;
        this.mContext = context;
        if (path.length >= 1 && path[0] != null) {
            String tmpPath = path[0];
            if (tmpPath.contains(Constants.FILE_NCC_NAME_NOT_CAPS)) {
                this.path = tmpPath.replace(Constants.FILE_NCC_NAME_NOT_CAPS, "");
            } else if (tmpPath.contains(Constants.FILE_NCC_NAME_CAPS)) {
                this.path = tmpPath.replace(Constants.FILE_NCC_NAME_CAPS, "");
            } else {
                this.path = tmpPath;
            }
        }
    }

    public String getMessage() {
        return message;
    }

    /**
     * This function will show a dialog error to user.
     * 
     * @param intent
     */
    public void showDialogException(IntentController intent) {
        boolean isExists = true;
        if (path != null) {
            isExists = new File(path).exists();
        }
        if (ex instanceof IOException && isExists) {
            intent.pushToDialog(
                    String.format(mContext.getString(R.string.error_parse_file_ncc), path),
                    mContext.getString(R.string.error_title), R.raw.error, true, false, null);
        } else if (ex instanceof IllegalStateException || !isExists) {
            intent.pushToDialog(
                    String.format(mContext.getString(R.string.error_no_path_found), path),
                    mContext.getString(R.string.error_title), R.raw.error, true, false, null);
        } else if (ex instanceof NullPointerException) {
            intent.pushToDialog(mContext.getString(R.string.error_wrong_format),
                    mContext.getString(R.string.error_title), R.raw.error, true, false, null);
        } else if (ex instanceof RuntimeException) {
            intent.pushToDialog(mContext.getString(R.string.error_no_audio_found),
                    mContext.getString(R.string.error_title), R.raw.error, false, false, null);
        } else if (ex instanceof UnknownHostException) {
            intent.pushToDialog(mContext.getString(R.string.error_connect_internet),
                    mContext.getString(R.string.error_title), R.raw.error, false, false, null);
        } else {
            intent.pushToDialog(
                    String.format(mContext.getString(R.string.error_parse_file_ncc), path),
                    mContext.getString(R.string.error_title), R.raw.error, true, false, null);
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
        } else if (ex instanceof SQLiteException) {
            Log.i(mContext.getClass().toString(), SQLiteException.class.toString());
        } else if (ex instanceof SQLiteConstraintException) {
            Log.i(mContext.getClass().toString(), SQLiteConstraintException.class.toString());
        } else if (ex instanceof ParserConfigurationException) {
            Log.i(mContext.getClass().toString(), ParserConfigurationException.class.toString());
        } else if (ex instanceof TransformerException) {
            Log.i(mContext.getClass().toString(), TransformerException.class.toString());
        } else if (ex instanceof IllegalArgumentException) {
            Log.i(mContext.getClass().toString(), IllegalArgumentException.class.toString());
        } else if (ex instanceof InterruptedException) {
            Log.i(mContext.getClass().toString(), InterruptedException.class.toString());
        } else {
            Log.i(mContext.getClass().toString(), Exception.class.toString());
        }

    }

    /**
     * This function show a dialog error to user in case download.
     */
    public void showDialogDowloadException(IntentController intent) {
        if (ex instanceof UnknownHostException) {
            intent.pushToDialog(mContext.getString(R.string.error_unknown_host),
                    mContext.getString(R.string.error_title), R.raw.error, false, false, null);
        } else if (ex instanceof SocketException) {
            intent.pushToDialog(mContext.getString(R.string.error_connect_internet),
                    mContext.getString(R.string.error_title), R.raw.error, false, false, null);
        } else if (ex instanceof FileNotFoundException) {
            intent.pushToDialog(mContext.getString(R.string.error_file_not_found),
                    mContext.getString(R.string.error_title), R.raw.error, false, false, null);
        } else if (ex instanceof IOException) {
        } else {
            intent.pushToDialog(mContext.getString(R.string.error_cannot_dowload),
                    mContext.getString(R.string.error_title), R.raw.error, false, false, null);
        }
    }
}
