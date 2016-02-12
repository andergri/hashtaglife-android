package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;


public class LocationFragment extends Fragment {

    private EditText locationTextView;
    private ListView locationListView;
    private ArrayList<String> locations = new ArrayList<String>();
    private LocationAdapter<String> adapter;

    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;
    enum Direction {LEFT, RIGHT;}
    onActionListener mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location, parent, false);

        locationListView = (ListView) v.findViewById(R.id.locations);
        locationTextView = ((EditText) v.findViewById(R.id.location_header));

        locations.add("College / University");
        locations.add("High School");
        locations.add("Other");

        adapter = new LocationAdapter<String>(getActivity().getApplicationContext(),
                R.layout.item_list_locations, R.id.text1, locations, this, locationTextView);
        locationListView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);


        locationTextView.setBackgroundColor(rainbow[picker]);
        locationTextView.setEnabled(false);
        locationTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        locationListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(locationTextView.getWindowToken(), 0);
                locationTextView.setCursorVisible(false);

                return false;
            }
        });


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

    @Override
    public void onStop(){
        super.onStop();

        /**
        try {
            ((MainActivity)getActivity()).locationButtonPressed(true);
        } catch (ParseException e) {
            Log.d("Location Problem", e.getMessage());
        }
        ((MainActivity)getActivity()).unlockButtons();
         **/
    }

    public interface onActionListener {
        public void onHideFragment();
    }
}