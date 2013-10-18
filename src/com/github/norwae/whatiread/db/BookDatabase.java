package com.github.norwae.whatiread.db;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;

public class BookDatabase {

	private BookDatabaseHelper bookDatabaseAccess;

	
	
	public BookDatabase(Context context) {
		bookDatabaseAccess = new BookDatabaseHelper(context);
	}
	

	public BookInfo getForEAN13(String ean13) {
		return new BookInfo("The Hook Mountain Massacre", "Nicolas Louge", "9781601250384", new Date(), 4, "Gore-drenched but gripping");
	}
	

	public List<BookInfo> getForString(String string) {
		return Collections.emptyList();
	}
}
