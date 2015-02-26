package com.highlanderchef;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MakeARecipe3 extends ActionBarActivity {

	Recipe recipe = new Recipe();
	String picturePath;
	String errorMessage = "";

	int dir_added_count = 0;
	int prevTextViewId;

	boolean newimage = false;
	Bitmap newimagebm;

	User currentUser;

	ArrayList<Bitmap> added_images = new ArrayList<Bitmap>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_a_recipe3);

		Intent intent = getIntent();
		recipe = RecipeCache.recipe;
		RecipeCache.recipe = null;
		System.out.println("MAR3 recipe categories: " + recipe.categories.toString());

		if (recipe.directions == null) {
			recipe.directions = new ArrayList<>();
		}
		for (int i = 0; i < recipe.directions.size(); i++) {
			Direction d = recipe.directions.get(i);
			addDirectionToLayout(d.text);
			for (int j = 0; j < d.images.size(); j++) {
				LinearLayout linear_layout = (LinearLayout) findViewById(R.id.LinearLayout1);
				ImageView imageView = new ImageView(this);
				final RelativeLayout.LayoutParams params =
						new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, prevTextViewId);
				imageView.setLayoutParams(params);

				imageView.setImageBitmap(d.images.get(j));
				linear_layout.addView(imageView, params);
				added_images.add(d.images.get(j));
			}
		}

		setMainImage();

		TextView tv_header = (TextView) findViewById(R.id.makearecipe3header);
		String header = tv_header.getText().toString();
		tv_header.setText(header + " for " + recipe.getName());

		TextView tv_error = (TextView) findViewById(R.id.submit_error);
		tv_error.setVisibility(View.INVISIBLE);
		prevTextViewId = R.id.added_image;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.make_arecipe3, menu);
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

	public void addDirectionToLayout(String new_dir)
	{
		//checks if inputed text length is greater than zero
		if(new_dir.length() == 0 )
		{ return; }

		//creates new text for ingredients list, includes newly added ingredient
		LinearLayout linear_layout = (LinearLayout) findViewById(R.id.LinearLayout1);

		++dir_added_count;
		TextView tv = new TextView(MakeARecipe3.this);
		tv.setText(dir_added_count + ") " + new_dir);
		tv.setId(dir_added_count);

		final RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, prevTextViewId);
		tv.setLayoutParams(params);

		prevTextViewId = dir_added_count;
		linear_layout.addView(tv, params);
	}

	public void addADirectionPressed(View view)
	{
		//gets text for newly added direction
		EditText edittext_new_dir = (EditText) findViewById(R.id.addadirection);
		String new_dir = edittext_new_dir.getText().toString();

		addDirectionToLayout(new_dir);




		recipe.AddADirection(new_dir, added_images);
		added_images.clear();
		edittext_new_dir.getText().clear();
		//ImageView imageview = (ImageView) findViewById(R.id.added_image);
		//imageview.setImageResource(R.drawable.uploadimage);
	}

	private static int RESULT_LOAD_IMAGE = 1;
	public void addImageToDirectionPressed(View view)
	{
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();

			LinearLayout linear_layout = (LinearLayout) findViewById(R.id.LinearLayout1);
			ImageView imageView = new ImageView(this);

			final RelativeLayout.LayoutParams params =
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, prevTextViewId);
			imageView.setLayoutParams(params);



			//load bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(picturePath, bmOptions);

			//Get the dimensions of the bitmap
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			//scale image
			int scalefactor = Math.min(photoW/100, photoH/100);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scalefactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(picturePath, bmOptions);

			if(bitmap != null)
			{
				imageView.setImageBitmap(bitmap);
				linear_layout.addView(imageView, params);
				added_images.add(bitmap);
			}
			else
			{
				//TODO some error message
			}

		}
	}
	public void SaveAsDraftPressed(View iView)
	{
		currentUser = Comm.getUser();
		recipe.setUID(currentUser.getID());
		recipe.setUsername(currentUser.getUsername());
		Utility.UploadDraft(recipe);
		finish();
		Intent intent = new Intent(this, MainMenu.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	public void submitRecipePressed(View view)
	{
		new UploadRecipeTask().execute(recipe);
		TextView tv_error = (TextView) findViewById(R.id.submit_error);
		tv_error.setVisibility(View.INVISIBLE);//resume_here
	}
	public void onSuccess()
	{
		finish();
		Intent intent = new Intent(this, MainMenu.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Recipe Confirmation", "Recipe added successfully");
		startActivity(intent);
	}
	public void onFailure()
	{
		TextView tv_error = (TextView) findViewById(R.id.submit_error);
		tv_error.setText(errorMessage);
		tv_error.setVisibility(View.VISIBLE);

		//TODO implement better case for failure.
	}

	private class UploadRecipeTask extends AsyncTask<Recipe, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Recipe... params) {
			Comm c = new Comm();
			int ret = c.uploadRecipe(params[0]);
			if(ret == Comm.NETWORK_FAIL) {
				errorMessage = "Error! Network Failed to connect. Check your network";
			} else if(ret == Comm.API_FAIL) {
				errorMessage = "Sorry! There was an error making your recipe";
			}
			return (ret == Comm.SUCCESS);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				onSuccess();
			} else {
				onFailure();
			}
		}
	}


	public void setMainImage()
	{
		recipe.loadImageFromPath();
	}
}
