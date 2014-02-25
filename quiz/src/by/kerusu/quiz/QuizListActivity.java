package by.kerusu.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class QuizListActivity extends ActionBarActivity {

    private static final String TAG = QuizListActivity.class.getSimpleName();

    private ParseUser user;
    private List<ParseObject> actors;
    private List<ParseObject> answers;
    private List<ParseObject> pictures;

    private Map<String, String> userNameByPictureId;

    private ListView quizListView;
    private ParseQueryAdapter<ParseObject> quizListViewAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        ParseAnalytics.trackAppOpened(getIntent());

        user = ParseUser.getCurrentUser();
        if (user == null) {
            // we are not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        quizListView = (ListView) findViewById(R.id.quizListView);

        startDataLoading();
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
        return super.onOptionsItemSelected(item);
    }

    private void startDataLoading() {
        ParseQuery<ParseObject> query;
        query = ParseQuery.getQuery("Actor");
        query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                onActorsLoaded(objects);
            }
        });

        query = ParseQuery.getQuery("Answer");
        query.whereEqualTo("userId", user.getObjectId());
        query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                onAnswersLoaded(objects);
            }
        });
    }

    private void onActorsLoaded(List<ParseObject> newActors) {
        actors = newActors;
        udpateListViewIfNeeded();
    }

    private void onAnswersLoaded(List<ParseObject> newAnswers) {
        answers = newAnswers;
        udpateListViewIfNeeded();
    }

    private void udpateListViewIfNeeded() {
        if (actors == null || answers == null) {
            return;
        }

        quizListViewAdapter = new ParseQueryAdapter<ParseObject>(this, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Picture");
                query.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
                return query;
            }
        }, R.layout.quiz_listview_item) {
            @Override
            public View getItemView(ParseObject object, View v, ViewGroup parent) {
                v = super.getItemView(object, v, parent);
                String username = userNameByPictureId.get(object.getObjectId());

                TextView usernameTextView = (TextView) v.findViewById(R.id.username);

                if (username != null) {
                    usernameTextView.setText(username);
                    usernameTextView.setTextColor(QuizListActivity.this.getResources().getColor(R.color.username_color));
                } else {
                    usernameTextView.setText(R.string.default_username);
                    usernameTextView.setTextColor(QuizListActivity.this.getResources().getColor(R.color.default_username_color));
                }

                return v;
            }
        };

        quizListViewAdapter.setImageKey("rawData");
        quizListView.setAdapter(quizListViewAdapter);

        quizListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parentAdapter, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(QuizListActivity.this);
                ArrayList<String> names = new ArrayList<String>();
                for (final ParseObject actor : actors) {
                    names.add(actor.getString("name"));
                }
                builder.setItems((CharSequence[]) names.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseObject answer = new ParseObject("Answer");
                        answer.put("userId", user.getObjectId());
                        answer.put("pictureId", ((ParseObject) parentAdapter.getItemAtPosition(position)).getObjectId());
                        answer.put("actorId", actors.get(which).getObjectId());
                        answer.setACL(new ParseACL(user));

                        answer.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    // reload all data
                                    startDataLoading();
                                }
                            }
                        });
                    }
                });
                builder.create().show();
            }
        });

        userNameByPictureId = new HashMap<String, String>();

        for (final ParseObject answer : answers) {
            String pictureId = answer.getString("pictureId");
            if (TextUtils.isEmpty(pictureId)) {
                continue;
            }
            String actorId = answer.getString("actorId");
            if (TextUtils.isEmpty(actorId)) {
                continue;
            }
            for (final ParseObject actor : actors) {
                if (!actor.getObjectId().equals(actorId)) {
                    continue;
                }
                String name = actor.getString("name");
                if (TextUtils.isEmpty(name)) {
                    break;
                }
                userNameByPictureId.put(pictureId, name);
                break;
            }
        }
    }
}
