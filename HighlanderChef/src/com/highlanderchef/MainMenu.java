package com.highlanderchef;

//import android.R;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainMenu extends ActionBarActivity {

	private static final int LENGTH_LONG = 3500;
	private User currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new UsernameTask().execute();
		setContentView(R.layout.activity_main_menu);

		Intent intent = getIntent();
		String con_msg = (String)intent.getSerializableExtra("Recipe Confirmation");
		if(con_msg == null)
		{
			;
		}
		else
		{
			int pos[] = {0 , 0};
			pos[0] = this.getResources().getDisplayMetrics().widthPixels;
			pos[1] = this.getResources().getDisplayMetrics().heightPixels / 4;
			Toast followToast = Toast.makeText(getApplicationContext(), "Recipe added successfully", LENGTH_LONG);
			followToast.setGravity(Gravity.TOP, 0, pos[1]); //gravity, x-offset, y-offset
			followToast.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	public void logout(View view) {
		new LogoutTask().execute();
		Intent intent = new Intent(this, StartupScreen.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void BrowsePressed(View view)
	{
		Intent intent = new Intent(this, BrowseActivity.class);
		startActivity(intent);
	}

	public void ComposePressed(View view)
	{
		Intent intent = new Intent(this, MakeARecipe1.class);
		startActivity(intent);
	}

	public void ViewMyRecipesPressed(View view)
	{
		System.out.println("MM.ViewMyRecipes()");
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}

	public void SearchPressed(View view)
	{
		System.out.println("MM.SearchedPressed()");
		String search_query = ((EditText) findViewById(R.id.editText1)).getText().toString();
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra("search_query", search_query);
		startActivity(intent);
	}

	public void ViewHomepage(View view)
	{
		Intent intent = new Intent(this, UserHomepage.class);
		Utility.FillHomepageIntent(intent, currentUser.getUsername(), currentUser.getID());
		startActivity(intent);
	}

	public void ViewDrafts(View view)
	{
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra("ViewDrafts", "View Drafts");
		startActivity(intent);
	}

	public void ViewFavorites(View view)
	{
		System.out.println("MM.ViewFavorites()");
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra("ViewFavorites", "ViewFavorites");
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	public void setUser(User iUser)
	{
		currentUser = iUser;
		String username = currentUser.getUsername();
		String strWelcomeFormat = getResources().getString(R.string.Welcome_Chef);
		String strWelcomeMsg = String.format(strWelcomeFormat,username);
		if (currentUser.notifications.isEmpty() == false)
		{
			((TextView) findViewById(R.id.ForumBoardTitle)).setTextColor(getResources().getColor(Utility.white));
			((TextView) findViewById(R.id.ForumBoardTitle)).setBackgroundDrawable((getResources().getDrawable(R.drawable.buttonshape)));
			((TextView) findViewById(R.id.ForumBoardTitle)).setClickable(true);
			((TextView) findViewById(R.id.ForumBoardTitle)).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View iView)
						{
							Intent intent = new Intent(MainMenu.this, SearchActivity.class);
							Utility.FillNotificationIntent(intent, currentUser.notifications);
							startActivity(intent);
						}
					});
			strWelcomeMsg = strWelcomeMsg + "\n" + "You have " + currentUser.notifications.size() + " new notifications!";
		}
		else
		{
			Utility.displayToast(this, "You have no new notifications.");
		}
		((TextView) findViewById(R.id.ForumBoardTitle)).setText(strWelcomeMsg);
	}

	private class LogoutTask extends AsyncTask<String, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(String... params) {

			Comm c = new Comm();
			return (c.logout() == Comm.SUCCESS);
		}


		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				System.out.println("Logout success");
			}
			else {
				System.out.println("Logout failure");
			}
		}

	}
	private class UsernameTask extends AsyncTask<String, Void, Boolean>
	{
		User cUser = new User();
		@Override
		protected Boolean doInBackground(String... params) {
			cUser = Comm.getUser();
			if (cUser == null)
				return false;
			return (cUser.getUsername().length() > 0);
		}


		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				setUser(cUser);
			}
			else {
				Log.e("getUser Failed","Could not get user from server.");
				setUser(new User());
			}
		}

	}
}
