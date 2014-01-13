package com.github.norwae.whatiread.googlebooks;

import android.content.Context;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNLookupProvider;

public class GoogleBooksLookupProvider implements ISBNLookupProvider {
	
	@Override
	public BookInfo getForISBN13(ISBN13 isbn, Context sourceActivity) {
		GoogleBooksISBNRequest googleBooksRequest = new GoogleBooksISBNRequest(isbn, Key.KEY, sourceActivity.getResources().getConfiguration().locale);
		
		return googleBooksRequest.execute();
	}

	@Override
	public String getProgressMessage(Context sourceContext) {
		return sourceContext.getString(R.string.query_googleBooks);
	}

}
