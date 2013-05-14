package org.androiddaisyreader.sqllite;

import java.util.ArrayList;
import org.androiddaisyreader.model.RecentBooks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlLiteRecentBookHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "RecentBooksDB";
	private static final String TABLE_NAME = "RecentBooks";
	private static final String NAME_KEY = "_name";
	private static final String PATH_KEY = "_path";
	private static final String SORT_KEY = "_sort";

	public SqlLiteRecentBookHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreateTable = "create table " + TABLE_NAME + "(" + NAME_KEY
				+ " text primary key," + PATH_KEY + " text," + SORT_KEY + " integer " + ")";
		db.execSQL(sqlCreateTable);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// drop table if version is different.
		db.execSQL("drop table if exists " + TABLE_NAME);
		// Create tables again
		onCreate(db);
	}

	/**
	 * Add a record to RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void addRecentBook(RecentBooks recentBooks) {
		SQLiteDatabase mdb = getWritableDatabase();
		ContentValues mValue = new ContentValues();

		mValue.put(NAME_KEY, recentBooks.getName());
		mValue.put(PATH_KEY, recentBooks.getPath());
		mValue.put(SORT_KEY, recentBooks.getSort());

		mdb.insert(TABLE_NAME, null, mValue);
		mdb.close();
	}

	/**
	 * Delete a record of RecentBooks table
	 * 
	 * @param recentBooks
	 */
	public void deleteRecentBook(RecentBooks recentBooks) {
		SQLiteDatabase mdb = getWritableDatabase();

		mdb.delete(TABLE_NAME, NAME_KEY + "=?",
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
		mValue.put(SORT_KEY, recentBooks.getSort());

		mdb.update(TABLE_NAME, mValue, NAME_KEY + "=?", new String[] { recentBooks.getName() });
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

		Cursor mCursor = mdb.query(TABLE_NAME, new String[] { NAME_KEY, PATH_KEY, SORT_KEY },
				NAME_KEY + "=?", new String[] { name }, null, null, null);

		// Check data null or empty
		if (mCursor != null)
			mCursor.moveToFirst();
		RecentBooks mRecentBooks = new RecentBooks(mCursor.getString(0), mCursor.getString(1),
				Integer.valueOf(mCursor.getString(2)));

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
		String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + SORT_KEY + " DESC";
		Cursor mCursor = mdb.rawQuery(sql, null);
		ArrayList<RecentBooks> arrRecentBooks = new ArrayList<RecentBooks>();

		if (mCursor.moveToFirst()) {
			do {
				// Add to ArrayList
				arrRecentBooks.add(new RecentBooks(mCursor.getString(0), mCursor.getString(1),
						Integer.valueOf(mCursor.getString(2))));
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
		Cursor mCursor = mdb.query(TABLE_NAME, new String[] { NAME_KEY, PATH_KEY, SORT_KEY },
				NAME_KEY + "=?", new String[] { name }, null, null, null);

		return mCursor.moveToFirst();
	}
}
