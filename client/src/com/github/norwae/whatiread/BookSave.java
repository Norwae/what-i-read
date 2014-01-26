package com.github.norwae.whatiread;

import android.content.Context;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBNStorageProvider;

public class BookSave extends StorageInteraction<BookInfo, String, Void> {
	
	public BookSave(Context ctx, AsyncCallbackReceiver<Void, String> callback) {
		super(callback, ctx);
	}

	@Override
	protected Void doInBackground(BookInfo... params) {
		for (ISBNStorageProvider provider : allProviders) {
			publishProgress(provider.getProgressMessage(context));

			if (provider.storeInfo(params[0], context)) {
				break;
			}
		}
		
		return null;
	}


}
