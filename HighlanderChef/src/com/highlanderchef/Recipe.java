package com.highlanderchef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Recipe implements Serializable{
	private static final long serialVersionUID = -6967995128725451333L;
	public int id;
	public int uid;
	public int did;
	public String username;
	public String name;
	public String description;
	public String cookTime;
	public Bitmap mainImage;
	public String mainImagepath;

	public float rating;

	public ArrayList<Integer> categories;
	public ArrayList<Ingredient> ingredients;
	public ArrayList<Direction> directions;
	public ArrayList<Comment> comments;
	public ArrayList<Question> questions;

	//add categories, description, img_url, name, rid

	public Recipe(int id, String name, String description, Bitmap mainImage) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.mainImage = mainImage;
		this.did = 0;

		categories = new ArrayList<Integer>();
		ingredients = new ArrayList<Ingredient>();
		directions = new ArrayList<Direction>();
		comments = new ArrayList<Comment>();
		questions = new ArrayList<Question>();
	}
	public Recipe(int id, String name, String description, Bitmap mainImage, int did) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.mainImage = mainImage;
		this.did = did;

		categories = new ArrayList<Integer>();
		ingredients = new ArrayList<Ingredient>();
		directions = new ArrayList<Direction>();
		comments = new ArrayList<Comment>();
		questions = new ArrayList<Question>();
	}

	public Recipe()
	{
		categories = new ArrayList<Integer>();
		ingredients = new ArrayList<Ingredient>();
		directions = new ArrayList<Direction>();
		comments = new ArrayList<Comment>();
		questions = new ArrayList<Question>();
		this.mainImage = null;
	}

	public void parseIngredientsFromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonNode node = mapper.readTree(json);
			Iterator<JsonNode> ite = node.getElements();
			while (ite.hasNext()) {
				JsonNode ing = ite.next();
				String name = mapper.readValue(ing.path("name"), String.class);
				String amount = mapper.readValue(ing.path("amount"), String.class);
				Ingredient ingr = new Ingredient(name, amount);
				ingredients.add(ingr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addDirection(String text, Bitmap bmp) {
		directions.add(new Direction(text, bmp));
	}

	public void addDirection(String text, ArrayList<Bitmap> bmps) {
		directions.add(new Direction(text, bmps));
	}

	public void parseCategoriesFromJson(String json){
		ObjectMapper mapper = new ObjectMapper();

		try	{
			JsonNode node = mapper.readTree(json);
			Iterator<JsonNode> ite = node.getElements();
			while(ite.hasNext()) {
				JsonNode cat = ite.next();
				Integer ingr = new Integer(cat.getIntValue());
				categories.add(ingr);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void parseCommentsFromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonNode node = mapper.readTree(json);
			Iterator<JsonNode> ite = node.getElements();
			while(ite.hasNext()) {
				JsonNode cat = ite.next();
				String cmt = cat.get("comment").asText();
				String usrnm = cat.get("username").asText();
				Integer rid = cat.get("recipeID").asInt();
				Integer rat = cat.get("rating").asInt();
				Comment c = new Comment(rid, rat, cmt, usrnm);
				comments.add(c);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void putIntoIntent(Intent intent)
	{
		intent.putExtra("ID", this.id);
		intent.putExtra("UID", this.uid);
		intent.putExtra("Username", this.username);
		intent.putExtra("Name", this.name);
		intent.putExtra("Description", this.description);
		intent.putExtra("Cook Time", this.cookTime);
		intent.putExtra("Main Image", this.mainImage);
		intent.putExtra("Main Image Path", this.mainImagepath);
		intent.putExtra("Rating", this.rating);
		intent.putExtra("Categories", this.categories.toArray());
		for (int i = 0; i < ingredients.size(); i++)
			ingredients.get(i).putIntoIntent(intent, "ingredient" + Integer.toString(i));
		for (int i = 0; i < directions.size(); i++)
			;
		for (int i = 0; i < comments.size(); i++)
			;
		for (int i = 0; i < questions.size(); i++)
			;
		/*
		 * public ArrayList<Integer> categories;
	public ArrayList<Ingredient> ingredients;
	public ArrayList<Direction> directions;
	public ArrayList<Comment> comments;
	public ArrayList<Question> questions;
		 */
	}
	public void setID(int i)
	{
		this.id = i;
	}
	public void setIngredients(ArrayList<Ingredient> i)
	{
		this.ingredients = i;
	}
	public ArrayList<Ingredient> getIngredients()
	{
		return this.ingredients;
	}
	public void addIngredient(Ingredient i)
	{
		ingredients.add(i);
	}
	public void setAnIngredient(int index, Ingredient i)
	{
		this.ingredients.set(index, i);
	}
	public Ingredient getAnIngredient(int index)
	{
		return this.ingredients.get(index);
	}
	public Direction getADirection(int index)
	{
		return this.directions.get(index);
	}
	public void AddADirection(String dir, ArrayList<Bitmap> bmp)
	{
		this.directions.add(new Direction(dir, bmp ));
		bmp.clear();
	}
	public void AddADirection(String dir)
	{
		this.directions.add(new Direction(dir));
	}
	public int ingredientSize()
	{
		return this.ingredients.size();
	}
	public int directionSize()
	{
		return this.directions.size();
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName()
	{
		return this.name;
	}
	public void setUsername(String name)
	{
		this.username = name;
	}
	public String getUsername()
	{
		return this.username;
	}
	public void setUID(int id)
	{
		this.uid = id;
	}
	public int getUID()
	{
		return this.uid;
	}
	public void setDescription(String d)
	{
		this.description = d;
	}
	public String getDescription()
	{
		return this.description;
	}
	public void setCookTime(String ct)
	{
		this.cookTime = ct;
	}
	public String getCookTime()
	{
		return this.cookTime;
	}
	public void setMainImage(Bitmap bm)
	{
		this.mainImage = bm;
	}
	public Bitmap getMainImage()
	{
		return this.mainImage;
	}
	public int getDid()
	{
		return this.did;
	}

	public boolean isMainImage()
	{
		return (this.mainImage != null);
	}

	public void loadImageFromPath()
	{
		String picturePath = this.mainImagepath;

		//load bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		bmOptions.outHeight = 768;
		bmOptions.outWidth = 1024;
		BitmapFactory.decodeFile(picturePath, bmOptions);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(picturePath, bmOptions);

		if(bitmap != null)
		{
			mainImage = bitmap;
		}
		else
		{
			System.out.println("Recipe.loadImageFromPath() failed");
		}

	}
}
