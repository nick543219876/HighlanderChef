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
//Added this library for SignUpTask extends AsyncTask(param1, param2, param3).

public class SignupActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntent();
		setContentView(R.layout.activity_signup);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signup, menu);
		TextView invalidSignUp = (TextView) findViewById(R.id.invalidAccount);
		invalidSignUp.setVisibility(View.INVISIBLE);
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
	//renamed function from submitPressed to the function below. See <Button> in activity_signup.xml
	public void validateSignUpSubmitPressed(View view)
	{
		EditText editTextUsername = (EditText) findViewById(R.id.enterusername);
		String strUsername = editTextUsername.getText().toString();

		EditText editTextPassword = (EditText) findViewById(R.id.enterpassword);
		String strPassword = editTextPassword.getText().toString();

		// TODO: only process this task .
		new SignUpTask().execute(strUsername, strPassword);
	}
	//Added this to be called in function onPostExecute(...)
	public void signUpSuccess()
	{
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	//Added this to be called in function onPostExecute(...)
	public void signUpFail(String msg)
	{
		TextView invalidSignUp = (TextView) findViewById(R.id.invalidAccount);
		//invalidSignUp.setText(msg); //Added this to make use of the msg (the error message).
		invalidSignUp.setVisibility(View.VISIBLE);
	}
	//Added this entire private class to be called in validateSignUpSubmitPressed(View view)
	private class SignUpTask extends AsyncTask<String, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(String... params)
		{
			Comm c = new Comm();
			int ret = c.newAccount(params[0], params[1]);
			return (ret == Comm.SUCCESS);
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if(result == true)
			{
				//Log.v("signUp_Submit", "Sign-Up Success");
				signUpSuccess();
			}
			else
			{
				//Log.v("signUp_fail", "Sign-Up failed");
				signUpFail("Something bad happened");
			}
		}
	}
}