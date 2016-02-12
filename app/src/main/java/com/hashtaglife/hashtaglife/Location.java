package com.hashtaglife.hashtaglife;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by griffinanderson on 12/19/14.
 */
@ParseClassName("Location")
public class Location extends ParseObject {

        /**
         * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
         * policy, but only call the callback once, with the first data available.
         */
        private abstract static class LocationFindCallback implements FindCallback<Location> {
            private boolean isCachedResult = true;
            private boolean calledCallback = false;

            @Override
            public void done(List<Location> objects, ParseException e) {
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
            protected abstract void doneOnce(List<Location> objects, ParseException e);
        }

        // Search
        public static void locationInBackground(String filter, String search,
                                              final FindCallback<Location> callback) {

            ParseQuery<Location> query = new ParseQuery<Location>(Location.class);

            query.whereEqualTo("active", true);
            query.whereEqualTo("type", filter);
            if (search != null && search.length() > 0) {
                query.whereContains("name", search);
            }
            query.orderByAscending("type");
            query.addAscendingOrder("name");
            query.findInBackground(new LocationFindCallback() {
                @Override

                protected void doneOnce(List<Location> objects, ParseException e) {
                    callback.done(objects, e);
                }
            });
        }

        // Class

        public String getName() {
            return getString("name");
        }


    public void updateLocation(final ParseObject location){

        location.increment("students");
        location.saveInBackground();

        ParseUser.getCurrentUser().put("location", location);
        ParseUser.getCurrentUser().saveInBackground();
        ParsePush.subscribeInBackground(location.getObjectId());
    }
}
