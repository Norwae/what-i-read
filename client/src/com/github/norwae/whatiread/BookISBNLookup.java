package com.github.norwae.whatiread;


import android.app.Activity;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNStorageProvider;

public class BookISBNLookup extends StorageInteraction<ISBN13, String, BookInfo>{
	
	public BookISBNLookup(AsyncCallbackReceiver<BookInfo, String> asyncCallbackReceiver, Activity origin) {
		super(asyncCallbackReceiver, origin);
	}

	@Override
	protected BookInfo doInBackground(ISBN13... params) {
		ISBN13 query = (ISBN13) params[0];
		BookInfo info = null;
		for (ISBNStorageProvider temp : allProviders) {
			String msg = temp.getProgressMessage(context);
			publishProgress(msg);
			
			info = temp.getBookinfo(query, context);
			
			if (info != null) {
				break;
			}
			
		}	
		return info;
	}


}
