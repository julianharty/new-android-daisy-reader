package org.androiddaisyreader.sqlite;

import java.util.ArrayList;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.model.DaisyBookInfo;

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

public class SQLiteRecentBookHelper extends SQLiteHandler {

	private Context mContext;

	public SQLiteRecentBookHelper(Context context) {
		super(context);
		this.mContext = context;
	}

	/**
	 * Add a record to RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void addRecentBook(DaisyBookInfo recentBooks) {

		ContentValues mValue = new ContentValues();

		mValue.put(NAME_KEY_RECENT_BOOKS, recentBooks.getTitle());
		mValue.put(PATH_KEY_RECENT_BOOKS, recentBooks.getPath());
		mValue.put(SORT_KEY_RECENT_BOOKS, recentBooks.getSort());
		try {
			SQLiteDatabase mdb = getWritableDatabase();
			mdb.insert(TABLE_NAME_RECENT_BOOKS, null, mValue);
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}

	}

	/**
	 * Delete a record of RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void deleteRecentBook(DaisyBookInfo recentBooks) {

		try {
			SQLiteDatabase mdb = getWritableDatabase();
			mdb.delete(TABLE_NAME_RECENT_BOOKS, NAME_KEY_RECENT_BOOKS + "=?",
					new String[] { String.valueOf(recentBooks.getTitle()) });
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}

	}

	/**
	 * Update a record of RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void updateRecentBook(DaisyBookInfo recentBooks) {

		ContentValues mValue = new ContentValues();
		mValue.put(SORT_KEY_RECENT_BOOKS, recentBooks.getSort());
		try {
			SQLiteDatabase mdb = getWritableDatabase();
			mdb.update(TABLE_NAME_RECENT_BOOKS, mValue, NAME_KEY_RECENT_BOOKS + "=?",
					new String[] { recentBooks.getTitle() });
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}

	}

	/**
	 * Get info a recent book
	 * 
	 * @param name
	 * @return
	 */
	public DaisyBookInfo getInfoRecentBook(String name) {
		DaisyBookInfo mRecentBooks = null;
		try {
			SQLiteDatabase mdb = getReadableDatabase();
			Cursor mCursor = mdb.query(TABLE_NAME_RECENT_BOOKS, new String[] {
					NAME_KEY_RECENT_BOOKS, PATH_KEY_RECENT_BOOKS, SORT_KEY_RECENT_BOOKS },
					NAME_KEY_RECENT_BOOKS + "=?", new String[] { name }, null, null, null);
			// Check data null or empty
			if (mCursor != null && mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				String valueName = mCursor.getString(mCursor.getColumnIndex(NAME_KEY_RECENT_BOOKS));
				String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_RECENT_BOOKS));
				int sort = Integer.valueOf(mCursor.getString(mCursor
						.getColumnIndex(SORT_KEY_RECENT_BOOKS)));
				mRecentBooks = new DaisyBookInfo("", valueName, path, "author", "publisher",
						"date", sort);
			}
			if (mCursor != null) {
				mCursor.close();
				mdb.close();
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return mRecentBooks;
	}

	/**
	 * Get all recent books from sql lite
	 * 
	 * @return
	 */
	public ArrayList<DaisyBookInfo> getAllRecentBooks() {
		ArrayList<DaisyBookInfo> arrRecentBooks = new ArrayList<DaisyBookInfo>();
		try {
			SQLiteDatabase mdb = getReadableDatabase();
			String sql = "SELECT * FROM " + TABLE_NAME_RECENT_BOOKS + " ORDER BY "
					+ SORT_KEY_RECENT_BOOKS + " DESC";
			Cursor mCursor = mdb.rawQuery(sql, null);

			if (mCursor.moveToFirst()) {
				do {
					String valueName = mCursor.getString(mCursor
							.getColumnIndex(NAME_KEY_RECENT_BOOKS));
					String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_RECENT_BOOKS));
					int sort = Integer.valueOf(mCursor.getString(mCursor
							.getColumnIndex(SORT_KEY_RECENT_BOOKS)));
					// Add to ArrayList
					arrRecentBooks.add(new DaisyBookInfo("", valueName, path, "author",
							"publisher", "date", sort));
				} while (mCursor.moveToNext());
			}
			mCursor.close();
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return arrRecentBooks;
	}

	/**
	 * Check exists.
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExists(String name) {
		boolean result = false;
		try {
			SQLiteDatabase mdb = getReadableDatabase();
			Cursor mCursor = mdb.query(TABLE_NAME_RECENT_BOOKS, new String[] {
					NAME_KEY_RECENT_BOOKS, PATH_KEY_RECENT_BOOKS, SORT_KEY_RECENT_BOOKS },
					NAME_KEY_RECENT_BOOKS + "=?", new String[] { name }, null, null, null);
			result = mCursor.moveToFirst();
			mCursor.close();
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return result;
	}
}
