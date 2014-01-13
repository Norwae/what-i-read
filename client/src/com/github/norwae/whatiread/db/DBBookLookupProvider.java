package com.github.norwae.whatiread.db;

import android.content.Context;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNLookupProvider;

public class DBBookLookupProvider implements ISBNLookupProvider {

	@Override
	public BookInfo getForISBN13(ISBN13 isbn, Context sourceActivity) {
		BookDatabase db = new BookDatabase(sourceActivity);
		try {
			return db.getForISBN13(isbn);
		} finally {
			db.close();
		}
	}

	@Override
	public String getProgressMessage(Context resources) {
		return resources.getString(R.string.query_localDB);
	}

}
