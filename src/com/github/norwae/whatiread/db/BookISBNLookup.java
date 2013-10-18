package com.github.norwae.whatiread.db;

import java.util.Date;

import com.github.norwae.whatiread.AsyncCallbackReceiver;
import com.github.norwae.whatiread.CallbackAsync;
import com.github.norwae.whatiread.R;

public class BookISBNLookup extends CallbackAsync<Object, String, BookInfo>{

	public BookISBNLookup(AsyncCallbackReceiver<BookInfo, String> asyncCallbackReceiver) {
		super(asyncCallbackReceiver);
	}

	@Override
	protected BookInfo doInBackground(Object... params) {
		BookDatabase tempDB = (BookDatabase) params[0];
		String tempQuery = (String) params[1];
			
		publishProgress(tempDB.getOriginContext().getString(R.string.query_localDB));
		BookInfo bookInfo = tempDB.getForEAN13(tempQuery);
		
		if (bookInfo == null) {
			publishProgress(tempDB.getOriginContext().getString(R.string.query_googleBooks));
			bookInfo = new BookInfo("The Hook Mountain Massacre", "Nicolas Louge", "9781601250384", new Date(), 4, "Gore-drenched but gripping");
		}
		
		return bookInfo;
	}


}
