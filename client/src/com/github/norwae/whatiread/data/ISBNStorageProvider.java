package com.github.norwae.whatiread.data;

import java.util.List;

import android.content.Context;

public interface ISBNStorageProvider {
	List<BookInfo> searchForTerm(List<String> terms, Context sourceActivity);
	BookInfo getBookinfo(ISBN13 isbn, Context sourceActivity);
	boolean storeInfo(BookInfo info, Context sourceActivity);
	boolean deleteInfo(BookInfo info, Context sourceActivity);
	
	String getProgressMessage(Context sourceContext);
}
