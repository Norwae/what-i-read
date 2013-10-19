package com.github.norwae.whatiread;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.R.string;
import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;
import com.github.norwae.whatiread.data.ISBNLookupProvider;
import com.github.norwae.whatiread.db.BookDatabase;
import com.github.norwae.whatiread.db.DBBookLookupProvider;

public class BookISBNLookup extends CallbackAsync<ISBN13, String, BookInfo>{
	
	private List<ISBNLookupProvider> allProviders = Arrays.<ISBNLookupProvider>asList(new DBBookLookupProvider());
	private Context context;

	public BookISBNLookup(AsyncCallbackReceiver<BookInfo, String> asyncCallbackReceiver, Context origin) {
		super(asyncCallbackReceiver);
		
		context = origin;
	}

	@Override
	protected BookInfo doInBackground(ISBN13... params) {
		ISBN13 query = (ISBN13) params[1];
		BookInfo info = null;
		for (ISBNLookupProvider temp : allProviders) {
			String msg = temp.getProgressMessage(context);
			publishProgress(msg);
			
			info = temp.getForISBN13(query, context);
			
			if (info != null) {
				return info;
			}
			
		}
		publishProgress(context.getString(R.string.query_googleBooks));
		info = new BookInfo("The Hook Mountain Massacre", "Nicolas Louge", "9781601250384", new Date(), 4, "Gore-drenched but gripping");
			
		return info;
	}


}
