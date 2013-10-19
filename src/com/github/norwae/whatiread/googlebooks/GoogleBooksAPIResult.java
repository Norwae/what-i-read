package com.github.norwae.whatiread.googlebooks;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.norwae.whatiread.data.BookInfo;

public class GoogleBooksAPIResult {

	public List<BookInfo> getBookInfos() {
		return Collections.singletonList(
				new BookInfo("The Hook Mountain Massacre", "Nicolas Louge", "9781601250384", new Date(), 4, "Gore-drenched but gripping")
		);
	}

}
