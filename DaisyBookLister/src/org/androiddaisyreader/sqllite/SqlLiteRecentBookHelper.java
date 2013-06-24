package org.androiddaisyreader.sqllite;

import java.util.ArrayList;
import org.androiddaisyreader.model.RecentBooks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * This adapter to handle sqlite of recent book
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class SqlLiteRecentBookHelper extends HandleSqlLite {

	public SqlLiteRecentBookHelper(Context context) {
		super(context);
	}

	/**
	 * Add a record to RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void addRecentBook(RecentBooks recentBooks) {
		SQLiteDatabase mdb = getWritableDatabase();
		ContentValues mValue = new ContentValues();

		mValue.put(NAME_KEY_RECENT_BOOKS, recentBooks.getName());
		mValue.put(PATH_KEY_RECENT_BOOKS, recentBooks.getPath());
		mValue.put(SORT_KEY_RECENT_BOOKS, recentBooks.getSort());

		mdb.insert(TABLE_NAME_RECENT_BOOKS, null, mValue);
		mdb.close();
	}

	/**
	 * Delete a record of RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void deleteRecentBook(RecentBooks recentBooks) {
		SQLiteDatabase mdb = getWritableDatabase();

		mdb.delete(TABLE_NAME_RECENT_BOOKS, NAME_KEY_RECENT_BOOKS + "=?",
				new String[] { String.valueOf(recentBooks.getName()) });
		mdb.close();
	}

	/**
	 * Update a record of RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void updateRecentBook(RecentBooks recentBooks) {
		SQLiteDatabase mdb = getWritableDatabase();

		ContentValues mValue = new ContentValues();
		mValue.put(SORT_KEY_RECENT_BOOKS, recentBooks.getSort());

		mdb.update(TABLE_NAME_RECENT_BOOKS, mValue, NAME_KEY_RECENT_BOOKS + "=?",
				new String[] { recentBooks.getName() });
		mdb.close();
	}

	/**
	 * Get info a recent book
	 * 
	 * @param name
	 * @return
	 */
	public RecentBooks getInfoRecentBook(String name) {
		SQLiteDatabase mdb = getReadableDatabase();

		Cursor mCursor = mdb.query(TABLE_NAME_RECENT_BOOKS, new String[] { NAME_KEY_RECENT_BOOKS,
				PATH_KEY_RECENT_BOOKS, SORT_KEY_RECENT_BOOKS }, NAME_KEY_RECENT_BOOKS + "=?",
				new String[] { name }, null, null, null);
		RecentBooks mRecentBooks = null;
		// Check data null or empty
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			String valueName = mCursor.getString(mCursor.getColumnIndex(NAME_KEY_RECENT_BOOKS));
			String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_RECENT_BOOKS));
			int sort = Integer.valueOf(mCursor.getString(mCursor
					.getColumnIndex(SORT_KEY_RECENT_BOOKS)));
			mRecentBooks = new RecentBooks(valueName, path, sort);
		}
		mCursor.close();
		mdb.close();
		return mRecentBooks;
	}

	/**
	 * Get all recent books from sql lite
	 * 
	 * @return
	 */
	public ArrayList<RecentBooks> getAllRecentBooks() {
		SQLiteDatabase mdb = getReadableDatabase();
		String sql = "SELECT * FROM " + TABLE_NAME_RECENT_BOOKS + " ORDER BY "
				+ SORT_KEY_RECENT_BOOKS + " DESC";
		Cursor mCursor = mdb.rawQuery(sql, null);
		ArrayList<RecentBooks> arrRecentBooks = new ArrayList<RecentBooks>();
		if (mCursor.moveToFirst()) {
			do {
				String valueName = mCursor.getString(mCursor.getColumnIndex(NAME_KEY_RECENT_BOOKS));
				String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_RECENT_BOOKS));
				int sort = Integer.valueOf(mCursor.getString(mCursor
						.getColumnIndex(SORT_KEY_RECENT_BOOKS)));
				// Add to ArrayList
				arrRecentBooks.add(new RecentBooks(valueName, path, sort));
			} while (mCursor.moveToNext());
		}

		mCursor.close();
		mdb.close();

		return arrRecentBooks;
	}

	/**
	 * Check exists.
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExists(String name) {
		SQLiteDatabase mdb = getReadableDatabase();
		Cursor mCursor = mdb.query(TABLE_NAME_RECENT_BOOKS, new String[] { NAME_KEY_RECENT_BOOKS,
				PATH_KEY_RECENT_BOOKS, SORT_KEY_RECENT_BOOKS }, NAME_KEY_RECENT_BOOKS + "=?",
				new String[] { name }, null, null, null);
		return mCursor.moveToFirst();
	}
}
