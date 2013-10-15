package org.androiddaisyreader.sqlite;

import java.util.ArrayList;
import java.util.UUID;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.model.DaisyBookInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteDaisyBookHelper extends SQLiteHandler {
	private Context mContext;

	public SQLiteDaisyBookHelper(Context context) {
		super(context);
		this.mContext = context;
	}

	/**
	 * Add a record to DaisyBook table
	 * 
	 * @param DaisyBookInfo
	 */
	public boolean addDaisyBook(DaisyBookInfo daisyBook, String type) {

		boolean result = false;
		ContentValues mValue = new ContentValues();
		mValue.put(ID_KEY_DAISY_BOOK, UUID.randomUUID().toString());
		mValue.put(TITLE_KEY_DAISY_BOOK, daisyBook.getTitle());
		mValue.put(PATH_KEY_RECENT_BOOKS, daisyBook.getPath());
		mValue.put(AUTHOR_KEY_DAISY_BOOK, daisyBook.getAuthor());
		mValue.put(PUBLISHER_KEY_DAISY_BOOK, daisyBook.getPublisher());
		mValue.put(DATE_DAISY_BOOK, daisyBook.getDate());
		mValue.put(TYPE_OF_METADATA_DAISY_BOOK, type);
		mValue.put(SORT_KEY_RECENT_BOOKS, daisyBook.getSort());
		try {
			SQLiteDatabase mdb = getWritableDatabase();
			long i = mdb.insert(TABLE_NAME_DAISY_BOOK, null, mValue);
			if (i != -1) {
				result = true;
			}
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return result;

	}

	/**
	 * Get all daisy book from sqlite
	 * 
	 * @return
	 */
	public ArrayList<DaisyBookInfo> getAllDaisyBook(String type) {
		ArrayList<DaisyBookInfo> arrDaisyBook = new ArrayList<DaisyBookInfo>();
		try {
			SQLiteDatabase mdb = getReadableDatabase();
			Cursor mCursor = mdb.query(TABLE_NAME_DAISY_BOOK, new String[] { ID_KEY_DAISY_BOOK,
					TITLE_KEY_DAISY_BOOK, PATH_KEY_DAISY_BOOK, AUTHOR_KEY_DAISY_BOOK,
					PUBLISHER_KEY_DAISY_BOOK, DATE_DAISY_BOOK, SORT_KEY_DAISY_BOOK },
					TYPE_OF_METADATA_DAISY_BOOK + "=?", new String[] { type },
					TITLE_KEY_DAISY_BOOK, null, SORT_KEY_BOOKMARK);

			if (mCursor.moveToFirst()) {
				do {
					String id = mCursor.getString(mCursor.getColumnIndex(ID_KEY_DAISY_BOOK));
					String title = mCursor.getString(mCursor.getColumnIndex(TITLE_KEY_DAISY_BOOK));
					String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_DAISY_BOOK));
					String author = mCursor
							.getString(mCursor.getColumnIndex(AUTHOR_KEY_DAISY_BOOK));
					String publisher = mCursor.getString(mCursor
							.getColumnIndex(PUBLISHER_KEY_DAISY_BOOK));
					String date = mCursor.getString(mCursor.getColumnIndex(DATE_DAISY_BOOK));
					int sort = Integer.valueOf(mCursor.getString(mCursor
							.getColumnIndex(SORT_KEY_DAISY_BOOK)));
					// Add to ArrayList
					arrDaisyBook.add(new DaisyBookInfo(id, title, path, author, publisher, date,
							sort));
				} while (mCursor.moveToNext());
			}
			mCursor.close();
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return arrDaisyBook;
	}

	public DaisyBookInfo getDaisyBookByTitle(String title, String type) {
		DaisyBookInfo daisyBook = null;
		try {
			SQLiteDatabase mdb = getReadableDatabase();
			Cursor mCursor = mdb.query(TABLE_NAME_DAISY_BOOK, new String[] { ID_KEY_DAISY_BOOK,
					TITLE_KEY_DAISY_BOOK, PATH_KEY_DAISY_BOOK, AUTHOR_KEY_DAISY_BOOK,
					PUBLISHER_KEY_DAISY_BOOK, DATE_DAISY_BOOK, SORT_KEY_DAISY_BOOK },
					TITLE_KEY_DAISY_BOOK + "=?" + " AND " + TYPE_OF_METADATA_DAISY_BOOK + "=?",
					new String[] { title, type }, null, null, null);
			// Check data null or empty
			if (mCursor != null && mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				String id = mCursor.getString(mCursor.getColumnIndex(ID_KEY_DAISY_BOOK));
				String titleBook = mCursor.getString(mCursor.getColumnIndex(TITLE_KEY_DAISY_BOOK));
				String path = mCursor.getString(mCursor.getColumnIndex(PATH_KEY_DAISY_BOOK));
				String author = mCursor.getString(mCursor.getColumnIndex(AUTHOR_KEY_DAISY_BOOK));
				String publisher = mCursor.getString(mCursor
						.getColumnIndex(PUBLISHER_KEY_DAISY_BOOK));
				String date = mCursor.getString(mCursor.getColumnIndex(DATE_DAISY_BOOK));
				int sort = Integer.valueOf(mCursor.getString(mCursor
						.getColumnIndex(SORT_KEY_DAISY_BOOK)));
				daisyBook = new DaisyBookInfo(id, titleBook, path, author, publisher, date, sort);
			}
			if (mCursor != null) {
				mCursor.close();
				mdb.close();
			}
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return daisyBook;
	}

	public boolean deleteAllDaisyBook(String type) {
		boolean result = false;
		try {
			SQLiteDatabase mdb = getWritableDatabase();
			result = mdb.delete(TABLE_NAME_DAISY_BOOK, TYPE_OF_METADATA_DAISY_BOOK + "=?",
					new String[] { type }) > 0;
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return result;
	}

	public boolean deleteDaisyBook(String id) {
		boolean result = false;
		try {
			SQLiteDatabase mdb = getWritableDatabase();
			result = mdb.delete(TABLE_NAME_DAISY_BOOK, ID_KEY_DAISY_BOOK + "=?",
					new String[] { id }) > 0;
			mdb.close();
		} catch (Exception e) {
			PrivateException ex = new PrivateException(e, mContext);
			ex.writeLogException();
		}
		return result;
	}

	/**
	 * Check exists.
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExists(String name, String type) {
		boolean result = false;
		try {
			SQLiteDatabase mdb = getReadableDatabase();
			Cursor mCursor = mdb.query(TABLE_NAME_DAISY_BOOK, new String[] { ID_KEY_DAISY_BOOK,
					TITLE_KEY_DAISY_BOOK, PATH_KEY_DAISY_BOOK, AUTHOR_KEY_DAISY_BOOK,
					PUBLISHER_KEY_DAISY_BOOK, DATE_DAISY_BOOK, SORT_KEY_DAISY_BOOK },
					TITLE_KEY_DAISY_BOOK + "=?" + " AND " + TYPE_OF_METADATA_DAISY_BOOK + "=?",
					new String[] { name, type }, null, null, null);
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
