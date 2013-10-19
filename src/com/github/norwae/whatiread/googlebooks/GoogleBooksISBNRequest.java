package com.github.norwae.whatiread.googlebooks;

import java.util.Locale;

import com.github.norwae.whatiread.data.BookInfo;
import com.github.norwae.whatiread.data.ISBN13;

public class GoogleBooksISBNRequest {

	private final String key;
	private final String code;
	private final Locale locale;

	public GoogleBooksISBNRequest(ISBN13 isbn, String key, Locale locale) {
		this.key = key;
		this.code = isbn.toString();
		this.locale = locale;
		
	}

	public BookInfo execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
