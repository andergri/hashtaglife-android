package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 7/17/15.
 */
@ParseClassName("Vote")
public class Vote extends ParseObject {

    public Vote() {
        // A default constructor is required.
    }

    public Context context;
    /**
     * Wraps a FindCallback so that we can use the CACHE_THEN_NETWORK caching
     * policy, but only call the callback once, with the first data available.
     */
    private abstract class VoteFindCallback implements FindCallback<Vote> {
        private boolean isCachedResult = true;
        private boolean calledCallback = false;

        @Override
        public void done(List<Vote> objects, ParseException e) {
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
        protected abstract void doneOnce(List<Vote> objects, ParseException e);
    }

    private void removeVote(Vote vote){
        vote.deleteInBackground();
    }

    private void updateVote(Vote vote , boolean reaction, Selfie selfie){
        vote.put("voter", ParseUser.getCurrentUser());
        vote.put("voterName", ParseUser.getCurrentUser().getUsername());
        vote.put("voterReaction", reaction);
        vote.put("selfie", selfie);
        vote.put("poster", selfie.getFrom());
        vote.put("notifyAttempted", false);
        vote.saveInBackground(new SaveCallback(){

            @Override
            public void done(ParseException e){
                if (e == null)
                {
                    Log.d("Parse", "User save in background worked... but not really.");
                } else
                {
                    Log.d("Parse", "failed save in background with following message:");
                    Log.d("Parse", e.getMessage());
                }
            }
        });
    }

    public Vote createFakeVote(String fakeUsername, boolean reaction, Selfie selfie){
        Vote fakeVote = new Vote();
        fakeVote.put("voterName", fakeUsername);
        fakeVote.put("voterReaction", reaction);
        fakeVote.put("selfie", selfie);
        fakeVote.put("poster", selfie.getFrom());
        fakeVote.put("notifyAttempted", true);
        fakeVote.saveInBackground();
        return fakeVote;
    }

    public String createFakeUsername(){
        String[] usernames =  context.getResources().getStringArray(R.array.usernamePicker);
        Random rn = new Random();
        int picker = rn.nextInt(usernames.length);
        int pickera = rn.nextInt(usernames.length);
        return usernames[picker] + usernames[pickera];
    }

    // Search
    public void saveVoteData(final boolean reaction, final boolean change, final Selfie selfie) {

        final Vote athis = new Vote();
        checkForOldVotesInBackground(selfie, new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                switch (votes.size()) {
                    case 0:
                        updateVote(athis, reaction, selfie);
                        break;
                    case 1:
                        Vote avote = votes.get(0);
                        if (change){
                            updateVote(avote, reaction, selfie);
                        }else if(avote.getVoterReaction() == reaction) {
                            break;
                        }else{
                            removeVote(avote);
                        }
                        break;
                    default:
                        Log.d("Vote Object","too many returned results");
                        break;
                }
            }
        });
    }

    // CheckForOldVotes
    private void checkForOldVotesInBackground(Selfie selfie, final FindCallback<Vote> callback) {

        ParseQuery<Vote> query = new ParseQuery<Vote>(Vote.class);
        query.whereEqualTo("selfie", selfie);
        query.whereEqualTo("voter",ParseUser.getCurrentUser());
        query.findInBackground(new VoteFindCallback() {
            @Override
            protected void doneOnce(List<Vote> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    // GetList
    private void getAuthenticVoteListInBackground(Selfie selfie, final FindCallback<Vote> callback) {

        ParseQuery<Vote> query = new ParseQuery<Vote>(Vote.class);
        query.whereEqualTo("selfie", selfie);
        query.findInBackground(new VoteFindCallback() {
            @Override
            protected void doneOnce(List<Vote> objects, ParseException e) {
                callback.done(objects, e);
            }
        });
    }

    // GetList
    private List<Vote> getFakeVoteListInBackground(final Selfie selfie, List<Vote> authenticVotes) {

        long countSum = 0;
        for (int i = 0; i < authenticVotes.size(); i++) {
            boolean reaction = authenticVotes.get(i).getVoterReaction();
            if (reaction)
                countSum += 1;
            else
                countSum -= 1;
        }
        Log.d("Votes", "A.1 votes in a:"+countSum);
        Log.d("Votes", "A.1 votes in b:"+selfie.getLikes());
        long distance = Integer.valueOf(selfie.getLikes()) - countSum;
        Log.d("Votes", "A.1 votes in:"+distance);
        List<Vote> fakeVotes = authenticVotes;
        for (int i = 0; i < Math.abs(distance); i++) {
            String fakeUsername = createFakeUsername();
            boolean reaction = distance < 0.0 ? false : true;
            Vote fakeVote = createFakeVote(fakeUsername, reaction, selfie);
            fakeVotes.add(fakeVote);
        }
        return fakeVotes;
    }

    // Get List
    public void getFullVoteListInBackground(final Selfie selfie, final FindCallback<Vote> callback){

       getAuthenticVoteListInBackground(selfie, new FindCallback<Vote>() {
           @Override
           public void done(List<Vote> votes, ParseException e) {
               if (e == null) {
                   Log.d("Votes", "Real votes in:"+votes.size());
                   List<Vote> allVotes = getFakeVoteListInBackground(selfie, votes);
                   callback.done(allVotes, e);
               }else {
                   callback.done(votes, e);
               }
           }
       });
    }


    // Class

    public ParseUser getVoter() {
        return getParseUser("voter");
    }

    public String getVoterName() {
        return getString("voterName");
    }

    public boolean getVoterReaction() {
        return getBoolean("voterReaction");
    }

    public ParseObject getSelfie() {
        return getParseObject("selfie");
    }

    public ParseUser getPoster(){
        return getParseUser("poster");
    }

    public boolean getPosterNotified(){
        return getBoolean("posterNotified");
    }
}
