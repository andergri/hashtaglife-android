package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 12/12/14.
 */
public class CountFragment  extends Fragment {


    private View v;
    onActionListener mCallback;
    private ImageButton countExit;
    private ImageButton countShare;
    private Button countCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {
        v = inflater.inflate(R.layout.fragment_count, parent, false);

        countExit = ((ImageButton) v.findViewById(R.id.countExit));
        countShare = ((ImageButton) v.findViewById(R.id.countShare));
        countCount = ((Button) v.findViewById(R.id.countCount));

        countExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onHideFragment();
            }
        });

        int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);

        v.setBackgroundColor(rainbow[picker]);
        countCount.setTextColor(rainbow[picker]);

        Drawable dAccept = getResources().getDrawable(R.drawable.icon_outgoing);
        dAccept.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        countShare.setImageDrawable(dAccept);
        countShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the #life app on apple https://itunes.apple.com/us/app/life-hashtag-your-life/id904884186, or android ");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        getNumberUserLikes();

        return v;
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
                        countCount.setText(String.valueOf(acount));
                    }else{
                        countCount.setText("0");
                    }
                }
            });
        }else{
            countCount.setText("0");
        }
    }

    public interface onActionListener {
        public void onHideFragment();
    }
}