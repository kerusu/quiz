package by.kerusu.quiz;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.parse.ParseAnalytics;

public class QuizListActivity extends ActionBarActivity {

    private static final String TAG = QuizListActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        ParseAnalytics.trackAppOpened(getIntent());
    }

}
