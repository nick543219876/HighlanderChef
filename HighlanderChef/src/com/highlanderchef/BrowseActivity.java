package com.highlanderchef;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
//import android.view.ViewGroup;

public class BrowseActivity extends Activity {

	//variables
	public static final int LEFTPADDING = 32;
	private ArrayList<LeveledCheckBox> CheckBoxList;
	private LinearLayout iLinearLayout;
	private ProgressBar cSpinner;

	//functions
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CheckBoxList = new ArrayList<LeveledCheckBox>();
		ScrollView iScrollView = new ScrollView(this);
		iLinearLayout = new LinearLayout(this);
		iLinearLayout.setOrientation(LinearLayout.VERTICAL);
		iScrollView.addView(iLinearLayout);
		//Get categories' names
		new CategoryTask().execute();
		this.setContentView(iScrollView);
	}

	public void CreateCategories(ArrayList<Category> iCategoryList) {
		ID_Maker currID = ID_Maker.getInstance();
		for (int i = 0; i < iCategoryList.size() ; i++)
		{
			LeveledCheckBox iCheckBox = new LeveledCheckBox(this,iCategoryList.get(i).level,iCategoryList.get(i));
			iCheckBox.setText(iCheckBox.cData.name.toCharArray(),
					0, iCheckBox.cData.name.length());
			iCheckBox.setPadding(iCheckBox.getLevel()*LEFTPADDING, 0, 0, 0);
			iCheckBox.setId(currID.useCurrID());
			iCheckBox.setOnClickListener(CheckboxGetsChecked(iCheckBox));
			CheckBoxList.add(iCheckBox);
			if (iCheckBox.cData.level == 0)
				iLinearLayout.addView(iCheckBox);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse, menu);

		return true;
	}

	View.OnClickListener CheckboxGetsChecked(final LeveledCheckBox iCheckBox)
	{
		return new View.OnClickListener() {
			@Override
			public void onClick(View iView) {
				LinearLayout iLinearLayout = (LinearLayout) (iCheckBox.getParent());
				//find children
				ArrayList<LeveledCheckBox> iChildren = getCategoryChildren(CheckBoxList.indexOf(iCheckBox));

				if (iChildren.isEmpty() && iCheckBox.isChecked())
				{
					//Checkbox is a leaf so open new activity
					Intent intent = new Intent(BrowseActivity.this, SearchActivity.class);
					intent.putExtra("category_query", Integer.toString(iCheckBox.cData.id));
					intent.putExtra("CategoryID", iCheckBox.cData.id);
					startActivity(intent);
				}

				if (((CheckBox) iView).isChecked())
				{
					//make children visible
					for (int i = 0 ; i < iChildren.size() ; i++)
					{
						iLinearLayout.addView(iChildren.get(i));
						iCheckBox.addChild(iChildren.get(i));
						iChildren.get(i).setVisibility(View.VISIBLE);
					}
				}
				else
				{
					//make children invisible
					for (int i = 0 ; i < iChildren.size() ; i++)
					{
						iLinearLayout.removeView(iChildren.get(i));
						iChildren.get(i).setVisibility(View.GONE);
						iChildren.get(i).setChecked(false);
					}
					iCheckBox.removeChildren();
				}
				//Reorder the stack so the children are displayed underneath the parent
				reorderItems();
			}
		};
	}

	private void reorderItems()
	{
		for (int i = 0; i < CheckBoxList.size() ; i++)
		{
			CheckBoxList.get(i).bringToFront();
		}
	}

	private ArrayList<LeveledCheckBox> getCategoryChildren(int index)
	{
		ArrayList<LeveledCheckBox> Children = new ArrayList<LeveledCheckBox>();
		int init_level = CheckBoxList.get(index).cData.level;
		for (int i = index + 1; i < CheckBoxList.size() ; i++)
		{
			if(CheckBoxList.get(i).cData.level == init_level + 1)
				Children.add(CheckBoxList.get(i));
			else if(CheckBoxList.get(i).cData.level == init_level)
				return Children;
		}
		return Children;
	}

	private class CategoryTask extends AsyncTask<String, Void, Boolean> {
		ArrayList<Category> CategoryList;
		@Override
		protected Boolean doInBackground(String... params) {
			Comm iComm = new Comm();
			CategoryList = iComm.getCategories();
			boolean ret = false;
			if (CategoryList != null)
				ret = (!CategoryList.isEmpty());
			return ret;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				Log.v("Got_Categories","Successfully recieved categories from server");
				CreateCategories(CategoryList);
			} else {
				Log.e("NO_CATEGORIES", "Did not get any categories from the server");
			}
		}

	}
}
