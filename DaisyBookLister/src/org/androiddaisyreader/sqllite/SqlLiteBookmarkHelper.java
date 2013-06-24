package org.androiddaisyreader.sqllite;

import java.util.ArrayList;
import java.util.UUID;
import org.androiddaisyreader.model.Bookmark;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * This adapter to handle sqlite of bookmark
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class SqlLiteBookmarkHelper extends HandleSqlLite {

	public SqlLiteBookmarkHelper(Context context) {
		super(context);
	}

	/**
	 * Add a record of Bookmark table
	 * 
	 * @param bookmark
	 */
	public void addBookmark(Bookmark bookmark) {
		SQLiteDatabase mdb = getWritableDatabase();
		ContentValues mValue = new ContentValues();
		mValue.put(PATH_KEY_BOOKMARK, bookmark.getPath());
		mValue.put(TEXT_KEY_BOOKMARK, bookmark.getText());
		mValue.put(TIME_KEY_BOOKMARK, bookmark.getTime());
		mValue.put(SECTION_KEY_BOOKMARK, bookmark.getSection());
		mValue.put(SORT_KEY_BOOKMARK, bookmark.getSort());
		mValue.put(ID_KEY_BOOKMARK, UUID.randomUUID().toString());

		mdb.insert(TABLE_NAME_BOOKMARK, null, mValue);
		mdb.close();
	}

	/**
	 * Delete a record of Bookmark table
	 * 
	 * @param id
	 */
	public void deleteBookmark(String id) {
		SQLiteDatabase mdb = getWritableDatabase();

		mdb.delete(TABLE_NAME_BOOKMARK, ID_KEY_BOOKMARK + "=?", new String[] { id });
		mdb.close();
	}

	/**
	 * Update bookmark to sqlite
	 * 
	 * @param bookmark
	 */
	public void updateBookmark(Bookmark bookmark) {
		SQLiteDatabase mdb = getWritableDatabase();

		ContentValues mValue = new ContentValues();
		mValue.put(PATH_KEY_BOOKMARK, bookmark.getPath());
		mValue.put(TEXT_KEY_BOOKMARK, bookmark.getText());
		mValue.put(TIME_KEY_BOOKMARK, bookmark.getTime());
		mValue.put(SECTION_KEY_BOOKMARK, bookmark.getSection());
		mValue.put(SORT_KEY_BOOKMARK, bookmark.getSort());
		mdb.update(TABLE_NAME_BOOKMARK, mValue, ID_KEY_BOOKMARK + "=?", new String[] { bookmark.getId() });
		mdb.close();
	}

	/**
	 * Get Info (name, path, section, ect) by ID
	 * 
	 * @param id
	 * @return Bookmark
	 */
	public Bookmark getInfoBookmark(String id) {
		SQLiteDatabase mdb = getReadableDatabase();

		Cursor mCursor = mdb.query(TABLE_NAME_BOOKMARK, new String[] { PATH_KEY_BOOKMARK, TEXT_KEY_BOOKMARK, TIME_KEY_BOOKMARK,
				SECTION_KEY_BOOKMARK, SORT_KEY_BOOKMARK, ID_KEY_BOOKMARK }, ID_KEY_BOOKMARK + "=?", new String[] { id }, null, null,
				null);

		// Check data null or empty
		Bookmark bookmark = null;
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_BOOKMARK));
			String text = mCursor.getString(mCursor.getColumnIndex(TEXT_KEY_BOOKMARK));
			int time = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(TIME_KEY_BOOKMARK)));
			int section = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(SECTION_KEY_BOOKMARK)));
			int sort = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(SORT_KEY_BOOKMARK)));
			String valueId = mCursor.getString(mCursor.getColumnIndex(ID_KEY_BOOKMARK));
			bookmark = new Bookmark(path, text, time, section, sort, valueId);
		}
		mCursor.close();
		mdb.close();
		return bookmark;
	}

	/**
	 * Get all bookmark by path of book
	 * 
	 * @param path
	 * @return ArrayList<Bookmark>
	 */
	public ArrayList<Bookmark> getAllBookmark(String path) {
		SQLiteDatabase mdb = getReadableDatabase();
		Cursor mCursor = mdb.query(TABLE_NAME_BOOKMARK, new String[] { PATH_KEY_BOOKMARK, TEXT_KEY_BOOKMARK, TIME_KEY_BOOKMARK,
				SECTION_KEY_BOOKMARK, SORT_KEY_BOOKMARK, ID_KEY_BOOKMARK }, PATH_KEY_BOOKMARK + "=?", new String[] { path }, null,
				null, SORT_KEY_BOOKMARK);
		ArrayList<Bookmark> arrBookmark = new ArrayList<Bookmark>();

		if (mCursor.moveToFirst()) {
			do {
				String valuePath = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_BOOKMARK));
				String text = mCursor.getString(mCursor.getColumnIndex(TEXT_KEY_BOOKMARK));
				int time = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(TIME_KEY_BOOKMARK)));
				int section = Integer
						.valueOf(mCursor.getString(mCursor.getColumnIndex(SECTION_KEY_BOOKMARK)));
				int sort = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(SORT_KEY_BOOKMARK)));
				String valueId = mCursor.getString(mCursor.getColumnIndex(ID_KEY_BOOKMARK));
				// Add to ArrayList
				arrBookmark.add(new Bookmark(valuePath, text, time, section, sort, valueId));
			} while (mCursor.moveToNext());
		}

		mCursor.close();
		mdb.close();

		return arrBookmark;
	}

}
