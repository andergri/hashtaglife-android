package com.hashtaglife.hashtaglife;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by griffinanderson on 11/28/15.
 */
@ParseClassName("Inbox")
public class Inbox extends ParseObject {

    public Inbox() {
        // A default constructor is required.
    }

    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class InboxFindCallback implements FindCallback<Inbox> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Inbox> objects, ParseException e) {
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
        protected abstract void doneOnce(List<Inbox> objects, ParseException e);
    }

    // Load Inbox In Background
    public static void loadInboxInBackground(final FindCallback<Inbox> callback) {

        if (ParseUser.getCurrentUser() == null){
            return;
        }

        ParseQuery<Inbox> query = new ParseQuery<Inbox>(Inbox.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("has_seen", false);
        query.findInBackground(new InboxFindCallback() {
            @Override
            protected void doneOnce(List<Inbox> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    // Mark Inbox
    public void markInbox(){
        this.put("has_seen", true);
        this.saveInBackground();
    }

    // Class
    public ParseUser getUser() {
        return getParseUser("user");
    }
    public String getHashtag() {
        return getString("hashtag");
    }
    public String getLocation() {
        return String.valueOf(getParseObject("location"));
    }
}