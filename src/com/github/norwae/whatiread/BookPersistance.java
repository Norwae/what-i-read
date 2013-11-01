package com.github.norwae.whatiread;

import android.content.Context;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.db.BookDatabase;

public class BookPersistance extends CallbackAsync<BookInfo, String, Void> {
	
	private Context context;

	public BookPersistance(Context ctx, AsyncCallbackReceiver<Void, String> callback) {
		super(callback);
		this.context = ctx; 
	}

	@Override
	protected Void doInBackground(BookInfo... params) {
		publishProgress(context.getString(R.string.progress_openDB));

		BookDatabase db = new BookDatabase(context);
		try {		
			publishProgress(context.getString(R.string.progress_saveBook));
			db.saveOrUpdate(params[0]);
		} finally {
			db.close();
		}
		
		
		return null;
	}


}
