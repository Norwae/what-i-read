package com.github.norwae.whatiread.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDatabaseHelper extends SQLiteOpenHelper {


	private static final String DATABASE_NAME = "com.github.norwae.whatiread.db.BookDatabase";

	private static final String BOOKSHELF_TABLE_NAME = "bookshelf";
	
	private static final String ISBN_COLUMN = "isbn";
	private static final String AUTHOR_COLUMN = "author";
	private static final String RATING_COLUMN = "rating";
	private static final String SCANNED_COLUMN = "scanned";
	private static final String COMMENT_COLUMN = "comment";
	private static final String TITLE_COLUMN = "title";
	
	private static final String CREATE_BOOKINFO_TABLE = "CREATE TABLE " + BOOKSHELF_TABLE_NAME + " ("
			+ ISBN_COLUMN + " TEXT,"
			+ TITLE_COLUMN + " TEXT," 
			+ AUTHOR_COLUMN + " TEXT,"
			+ RATING_COLUMN + " INTEGER,"
			+ COMMENT_COLUMN + " TEXT,"
			+ SCANNED_COLUMN + " REAL);";

	public BookDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 0);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BOOKINFO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// NOT versioned right now

	}

}
