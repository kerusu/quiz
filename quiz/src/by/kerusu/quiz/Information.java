package by.kerusu.quiz;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class Information extends Activity {

    public static void show(FragmentActivity activity, int messageId) {
        show(activity, activity.getString(messageId));
    }

    public static void show(FragmentActivity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
