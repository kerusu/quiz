package by.kerusu.quiz.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import by.kerusu.quiz.LoginActivity;
import by.kerusu.quiz.R;
import by.kerusu.quiz.R.color;
import by.kerusu.quiz.R.id;
import by.kerusu.quiz.R.layout;
import by.kerusu.quiz.R.menu;
import by.kerusu.quiz.R.string;

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

    private AlertDialog activeDialog;

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
        query.orderByAscending("updatedAt");
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
        }, R.layout.old_quiz_listview_item) {
            @Override
            public View getItemView(ParseObject object, View v, ViewGroup parent) {
                v = super.getItemView(object, v, parent);
                String username = userNameByPictureId.get(object.getObjectId());

                TextView usernameTextView = (TextView) v.findViewById(R.id.username);

                if (username != null) {
                    AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                    alpha.setDuration(0); // Make animation instant
                    alpha.setFillAfter(true); // Tell it to persist after the
                                              // animation ends
                    // And then on your layout
                    v.startAnimation(alpha);
                    usernameTextView.setText(username);
                    usernameTextView.setTextColor(QuizListActivity.this.getResources().getColor(R.color.username_color));
                } else {
                    AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                    alpha.setDuration(0); // Make animation instant
                    alpha.setFillAfter(true); // Tell it to persist after the
                                              // animation ends
                    // And then on your layout
                    v.startAnimation(alpha);
                    usernameTextView.setText(R.string.default_username);
                    usernameTextView.setTextColor(QuizListActivity.this.getResources().getColor(R.color.default_username_color));
                }

                ImageView imageView = (ImageView) v.findViewById(android.R.id.icon);
                if (imageView != null) {
                    ViewGroup.LayoutParams lp = imageView.getLayoutParams();

                    int newWidth = getScreenWidth();
                    int newHeight = (int) (newWidth * 1.33);

                    if (lp.width != newWidth || lp.height != newHeight) {
                        lp.width = newWidth;
                        lp.height = newHeight;
                        imageView.setLayoutParams(lp);
                    }
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
                        activeDialog = null;

                        final String userId = user.getObjectId();
                        final String pictureId = ((ParseObject) parentAdapter.getItemAtPosition(position)).getObjectId();
                        final String actorId = actors.get(which).getObjectId();

                        ParseObject answer = null;

                        if (answers != null && !answers.isEmpty()) {

                            for (final ParseObject answerIt : answers) {
                                String itUserId = answerIt.getString("userId");
                                String itPctureId = answerIt.getString("pictureId");

                                if (!userId.equals(itUserId)) {
                                    continue;
                                }
                                if (!pictureId.equals(itPctureId)) {
                                    continue;
                                }

                                answer = answerIt;
                            }
                        }

                        if (answer == null) {
                            final ParseObject newAnswer = new ParseObject("Answer");
                            newAnswer.put("userId", userId);
                            newAnswer.put("pictureId", pictureId);
                            newAnswer.put("actorId", actorId);
                            newAnswer.setACL(new ParseACL(user));

                            newAnswer.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        answers.add(newAnswer);
                                        rebuildUserNameByPictureIdMapping(answers, actors);
                                        if (quizListViewAdapter != null) {
                                            quizListViewAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        } else {
                            answer.put("userId", userId);
                            answer.put("pictureId", pictureId);
                            answer.put("actorId", actorId);
                            answer.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        rebuildUserNameByPictureIdMapping(answers, actors);
                                        if (quizListViewAdapter != null) {
                                            quizListViewAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
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
        });

        rebuildUserNameByPictureIdMapping(answers, actors);
    }

    private void rebuildUserNameByPictureIdMapping(List<ParseObject> answers, List<ParseObject> actors) {
        userNameByPictureId = new HashMap<String, String>();

        if (answers == null || actors == null) {
            return;
        }

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
    }
}
