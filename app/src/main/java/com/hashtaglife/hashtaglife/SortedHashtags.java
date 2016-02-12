package com.hashtaglife.hashtaglife;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * Created by griffinanderson on 12/9/14.
 */
public class SortedHashtags {

    private void sortHashtags(Selfie selfie){
    }

    private boolean isMarqueed(String text, int textWidth, TextView tv) {
        Paint testPaint = new Paint();
        testPaint.set(tv.getPaint());
        boolean isMarquee = true;

        if (textWidth > 0) {
            int availableWidth = (int) (textWidth - tv.getPaddingLeft() - tv.getPaddingRight()-testPaint.measureText(text));
            isMarquee = false;
        }
        return isMarquee;
    }

    /**
    private int getHeightOfMultiLineText(String text, int textSize, int maxWidth) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        int index = 0;
        int linecount = 0;
        while (index < text.length()) {
            index += paint.breakText(text, index, text.length, true, maxWidth, null);

            linecount++;
        }

        Rect bounds = new Rect();
        paint.getTextBounds("Yy", 0, 2, bounds);
        // obtain space between lines
        double lineSpacing = Math.max(0, ((lineCount - 1) * bounds.height() * 0.25));

        return (int) Math.floor(lineSpacing + lineCount * bounds.height());
    }**/

    public void makeHashtagsClickable(TextView tv, List<String> hashtags, View.OnClickListener mListenter) {

        for(String hash : hashtags){

            SpannableString link = makeLinkSpan("#"+hash, mListenter);
            tv.append(link);
            tv.append(" ");
        }
        makeLinksFocusable(tv);
    }

/*
 * Methods used above.
 */

    private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
        SpannableString link = new SpannableString(text);

        link.setSpan(new ClickableString(listener, text), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        link.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        link.setSpan(new NoUnderlineSpan(), 0, text.length(),
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        link.setSpan(new RelativeSizeSpan(1.15f), 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return link;
    }

    private void makeLinksFocusable(TextView tv) {
        MovementMethod m = tv.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (tv.getLinksClickable()) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

/*
 * ClickableString class
 */

    private static class ClickableString extends ClickableSpan {
        private View.OnClickListener mListener;
        private CharSequence mText;
        public ClickableString(View.OnClickListener listener, CharSequence text) {
            mListener = listener;
            mText = text;
        }
        @Override
        public void onClick(View v) {
            v.setTag(mText);
            mListener.onClick(v);
        }
    }
    public class NoUnderlineSpan extends UnderlineSpan {
        public NoUnderlineSpan() {}

        public NoUnderlineSpan(Parcel src) {}

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }
    }

}
