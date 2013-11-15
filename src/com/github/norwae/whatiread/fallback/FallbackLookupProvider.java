package com.github.norwae.whatiread.fallback;

import java.util.Date;

import android.content.Context;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNLookupProvider;

public class FallbackLookupProvider implements ISBNLookupProvider {

	@Override
	public BookInfo getForISBN13(ISBN13 isbn, Context sourceActivity) {
		return new BookInfo(isbn.toString(), null, isbn.toString(), new Date(), 0, null, true);
	}

	@Override
	public String getProgressMessage(Context sourceContext) {
		return sourceContext.getString(R.string.query_fallback);
	}

}
