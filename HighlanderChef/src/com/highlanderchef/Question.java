package com.highlanderchef;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8686942949033916152L;
	int qid;
	int uid;
	String username;
	String text;
	ArrayList<Question> replies;

	public Question(int uid, String username, String text) {
		this.uid = uid;
		this.username = username;
		this.text = text;
		replies = new ArrayList<Question>();
	}

	public Question(int qid, int uid, String username, String text, ArrayList<Question> replies) {
		this.qid = qid;
		this.uid = uid;
		this.username = username;
		this.text = text;
		if (replies == null) {
			replies = new ArrayList<Question>();
		} else {
			this.replies = (ArrayList<Question>) replies.clone();
		}
		for (int i = 0; i < replies.size(); i++) {
			System.out.println("QUESTION with reply " + replies.get(i).text);
		}
	}

	public void editQuestion(String text) {
		this.text = text;
	}
}