package com.hashtaglife.hashtaglife;

import android.graphics.Bitmap;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by griffinanderson on 11/28/14.
 */
@ParseClassName("Selfie")
public class Selfie extends ParseObject {

    public Selfie() {
        // A default constructor is required.
    }

    private Bitmap bmp;
    private FileInputStream fis;
    private Vote vote;


    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class SelfieFindCallback implements FindCallback<Selfie> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Selfie> objects, ParseException e) {
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
        protected abstract void doneOnce(List<Selfie> objects, ParseException e);
    }

    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract static class SelfieGetCallback implements GetCallback<Selfie> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(Selfie object, ParseException e) {
            if (!calledCallback) {
                if (object != null) {

                    // We got a result, use it.
                    calledCallback = true;
                    doneOnce(object, null);
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
        protected abstract void doneOnce(Selfie object, ParseException e);
    }

    // Load Hashtag
    public static void loadHashtagInBackground(Boolean filter, String name,
                                          final FindCallback<Selfie> callback) {

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        ParseQuery<Selfie> query = new ParseQuery<Selfie>(Selfie.class);
        query.whereEqualTo("hashtags", name);
        query.whereGreaterThan("likes", -4);
        query.whereLessThanOrEqualTo("flags", 3);
        query.orderByDescending("createdAt");

        if (filter && location != null) {
            query.whereEqualTo("location", location);
        }

        query.findInBackground(new SelfieFindCallback() {
            @Override
            protected void doneOnce(List<Selfie> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    // Load Fresh
    public static void loadFreshInBackground(Boolean filter, final FindCallback<Selfie> callback) {

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        Date d = new Date();
        int time = (4 * 3600 * 1000);
        Date newDate = new Date(d.getTime() - (time));
        String[] removed = {"Admin", "Delete"};

        ParseQuery<Selfie> query = new ParseQuery<Selfie>(Selfie.class);
        query.whereGreaterThan("likes", -4);
        query.whereLessThanOrEqualTo("flags", 3);
        if (filter && location != null) {
            query.whereEqualTo("location", location);
        }

        ParseQuery<Selfie> querya = new ParseQuery<Selfie>(Selfie.class);
        querya.whereEqualTo("from", ParseUser.getCurrentUser());
        querya.whereGreaterThan("createdAt",newDate);
        querya.whereNotContainedIn("complaint", Arrays.asList(removed));
        if (filter && location != null) {
            querya.whereEqualTo("location", location);
        }

        List<ParseQuery<Selfie>> queries = new ArrayList<ParseQuery<Selfie>>();
        queries.add(query);
        queries.add(querya);

        ParseQuery<Selfie> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground(new SelfieFindCallback() {
            @Override
            protected void doneOnce(List<Selfie> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    // Load Object
    public static void loadObjectInBackground(String objectId, final FindCallback<Selfie> callback) {

        ParseQuery<Selfie> query = new ParseQuery<Selfie>(Selfie.class);
        query.getInBackground(objectId, new SelfieGetCallback() {
            @Override
            protected void doneOnce(Selfie object, ParseException e) {
                List<Selfie> list = new ArrayList<Selfie>();
                if (e == null) {
                    list.add(object);
                }
                callback.done(list, e);
            }
        });
    }

    // Load Popular
    public static void loadPopularInBackground(Boolean filter, final FindCallback<Selfie> callback) {

        Date d = new Date();
        int time = (18 * 3600 * 1000);
        Date newDate = new Date(d.getTime() - (time));

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");

        ParseQuery<Selfie> query = new ParseQuery<Selfie>(Selfie.class);
        query.whereLessThanOrEqualTo("flags", 3);
        query.whereGreaterThan("likes", 0);
        query.whereGreaterThan("createdAt", newDate);
        query.orderByDescending("likes");

        if (filter && location != null) {
            query.whereEqualTo("location", location);
        }

        query.findInBackground(new SelfieFindCallback() {
            @Override
            protected void doneOnce(List<Selfie> objects, ParseException e) {

                callback.done(objects, e);
            }
        });
    }

    // Load User Photos
    public static void loadUserPhotosInBackground(final FindCallback<Selfie> callback) {

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject location = currentUser.getParseObject("location");
        Date d = new Date();
        int time = (4 * 3600 * 1000);
        Date newDate = new Date(d.getTime() - (time));
        String[] removed = {"Admin", "Delete"};

        ParseQuery<Selfie> query = new ParseQuery<Selfie>(Selfie.class);
        query.whereEqualTo("from", ParseUser.getCurrentUser());
        query.whereGreaterThan("likes", -4);
        query.whereLessThanOrEqualTo("flags", 3);

        ParseQuery<Selfie> querya = new ParseQuery<Selfie>(Selfie.class);
        querya.whereEqualTo("from", ParseUser.getCurrentUser());
        querya.whereGreaterThan("createdAt",newDate);
        querya.whereNotContainedIn("complaint", Arrays.asList(removed));

        List<ParseQuery<Selfie>> aqueries = new ArrayList<ParseQuery<Selfie>>();
        aqueries.add(query);
        aqueries.add(querya);

        ParseQuery<Selfie> mainQuery = ParseQuery.or(aqueries);
        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground(new SelfieFindCallback() {
            @Override
            protected void doneOnce(List<Selfie> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    public void addVote(Boolean upvote, Boolean change){
        try {
            if (upvote) {
                this.increment("likes");
                if (change) {
                    this.increment("likes");
                }
            }else{
                int like = Integer.parseInt(this.getLikes());
                like = like - 1;
                if (change) {
                    like = like - 1;
                }
                this.put("likes",like);
            }
            this.saveInBackground();
        } catch (Exception e) {
            System.err.println("IndexOutOfBoundsException: " + e.getMessage());
        }
        Vote avote = getVote();
        avote.saveVoteData(upvote, change, this);
    }

    public void addFlag(Boolean increment, Selfie selfie, String reason){
        try {
            if (increment){

                    selfie.increment("flags");
                    selfie.addAllUnique("complaint", Arrays.asList(reason));

                if (ParseUser.getCurrentUser() != null){
                    selfie.addAllUnique("complainer", Arrays.asList(ParseUser.getCurrentUser().getObjectId()));
                }

                if (reason.equals("Delete")){
                    selfie.increment("flags");
                    selfie.increment("flags");
                    selfie.increment("flags");
                    selfie.increment("flags");
                    selfie.increment("flags");
                }
            }else{
            }
            selfie.saveInBackground();
        } catch (Exception e) {
            System.err.println("IndexOutOfBoundsException: " + e.getMessage());
        }
    }

    public void addVisit(){
        this.increment("visits");
        this.saveInBackground();
    }

    public Boolean didUserCreate(){

        final ParseUser user = ParseUser.getCurrentUser();
        if(this.getFrom().getObjectId().equals(user.getObjectId())){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    public void setBmp(Bitmap bitmap) { this.bmp = bitmap; }

    public Bitmap getBmp(){ return this.bmp; }

    public Vote getVote(){
        if (vote == null)
            vote = new Vote();
        return vote;
    }

    public void setFis(FileInputStream fis) { this.fis = fis; }

    public FileInputStream getFis(){ return this.fis; }

    // Class
    public ParseUser getFrom() {
        return getParseUser("from");
    }

    public List<String> getHashtags() {
        return getList("hashtags");
    }

    public String getLocation() {
        return String.valueOf(getParseObject("location"));
    }

    public String getLikes() {
        return Integer.toString(getInt("likes"));
    }

    public String getVisits() {
        int count = getInt("visits");
        return format(count);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public ParseFile getVideo() {
        return getParseFile("video");
    }

    public void setNewSelfie(ParseFile file, ParseUser user, ParseObject location, ArrayList hashtags, ParseFile videoFile){
        put("likes", 0);
        put("flags", 0);
        put("visits", 1);
        put("from", user);
        addAllUnique("hashtags", hashtags);
        put("image", file);
        put("location", location);
        if (videoFile != null){
            put("video", videoFile);
        }
    }


    // Format long values
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
