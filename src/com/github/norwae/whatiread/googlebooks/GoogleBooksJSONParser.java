package com.github.norwae.whatiread.googlebooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.norwae.whatiread.data.BookInfo;

import android.util.Log;

public class GoogleBooksJSONParser {

	private InputStream stream;
	private String ean;

	public GoogleBooksJSONParser(InputStream inputStream, String code) {
		stream = inputStream;
		ean = code;
	}

	public GoogleBooksAPIResult parse() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
		
		StringBuilder buffer = new StringBuilder();
		String next;
	
		while ((next = reader.readLine()) != null) {
			Log.d("parse-api-result", next);
			buffer.append(next).append("\r\n");
		}
		
		try {
			GoogleBooksAPIResult result = new GoogleBooksAPIResult();
			JSONObject json = new JSONObject(buffer.toString());
			JSONArray items = json.getJSONArray("items");
			
			if (items != null) {
				for (int i = 0; i < items.length(); i++) {
					JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
					
					String author = null;
					JSONArray authors = volumeInfo.getJSONArray("authors");
					
					if (authors != null && authors.length() > 0) {
						author = authors.getString(0);
					}
					
					BookInfo book = new BookInfo(volumeInfo.getString("title"), author, ean, new Date(), 0, "");
				}
			}
			return result;
		} catch (JSONException e) {
			Log.e("parse-api-result", "error parsing json '" + buffer + "'", e);
		}

		return null;
		
	}

}
