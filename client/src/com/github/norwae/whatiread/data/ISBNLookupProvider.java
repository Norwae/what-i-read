package com.github.norwae.whatiread.data;

import android.content.Context;

public interface ISBNLookupProvider {
	BookInfo getForISBN13(ISBN13 isbn, Context sourceActivity);
	
	String getProgressMessage(Context sourceContext);
}
