package com.github.norwae.whatiread.data;

import java.util.List;

import android.app.Activity;

public interface ISBNStorageProvider {
	List<BookInfo> searchForTerm(List<String> terms, Activity sourceActivity);
	BookInfo getBookinfo(ISBN13 isbn, Activity sourceActivity);
	boolean storeInfo(BookInfo info, Activity sourceActivity);
	boolean deleteInfo(BookInfo info, Activity sourceActivity);
	
	String getProgressMessage(Activity sourceContext);
}
