package by.kerusu.quiz;

import android.app.Application;

import com.parse.Parse;

public class QuizApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "HGtB5UNFJCquxyTsUHlYFYV9kexPVmOaFRDVVgpS", "DRA4Wt0g58Rl7N47vk1LP2vh1sQbt5waKkhFuEwp");
    }
}
