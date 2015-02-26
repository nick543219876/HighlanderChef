package com.highlanderchef;

import java.io.Serializable;

public class Comment implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1193647902768555262L;
	public int recipeID;
	public int rating;
	public String comment;
	public String username;

	Comment(int recipeID, int rating, String comment) {
		this.recipeID = recipeID;
		this.rating = rating;
		this.comment = comment;
	}

	Comment(int recipeID, int rating, String comment, String username) {
		this.recipeID = recipeID;
		this.rating = rating;
		this.comment = comment;
		this.username = username;
	}

	Comment(int recipeID, float rating, String comment, String username) {
		this.recipeID = recipeID;
		this.rating = (int)rating;
		this.comment = comment;
		this.username = username;
	}

	Comment(int recipeID, float rating, String comment) {
		this.recipeID = recipeID;
		this.rating = (int)rating;
		this.comment = comment;
	}
}
