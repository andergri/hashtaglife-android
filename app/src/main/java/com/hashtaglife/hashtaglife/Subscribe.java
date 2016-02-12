package com.hashtaglife.hashtaglife;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by griffinanderson on 11/28/15.
 */
@ParseClassName("Subscribe")
public class Subscribe extends ParseObject {

    public ArrayList<Subscribe> subscribes;

    public Subscribe() {
        // A default constructor is required.
        subscribes = new ArrayList<Subscribe>();
    }

    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class SubscribeFindCallback implements FindCallback<Subscribe> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Subscribe> objects, ParseException e) {
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
        protected abstract void doneOnce(List<Subscribe> objects, ParseException e);
    }

    public void loadSubscribe(){
        loadSubscribeInBackground(new FindCallback<Subscribe>() {
            @Override
            public void done(List<Subscribe> list, ParseException e) {
                subscribes.addAll(list);
            }
        });
    }

    // Load Subscribe in Background
    public static void loadSubscribeInBackground(final FindCallback<Subscribe> callback) {

        if (ParseUser.getCurrentUser() == null){
            return;
        }

        ParseQuery<Subscribe> query = new ParseQuery<Subscribe>(Subscribe.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new SubscribeFindCallback() {
            @Override
            protected void doneOnce(List<Subscribe> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    public boolean isHashtagInSubscribe(String hashtag){

        for (ParseObject subscribe : subscribes) {
            String sHashtag = subscribe.getString("hashtag");
            if (sHashtag.equals(hashtag)){
                return true;
            }
        }
        return false;
    }

    public void toggleFollow(String hashtag){
        List<ParseObject> toRemove = new CopyOnWriteArrayList<ParseObject>();
        for (ParseObject subscribe : subscribes) {
            String sHashtag = subscribe.getString("hashtag");
            if (sHashtag.equals(hashtag)){
                toRemove.add(subscribe);
                removeSubscribe(hashtag);
                subscribes.removeAll(toRemove);
                return;
            }
        }
        addSubscribe(hashtag);
    }

    private void removeSubscribe(String hashtag){

        if (ParseUser.getCurrentUser() == null){
            return;
        }
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        ParseQuery<Subscribe> query = new ParseQuery<Subscribe>(Subscribe.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("hashtag", hashtag);
        query.whereEqualTo("location", location);
        query.findInBackground(new SubscribeFindCallback() {
            @Override
            protected void doneOnce(List<Subscribe> objects, ParseException e) {
                ParseObject.deleteAllInBackground(objects);
            }
        });
    }

    private void addSubscribe(String hashtag) {

        if (ParseUser.getCurrentUser() == null){
            return;
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        Subscribe subscribe = new Subscribe();
        subscribe.put("location", location);
        subscribe.put("hashtag", hashtag);
        subscribe.put("user", currentUser);
        subscribe.put("muted", false);
        subscribe.saveInBackground();
        subscribes.add(subscribe);
        updateTagFollowers(hashtag);
        return;
    }

    private void updateTagFollowers(String hashtag){

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        Tag tag = new Tag();
        tag.updateFollowerCount(location, hashtag);
    }

    // Class
    public ParseUser getFrom() {
        return getParseUser("user");
    }
    public String getHashtag() {
        return getString("hashtag");
    }
    public String getLocation() {
        return String.valueOf(getParseObject("location"));
    }
}