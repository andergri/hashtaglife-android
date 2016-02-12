package com.hashtaglife.hashtaglife;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by griffinanderson on 5/4/15.
 */
@ParseClassName("Trending")
public class Trending extends ParseObject {

    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class TrendingFindCallback implements FindCallback<Trending> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Trending> objects, ParseException e) {
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
        protected abstract void doneOnce(List<Trending> objects, ParseException e);
    }

    // Search
    public static void trendingInBackground(final FindCallback<Trending> callback) {
        ParseQuery<Trending> query = new ParseQuery<Trending>(Trending.class);
        query.whereEqualTo("active", true);
        query.orderByDescending("trending");
        query.addDescendingOrder("name");
        query.findInBackground(new TrendingFindCallback() {
            @Override
            protected void doneOnce(List<Trending> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    public static List<Hashtag> getCleanHashtags(List<Trending> tags){

        List<Hashtag> hashtags = new ArrayList<Hashtag>();

        for (int i = 0; i < tags.size(); i++){
            Hashtag hashtag = new Hashtag();
            String name = ((Trending) tags.get(i)).getName();
            hashtag.setName(name);
            hashtags.add(hashtag);
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

}
