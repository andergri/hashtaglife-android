package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;
import java.util.Random;


public class SecondFragment extends Fragment {

    private View v;
    onActionListener mCallback;

    // Footer
    private Button gamingButton;
    private ImageButton rightButton;
    private TextView gamingQuestionText;

    private HashtagsAdapter hashtagAdapter;
    //private ListView lv;
    private int myphotoColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {

        v = inflater.inflate(R.layout.fragment_second, parent, false);

        // Background Color
        int[] rainbow = getActivity().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);

        myphotoColor = rainbow[picker];


        RelativeLayout gaming = (RelativeLayout) v.findViewById(R.id.count);
        //Button gamingLevel = (Button) v.findViewById(R.id.gamingLevel);
        gamingQuestionText = (TextView) v.findViewById(R.id.gamingQuestionText);
        Button gamingQuestion = (Button) v.findViewById(R.id.gamingQuestion);
        //ImageView gamingThumbImage = (ImageView) v.findViewById(R.id.thumb);
        gamingButton = (Button) v.findViewById(R.id.gamingCount);
        //gamingLevel.setText("Not Ranked");
        //gamingThumbImage.setColorFilter(rainbow[picker]);
        gamingQuestion.setTextColor(rainbow[picker]);
        gamingButton.setTextColor(rainbow[picker]);
        //gaming.addView(new HalfCircleView(getActivity()), 0);
        gamingQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeGamingMessage();
            }
        });
        gamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeGamingMessage();
            }
        });
        gaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeGamingMessage();
            }
        });


        // Footer //
        RelativeLayout bottomBar = (RelativeLayout) v.findViewById(R.id.bottomBar);
        bottomBar.setBackgroundColor(rainbow[picker]);

        rightButton = (ImageButton) v.findViewById(R.id.rightButton);
        rightButton.setColorFilter(rainbow[picker]);
        ((GradientDrawable)rightButton.getBackground()).setStroke(7, rainbow[picker]);
/**        gamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCountSelected();
            }
        });**/
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSwipeSelected();
            }
        });

        // Subclass of ParseQueryAdapter
        /**
        lv = (ListView) v.findViewById(R.id.listview);
        ArrayList<Hashtag> entries = new ArrayList<Hashtag>();
        hashtagAdapter = new HashtagsAdapter(getActivity(), entries, true);
        lv.setAdapter(hashtagAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Hashtag hashtag = (Hashtag) lv.getAdapter().getItem(position);
                showPhotos(hashtag, position, view);
            }
        });**/

        //trendingHashtags();

        getNumberUserLikes();

        ImageView shareImage = (ImageView) v.findViewById(R.id.share1);
        ImageView safetyImage = (ImageView) v.findViewById(R.id.safety1);
        ImageView schoolImage = (ImageView) v.findViewById(R.id.school1);
        shareImage.setColorFilter(rainbow[picker]);
        safetyImage.setColorFilter(rainbow[picker]);
        schoolImage.setColorFilter(rainbow[picker]);

        // Photos
        TextView photosText = (TextView) v.findViewById(R.id.photos3);
        ImageView photosImage = (ImageView) v.findViewById(R.id.photos1);
        photosText.setText("Photos");
        photosImage.setImageResource(R.drawable.design_profile);
        photosText.setTextColor(rainbow[picker]);
        photosImage.setColorFilter(rainbow[picker]);
        RelativeLayout myphoto = (RelativeLayout) v.findViewById(R.id.photos);
        myphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.showSelfie("my photos", myphotoColor, false);
            }
        });

        // School //
        RelativeLayout school = (RelativeLayout) v.findViewById(R.id.school);
        school.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onLocationSelected();
            }
        });
        // Safety //
        RelativeLayout safety = (RelativeLayout) v.findViewById(R.id.safety);
        safety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hashtaglifeapp.com/community"));
                startActivity(browserIntent);
            }
        });
        // Share //
        RelativeLayout share = (RelativeLayout) v.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the #life app on apple https://itunes.apple.com/us/app/life-hashtag-your-life/id904884186, or android ");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });



        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("Container", "size sec "+ v.getHeight() + " w"+ v.getWidth());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void fadeGamingMessage(){

        gamingQuestionText.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gamingQuestionText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        gamingQuestionText.setAnimation(animation);
        gamingQuestionText.animate().setStartDelay(8000);
    }

/**
    public void trendingHashtags() {

        Trending.trendingInBackground(new FindCallback<Trending>() {
            @Override
            public void done(List<Trending> trendings, ParseException e) {

                List<Hashtag> hashtags = Trending.getCleanHashtags(trendings);

                if (hashtags != null) {
                    if (hashtagAdapter != null) {
                        hashtagAdapter.setNotifyOnChange(true);
                        hashtagAdapter.clear();
                        if (hashtags.size() > 2) {
                            for (int i = 0; i < 3; i++)Ft
                                hashtagAdapter.add(hashtags.get(i));
                        }
                    }
                }
            }
        });
    }**/


    // Addiontal methods


    private void getNumberUserLikes(){

        if(ParseUser.getCurrentUser() != null){

            ParseQuery<Selfie> query = new ParseQuery<Selfie>(Selfie.class);
            query.whereEqualTo("from", ParseUser.getCurrentUser());
            query.whereGreaterThan("flags", -4);
            query.findInBackground(new FindCallback<Selfie>() {
                @Override
                public void done(List<Selfie> selfies, ParseException e) {
                    if(selfies != null){
                        int acount = 0;
                        for(int i = 0; i < selfies.size(); i++){
                            acount += selfies.get(i).getInt("likes");
                        }
                        gamingButton.setText(String.valueOf(acount));
                        //setLevel(acount);
                        //RelativeLayout gaming = (RelativeLayout) v.findViewById(R.id.count);
                        //gaming.addView(new HalfCircleView(getActivity(), myphotoColor, acount), 1);
                    }else{
                        gamingButton.setText("0");
                    }
                }
            });
        }else{
            gamingButton.setText("0");
        }
    }

    private void showPhotos(Hashtag hashtag, int position, View view) {
        String clickedHashtag;
        int clickedColor;

        clickedHashtag = hashtag.getNameWithHashtag();
        //clickedColor = ((ColorDrawable) view.getBackground()).getColor();
        mCallback.showSelfie(clickedHashtag, myphotoColor, false);
    }

    public interface onActionListener {
        public void onCountSelected();
        public void onSwipeSelected();
        public void onLocationSelected();
        public void showSelfie(String hashtag, int color, boolean filtered);
    }

    public class HalfCircleView extends View {

        private int color = 0;
        private int votes = 0;

        public HalfCircleView(Context context) {
            super(context);
        }

        public HalfCircleView(Context context, int acolor,  int avotes) {
            super(context);
            color = acolor;
            votes = avotes;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            float width = (float)getWidth();
            float height = (float)getHeight();
            float radius = 100;

            Path path = new Path();
            path.addCircle(width/2,
                    height/2, radius,
                    Path.Direction.CW);

            Paint paint = new Paint();
            if (color != 0) {
                paint.setColor(color);
            }else{
                paint.setColor(getResources().getColor(R.color.text_gray));
            }
            paint.setStrokeWidth(pxToDp(24));
            paint.setStyle(Paint.Style.FILL);

            float center_x, center_y;
            final RectF oval = new RectF();

            paint.setStyle(Paint.Style.STROKE);
            center_x = width/2;
            center_y = height/2;

            oval.set(center_x - radius,
                    center_y - radius,
                    center_x + radius,
                    center_y + radius);
            if (color == 0) {
                canvas.drawArc(oval, 135, 270, false, paint);
            }else{

                int numDaysIn = getDaysSinceJoined();
                float totalStatus = (34 * numDaysIn);
                int distance = (int)((votes / totalStatus) * 270.00);
                Log.d("distance",String.valueOf(distance));
                distance = Math.max(distance, 2);
                distance = Math.min(distance, 270);
                canvas.drawArc(oval, 135, distance, false, paint);
            }
        }

        public int pxToDp(int px) {
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            return dp;
        }
    }

    private int getDaysSinceJoined(){

        Date startDate = ParseUser.getCurrentUser().getCreatedAt();
        Date endDate = new Date();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24);
        return (int) diffDays;
    }
/**
    private void setLevel(int pts){
        Button gamingLevel = (Button) v.findViewById(R.id.gamingLevel);
        int numDaysIn = getDaysSinceJoined();
        if ((34 * numDaysIn) < pts) {
            gamingLevel.setText("Top 1%");
        }else if((21 * numDaysIn) < pts) {
            gamingLevel.setText("Top 3%");
        }else if((13 * numDaysIn) < pts) {
            gamingLevel.setText("Top 5%");
        }else if((8 * numDaysIn) < pts) {
            gamingLevel.setText("Top 10%");
        }else if((5 * numDaysIn) < pts) {
            gamingLevel.setText("Top 25%");
        }else if((1 * numDaysIn) < pts) {
            gamingLevel.setText("Top 50%");
        }else{
        }
    }**/
}
