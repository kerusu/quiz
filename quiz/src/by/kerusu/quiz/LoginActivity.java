package by.kerusu.quiz;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews.ActionException;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends  {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText username;
    private EditText password;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpened(getIntent());

        username = (EditText) findViewById(R.id.user_name);
        password = (EditText) findViewById(R.id.password);

        findViewById(R.id.signinsignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(username.getText().toString(), password.getText().toString(), true);
            }
        });
    }

    private void signIn(final String username, final String password, final boolean firstTry) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (firstTry) {
                    if (user == null) {
                        Log.d(TAG, "Trying to sign up a new user as sign in failed");
                        signUp(username, password);
                    } else {
                        Log.d(TAG, "User signed up");
                        Log.d(TAG, "User = " + String.valueOf(ParseUser.getCurrentUser()));
                    }
                } else {
                    Log.w(TAG, "Got error for a second try while logging in");
                }
            }
        });

    }

    private void signUp(final String username, final String password) {
        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(username);
        parseUser.setPassword(password);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    signIn(username, password, false);
                } else {
                    onSignUpFiledWithError(e);
                }
            }
        });
    }

    private void onSignUpFiledWithError(ParseException e) {
        if (e.getCode() == ParseCodes.USERNAME_ALREADY_TAKEN) {
            // user name already taken
        } else {
            // show generic error
        }
    }
}
