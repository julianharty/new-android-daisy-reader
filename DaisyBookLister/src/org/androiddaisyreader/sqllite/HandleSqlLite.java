package org.androiddaisyreader.sqllite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HandleSqlLite extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "EbookReaderDB";

	public static final String TABLE_NAME_BOOKMARK = "Bookmarks";
	public static final String ID_KEY_BOOKMARK = "_id";
	public static final String PATH_KEY_BOOKMARK = "_path";
	public static final String TEXT_KEY_BOOKMARK = "_text";
	public static final String TIME_KEY_BOOKMARK = "_time";
	public static final String SECTION_KEY_BOOKMARK = "_section";
	public static final String SORT_KEY_BOOKMARK = "_sort";

	public static final String TABLE_NAME_CURRENT_INFORMATION = "CurrentInformation";
	public static final String ID_KEY_CURRENT_INFORMATION = "_id";
	public static final String PATH_KEY_CURRENT_INFORMATION = "_path";
	public static final String TIME_KEY_CURRENT_INFORMATION = "_time";
	public static final String SECTION_KEY_CURRENT_INFORMATION = "_section";
	public static final String PLAYING_KEY_CURRENT_INFORMATION = "_playing";
	public static final String SENTENCE_KEY_CURRENT_INFORMATION = "_sentence";
	public static final String ACTIVITY_KEY_CURRENT_INFORMATION = "_activity";
	public static final String FIRST_NEXT_KEY_CURRENT_INFORMATION = "_first_next";
	public static final String FIRST_PREVIOUS_KEY_CURRENT_INFORMATION = "_first_previous";
	public static final String AT_THE_END_KEY_CURRENT_INFORMATION = "_at_the_end";

	public static final String TABLE_NAME_RECENT_BOOKS = "RecentBooks";
	public static final String NAME_KEY_RECENT_BOOKS = "_name";
	public static final String PATH_KEY_RECENT_BOOKS = "_path";
	public static final String SORT_KEY_RECENT_BOOKS = "_sort";

	public HandleSqlLite(Context context) {
		super(context, DATABASE_NAME, null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreateTableBookmark = "create table " + TABLE_NAME_BOOKMARK + "("
				+ ID_KEY_BOOKMARK + " text primary key," + PATH_KEY_BOOKMARK + " text,"
				+ TEXT_KEY_BOOKMARK + " text," + TIME_KEY_BOOKMARK + " integer,"
				+ SECTION_KEY_BOOKMARK + " integer," + SORT_KEY_BOOKMARK + " integer " + ")";
		db.execSQL(sqlCreateTableBookmark);

		String sqlCreateTableCurrentInformation = "create table " + TABLE_NAME_CURRENT_INFORMATION
				+ "(" + ID_KEY_CURRENT_INFORMATION + " text primary key,"
				+ PATH_KEY_CURRENT_INFORMATION + " text," + TIME_KEY_CURRENT_INFORMATION
				+ " integer," + SECTION_KEY_CURRENT_INFORMATION + " integer,"
				+ PLAYING_KEY_CURRENT_INFORMATION + " boolean," + SENTENCE_KEY_CURRENT_INFORMATION
				+ " integer," + ACTIVITY_KEY_CURRENT_INFORMATION + " text,"
				+ FIRST_NEXT_KEY_CURRENT_INFORMATION + " boolean,"
				+ FIRST_PREVIOUS_KEY_CURRENT_INFORMATION + " boolean,"
				+ AT_THE_END_KEY_CURRENT_INFORMATION + " text " + ")";
		db.execSQL(sqlCreateTableCurrentInformation);

		String sqlCreateTableRecentBooks = "create table " + TABLE_NAME_RECENT_BOOKS + "("
				+ NAME_KEY_RECENT_BOOKS + " text primary key," + PATH_KEY_RECENT_BOOKS + " text,"
				+ SORT_KEY_RECENT_BOOKS + " integer " + ")";
		db.execSQL(sqlCreateTableRecentBooks);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BOOKMARK);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CURRENT_INFORMATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_RECENT_BOOKS);
		onCreate(db);
	}

}
