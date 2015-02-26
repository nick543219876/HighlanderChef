package com.highlanderchef;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserHomepage extends ActionBarActivity {

	User UserLoggedIn;
	User UserBeingViewed;
	int loggedInUserID;

	private static final int LENGTH_SHORT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_homepage);
		Intent intent = getIntent();
		UserBeingViewed = Utility.GetHomepageIntent(intent);
		if (UserBeingViewed.id == 0)
			this.finish();
		setUsername(UserBeingViewed.username);
		new UserRecipes().execute(UserBeingViewed.id);
		if(UserBeingViewed.id == Comm.getUser().id)
		{
			Button follow = (Button) findViewById(R.id.Follow);
			follow.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_homepage, menu);
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

	public void FollowUser(View iView)
	{
		int pos[] = {0 , 0};
		Button follow = (Button) findViewById(R.id.Follow);
		follow.getLocationOnScreen(pos);
		boolean following = false;
		ArrayList<Integer> followers = UserBeingViewed.followers;

		for(int i = 0; i < followers.size(); ++i) {
			if(UserBeingViewed.id == followers.get(i)) {
				following = true;
			}
		}
		if(following == true) {
			Toast followToast = Toast.makeText(getApplicationContext(), "You are already following this chef!", LENGTH_SHORT);
			followToast.setGravity(Gravity.TOP, 0, pos[1] + 20); //gravity, x-offset, y-offset
			followToast.show();
		}
		else {
			new followTask().execute(UserBeingViewed.id);
			Toast followToast = Toast.makeText(getApplicationContext(), "You are now following this chef", LENGTH_SHORT);
			followToast.setGravity(Gravity.TOP, 0, pos[1] + 20); //gravity, x-offset, y-offset
			followToast.show();
		}
	}

	public void setUsername(String iName)
	{
		((TextView) findViewById(R.id.Username)).setText(iName);
	}


	@SuppressWarnings("unused")
	private class UsernameTask extends AsyncTask<Integer, Void, Boolean>
	{
		String cUsername = new String();
		@Override
		protected Boolean doInBackground(Integer... params) {
			cUsername = Comm.getEmail();
			return (cUsername.length() > 0);
		}


		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				setUsername(cUsername);
			}
			else {
				Log.e("get_username_fail","Could not get username from server.");
				setUsername(new String());
			}
		}

	}

	public void PopulateRecipeList(ArrayList<Recipe> RecipeList)
	{
		RelativeLayout ILayout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
		ID_Maker MakerInstance = ID_Maker.getInstance();
		View lastView = null;
		for(int i = 0; i < RecipeList.size(); ++i)
		{
			if(RecipeList.get(i) == null )
				continue;
			//code for dividers

			ImageView iv_divider = new ImageView(this);
			if(i >= 1)
			{
				iv_divider.setId(MakerInstance.useCurrID());

				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(getResources(), R.drawable.divider, bmOptions);

				//Get the dimensions of the bitmap
				int photoW = bmOptions.outWidth;
				int photoH = bmOptions.outHeight;

				int targetW = 200; //TODO find better way to do this.
				int targetH = 3;
				//scale image
				int scalefactor = Math.min(photoW/targetW, photoH/targetH);

				// Decode the image file into a Bitmap sized to fill the View
				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scalefactor;
				bmOptions.inPurgeable = true;

				Bitmap div = BitmapFactory.decodeResource(getResources(), R.drawable.divider, bmOptions);

				iv_divider.setImageBitmap(div);
				RelativeLayout.LayoutParams ivdParams =
						new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				ivdParams.addRule(RelativeLayout.BELOW, lastView.getId());
				ivdParams.setMargins(0, 5, 0, 5);
				ILayout.addView(iv_divider, ivdParams);
			}

			TextView tv = new TextView(this);
			tv.setId(MakerInstance.useCurrID());
			tv.setText(RecipeList.get(i).getName());
			RelativeLayout.LayoutParams tParams =
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if (ILayout.getChildCount() > 0)
				tParams.addRule(RelativeLayout.BELOW, iv_divider.getId());
			tParams.setMargins(0, 5, 0, 5);
			tv.setLayoutParams(tParams);
			ILayout.addView(tv);

			ImageView iv = new ImageView(this);
			iv.setId(MakerInstance.useCurrID());
			if(RecipeList.get(i).isMainImage())
			{
				iv.setImageBitmap(Bitmap.createScaledBitmap(RecipeList.get(i).getMainImage(), 100, 100, false));
			}
			RelativeLayout.LayoutParams ivParams =
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			ivParams.addRule(RelativeLayout.BELOW, tv.getId());
			ivParams.addRule(RelativeLayout.ALIGN_LEFT, tv.getId());
			ivParams.setMargins(0, 5, 10, 5);
			iv.setLayoutParams(ivParams);
			ILayout.addView(iv);
			lastView = iv;

			TextView tv_descr = new TextView(this);
			tv_descr.setId(MakerInstance.useCurrID());
			String description = "Description: " + RecipeList.get(i).getDescription();
			if (description.length() >= 100)
				tv_descr.setText(description.substring(0,100) + "...");
			else
				tv_descr.setText(description);
			RelativeLayout.LayoutParams dParams =
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			dParams.addRule(RelativeLayout.RIGHT_OF, iv.getId());
			dParams.addRule(RelativeLayout.ALIGN_TOP, iv.getId());
			dParams.setMargins(0, 5, 0, 5);
			ILayout.addView(tv_descr, dParams);

			final int j = RecipeList.get(i).id; //so java doesn't complain

			Button b_view = new Button(this);
			b_view.setId(MakerInstance.useCurrID());
			b_view.setText("View");
			b_view.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v)
				{

					callRecipeIntent(j);
				}
			});
			RelativeLayout.LayoutParams bParams =
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT );
			if(RecipeList.get(i).isMainImage())
			{
				bParams.addRule(RelativeLayout.BELOW, iv.getId());
				bParams.addRule(RelativeLayout.ALIGN_LEFT, iv.getId());
			}
			else
			{
				bParams.addRule(RelativeLayout.BELOW, tv_descr.getId());
				bParams.addRule(RelativeLayout.ALIGN_LEFT, tv_descr.getId());
			}
			bParams.setMargins(0, 5, 0, 5);
			ILayout.addView(b_view, bParams);
			lastView = b_view;
		}
	}

	public void callRecipeIntent(int index)
	{
		Intent intent = new Intent(this, RecipeForum.class);
		intent.putExtra("recipeID", index);
		startActivity(intent);
	}

	@SuppressWarnings("unused")
	private class UserRecipes extends AsyncTask<Integer, Void, Boolean>
	{
		ArrayList<Recipe> UserRecipeList;
		@Override
		protected Boolean doInBackground(Integer... params)
		{
			Comm IComm = new Comm();
			//Get Recipes of the User
			System.out.println("User ID: " + params[0]);
			UserRecipeList = IComm.searchRecipesByUID(params[0]);
			return (UserRecipeList.size() >  0);
		}


		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result == false)
			{
				Log.e("LOAD_RECIPE", "Failed to load user's recipes");
			}
			PopulateRecipeList(UserRecipeList);
		}

	}

	@SuppressWarnings("unused")
	private class followTask extends AsyncTask<Integer, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Integer... params)
		{
			Comm IComm = new Comm();
			IComm.follow(params[0]);
			return (true);
		}


		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result != true)
			{
				Log.e("FOLLOW_USER", "Failed to follow target user");
			}
			else
			{
				Log.v("FOLLOW_USER", "Sucessfully followed target user");
			}
		}

	}

	private class getNotifications extends AsyncTask<Integer, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Integer... params)
		{
			Comm IComm = new Comm();
			IComm.follow(params[0]);
			return (true);
		}


		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result != true)
			{
				Log.e("FOLLOW_USER", "Failed to follow target user");
			}
			else
			{
				Log.v("FOLLOW_USER", "Sucessfully followed target user");
			}
		}

	}
}
