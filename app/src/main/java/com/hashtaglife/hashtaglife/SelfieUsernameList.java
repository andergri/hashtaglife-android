package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by griffinanderson on 8/12/15.
 */
public class SelfieUsernameList {

    // Visual
    private Context context;
    private ImageView selfieImage;
    private SurfaceView vidSurface;
    private TextView slideText;
    private ImageButton slideArrow;
    private ImageButton slideArrowUp;
    private RelativeLayout animateOverlay;
    public float distance;

    // Adapter
    public UsernameAdapter usernameAdapter;
    private List<Vote> entries;

    // Current Item
    private Selfie usernameListSelfie;
    public Boolean isUsernameListOpen = false;


    /** Constructor **/
    public SelfieUsernameList(Context context, ImageView selfieImage, SurfaceView vidSurface,
                              TextView slideText, ImageButton slideArrow, ImageButton slideArrowUp,
                                RelativeLayout animateOverlay) {
        this.context = context;
        this.selfieImage = selfieImage;
        this.vidSurface = vidSurface;
        this.slideText = slideText;
        this.slideArrow = slideArrow;
        this.slideArrowUp = slideArrowUp;
        this.animateOverlay = animateOverlay;
        this.distance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 225, context.getResources().getDisplayMetrics());

        entries = new ArrayList<Vote>();
        usernameAdapter = new UsernameAdapter(context, entries);

        startAnimation();
    }

    /** set each selife with this **/
    public void resetWithSelfie(Selfie selfie){
        isUsernameListOpen = false;
        usernameListSelfie = selfie;
        if (usernameListSelfie.didUserCreate()){
            // Show
            slideText.setVisibility(View.VISIBLE);
            slideArrow.setVisibility(View.VISIBLE);
            slideArrowUp.setVisibility(View.INVISIBLE);
        }else{
            slideText.setVisibility(View.INVISIBLE);
            slideArrow.setVisibility(View.INVISIBLE);
            slideArrowUp.setVisibility(View.INVISIBLE);
        }
    }

    public boolean didUserCreate(){
        if (usernameListSelfie == null)
            return false;
        return usernameListSelfie.didUserCreate();
    }

    public boolean isUsernameListOpen(){
        return isUsernameListOpen;
    }

    public void slideUsernameList(){

        if (usernameListSelfie != null && usernameListSelfie.didUserCreate()) {
            if (!isUsernameListOpen) {
                entries.clear();
                Vote vote = usernameListSelfie.getVote();
                vote.context = context;
                vote.getFullVoteListInBackground(usernameListSelfie, new FindCallback<Vote>() {
                    @Override
                    public void done(List<Vote> votes, ParseException e) {
                        entries.addAll(votes);
                        usernameAdapter.notifyDataSetChanged();
                    }
                });
                // here
                selfieImage.animate().translationY(animateOverlay.getHeight() - distance);
                vidSurface.animate().translationY(animateOverlay.getHeight() - distance);
                slideText.setVisibility(View.INVISIBLE);
                slideArrow.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        slideArrowUp.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            } else {
                selfieImage.animate().translationY(0);
                vidSurface.animate().translationY(0);
                entries.clear();
                usernameAdapter.notifyDataSetChanged();
                slideArrowUp.setVisibility(View.INVISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        slideText.setVisibility(View.VISIBLE);
                        slideArrow.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            }
        }
        isUsernameListOpen = !isUsernameListOpen;
    }

    private void startAnimation(){

        // Animation
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation = AnimationUtils.loadAnimation(context, R.anim.slide);
                animation.setAnimationListener(this);
                animateOverlay.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animateOverlay.startAnimation(animation);
    }
}
