package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class SuggestionsFragment extends Fragment {
    private View v;
    onActionListener mCallback;

    // Footer
    private Button leftButton;
    private ImageButton rightButton;

    private SuggestionsAdapter suggestionAdapter;
    private HashtagsGridView  gv;
    private SuggestionsAdapter suggestionAdapter1;
    private HashtagsGridView  gv1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {

        v = inflater.inflate(R.layout.fragment_suggestions, parent, false);

        // Background Color
        int[] rainbow = getActivity().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);
        int picker1 = rn.nextInt(rainbow.length);


        TextView header = (TextView) v.findViewById(R.id.header);
        header.setBackgroundColor(rainbow[picker]);
        TextView header1 = (TextView) v.findViewById(R.id.header1);
        header1.setBackgroundColor(rainbow[picker1]);

        // Footer //

        // Subclass of ParseQueryAdapter
        gv = (HashtagsGridView) v.findViewById(R.id.gridview);
        ArrayList<Hashtag> entries = new ArrayList<Hashtag>();
        suggestionAdapter = new SuggestionsAdapter(getActivity(), entries, true);
        gv.setAdapter(suggestionAdapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Hashtag hashtag = (Hashtag) gv.getAdapter().getItem(position);
                Log.d("hashtag clicked", hashtag.getName());
                mCallback.offSuggestions(hashtag.getName());

            }
        });

        gv1 = (HashtagsGridView) v.findViewById(R.id.gridview1);
        ArrayList<Hashtag> entries1 = new ArrayList<Hashtag>();
        suggestionAdapter1 = new SuggestionsAdapter(getActivity(), entries1, true);
        gv1.setAdapter(suggestionAdapter1);
        gv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Hashtag hashtag = (Hashtag) gv1.getAdapter().getItem(position);
                Log.d("hashtag clicked", hashtag.getName());
                mCallback.offSuggestions(hashtag.getName());

            }
        });

        Button exitButton = (Button) v.findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.offSuggestions(null);
            }
        });

        trendingHashtags();
        suggestedHashtags();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
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


    public void trendingHashtags() {

        Tag.popularInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> tags, ParseException e) {

                Hashtag hash = new Hashtag();
                List<Hashtag> hashtags = hash.getCleanHashtags(tags);
                Collections.sort(hashtags, new MyComparator());
                if (hashtags != null) {
                    if (suggestionAdapter1 != null) {
                        suggestionAdapter1.setNotifyOnChange(true);
                        suggestionAdapter1.clear();
                        int max = hashtags.size() > 8 ? 8 : hashtags.size();
                        for (int i = 0; i < max; i++)
                            suggestionAdapter1.add(hashtags.get(i));

                    }
                }
            }
        });
    }

    public void suggestedHashtags() {

        Trending.trendingInBackground(new FindCallback<Trending>() {
            @Override
            public void done(List<Trending> trendings, ParseException e) {

                List<Hashtag> hashtags = Trending.getCleanHashtags(trendings);
                Collections.sort(hashtags, new MyComparator());
                if (hashtags != null) {
                    if (suggestionAdapter != null) {
                        suggestionAdapter.setNotifyOnChange(true);
                        suggestionAdapter.clear();
                        int max = hashtags.size() > 8 ? 8 : hashtags.size();
                        for (int i = 0; i < max; i++)
                            suggestionAdapter.add(hashtags.get(i));

                    }
                }
            }
        });
    }

    public interface onActionListener {
        public void offSuggestions(String hashtag);
    }

    public class MyComparator implements Comparator<Hashtag> {
        @Override
        public int compare(Hashtag o1, Hashtag o2) {
            if (o1.getName().length() > o2.getName().length()) {
                return 1;
            } else if (o1.getName().length() < o2.getName().length()) {
                return -1;
            }
            return 0;
        }
    }

    public View GetViewByPosition(int position) {
        int firstPosition = gv.getFirstVisiblePosition();
        int lastPosition = gv.getLastVisiblePosition();

        if ((position < firstPosition) || (position > lastPosition))
            return null;

        return gv.getChildAt(position - firstPosition);
    }
}

