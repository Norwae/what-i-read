package com.github.norwae.whatiread;

import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNLookupProvider;
import com.github.norwae.whatiread.db.DBBookLookupProvider;
import com.github.norwae.whatiread.googlebooks.GoogleBooksLookupProvider;

public class BookISBNLookup extends CallbackAsync<ISBN13, String, BookInfo>{
	
	private List<ISBNLookupProvider> allProviders = Arrays.asList(
			new DBBookLookupProvider(),
			new GoogleBooksLookupProvider()
	);
	private Context context;

	public BookISBNLookup(AsyncCallbackReceiver<BookInfo, String> asyncCallbackReceiver, Context origin) {
		super(asyncCallbackReceiver);
		
		context = origin;
	}

	@Override
	protected BookInfo doInBackground(ISBN13... params) {
		ISBN13 query = (ISBN13) params[0];
		BookInfo info = null;
		for (ISBNLookupProvider temp : allProviders) {
			String msg = temp.getProgressMessage(context);
			publishProgress(msg);
			
			info = temp.getForISBN13(query, context);
			
			if (info != null) {
				return info;
			}
			
		}	
		return info;
	}


}
