package com.github.norwae.whatiread.db;

import java.util.List;

import com.github.norwae.whatiread.AsyncCallbackReceiver;
import com.github.norwae.whatiread.CallbackAsync;
import com.github.norwae.whatiread.R;

public class BookDBQuery extends CallbackAsync<Object, String, List<BookInfo>> {

	protected BookDBQuery(AsyncCallbackReceiver<List<BookInfo>, String> receiver) {
		super(receiver);
	}

	@Override
	protected List<BookInfo> doInBackground(Object... params) {
		BookDatabase tempDB = (BookDatabase) params[0];
		String tempQuery = (String) params[1];
		
		
		publishProgress(tempDB.getOriginContext().getString(R.string.query_localDB));
		return tempDB.getForString(tempQuery);
	}


}
