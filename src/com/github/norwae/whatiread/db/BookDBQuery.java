package com.github.norwae.whatiread.db;

import java.util.List;

import android.content.Context;

import com.github.norwae.whatiread.AsyncCallbackReceiver;
import com.github.norwae.whatiread.CallbackAsync;
import com.github.norwae.whatiread.MainActivity;
import com.github.norwae.whatiread.R;
import com.github.norwae.whatiread.data.BookInfo;

public class BookDBQuery extends CallbackAsync<String, String, List<BookInfo>> {

	private Context context;

	public BookDBQuery(AsyncCallbackReceiver<List<BookInfo>, String> receiver, Context origin) {
		super(receiver);
		context = origin;
	}

	@Override
	protected List<BookInfo> doInBackground(String... params) {
		String query = (String) params[0];

		publishProgress(context.getString(R.string.query_localDB));
		BookDatabase db = new BookDatabase(context);
		
		try {
			return db.getForString(query);
		} finally {
			db.close();
		}
	}


}
