package com.github.norwae.whatiread;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBNStorageProvider;

public class BookSearch extends StorageInteraction<String, String, List<BookInfo>>{

	public BookSearch(AsyncCallbackReceiver<List<BookInfo>, String> receiver,
			Activity origin) {
		super(receiver, origin);
	}

	@Override
	protected List<BookInfo> doInBackground(String... params) {
		for (ISBNStorageProvider provider : allProviders) {
			List<BookInfo> results = provider.searchForTerm(Arrays.asList(params), context);
			if (results != null) {
				return results;
			}
		}
		
		return null;
	}

}
