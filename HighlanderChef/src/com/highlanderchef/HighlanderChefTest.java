package com.highlanderchef;
import java.util.ArrayList;

import junit.framework.TestCase;
import android.graphics.Bitmap;

public class HighlanderChefTest extends TestCase {
	protected Recipe r;
	protected Bitmap b;

	@Override
	public void setUp() {
		r = new Recipe(89,"name","this is a description",b);
	}

	public void testPrint() {

		System.out.println(r.toString());
		assertTrue(true);
	}

	public void testCommMain() {
		//runningAndroid = false;
		Comm c = new Comm();
		c.login("test@test.net", "test1234");
		c.newAccount("bob@test.net", "bobhasbadpasswords");
		System.out.println("c.login returns " + c.login("bob@test.net", "bobhasbadpasswords"));
		System.out.println("c.login returns " + c.login("bob@test.net", "bobhasGOODpasswords"));

		c.searchRecipes("soup");
		Recipe simple = c.getRecipe(1);
		c.uploadRecipe(simple);
		ArrayList<Ingredient> list = simple.ingredients;
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i + " " + list.get(i).amount + " " + list.get(i).name);
		}
		ArrayList<Direction> dirs = simple.directions;
		for (int i = 0; i < dirs.size(); i++) {
			System.out.println(i + " " + dirs.get(i).text);
		}
		System.out.println("now display the categories");
		ArrayList<Integer> cats = simple.categories;
		for(int i = 0; i < cats.size(); i++) {
			System.out.println(i + " " + cats.get(i));
		}
		if(simple.categories.size() == 0)
			System.out.println("there are no categories");

	}
}

