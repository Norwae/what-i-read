package com.github.norwae.whatiread.db;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.github.norwae.whatiread.data.BookInfo;

public class BookDatabaseHelper extends SQLiteOpenHelper {


	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "com.github.norwae.whatiread.db.BookDatabase";
	private static final String BOOKSHELF_TABLE_NAME = "bookshelf";
	private static final DateFormat SIMPLE_DATE = new SimpleDateFormat("yyyy-MM-DD");
	
	static final String ISBN_COLUMN = "isbn";
	static final String AUTHOR_COLUMN = "author";
	static final String RATING_COLUMN = "rating";
	static final String SCANNED_COLUMN = "scanned";
	static final String COMMENT_COLUMN = "comment";
	static final String TITLE_COLUMN = "title";
	
	private static final String[] BOOKSHELF_FIELD_NAMES = {
		ISBN_COLUMN,
		AUTHOR_COLUMN,
		RATING_COLUMN,
		SCANNED_COLUMN, 
		COMMENT_COLUMN,
		TITLE_COLUMN
	};
	
	private static final String CREATE_BOOKINFO_TABLE = "CREATE TABLE " + BOOKSHELF_TABLE_NAME + " ("
			+ ISBN_COLUMN + " TEXT PRIMARY KEY,"
			+ TITLE_COLUMN + " TEXT," 
			+ AUTHOR_COLUMN + " TEXT,"
			+ RATING_COLUMN + " INTEGER,"
			+ COMMENT_COLUMN + " TEXT,"
			+ SCANNED_COLUMN + " TEXT);";

	private static final String INSERT_OR_UPDATE_BOOK_INFO = "INSERT OR REPLACE INTO " + BOOKSHELF_TABLE_NAME + " ("
			+ ISBN_COLUMN + ","
			+ TITLE_COLUMN + ","
			+ AUTHOR_COLUMN + ","
			+ RATING_COLUMN + ","
			+ COMMENT_COLUMN + ","
			+ SCANNED_COLUMN + ") VALUES (?, ?, ?, ?, ?, ?)";

	public BookDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BOOKINFO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// NOT versioned right now

	}

	public List<BookInfo> selectBookInfo(Context ctx, String aQuery, String ...someQueryArgs) {
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = database.query(BOOKSHELF_TABLE_NAME, BOOKSHELF_FIELD_NAMES, aQuery, someQueryArgs, null, null, null);

		int authorIdx = cursor.getColumnIndex(AUTHOR_COLUMN);
		int titleIdx = cursor.getColumnIndex(TITLE_COLUMN);
		int isbnIdx = cursor.getColumnIndex(ISBN_COLUMN);
		int commentIdx = cursor.getColumnIndex(COMMENT_COLUMN);
		int scannedIdx = cursor.getColumnIndex(SCANNED_COLUMN);
		int ratingIdx = cursor.getColumnIndex(RATING_COLUMN);
		
		List<BookInfo> rv = new ArrayList<BookInfo>();
		
		while (cursor.moveToNext()) {
			String author = cursor.getString(authorIdx);
			String title = cursor.getString(titleIdx);
			String isbn = cursor.getString(isbnIdx);
			String comment = cursor.getString(commentIdx);
			Date scanned;
			try {
				scanned = ((DateFormat) SIMPLE_DATE.clone()).parse(cursor.getString(scannedIdx));
			} catch (ParseException e) {
				Log.e("Book Database", "Failed to convert stored scan-date", e);
				scanned = new Date();
			}
			int rating = cursor.getInt(ratingIdx);
			
			BookInfo info = new BookInfo(title, author, isbn, scanned, rating, comment);
			
			rv.add(info);
		}
		
		return rv;
	}

	public void saveOrUpdate(BookInfo bookInfo) {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteStatement statement = db.compileStatement(INSERT_OR_UPDATE_BOOK_INFO);
		statement.bindString(1, bookInfo.getEan13());
		statement.bindString(2, bookInfo.getTitle());
		statement.bindString(3, bookInfo.getAuthor());
		statement.bindLong(4, bookInfo.getRating());
		statement.bindString(5, bookInfo.getComment());
		statement.bindString(6, ((DateFormat)SIMPLE_DATE.clone()).format(bookInfo.getFirstView()));
		
		statement.execute();
	}

}
