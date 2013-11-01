package com.github.norwae.whatiread.googlebooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.norwae.whatiread.data.BookInfo;

public class GoogleBooksAPIResult {
	
	private List<BookInfo> infos = new ArrayList<BookInfo>();

	public List<BookInfo> getBookInfos() {
		
		return infos;
//		return Collections.singletonList(
//				new BookInfo("The Hook Mountain Massacre", "Nicolas Louge", "9781601250384", new Date(), 4, "Gore-drenched but gripping")
//		);
	}

	public void addBook(BookInfo book) {
		infos.add(book);
	}

}
