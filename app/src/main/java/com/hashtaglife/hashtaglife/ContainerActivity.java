package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by griffinanderson on 4/15/15.
 */
@DeepLink("hashtaglife://hashtaglife.com/selfie/{selfieId}")
public class ContainerActivity extends Activity implements MainFragment.onActionListener,
        SecondFragment.onActionListener, CountFragment.onActionListener,
        CaptureFragment.onActionListener, PhotoFragment.onActionListener,
        NewPhotoFragment.onActionListener, SelfieFragment.onActionListener,
        LocationFragment.onActionListener, SuggestionsFragment.onActionListener,
        VideoCameraFragment.Contract, VideoCameraFragment.onActionListener
        {

    private MyAdapter mAdapter;
    private ViewPager mPager;
    private FrameLayout frame;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        mAdapter = new MyAdapter(getFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);

        /**
        ViewTreeObserver viewTreeObserver = mPager.getViewTreeObserver();
        viewTreeObserver
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT);

                        layoutParams.width = mPager.getHeight();
                        layoutParams.height = mPager.getWidth();

                        Log.d("Container ", "w"+mPager.getWidth());
                        Log.d("Container ", "h"+mPager.getHeight());
                        mPager.setLayoutParams(layoutParams);
                        Log.d("Container ", "h"+mPager.getLayoutParams().height);
                        mPager.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                        mPager.getRootView().invalidate();
                    }
                });
        **/

        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(1);

        frame = (FrameLayout) findViewById(R.id.fragmentContainer);
        frame.setVisibility(View.INVISIBLE);

        //ActionBar actionBar = getActionBar();
        //actionBar.hide();

        if (getIntent().getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            Bundle parameters = getIntent().getExtras();

            String idSelfie = parameters.getString("selfieId");

            int[] rainbow = getResources().getIntArray(R.array.colorPicker);
            int color = rainbow[0];
            showSelfie("", color, false, idSelfie);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try{
            checkLocation();
            checkBanned();
        }catch (ParseException e){
            Log.d("Location OR Banned Problem", e.getMessage());
        }
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new SecondFragment();
                case 1:
                    return new MainFragment();
                default:
                    return null;
            }
        }
    }

            /**
             *  case 2:
                return new VideoCameraFragment();
              */

    /** Check banned and ipaddress **/
    private void checkBanned() throws ParseException {

        // check banned
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    String banned = parseObject.getString("banned");
                    if (banned != null && banned.length() > 0) {
                        showBannedFragment();
                    }
                }
            }
        });

        // Set ipAddress // IPv4
        String ipAddress = Utils.getIPAddress(true);
        if (ipAddress != null){
            List<String> ipAddresses = new ArrayList<>();
            ipAddresses.add(ipAddress);
            currentUser.addAllUnique("ipAddress", ipAddresses);
            currentUser.saveInBackground();
        }
    }


    // Methods from fragements //

    // Camera Methods //

    public void onSuggestions(){

        SuggestionsFragment newFragment = new SuggestionsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, newFragment, "suggestions_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);
    }

    public void offSuggestions(String hashtag){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment suggestionsfragment = getFragmentManager().findFragmentByTag("suggestions_fragment");
        if(suggestionsfragment != null) {
            transaction.remove(suggestionsfragment).commit();
        }
        if (hashtag != null){
            NewPhotoFragment fragment = (NewPhotoFragment) getFragmentManager().findFragmentByTag("new_photo_fragment");
            if (fragment != null){
                fragment.setHashtag(hashtag);
            }else{
            }
        }

        frame.setVisibility(View.VISIBLE);
    }

    public void onCameraSelected(){

        //frame.setVisibility(View.INVISIBLE);
        //mPager.setCurrentItem(2);

        VideoCameraFragment newFragment = new VideoCameraFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("camera_fragment");
        transaction.commit();
        frame.setVisibility(View.VISIBLE);


        /**CaptureFragment newFragment = new CaptureFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("camera_fragment");
        transaction.commit();
        frame.setVisibility(View.VISIBLE);
**/

    }

    public void onImageSelected(){

        PhotoFragment newFragment = new PhotoFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("image_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);

    }

    public void transitonToPosting(boolean fromCamera, Bitmap data, String videopath){
        NewPhotoFragment newFragment = new NewPhotoFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putBoolean("from_camera" , fromCamera);
        args.putParcelable("image", data);
        if(videopath != null)
            args.putString("video", videopath);
        newFragment.setArguments(args);
        transaction.replace(R.id.fragmentContainer, newFragment, "new_photo_fragment");
        transaction.addToBackStack("new_photo_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);
    }

    public void onTransitionFromPostion(boolean from_camera){
        if(from_camera){
            onCameraSelected();
        }else{
            onImageSelected();
        }
    }

    public void lockCameraFragment(boolean lock){
        if (lock){
            mPager.setOnTouchListener(new View.OnTouchListener(){

                public boolean onTouch(View arg0, MotionEvent arg1) {
                    return true;
                }
            });
        }else{
            mPager.setOnTouchListener(null);
        }
    }

    // End Camera Methods //

    public void showSelfie(String hashtag, int color, boolean filtered) {
        showSelfie(hashtag, color, filtered, null);
    }

    public void showSelfie(String hashtag, int color, boolean filtered, String selfieId){

        Log.d("show selfie","hashtag = "+ hashtag);
        SelfieFragment newFragment = new SelfieFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString("hashtag", hashtag);
        args.putInt("color", color);
        args.putBoolean("filtered", filtered);
        args.putString("selfieId", selfieId);
        newFragment.setArguments(args);
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("selfie_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);
    }

    public void onCountSelected(){

        CountFragment newFragment = new CountFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("count_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);
    }

    public void onLocationSelected(){

        LocationFragment newFragment = new LocationFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("location_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);
    }

    public void onSwipeSelected(){
        if (mPager.getCurrentItem() == 0 || mPager.getCurrentItem() == 2){
            mPager.setCurrentItem(1);
        }else{
            mPager.setCurrentItem(0);
        }
    }

    public void onHideFragment(){
        lockCameraFragment(false);
        frame.setVisibility(View.INVISIBLE);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment countfragment = getFragmentManager().findFragmentByTag("count_fragment");
        if(countfragment != null)
            transaction.remove(countfragment).commit();
        Fragment camerafragment = getFragmentManager().findFragmentByTag("camera_fragment");
        if(camerafragment != null)
            transaction.remove(camerafragment).commit();
        Fragment imagefragment = getFragmentManager().findFragmentByTag("image_fragment");
        if(imagefragment != null)
            transaction.remove(imagefragment).commit();
        Fragment newPhotofragment = getFragmentManager().findFragmentByTag("new_photo_fragment");
        if(newPhotofragment != null)
            transaction.remove(newPhotofragment).commit();
        Fragment selfiefragment = getFragmentManager().findFragmentByTag("selfie_fragment");
        if(selfiefragment != null)
            transaction.remove(selfiefragment).commit();
        Fragment locationfragment = getFragmentManager().findFragmentByTag("location_fragment");
        if(locationfragment != null)
            transaction.remove(locationfragment).commit();
        getFragmentManager().popBackStack();
    }

    // Aditional Methods //

    public void showBannedFragment(){

        BannedFragment newFragment = new BannedFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, newFragment);
        transaction.addToBackStack("banned_fragment");
        transaction.commit();

        frame.setVisibility(View.VISIBLE);
    }

    public void checkLocation() {

        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(parseObject != null) {
                    ParseObject location = parseObject.getParseObject("location");
                    if (location == null) {
                        onLocationSelected();
                    }
                }
            }
        });
    }

    public void onBackPressed() {
    }
}