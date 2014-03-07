package by.kerusu.quiz;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import by.kerusu.quiz.QuizAdapter.FilterMode;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class QuizListActivity extends ActionBarActivity {

    private static final String TAG = QuizListActivity.class.getSimpleName();

    private ParseUser user;

    private ListView quizListView;
    private QuizAdapter quizListViewAdapter;

    private List<ParseObject> pictures;
    private List<ParseObject> answers;
    private List<ParseObject> actors;

    private AlertDialog activeDialog;

    private ComplementProvider complementProvider;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        user = ParseUser.getCurrentUser();
        if (user == null) {
            // we are not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        quizListView = (ListView) findViewById(R.id.quizListView);
        quizListViewAdapter = new QuizAdapter(this, getScreenWidth());
        quizListView.setAdapter(quizListViewAdapter);

        quizListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                onPictureSelected(position);
            }
        });

        startDataLoading();

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.action_list,
                android.R.layout.simple_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long id) {

                if (quizListViewAdapter != null) {
                    switch (position) {
                    case 0:
                        quizListViewAdapter.applyFilterMode(FilterMode.None);
                        break;
                    case 1:
                        quizListViewAdapter.applyFilterMode(FilterMode.Answered);
                        break;
                    case 2:
                        quizListViewAdapter.applyFilterMode(FilterMode.NotAnswered);
                        break;
                    default:
                        break;
                    }
                }

                return false;
            }
        });

        complementProvider = new ComplementProvider(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.logout == item.getItemId()) {
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        if (R.id.show_complement == item.getItemId()) {
            showComplement();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showComplement() {
        Toast.makeText(getApplicationContext(), complementProvider.getRandomComplement(), Toast.LENGTH_LONG).show();
    }

    private void startDataLoading() {
        ParseQuery<ParseObject> query;
        query = ParseQuery.getQuery("Actor");
        query.orderByAscending("name");
        query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                onActorsLoaded(objects);
            }
        });

        query = ParseQuery.getQuery("Answer");
        query.whereEqualTo("userId", user.getObjectId());
        query.orderByAscending("updatedAt");
        query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                onAnswersLoaded(objects);
            }
        });

        query = ParseQuery.getQuery("Picture");
        query.orderByAscending("index");
        query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                onPicturesLoaded(objects);
            }
        });

        ProgressFragment.show(this, R.string.loading_data);
    }

    private void onActorsLoaded(List<ParseObject> newActors) {
        actors = newActors;
        quizListViewAdapter.setActors(newActors);
        hideProgressFragmentIfNeeded();
    }

    private void onAnswersLoaded(List<ParseObject> newAnswers) {
        answers = newAnswers;
        quizListViewAdapter.setAnswers(newAnswers);
        hideProgressFragmentIfNeeded();
    }

    private void onPicturesLoaded(List<ParseObject> newPictures) {
        pictures = newPictures;
        quizListViewAdapter.setPictures(newPictures);
        hideProgressFragmentIfNeeded();
    }

    private void hideProgressFragmentIfNeeded() {
        if (quizListViewAdapter != null && quizListViewAdapter.allDataSet()) {
            ProgressFragment.dismiss(this);
        }
    }

    private void onPictureSelected(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(QuizListActivity.this);
        ArrayList<String> names = new ArrayList<String>();
        for (final ParseObject actor : actors) {
            names.add(actor.getString("name"));
        }

        builder.setItems(names.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activeDialog = null;
                onPictureAndActorSelected(position, which);
            }
        });

        builder.setTitle(R.string.choose_actor_dialog_title);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activeDialog = null;
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                activeDialog = null;
            }
        });
        (activeDialog = builder.create()).show();
    }

    private void onPictureAndActorSelected(int pictureIndex, int actorIndex) {
        if (pictures == null || pictures.isEmpty() || pictureIndex < 0 || pictureIndex >= pictures.size()) {
            return;
        }
        if (actors == null || actors.isEmpty() || actorIndex < 0 || actorIndex >= actors.size()) {
            return;
        }

        // String pictureId = pictures.get(pictureIndex).getObjectId();
        String pictureId = quizListViewAdapter.getPictureIdByIndex(pictureIndex);
        if (TextUtils.isEmpty(pictureId)) {
            return;
        }

        String actorId = actors.get(actorIndex).getObjectId();

        ParseObject currentAnswer = null;

        for (ParseObject answer : answers) {
            String answerPictureId = answer.getString("pictureId");
            if (pictureId.equals(answerPictureId)) {
                currentAnswer = answer;
            }
        }

        ProgressFragment.show(this, R.string.saving_your_answer);

        if (currentAnswer == null) {

            if (user == null) {
                return;
            }

            final ParseObject newAnswer = new ParseObject("Answer");
            newAnswer.put("actorId", actorId);
            newAnswer.put("pictureId", pictureId);
            newAnswer.put("userId", user.getObjectId());

            ParseACL parseACL = new ParseACL();
            parseACL.setReadAccess(user, true);
            parseACL.setWriteAccess(user, true);
            parseACL.setPublicReadAccess(true);

            newAnswer.setACL(parseACL);

            newAnswer.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    ProgressFragment.dismiss(QuizListActivity.this);

                    if (e != null) {
                        Information.show(QuizListActivity.this, String.valueOf(e.getMessage()));
                        showRandomComplementIfNeeded();
                        return;
                    }
                    if (answers == null || quizListViewAdapter == null) {
                        showRandomComplementIfNeeded();
                        return;
                    }
                    answers.add(newAnswer);
                    quizListViewAdapter.setAnswers(answers);
                    showRandomComplementIfNeeded();
                }
            });

        } else {
            final ParseObject newAnswer = currentAnswer;
            newAnswer.put("actorId", actorId);
            newAnswer.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    ProgressFragment.dismiss(QuizListActivity.this);

                    if (e != null) {
                        Information.show(QuizListActivity.this, String.valueOf(e.getMessage()));
                        showRandomComplementIfNeeded();
                        return;
                    }

                    if (answers == null || quizListViewAdapter == null) {
                        showRandomComplementIfNeeded();
                        return;
                    }
                    answers.remove(newAnswer);
                    answers.add(newAnswer);
                    quizListViewAdapter.setAnswers(answers);
                    showRandomComplementIfNeeded();
                }
            });
        }
    }

    private void showRandomComplementIfNeeded() {
        if (Math.random() < 0.5) {
            showComplement();
        }
    }

    @SuppressLint("NewApi")
    private int getScreenWidth() {
        int width;
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else {
            Display display = getWindowManager().getDefaultDisplay();
            width = display.getWidth(); // deprecated
        }
        return width;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (activeDialog != null) {
            if (activeDialog.isShowing()) {
                activeDialog.dismiss();
            }
            activeDialog = null;
        }

        ProgressFragment.dismiss(this);
    }
}
