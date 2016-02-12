package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Created by griffinanderson on 7/22/15.
 */
public class OnSwipeListener implements OnTouchListener {

    public final GestureDetector gestureDetector;
    private final int bottomDistance;
    private boolean usernameListOpen = false;

    public OnSwipeListener (Context ctx, int bottomDistance){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        this.bottomDistance = bottomDistance;
    }

    public void usernameListOpen(Boolean listOpen){
        usernameListOpen = listOpen;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 10;
        private static final int SWIPE_VELOCITY_THRESHOLD = 10;

        @Override
        public boolean onDown(MotionEvent e) {

            if (usernameListOpen){
                if (e.getY() > bottomDistance){
                    return true;
                }else{
                    return false;
                }
            }else{
                if (e.getY() > 80 && e.getY() < bottomDistance)  {
                    return true;
                }else{
                    return false;
                }
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            onTap();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                } else{
                    onTap();
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
    public void onTap() {
    }
}