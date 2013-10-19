package com.github.norwae.whatiread.googlebooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.util.Log;

public class GoogleBooksJSONParser {

	private InputStream stream;

	public GoogleBooksJSONParser(InputStream inputStream) {
		stream = inputStream;
	}

	public GoogleBooksAPIResult parse() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
		
		String next;
	
		while ((next = reader.readLine()) != null) {
			Log.e("parse-api-result", next);
		}
		
		return new GoogleBooksAPIResult();
	}

}
