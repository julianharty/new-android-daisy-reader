package org.androiddaisyreader.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHandler extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "EbookReaderDB";

	public static final String TABLE_NAME_BOOKMARK = "Bookmarks";
	public static final String ID_KEY_BOOKMARK = "_id";
	public static final String AUDIO_FILE_NAME_KEY_BOOKMARK = "_audio_file_name";
	public static final String PATH_KEY_BOOKMARK = "_path";
	public static final String TEXT_KEY_BOOKMARK = "_text";
	public static final String TIME_KEY_BOOKMARK = "_time";
	public static final String SECTION_KEY_BOOKMARK = "_section";
	public static final String SORT_KEY_BOOKMARK = "_sort";

	public static final String TABLE_NAME_CURRENT_INFORMATION = "CurrentInformation";
	public static final String ID_KEY_CURRENT_INFORMATION = "_id";
	public static final String AUDIO_NAME_KEY_CURRENT_INFORMATION = "_audio_name";
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

	public static final String TABLE_NAME_DAISY_BOOK = "DaisyBook";
	public static final String ID_KEY_DAISY_BOOK = "_id";
	public static final String TITLE_KEY_DAISY_BOOK = "_name";
	public static final String PATH_KEY_DAISY_BOOK = "_path";
	public static final String AUTHOR_KEY_DAISY_BOOK = "_author";
	public static final String PUBLISHER_KEY_DAISY_BOOK = "_publisher";
	public static final String DATE_DAISY_BOOK = "_date";
	public static final String TYPE_OF_METADATA_DAISY_BOOK = "_type";
	public static final String SORT_KEY_DAISY_BOOK = "_sort";

	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreateTableBookmark = "create table " + TABLE_NAME_BOOKMARK + "("
				+ ID_KEY_BOOKMARK + " text primary key," + AUDIO_FILE_NAME_KEY_BOOKMARK + " text,"
				+ PATH_KEY_BOOKMARK + " text," + TEXT_KEY_BOOKMARK + " text," + TIME_KEY_BOOKMARK
				+ " integer," + SECTION_KEY_BOOKMARK + " integer," + SORT_KEY_BOOKMARK
				+ " integer " + ")";
		db.execSQL(sqlCreateTableBookmark);

		String sqlCreateTableCurrentInformation = "create table " + TABLE_NAME_CURRENT_INFORMATION
				+ "(" + ID_KEY_CURRENT_INFORMATION + " text primary key,"
				+ AUDIO_NAME_KEY_CURRENT_INFORMATION + " text," + PATH_KEY_CURRENT_INFORMATION
				+ " text," + TIME_KEY_CURRENT_INFORMATION + " integer,"
				+ SECTION_KEY_CURRENT_INFORMATION + " integer," + PLAYING_KEY_CURRENT_INFORMATION
				+ " boolean," + SENTENCE_KEY_CURRENT_INFORMATION + " integer,"
				+ ACTIVITY_KEY_CURRENT_INFORMATION + " text," + FIRST_NEXT_KEY_CURRENT_INFORMATION
				+ " boolean," + FIRST_PREVIOUS_KEY_CURRENT_INFORMATION + " boolean,"
				+ AT_THE_END_KEY_CURRENT_INFORMATION + " text " + ")";
		db.execSQL(sqlCreateTableCurrentInformation);

		String sqlCreateTableRecentBooks = "create table " + TABLE_NAME_RECENT_BOOKS + "("
				+ NAME_KEY_RECENT_BOOKS + " text primary key," + PATH_KEY_RECENT_BOOKS + " text,"
				+ SORT_KEY_RECENT_BOOKS + " integer " + ")";
		db.execSQL(sqlCreateTableRecentBooks);

		String sqlCreateTableDaisyBook = "create table " + TABLE_NAME_DAISY_BOOK + "("
				+ ID_KEY_DAISY_BOOK + " text primary key," + PATH_KEY_DAISY_BOOK + " text,"
				+ TITLE_KEY_DAISY_BOOK + " text NOT NULL," + AUTHOR_KEY_DAISY_BOOK + " text,"
				+ PUBLISHER_KEY_DAISY_BOOK + " text," + TYPE_OF_METADATA_DAISY_BOOK + " text,"
				+ DATE_DAISY_BOOK + " text," + SORT_KEY_RECENT_BOOKS + " integer " + ")";
		db.execSQL(sqlCreateTableDaisyBook);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BOOKMARK);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CURRENT_INFORMATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_RECENT_BOOKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DAISY_BOOK);
		onCreate(db);
	}

}
