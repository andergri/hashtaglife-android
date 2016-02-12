package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class WelcomeActivity extends Activity {

    private TextView extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Log in button click handler
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        });

        // Sign up button click handler
        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Starts an intent for the sign up activity
                startActivity(new Intent(WelcomeActivity.this, RulesActivity.class));
            }
        });

        extras = (TextView) findViewById(R.id.textview_3);
        makeClickable(extras, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("welcome a", String.valueOf(v.getTag()));
                String place = String.valueOf(v.getTag());
                Log.d("welcome", place);
                if (place.equals("Terms of Service")){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hashtaglifeapp.com/terms"));
                    startActivity(browserIntent);
                }else if(place.equals("Privacy Policy")){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hashtaglifeapp.com/privacy"));
                    startActivity(browserIntent);
                }
            }
        });


    }

    public void makeClickable(TextView tv, View.OnClickListener mListenter) {


        SpannableString linkA = makeLinkSpan("Terms of Service", mListenter);
        SpannableString linkB = makeLinkSpan("Privacy Policy", mListenter);
        tv.append(linkA);
        tv.append(" and ");
        tv.append(linkB);
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
            Log.d("welcome b", String.valueOf(mText));
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
