package org.androiddaisyreader.sqllite;

import java.util.ArrayList;
import java.util.UUID;

import org.androiddaisyreader.model.Bookmark;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlLiteBookmarkHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "BookmarksDB";
	private static final String TABLE_NAME = "Bookmarks";
	private static final String ID_KEY = "_id";
	private static final String BOOK_KEY = "_book";
	private static final String TEXT_KEY = "_text";
	private static final String TIME_KEY = "_time";
	private static final String SECTION_KEY = "_section";
	private static final String SORT_KEY = "_sort";

	public SqlLiteBookmarkHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreateTable = "create table " + TABLE_NAME + "(" + ID_KEY
				+ " text primary key," + BOOK_KEY + " text," + TEXT_KEY
				+ " text," + TIME_KEY + " integer," + SECTION_KEY + " integer,"
				+ SORT_KEY + " integer " + ")";
		db.execSQL(sqlCreateTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// drop table if version is different.
		db.execSQL("drop table if exists " + TABLE_NAME);
		// Create tables again
		onCreate(db);
	}

	public void addBookmark(Bookmark bookmark) {
		SQLiteDatabase mdb = getWritableDatabase();
		ContentValues mValue = new ContentValues();
		mValue.put(BOOK_KEY, bookmark.getBook());
		mValue.put(TEXT_KEY, bookmark.getText());
		mValue.put(TIME_KEY, bookmark.getTime());
		mValue.put(SECTION_KEY, bookmark.getSection());
		mValue.put(SORT_KEY, bookmark.getSort());
		mValue.put(ID_KEY, UUID.randomUUID().toString());

		mdb.insert(TABLE_NAME, null, mValue);
		mdb.close();
	}

	public void deleteBookmark(String id) {
		SQLiteDatabase mdb = getWritableDatabase();

		mdb.delete(TABLE_NAME, ID_KEY + "=?", new String[] { id });
		mdb.close();
	}

	public void updateBookmark(Bookmark bookmark) {
		SQLiteDatabase mdb = getWritableDatabase();

		ContentValues mValue = new ContentValues();
		mValue.put(BOOK_KEY, bookmark.getBook());
		mValue.put(TEXT_KEY, bookmark.getText());
		mValue.put(TIME_KEY, bookmark.getTime());
		mValue.put(SECTION_KEY, bookmark.getSection());
		mValue.put(SORT_KEY, bookmark.getSort());
		mdb.update(TABLE_NAME, mValue, ID_KEY + "=?",
				new String[] { bookmark.getId() });
		mdb.close();
	}

	public Bookmark getInfoBookmark(String id) {
		SQLiteDatabase mdb = getReadableDatabase();

		Cursor mCursor = mdb.query(TABLE_NAME, new String[] { BOOK_KEY,
				TEXT_KEY, TIME_KEY, SECTION_KEY, SORT_KEY, ID_KEY }, ID_KEY + "=?",
				new String[] { id }, null, null, null);

		// Check data null or empty
		if (mCursor != null)
			mCursor.moveToFirst();
		Bookmark mBookmark = new Bookmark(mCursor.getString(0),
				mCursor.getString(1), Integer.valueOf(mCursor.getString(2)),
				Integer.valueOf(mCursor.getString(3)), Integer.valueOf(mCursor
						.getString(4)), mCursor.getString(5));

		mCursor.close();
		mdb.close();
		return mBookmark;
	}

	public ArrayList<Bookmark> getAllBookmark(String book) {
		SQLiteDatabase mdb = getReadableDatabase();
		Cursor mCursor = mdb.query(TABLE_NAME, new String[] { BOOK_KEY,
				TEXT_KEY, TIME_KEY, SECTION_KEY, SORT_KEY, ID_KEY }, BOOK_KEY + "=?",
				new String[] { book }, null, null, SORT_KEY);
		ArrayList<Bookmark> arrBookmark = new ArrayList<Bookmark>();

		if (mCursor.moveToFirst()) {
			do {
				// Add to ArrayList
				arrBookmark.add(new Bookmark(mCursor.getString(0), mCursor
						.getString(1), Integer.valueOf(mCursor.getString(2)),
						Integer.valueOf(mCursor.getString(3)), Integer
								.valueOf(mCursor.getString(4)), mCursor.getString(5)));
			} while (mCursor.moveToNext());
		}

		mCursor.close();
		mdb.close();

		return arrBookmark;
	}

}
