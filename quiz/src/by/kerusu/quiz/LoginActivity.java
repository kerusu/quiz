package by.kerusu.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends FragmentActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText username;
    private EditText password;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpened(getIntent());

        if (ParseUser.getCurrentUser() != null) {
            onSignedIn();
            return;
        }

        username = (EditText) findViewById(R.id.user_name);
        password = (EditText) findViewById(R.id.password);

        findViewById(R.id.signinsignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInSignUpClicked();
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    onSignInSignUpClicked();
                    return true;
                }
                return false;
            }
        });
    }

    private void onSignInSignUpClicked() {
        signIn(username.getText().toString(), password.getText().toString(), true);
    }

    private void signIn(final String username, final String password, final boolean firstTry) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user == null) {
                    if (firstTry) {
                        Log.d(TAG, "Trying to sign up a new user as sign in failed");
                        signUp(username, password);
                    } else {
                        Log.w(TAG, "Got error for a second try while logging in");
                        Information.show(LoginActivity.this, R.string.error_username_already_taken);
                    }
                } else {
                    Log.d(TAG, "User signed up");
                    Log.d(TAG, "User = " + String.valueOf(ParseUser.getCurrentUser()));
                    onSignedIn();
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
            Information.show(LoginActivity.this, R.string.error_username_already_taken);
        } else {
            // show generic error
            Information.show(LoginActivity.this, R.string.error_username_already_taken);
        }
    }

    private void onSignedIn() {
        startActivity(new Intent(this, QuizListActivity.class));
        finish();
    }
}
