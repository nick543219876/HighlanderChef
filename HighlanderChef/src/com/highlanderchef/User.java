package com.highlanderchef;

import java.util.ArrayList;

public class User {
	public volatile int id;
	public volatile String username = "";

	/*
	 * rids of my current recipes,
	 * draft ids of my drafts,
	 * rids of my favorite recipes,
	 * rids of any notifications,
	 * rids of favorite recipes,
	 * uids of followers and following
	 *
	 * If this is for another user, we only fill out recipes
	 */
	public ArrayList<Integer> recipes;
	public ArrayList<Integer> drafts;
	public ArrayList<Integer> favorites;
	public ArrayList<Integer> notifications;
	public ArrayList<Integer> followers;
	public ArrayList<Integer> following;

	public User() {
		id = 0;
		username = "";
		favorites = new ArrayList<Integer>();
		notifications = new ArrayList<Integer>();
		followers = new ArrayList<Integer>();
		following = new ArrayList<Integer>();
		recipes = new ArrayList<Integer>();
		drafts = new ArrayList<Integer>();
	}

	public User(int uid, String username, ArrayList<Integer> recipes) {
		this.id = uid;
		this.username = username;
		this.recipes = recipes;
		favorites = new ArrayList<Integer>();
		notifications = new ArrayList<Integer>();
		followers = new ArrayList<Integer>();
		following = new ArrayList<Integer>();
		drafts = new ArrayList<Integer>();
	}

	public User(int id, String username, ArrayList<Integer> favorites, ArrayList<Integer> notifications, ArrayList<Integer> followers, ArrayList<Integer> following, ArrayList<Integer> recipes, ArrayList<Integer> drafts)
	{
		this.id = id;
		this.username = username;
		this.favorites = favorites;
		this.notifications = notifications;
		this.followers = followers;
		this.following = following;
		this.recipes = recipes;
		this.drafts = drafts;
	}
	public boolean isInFavorites(int RecipeID)
	{
		return favorites.indexOf(RecipeID) >= 0;
	}
	public int getID() {
		return id;
	}
	public String getUsername() {
		return username;
	}
	public ArrayList<Integer> getFavorites() {
		return favorites;
	}
	public ArrayList<Integer> getFollowers() {
		return followers;
	}
	public ArrayList<Integer> getFollowing() {
		return following;
	}
	public ArrayList<Integer> getRecipes() { //already have a similar function searchRecipesByUID() in Comm.java
		return recipes;
	}
	public void setID(int id) {
		this.id = id;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setFavorites(ArrayList<Integer> favorites) {
		this.favorites = favorites;
	}
	public void setFollowers(ArrayList<Integer> followers) {
		this.followers = followers;
	}
	public void setFollowing(ArrayList<Integer> following) {
		this.following = following;
	}

	public void follow(Integer uid) {
		this.following.add(uid);
	}
	public void unfollow(Integer uid) {
		this.following.remove(uid);
	}

	public void addRecipe(int rid) {
		recipes.add(id);
	}

	public Boolean isFollowing(int id) {
		for(int i = 0; i < following.size(); i++) {
			if(following.get(i) == id)
				return true;
		}
		return false;
	}

}
