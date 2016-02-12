package com.hashtaglife.hashtaglife;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by griffinanderson on 12/28/14.
 */
@ParseClassName("Tag")
public class Tag extends ParseObject {


    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class TagFindCallback implements FindCallback<Tag> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Tag> objects, ParseException e) {
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
        protected abstract void doneOnce(List<Tag> objects, ParseException e);
    }

    // Search
    public static void searchInBackground(String name,
                                          final FindCallback<Tag> callback) {

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        if (location != null) {

            ParseQuery<Tag> query = new ParseQuery<Tag>(Tag.class);
            query.whereEqualTo("location", location);
            query.whereContains("name", name);
            query.whereGreaterThan("count", 0);
            query.orderByDescending("count");

            query.findInBackground(new TagFindCallback() {
                @Override
                protected void doneOnce(List<Tag> objects, ParseException e) {
                    callback.done(objects, e);
                }
            });
        }
    }

    // Popular
    public static void popularInBackground(final FindCallback<Tag> callback) {

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        if (location != null) {

            ParseQuery<Tag> query = new ParseQuery<Tag>(Tag.class);
            query.whereEqualTo("location", location);
            query.whereGreaterThan("count", 0);
            query.whereGreaterThan("trending", -1);
            query.orderByDescending("trending");
            query.setLimit(24);


            query.findInBackground(new TagFindCallback() {
                @Override
                protected void doneOnce(List<Tag> objects, ParseException e) {
                    callback.done(objects, e);
                }
            });
        }
    }

    // Update Follower Count
    public static void updateFollowerCount(ParseObject location, String hashtag) {


        ParseQuery<Tag> query = new ParseQuery<Tag>(Tag.class);
        query.whereEqualTo("location", location);
        query.whereEqualTo("name", hashtag);
        query.findInBackground(new TagFindCallback() {
            @Override
            protected void doneOnce(List<Tag> objects, ParseException e) {
                if (e == null && objects.size() > 0){
                    Tag tag = objects.get(0);
                    tag.addFollower();
                }
            }
        });
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
    public void addFollower(){
        this.increment("followers");
        this.saveInBackground();
    }


}
