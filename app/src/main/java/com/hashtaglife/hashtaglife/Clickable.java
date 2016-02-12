package com.hashtaglife.hashtaglife;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by griffinanderson on 12/11/14.
 */
public class Clickable {

    private List<String> positivevotes = new ArrayList<String>();;
    private List<String> negativevotes = new ArrayList<String>();;
    private List<String> views = new ArrayList<String>();;

// Votes

    private Boolean canVote(String selfieId){
        if ( positivevotes.contains(selfieId) || negativevotes.contains(selfieId) ){
            return Boolean.FALSE;
        }else{
            return Boolean.TRUE;
        }
    }

    public Boolean isPositiveVote(String selfieId){
        return positivevotes.contains(selfieId);
    }

    public Boolean isNegativeVote(String selfieId){
        return negativevotes.contains(selfieId);
    }

    private void addPositiveVote(String selfieId){
        if (canVote(selfieId)){
            positivevotes.add(selfieId);
        }
        Log.d("vote", String.valueOf(positivevotes.size()));
    }
    private void addNegativeVote(String selfieId){
        if (canVote(selfieId)){
            negativevotes.add(selfieId);
        }
    }

    private void removeVote(String selfieId){
        if (!canVote(selfieId)){
            if (isPositiveVote(selfieId)){
                positivevotes.remove(selfieId);
            }
            if (isNegativeVote(selfieId)){
                negativevotes.remove(selfieId);
            }
        }
    }

    public void vote(Boolean upvote, String selfieId){
        if (!canVote(selfieId)){
            if (isPositiveVote(selfieId)){
                positivevotes.remove(selfieId);
            }
            if (isNegativeVote(selfieId)){
                negativevotes.remove(selfieId);
            }
        }else {
            if (upvote) {
                addPositiveVote(selfieId);
            } else {
                addNegativeVote(selfieId);
            }
        }
    }


// Views

    public Boolean canView(String selfieId){
        return !views.contains(selfieId);
    }

    public void addView(String selfieId){
        if (canView(selfieId)){
            views.add(selfieId);
        }
    }

}
