package com.hashtaglife.hashtaglife;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by griffinanderson on 11/28/14.
 */
@ParseClassName("Hashtag")
public class Hashtag extends ParseObject {

    private Boolean isInbox = false;

    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class HashtagFindCallback implements FindCallback<Hashtag> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Hashtag> objects, ParseException e) {
            if (!calledCallback) {
                if (objects != null) {
                    // We got a result, use it.
                    calledCallback = true;
                    doneOnce(objects, null);
                } else if (!isCachedResult) {
                    // We got called back twice, but got a null result both
                    // times. Pass on the latest error.
                    doneOnce(null, e);
                }
            }
            isCachedResult = false;
        }

        /**
         * Override this method with the callback that should only be called
         * once.
         */
        protected abstract void doneOnce(List<Hashtag> objects, ParseException e);
    }

    // Search
    public static void searchInBackground(String name,
                                        final FindCallback<Hashtag> callback) {
        ParseQuery<Hashtag> query = new ParseQuery<Hashtag>(Hashtag.class);
        query.whereContains("name", name);
        query.whereGreaterThan("count", 0);
        query.orderByDescending("count");

        query.findInBackground(new HashtagFindCallback() {
            @Override
            protected void doneOnce(List<Hashtag> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    // Popular
    public static void popularInBackground(final FindCallback<Hashtag> callback) {
        ParseQuery<Hashtag> query = new ParseQuery<Hashtag>(Hashtag.class);
        query.whereGreaterThan("count", 0);
        query.whereGreaterThan("trending", -1);
        query.orderByDescending("trending");
        query.setLimit(24);

        query.findInBackground(new HashtagFindCallback() {
            @Override
            protected void doneOnce(List<Hashtag> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    public List<Hashtag> getCleanHashtags(List<Tag> tags){

        List<Hashtag> hashtags = new ArrayList<Hashtag>();

        for (int i = 0; i < tags.size(); i++){
            Hashtag hashtag = new Hashtag();
            String name = ((Tag) tags.get(i)).getName();
            hashtag.setName(name);
            hashtag.setFollowers(((Tag) tags.get(i)).getFollowers());
            hashtags.add(hashtag);
        }
        return hashtags;
    }

    public List<Hashtag> getCleanInbox(List<Inbox> inboxes){

        List<Hashtag> hashtags = new ArrayList<Hashtag>();

        for (int i = 0; i < inboxes.size(); i++){
            Hashtag hashtag = new Hashtag();
            String name = ((Inbox) inboxes.get(i)).getHashtag();
            hashtag.setName(name);
            hashtags.add(hashtag);
            hashtag.markAsInbox();
        }
        return hashtags;
    }

    // Class

    public String getName() {
        return getString("name");
    }

    public String getNameWithHashtag() {
        return "#" + getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public int getFollowers() {
        return getInt("followers");
    }
    public void setFollowers(int followers) { put("followers", followers); }

    public void markAsInbox(){
        isInbox = true;
    }

    public boolean isInbox(){
        return isInbox;
    }

    public void saveHashtag(final String name){

        ParseQuery<Hashtag> query = new ParseQuery<Hashtag>(Hashtag.class);
        query.whereEqualTo("name", name);
        query.findInBackground(new FindCallback<Hashtag>() {
            public void done(List<Hashtag> hashtags, ParseException e) {
                if (e == null) {
                    if (hashtags.size() > 0) {
                        Log.d("hashtag create", "old");
                        ParseObject hashtag = hashtags.get(0);
                        hashtag.increment("count");
                        hashtag.increment("trending");
                        hashtag.saveInBackground();

                    } else {
                        Log.d("hashtag create", "new");
                        ParseObject hashtag = ParseObject.create("Hashtag");
                        hashtag.put("count", 1);
                        hashtag.put("trending", 1);
                        hashtag.put("name", name);
                        hashtag.saveInBackground();

                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

}
