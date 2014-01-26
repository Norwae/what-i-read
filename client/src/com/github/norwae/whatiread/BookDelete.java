package com.github.norwae.whatiread;

import android.content.Context;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBNStorageProvider;

public class BookDelete  extends StorageInteraction<BookInfo, String, Void> {

	protected BookDelete(Context context, AsyncCallbackReceiver<Void, String> receiver) {
		super(receiver, context);
	}

	@Override
	protected Void doInBackground(BookInfo... params) {
		for (ISBNStorageProvider provider : allProviders) {
			publishProgress(provider.getProgressMessage(context));

			if (provider.deleteInfo(params[0], context)) {
				break;
			}
		}
		
		return null;
	}
}
