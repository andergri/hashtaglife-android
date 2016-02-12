package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by griffinanderson on 8/7/15.
 */
public class CurvedText  extends View {
    private static final String Text = "hold for video";
    private Path myArc;
    private Paint mPaintText;

    public CurvedText(Context context) {
        super(context);
        //create Path object
        myArc = new Path();
        //create RectF Object
        RectF oval = new RectF(0, 0, pxToDp(230),pxToDp(230));
        //add Arc in Path with start angle -180 and sweep angle 200
        myArc.addArc(oval, -125, 110);
        //create paint object
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        //set style
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        //set color
        mPaintText.setColor(Color.WHITE);
        //set text Size
        mPaintText.setTextSize(21f);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Draw Text on Canvas
        canvas.drawTextOnPath(Text, myArc, 0, pxToDp(20), mPaintText);
        invalidate();
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}