package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by griffinanderson on 8/12/15.
 */
public class SelfieVote {

    private Context context;
    private ImageButton selfieUpvote;
    private ImageButton selfieDownvote;
    private TextView selfieVote;
    private RelativeLayout selfieUpvoteBackground;
    private RelativeLayout selfieDownvoteBackground;

    // Current Item
    private Clickable clickable;
    private int colorb;
    private int colorc;
    private Selfie voteSelfie;
    private String voted;

    public SelfieVote(Context context, ImageButton selfieUpvote, ImageButton selfieDownvote,
                      TextView selfieVote, RelativeLayout selfieUpvoteBackground, RelativeLayout selfieDownvoteBackground) {
        this.context = context;
        this.selfieVote = selfieVote;
        this.selfieDownvote = selfieDownvote;
        this.selfieUpvote = selfieUpvote;
        this.selfieDownvoteBackground = selfieDownvoteBackground;
        this.selfieUpvoteBackground = selfieUpvoteBackground;
        this.selfieUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapVote(Boolean.TRUE);
            }
        });
        this.selfieDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapVote(Boolean.FALSE);
            }
        });
        clickable = new Clickable();
        int[] rainbow = context.getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);
        colorb = rainbow[picker];
        int pickera = rn.nextInt(rainbow.length);
        colorc = rainbow[pickera];
        ((GradientDrawable)this.selfieUpvoteBackground.getBackground()).setColor(colorc);
        ((GradientDrawable)this.selfieDownvoteBackground.getBackground()).setColor(colorc);

    }

    public void resetWithSelfie(Selfie selfie){
        voted = null;
        voteSelfie = selfie;
        voted = null;
        selfieUpvoteBackground.setVisibility(View.INVISIBLE);
        selfieDownvoteBackground.setVisibility(View.INVISIBLE);
        selfieVote.setTextColor(colorb);
        Drawable upvote = context.getResources().getDrawable(R.drawable.icon_upvote);
        upvote.setColorFilter(colorb, PorterDuff.Mode.SRC_ATOP);
        selfieUpvote.setImageDrawable(upvote);
        Drawable downvote = context.getResources().getDrawable(R.drawable.icon_downvote);
        downvote.setColorFilter(colorb, PorterDuff.Mode.SRC_ATOP);
        selfieDownvote.setImageDrawable(downvote);

        if (clickable.canView(voteSelfie.getObjectId())){
            clickable.addView(selfie.getObjectId());
            voteSelfie.addVisit();
        }

        startVote();
    }

    private void startVote(){
        if (clickable.isPositiveVote(voteSelfie.getObjectId())){
            Drawable upvote = context.getResources().getDrawable(R.drawable.icon_upvote);
            upvote.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            selfieUpvote.setImageDrawable(upvote);
            selfieUpvoteBackground.setVisibility(View.VISIBLE);
            voted = "up";
            selfieVote.setTextColor(Color.WHITE);
        }else if (clickable.isNegativeVote(voteSelfie.getObjectId())){
            Drawable downvote = context.getResources().getDrawable(R.drawable.icon_downvote);
            downvote.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            selfieDownvote.setImageDrawable(downvote);
            selfieDownvoteBackground.setVisibility(View.VISIBLE);
            voted = "down";
            selfieVote.setTextColor(Color.WHITE);
        }else{
        }
    }


    private void tapVote(Boolean upvote){

        if(upvote && voted == null) {
            // New Vote Up
            Drawable aupvote = context.getResources().getDrawable(R.drawable.icon_upvote);
            aupvote.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            selfieUpvote.setImageDrawable(aupvote);
            voted = "up";
            voteSelfie.addVote(Boolean.TRUE, Boolean.FALSE);
            selfieVote.setTextColor(Color.WHITE);
            selfieUpvoteBackground.setVisibility(View.VISIBLE);
        }else if(upvote && voted.equals("down")){
            // Change Vote From Down to Up
            Drawable aupvote = context.getResources().getDrawable(R.drawable.icon_upvote);
            aupvote.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            selfieUpvote.setImageDrawable(aupvote);
            Drawable downvote = context.getResources().getDrawable(R.drawable.icon_downvote);
            downvote.setColorFilter(colorb, PorterDuff.Mode.SRC_ATOP);
            selfieDownvote.setImageDrawable(downvote);
            voted = "up";
            voteSelfie.addVote(Boolean.TRUE, Boolean.TRUE);
            selfieUpvoteBackground.setVisibility(View.VISIBLE);
            selfieDownvoteBackground.setVisibility(View.INVISIBLE);
        }else if(upvote && voted.equals("up")){
            // Change Vote From Down to Up
            Drawable aupvote = context.getResources().getDrawable(R.drawable.icon_upvote);
            aupvote.setColorFilter(colorb, PorterDuff.Mode.SRC_ATOP);
            selfieUpvote.setImageDrawable(aupvote);
            voted = null;
            voteSelfie.addVote(Boolean.FALSE, Boolean.FALSE);
            selfieVote.setTextColor(colorb);
            selfieUpvoteBackground.setVisibility(View.INVISIBLE);
        }else if(!(upvote) && voted == null){
            // New Vote Down
            Drawable downvote = context.getResources().getDrawable(R.drawable.icon_downvote);
            downvote.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            selfieDownvote.setImageDrawable(downvote);
            voted = "down";
            voteSelfie.addVote(Boolean.FALSE, Boolean.FALSE);
            selfieVote.setTextColor(Color.WHITE);
            selfieDownvoteBackground.setVisibility(View.VISIBLE);
        }else if(!(upvote) && voted.equals("up")){
            // Change Vote From Up to Down
            Drawable downvote = context.getResources().getDrawable(R.drawable.icon_downvote);
            downvote.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            selfieDownvote.setImageDrawable(downvote);
            Drawable aupvote = context.getResources().getDrawable(R.drawable.icon_upvote);
            aupvote.setColorFilter(colorb, PorterDuff.Mode.SRC_ATOP);
            selfieUpvote.setImageDrawable(aupvote);
            voted = "down";
            voteSelfie.addVote(Boolean.FALSE, Boolean.TRUE);
            selfieUpvoteBackground.setVisibility(View.INVISIBLE);
            selfieDownvoteBackground.setVisibility(View.VISIBLE);
        }else if(!(upvote) && voted.equals("down")){
            // Change Vote From Up to Down
            Drawable downvote = context.getResources().getDrawable(R.drawable.icon_downvote);
            downvote.setColorFilter(colorb, PorterDuff.Mode.SRC_ATOP);
            selfieDownvote.setImageDrawable(downvote);
            voted = null;
            voteSelfie.addVote(Boolean.TRUE, Boolean.FALSE);
            selfieVote.setTextColor(colorb);
            selfieDownvoteBackground.setVisibility(View.INVISIBLE);
        }else{
        }
        clickable.vote(upvote, voteSelfie.getObjectId());
        selfieVote.setText(voteSelfie.getLikes());
    }
}
