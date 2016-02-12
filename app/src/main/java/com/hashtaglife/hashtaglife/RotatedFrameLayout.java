package com.hashtaglife.hashtaglife;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by griffinanderson on 6/29/15.
 */
public class RotatedFrameLayout extends FrameLayout {

    public RotatedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RotatedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotatedFrameLayout(Context context) {
        super(context);
        init();
    }

    @SuppressLint("NewApi")
    private void init() {
        setPivotX(0);
        setPivotY(0);
        setRotation(90f);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setTranslationX(getMeasuredHeight());
    }
}
