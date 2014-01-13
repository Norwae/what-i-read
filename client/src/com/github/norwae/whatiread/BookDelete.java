package com.github.norwae.whatiread;

import android.content.Context;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.db.BookDatabase;

public class BookDelete  extends CallbackAsync<BookInfo, String, Void> {

	private Context context;

	protected BookDelete(Context context, AsyncCallbackReceiver<Void, String> receiver) {
		super(receiver);
		this.context = context;
	}

	@Override
	protected Void doInBackground(BookInfo... params) {
		publishProgress(context.getString(R.string.progress_openDB));

		BookDatabase db = new BookDatabase(context);
		try {		
			publishProgress(context.getString(R.string.progress_deleteBook));
			db.delete(params[0]);
		} finally {
			db.close();
		}
		return null;
	}

}
