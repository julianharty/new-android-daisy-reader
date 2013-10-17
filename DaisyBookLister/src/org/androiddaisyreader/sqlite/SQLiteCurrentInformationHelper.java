package org.androiddaisyreader.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.model.CurrentInformation;
import java.util.UUID;

public class SQLiteCurrentInformationHelper extends SQLiteHandler {

    private Context mContext;

    public SQLiteCurrentInformationHelper(Context context) {
        super(context);
        this.mContext = context;
    }

    /**
     * Add a record of current information table
     * 
     * @param current
     */
    public void addCurrentInformation(CurrentInformation current) {

        ContentValues mValue = new ContentValues();
        mValue.put(ID_KEY_CURRENT_INFORMATION, UUID.randomUUID().toString());
        putValue(mValue, current);
        try {
            SQLiteDatabase mdb = getWritableDatabase();
            mdb.insert(TABLE_NAME_CURRENT_INFORMATION, null, mValue);
            mdb.close();
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext);
            ex.writeLogException();
        }

    }

    /**
     * Delete a of current information table
     * 
     * @param id
     */
    public void deleteCurrentInformation(String id) {
        try {
            SQLiteDatabase mdb = getWritableDatabase();
            mdb.delete(TABLE_NAME_CURRENT_INFORMATION, ID_KEY_CURRENT_INFORMATION + "=?",
                    new String[] { id });
            mdb.close();
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext);
            ex.writeLogException();
        }

    }

    /**
     * Update current information to sqlite
     * 
     * @param current
     */
    public void updateCurrentInformation(CurrentInformation current) {
        ContentValues mValue = new ContentValues();
        try {
            putValue(mValue, current);
            SQLiteDatabase mdb = getWritableDatabase();
            mdb.update(TABLE_NAME_CURRENT_INFORMATION, mValue, ID_KEY_CURRENT_INFORMATION + "=?",
                    new String[] { current.getId() });
            mdb.close();
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext);
            ex.writeLogException();
        }
    }

    private void putValue(ContentValues value, CurrentInformation currentInfomation) {
        value.put(AUDIO_NAME_KEY_CURRENT_INFORMATION, currentInfomation.getAudioName());
        value.put(PATH_KEY_CURRENT_INFORMATION, currentInfomation.getPath());
        value.put(TIME_KEY_CURRENT_INFORMATION, currentInfomation.getTime());
        value.put(SECTION_KEY_CURRENT_INFORMATION, currentInfomation.getSection());
        value.put(PLAYING_KEY_CURRENT_INFORMATION, currentInfomation.getPlaying());
        value.put(SENTENCE_KEY_CURRENT_INFORMATION, currentInfomation.getSentence());
        value.put(ACTIVITY_KEY_CURRENT_INFORMATION, currentInfomation.getActivity());
        value.put(FIRST_NEXT_KEY_CURRENT_INFORMATION, currentInfomation.getFirstNext());
        value.put(FIRST_PREVIOUS_KEY_CURRENT_INFORMATION, currentInfomation.getFirstPrevious());
        value.put(AT_THE_END_KEY_CURRENT_INFORMATION, currentInfomation.getAtTheEnd());
    }

    /**
     * Get current information from sqlite
     * 
     * @return
     */
    public CurrentInformation getCurrentInformation() {
        String valueOfTrue = "1";
        CurrentInformation current = null;
        try {
            SQLiteDatabase mdb = getReadableDatabase();
            Cursor mCursor = mdb.query(TABLE_NAME_CURRENT_INFORMATION, new String[] {
                    AUDIO_NAME_KEY_CURRENT_INFORMATION, PATH_KEY_CURRENT_INFORMATION,
                    SECTION_KEY_CURRENT_INFORMATION, TIME_KEY_CURRENT_INFORMATION,
                    PLAYING_KEY_CURRENT_INFORMATION, SENTENCE_KEY_CURRENT_INFORMATION,
                    ACTIVITY_KEY_CURRENT_INFORMATION, ID_KEY_CURRENT_INFORMATION,
                    FIRST_NEXT_KEY_CURRENT_INFORMATION, FIRST_PREVIOUS_KEY_CURRENT_INFORMATION,
                    AT_THE_END_KEY_CURRENT_INFORMATION }, null, null, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                String audioName = mCursor.getString(mCursor
                        .getColumnIndex(AUDIO_NAME_KEY_CURRENT_INFORMATION));
                String path = mCursor.getString(mCursor
                        .getColumnIndex(PATH_KEY_CURRENT_INFORMATION));
                int section = Integer.valueOf(mCursor.getString(mCursor
                        .getColumnIndex(SECTION_KEY_CURRENT_INFORMATION)));
                int time = Integer.valueOf(mCursor.getString(mCursor
                        .getColumnIndex(TIME_KEY_CURRENT_INFORMATION)));
                boolean playing = mCursor.getString(
                        mCursor.getColumnIndex(PLAYING_KEY_CURRENT_INFORMATION)).contains(
                        valueOfTrue);
                int sentence = Integer.valueOf(mCursor.getString(mCursor
                        .getColumnIndex(SENTENCE_KEY_CURRENT_INFORMATION)));
                String activity = mCursor.getString(mCursor
                        .getColumnIndex(ACTIVITY_KEY_CURRENT_INFORMATION));
                String id = mCursor.getString(mCursor.getColumnIndex(ID_KEY_CURRENT_INFORMATION));
                boolean firstNext = mCursor.getString(
                        mCursor.getColumnIndex(FIRST_NEXT_KEY_CURRENT_INFORMATION)).contains(
                        valueOfTrue);
                boolean firstPrevious = mCursor.getString(
                        mCursor.getColumnIndex(FIRST_PREVIOUS_KEY_CURRENT_INFORMATION)).contains(
                        valueOfTrue);
                boolean atTheEnd = mCursor.getString(
                        mCursor.getColumnIndex(AT_THE_END_KEY_CURRENT_INFORMATION)).contains(
                        valueOfTrue);
                current = new CurrentInformation(audioName, path, section, time, playing, sentence,
                        activity, id, firstNext, firstPrevious, atTheEnd);
            }
            if (mCursor != null) {
                mCursor.close();
                mdb.close();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext);
            ex.writeLogException();
        }
        return current;
    }
}
