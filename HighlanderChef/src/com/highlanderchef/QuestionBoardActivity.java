package com.highlanderchef;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QuestionBoardActivity extends ActionBarActivity {

	private final int LENGTH_SHORT = 2000;
	int recipeID = 0;
	ArrayList<Question> questions = null;

	ID_Maker MakerInstance = ID_Maker.getInstance();
	RelativeLayout rflayout;
	Question newQuestion = null;
	Question newReply = null;
	EditText etQuestionToPost = null;
	EditText etToPostReply = null;
	Button btnAddQuestion = null;
	View lastView = null;
	TextView tv_questions;

	Recipe currentRecipe;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_board);
		rflayout = (RelativeLayout) findViewById(R.id.questionBoardLayout);
		TextView infoTextView = new TextView(this);
		etQuestionToPost = new EditText(this);
		etToPostReply = new EditText(this);
		btnAddQuestion = new Button(this);
		Intent intent = getIntent();
		if(intent != null) {
			System.out.println("MM.intent.get(recipe)");
			recipeID = intent.getIntExtra("recipeID", 0);
			new getRecipeTask().execute(recipeID);
		}
		else {
			System.out.println("Intent is null!");
		}

		//Textview -- text view to show purpose of the page
		infoTextView.setId(MakerInstance.useCurrID());
		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tParams.addRule(RelativeLayout.BELOW, R.id.ForumBoardTitle);
		tParams.setMargins(0, 5, 0, 5);
		infoTextView.setLayoutParams(tParams);
		infoTextView.setText("Ask questions about the recipes on this page.");
		rflayout.addView(infoTextView);
		lastView = infoTextView;

		//TODO:Edittext - text field to post a question
		etQuestionToPost.setId(MakerInstance.useCurrID());
		RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlParams.addRule(RelativeLayout.BELOW, lastView.getId());
		etQuestionToPost.setLayoutParams(rlParams);
		rflayout.addView(etQuestionToPost);
		lastView = etQuestionToPost;

		//TODO:button Add Question - upon click, a user adds a question
		btnAddQuestion.setId(MakerInstance.useCurrID());
		btnAddQuestion.setText("Ask a Question");
		rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlParams.addRule(RelativeLayout.BELOW, etQuestionToPost.getId());
		btnAddQuestion.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v)
			{
				//tv_questions.setText(tv_questions.getText() + "\n\n" + etQuestionToPost.getText());
				Question q = new Question(Comm.staticGetUserID(), Comm.getEmail(), etQuestionToPost.getText().toString());
				new postQuestionTask().execute(q);
				etQuestionToPost.getText().clear();
			}
		});
		btnAddQuestion.setLayoutParams(rlParams);
		rflayout.addView(btnAddQuestion);
		lastView = btnAddQuestion;

		if (questions == null)
			System.out.println("questions is null!");
		else {
			System.out.println("Questions is filled with things!");

		}

		tv_questions = new TextView(this);
		tv_questions.setId(MakerInstance.useCurrID());
		rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlParams.addRule(RelativeLayout.BELOW, lastView.getId());
		tv_questions.setLayoutParams(rlParams);
		tv_questions.setBackgroundColor(Color.WHITE);
		rflayout.addView(tv_questions);
		lastView = tv_questions;

		//new postQuestionTask()
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question_board, menu);
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

	public void addAbilityToPostQuestion(EditText etQuestionToPost, Button btnAddQuestion, View lastView) {


	}

	public void displayLiveQuestion( Question newlyAddedQuestion, View lastView) {
		displayQuestion(newlyAddedQuestion);
	}
	//TODO: Test display of questions first
	//TODO: Create a function that also displays replies

	void displayListOfReplies(ArrayList<Question> replies, final int qid) {
		System.out.println("MM.displayListOfReplies");
		if(replies != null && replies.size () > 0) {
			System.out.println("size of replies: " + replies.size());
			for(int i = 0; i < replies.size(); ++i) {
				displayQuestion(replies.get(i));
				System.out.println("replies.get(" + i + ") =" + replies.get(i).text);
			}
		}
		else {
			System.out.println("Size of replies: " + replies.size());
		}


		final EditText QustionReplyET = new EditText(this);
		QustionReplyET.setId(MakerInstance.useCurrID());
		QustionReplyET.setHint("Type a reply here");
		RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlParams.addRule(RelativeLayout.BELOW, lastView.getId());
		QustionReplyET.setLayoutParams(rlParams);
		rflayout.addView(QustionReplyET);
		lastView = QustionReplyET;

		Button replyButton = new Button(this);
		replyButton.setId(MakerInstance.useCurrID());
		replyButton.setHint("Reply");
		rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlParams.addRule(RelativeLayout.BELOW, lastView.getId());
		replyButton.setLayoutParams(rlParams);
		rflayout.addView(replyButton);
		lastView = replyButton;

		final TextView reply = new TextView(this);
		reply.setId(MakerInstance.useCurrID());
		replyButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v)
			{
				reply.setText(QustionReplyET.getText() + "\n\n" + QustionReplyET.getText());
				Question r = new Question(Comm.staticGetUserID(), Comm.getEmail(), QustionReplyET.getText().toString());
				r.qid = qid;
				new postReplyTask().execute(r);
				RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//rlParams.addRule(RelativeLayout.BELOW, QustionReplyET.getId() - 1);
				rlParams.addRule(RelativeLayout.ABOVE, QustionReplyET.getId());
				rlParams.addRule(RelativeLayout.ALIGN_LEFT, QustionReplyET.getId());
				//rlParams.setMargins(0, 5, 0, 5);
				reply.setBackgroundColor(Color.RED);
				reply.setLayoutParams(rlParams);
				rflayout.addView(reply);
				QustionReplyET.getText().clear();
			}
		});
	}
	void displayListOfQuestions(ArrayList<Question> questions, View lastView) {
		for(int i = 0; i < questions.size(); ++i) {
			displayQuestion(questions.get(i));
			displayListOfReplies(questions.get(i).replies, questions.get(i).qid);
		}
	}

	void displayQuestion(Question question)
	{
		TextView tv_question = new TextView(this);
		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tParams.addRule(RelativeLayout.BELOW, lastView.getId());
		tParams.addRule(RelativeLayout.ALIGN_LEFT, lastView.getId());
		tParams.setMargins(0, 10, 0, 0);
		tv_question.setLayoutParams(tParams);
		tv_question.setId(MakerInstance.useCurrID());
		tv_question.setText(question.username + "\n" + question.text);
		tv_question.setBackgroundColor(Color.WHITE);
		rflayout.addView(tv_question);
		lastView = tv_question;
	}

	void displayReply(Question reply) {
		TextView tv_questionReply = new TextView(this);
		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tParams.addRule(RelativeLayout.BELOW, lastView.getId());
		tParams.addRule(RelativeLayout.ALIGN_LEFT, lastView.getId());
		tParams.setMargins(0, 5, 0, 5);
		tv_questionReply.setLayoutParams(tParams);
		tv_questionReply.setText("\t" + reply.username + "\n\t" + reply.text);
		tv_questionReply.setBackgroundColor(Color.CYAN);
		rflayout.addView(tv_questionReply);
		lastView = tv_questionReply;

	}

	void postQuestionSuccess(Question question) {
		displayQuestion(question);

		Toast toastSuccessfullyPostQuestion = Toast.makeText(getApplicationContext(), "You have added a Question!", LENGTH_SHORT);
		toastSuccessfullyPostQuestion.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
		toastSuccessfullyPostQuestion.show();
		System.out.println("MM.postQuestionSuccess()");
	}

	void postQuestionFailure(Question question) {
		Utility.displayErrorToasts(getApplicationContext(), -3, LENGTH_SHORT);
	}

	public void postReplySuccess(Question reply) {
		//displayQuestion(reply);

		Toast toastSuccessfullyPostQuestion = Toast.makeText(getApplicationContext(), "You have added a Question!", LENGTH_SHORT);
		toastSuccessfullyPostQuestion.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
		toastSuccessfullyPostQuestion.show();
		System.out.println("MM.postReplySuccess()");

	}

	public void postReplyFailure() {
		System.out.println("Error: could not post reply");
		//Utility.displayErrorToasts(getApplicationContext(), -3, LENGTH_SHORT);
	}

	private class postQuestionTask extends AsyncTask<Question, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Question... params) {
			Comm c = new Comm();
			newQuestion = params[0];
			int ret = c.postQuestion(recipeID, params[0].text);
			return (ret == Comm.SUCCESS);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result == true) {
				Log.v("postQuestionSuccess","Successfully posted a question!");
				postQuestionSuccess(newQuestion);
			}
			else {
				Log.v("postQuestionFailure", "Failed to post a question!");
			}
		}
	}
	private class postReplyTask extends AsyncTask<Question, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Question... params) {
			Comm c = new Comm();
			newReply = params[0];
			int ret = c.postReply(params[0].qid, params[0].text);
			return (ret == Comm.SUCCESS);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result == true) {
				Log.v("postReplySuccess","Successfully posted a reply!");
				postReplySuccess(newReply);
			}
			else {
				Log.v("postReplyFailure", "Failed to post a reply!");
				postReplyFailure();
			}
		}
	}
	private void displayRecipeSuccess()
	{
		displayListOfQuestions(currentRecipe.questions, lastView);
	}

	private class getRecipeTask extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			Comm c = new Comm();
			Recipe ret = c.getRecipe(params[0]);
			currentRecipe = ret;
			return (ret != null);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				Log.v("getRecipeSuccess","Success: Recipe Received");
				displayRecipeSuccess();
			} else {
				Log.v("getRecipeFailure","Failure: Did not receive recipe");
			}
		}
	}

}
