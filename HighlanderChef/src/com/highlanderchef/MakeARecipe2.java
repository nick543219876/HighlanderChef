package com.highlanderchef;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MakeARecipe2 extends ActionBarActivity {

	Recipe recipe = new Recipe();
	boolean ViewingDraft = false;
	User currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_a_recipe2);

		TextView tv_header = (TextView) findViewById(R.id.makearecipe2header);
		String header = tv_header.getText().toString();

		Intent intent = getIntent();
		recipe = RecipeCache.recipe;
		RecipeCache.recipe = null;

		tv_header.setText(header + " for " + recipe.getName());

		RemakeIngredList();
		recipe.loadImageFromPath();

		System.out.println("MAR2 onCreate bitmap is " + recipe.mainImage);
		System.out.println("MAR2 onCreate imagepath is " + recipe.mainImagepath);

		currentUser = Comm.getUser();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.make_arecipe2, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void addAnotherIngredientPressed(View view)
	{

		//gets text for newly added ingredient
		EditText edittext_new_ingred = (EditText) findViewById(R.id.addaningredient);
		String new_ingred = edittext_new_ingred.getText().toString();

		//gets text for newly added ingredient
		EditText edittext_new_ingred_amount = (EditText) findViewById(R.id.ingredientmeasurement);
		String new_ingred_amount = edittext_new_ingred_amount.getText().toString();

		//checks if inputed text length is greater than zero
		if(edittext_new_ingred.length() == 0 || new_ingred_amount.length() == 0)
		{ return; }

		recipe.addIngredient(new Ingredient(new_ingred, new_ingred_amount));

		RemakeIngredList();
		edittext_new_ingred.getText().clear();
		edittext_new_ingred_amount.getText().clear();
	}

	private void RemakeIngredList()
	{
		TextView textview_ingred_list = (TextView) findViewById(R.id.listofaddedingredients);
		String new_ingred_list = "";
		for(int i = 0; i < recipe.ingredientSize(); ++i)
		{
			new_ingred_list += recipe.getAnIngredient(i).getAmount()+ '\t' + recipe.getAnIngredient(i).getName() + '\n';
		}
		textview_ingred_list.setText(new_ingred_list);
	}

	public void SaveAsDraftPressed(View iView)
	{
		recipe.setUID(currentUser.getID());
		recipe.setUsername(currentUser.getUsername());
		Utility.UploadDraft(recipe);
		finish();
		Intent intent = new Intent(this, MainMenu.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void addDirectionsPressed(View view)
	{
		finish();
		Intent intent = new Intent(this, MakeARecipe3.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		RecipeCache.recipe = recipe;
		startActivity(intent);
	}

	public void LoadDraft(Recipe iDraft)
	{
		System.out.println("MAR2.LoadDraft()");
		System.out.println("  bitmap is " + iDraft.mainImage);
		System.out.println("  imagepath is " + iDraft.mainImagepath);
		ViewingDraft = true;
		recipe = iDraft;
		RemakeIngredList();
	}

	private class GetDraft extends AsyncTask<Integer, Void, Recipe> {

		@Override
		protected Recipe doInBackground(Integer... params) {
			Comm iComm = new Comm();
			return iComm.getDraft(params[0]);
		}

		@Override
		protected void onPostExecute(Recipe result) {
			if (result == null)
				return;
			LoadDraft(result);
		}
	}
}
