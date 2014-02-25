package com.parse.starter;

import com.parse.Parse;
import com.parse.ParseACL;

import com.parse.ParseUser;

import android.app.Application;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(this, "HGtB5UNFJCquxyTsUHlYFYV9kexPVmOaFRDVVgpS", "DRA4Wt0g58Rl7N47vk1LP2vh1sQbt5waKkhFuEwp");

        // ParseUser.enableAutomaticUser();
        // ParseACL defaultACL = new ParseACL();
        //
        // // If you would like all objects to be private by default, remove
        // this
        // // line.
        // defaultACL.setPublicReadAccess(true);
        //
        // ParseACL.setDefaultACL(defaultACL, true);
    }

}
