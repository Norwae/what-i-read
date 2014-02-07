package com.github.norwae.whatiread;

import android.app.Activity;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBNStorageProvider;

public class BookDelete  extends StorageInteraction<BookInfo, String, Void> {

	protected BookDelete(Activity context, AsyncCallbackReceiver<Void, String> receiver) {
		super(receiver, context);
	}

	@Override
	protected Void doInBackground(BookInfo... params) {
		for (ISBNStorageProvider provider : allProviders) {
			if (provider.deleteInfo(params[0], context)) {
				break;
			}
		}
		
		return null;
	}
}
