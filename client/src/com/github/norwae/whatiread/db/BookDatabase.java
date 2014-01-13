package com.github.norwae.whatiread.db;

import java.util.List;

import android.content.Context;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;

public class BookDatabase {

	private BookDatabaseHelper bookDatabaseAccess;
	private Context ctx;
	
	public BookDatabase(Context context) {
		bookDatabaseAccess = new BookDatabaseHelper(context);
		ctx = context;
	}
	

	public BookInfo getForISBN13(ISBN13 isbn) {
		List<BookInfo> infos = bookDatabaseAccess.selectBookInfo(ctx, BookDatabaseHelper.ISBN_COLUMN + " == ?", isbn.toString());
		
		if (!infos.isEmpty()) {
			return infos.get(0);
		}
		
		return null;
	}

	public Context getOriginContext() {
		return ctx;
	}
	
	public void close() {
		bookDatabaseAccess.close();
	}
	

	public List<BookInfo> getForString(String arg) {
		String arg2 = "%" + arg + "%";
		return bookDatabaseAccess.selectBookInfo(ctx, BookDatabaseHelper.TITLE_COLUMN + " LIKE ? OR " + BookDatabaseHelper.AUTHOR_COLUMN + " LIKE ?", arg2, arg2);
		
	}


	public void saveOrUpdate(BookInfo bookInfo) {
		bookDatabaseAccess.saveOrUpdate(bookInfo);
	}


	public void delete(BookInfo bookInfo) {
		bookDatabaseAccess.delete(bookInfo);		
	}
}
