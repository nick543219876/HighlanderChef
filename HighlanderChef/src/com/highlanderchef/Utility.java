package com.highlanderchef;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Utility
{
	private static final int TOAST_MESSAGE_LENGTH = 3500;
	private static final String NotificationIntentKey = "ViewNotifications";
	public static int white = 0x0106000b;

	static public ArrayList<Integer> GetNotificationIntent(Intent intent)
	{
		ArrayList<Integer> notifications = intent.getIntegerArrayListExtra(NotificationIntentKey);
		if (notifications == null)
			notifications = new ArrayList<Integer>();
		return notifications;
	}
	static public void FillNotificationIntent(Intent intent, ArrayList<Integer> notifications)
	{
		intent.putExtra(NotificationIntentKey, 	notifications);
	}
	static public User GetHomepageIntent(Intent intent)
	{
		User returnUser = new User();
		returnUser.id = intent.getIntExtra("userID", Comm.staticGetUserID());
		returnUser.username = intent.getStringExtra("Username");
		return returnUser;
	}
	static public void FillHomepageIntent(Intent intent, User UserToView)
	{
		FillHomepageIntent(intent, UserToView.getUsername(), UserToView.getID());
	}
	static public void FillHomepageIntent(Intent intent, String username, int userID)
	{
		intent.putExtra("userID", userID);
		intent.putExtra("Username", username);
	}
	static public ProgressBar DisplaySpinner(Context iContext, ViewGroup iLayout)
	{
		ProgressBar ISpinner = new ProgressBar(iContext);
		ViewGroup.LayoutParams Params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		iLayout.addView(ISpinner, Params);
		return ISpinner;
	}
	static public void RemoveSpinner(ProgressBar iSpinner)
	{
		if (iSpinner != null)
			((ViewGroup) iSpinner.getParent()).removeView(iSpinner);
	}
	static public void UploadDraft(Recipe draft)
	{
		new UploadDraftTask().execute(draft);
	}
	static public void displayToast(Context iContext, String iMessage)
	{
		Toast toastErrorMessage;
		toastErrorMessage = Toast.makeText(iContext, iMessage, TOAST_MESSAGE_LENGTH);
		toastErrorMessage.setGravity(Gravity.CENTER, 0, 0); //gravity, x-offset, y-offset
		toastErrorMessage.show();
	}
	static public void displayErrorToast(Context iContext, String iMessage)
	{
		displayToast(iContext, iMessage);
	}
	static public void displayErrorToasts(Context context, Integer errorValue, Integer duration) {

		Toast toastErrorMessage;
		switch(errorValue) {
		case -1:
			toastErrorMessage = Toast.makeText(context, "Sorry! We could not load the recipe you want to view. Check your connection!", duration);
			toastErrorMessage.setGravity(Gravity.CENTER, 0, 0); //gravity, x-offset, y-offset
			toastErrorMessage.show();
			break;
		case -2:
			toastErrorMessage = Toast.makeText(context, "Sorry! We could not load the recipe you want to view. Check your connection!", duration);
			toastErrorMessage.setGravity(Gravity.CENTER, 0, 0); //gravity, x-offset, y-offset
			toastErrorMessage.show();
			break;
		default:
			break;
		}
	}

	static public Ingredient getFromIntent(Intent intent, String key)
	{
		Ingredient returnValue = new Ingredient(intent.getStringExtra(key + " name"), intent.getStringExtra(key + " amount"));
		if (returnValue.name == null || returnValue.amount == null)
			return null;
		return returnValue;
	}

	static private class UploadDraftTask extends AsyncTask<Recipe, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Recipe... params) {
			Comm IComm = new Comm();
			int Status = IComm.saveDraft(params[0]);
			return (Status == Comm.SUCCESS);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result != true) {
				Log.e("FailUpload","Could not upload draft to server.");
			}
			else {
				Log.v("UploadSucess", "Sucessfully uploaded draft to server.");
			}
		}

	}
}

class ID_Maker
{
	private static ID_Maker instance = null;
	private int CurrIdNum;
	protected ID_Maker() {
		CurrIdNum = 1;
	}
	public static ID_Maker getInstance() {
		if(instance == null) {
			instance = new ID_Maker();
		}
		return instance;
	}
	public int useCurrID()
	{
		int returnValue = CurrIdNum;
		CurrIdNum = CurrIdNum + 1;
		return returnValue;
	}
}

class LeveledCheckBox extends CheckBox
{
	private final int cLevel;
	private final ArrayList<LeveledCheckBox> cChildren;
	Category cData;
	public LeveledCheckBox(Context iContext, int iLevel, Category iData)
	{
		super(iContext);
		cChildren = new ArrayList<LeveledCheckBox>();
		cLevel = iLevel;
		cData = iData;
	}
	public int getLevel()
	{
		return cLevel;
	}
	public void addChild(LeveledCheckBox iCheckBox)
	{
		cChildren.add(iCheckBox);
	}
	public void removeChildren()
	{
		cChildren.clear();
	}
	public ArrayList<LeveledCheckBox> getCheck()
	{
		return null;
	}
	public int getDeepestLevel()
	{
		if (cChildren.isEmpty())
			return cLevel;
		else
		{
			int biggest = 0;
			for (int i = 0; i < cChildren.size() ; i++)
				if (cChildren.get(i).getDeepestLevel() > biggest)
					biggest = cChildren.get(i).getDeepestLevel();
			return biggest;
		}
	}
	@Override
	public void bringToFront()
	{
		super.bringToFront();
		for (int i = 0; i < cChildren.size(); i++)
		{
			cChildren.get(i).bringToFront();
		}
	}
}