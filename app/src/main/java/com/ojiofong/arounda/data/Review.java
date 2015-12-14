package com.ojiofong.arounda.data;

import java.io.Serializable;

public class Review implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String author;
	private String comment;
	private String rating;

	public Review(String author, String comment, String rating) {
		super();
		this.author = author;
		this.comment = comment;
		this.rating = rating;
	}

	public String getAuthor() {
		return author;
	}

	public String getComment() {
		return comment;
	}
	
	public String getRating() {
		return rating;
	}

}

