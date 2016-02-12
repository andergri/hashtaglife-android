package com.hashtaglife.hashtaglife;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.util.HashMap;


/**
 * Created by griffinanderson on 11/28/14.
 */
public class MyApplication extends Application {
    public void onCreate() {
        ParseObject.registerSubclass(Hashtag.class);
        ParseObject.registerSubclass(Tag.class);
        ParseObject.registerSubclass(Selfie.class);
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(Trending.class);
        ParseObject.registerSubclass(Vote.class);
        ParseObject.registerSubclass(Subscribe.class);
        ParseObject.registerSubclass(Inbox.class);
        Parse.initialize(this, "jjcVHlw8UwWC2FkXZhL7JNLqDiXJlyBnKVAIsrbO", "oivL7zqMRzHAv0fBKJkUuxnQch3tNkQ91t3WMJr1");
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (ParseUser.getCurrentUser() != null) {
            if (installation.get("user") == null) {
                installation.put("user", ParseUser.getCurrentUser());
                ParsePush.subscribeInBackground("global");
            }
            if (installation.get("location") == null){
                if (ParseUser.getCurrentUser().get("location") != null){
                    ParseObject loc = (ParseObject) ParseUser.getCurrentUser().get("location");
                    ParsePush.subscribeInBackground(loc.getObjectId());
                    installation.put("location", loc);
                }
            }
        }
        installation.saveInBackground();
        //Branch.getAutoInstance(this);
    }

    private static final String PROPERTY_ID = "UA-53643197-2";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
