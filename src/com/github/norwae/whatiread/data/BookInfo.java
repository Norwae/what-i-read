package com.github.norwae.whatiread.data;

import java.io.Serializable;
import java.util.Date;

public class BookInfo implements Serializable {

	private static final long serialVersionUID = 2317828706327659235L;


	private final boolean addition;
	private final Date firstView;
	private final String ean13;	
	
	private String title;
	private String author;
	private int rating;
	private String comment;

	public BookInfo(String title, String author, String ean13, Date date,
			int rating, String comment, boolean addition) {
		this.title = title;
		this.author = author;
		this.ean13 = ean13;
		this.firstView = date;
		this.addition = addition;
		this.rating = rating;
		this.comment = comment;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAddition() {
		return addition;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String getEan13() {
		return ean13;
	}
	
	public Date getFirstView() {
		return firstView;
	}
	
	public int getRating() {
		return rating;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
}
