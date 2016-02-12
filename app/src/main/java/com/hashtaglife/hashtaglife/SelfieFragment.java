package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hashtaglife.hashtaglife.VideoService.VideoBinder;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;

/**
 * Created by griffinanderson on 12/3/14.
 */
public class SelfieFragment extends Fragment implements SurfaceHolder.Callback {

    // top
    private View v;
    private RelativeLayout selfie;
    onActionListener mCallback;
    private int colora;

    // Back & Exit
    private ImageButton selfieExit;
    private ImageButton selfieBack;

    // Boxes
    private RelativeLayout selfieVoteBox;
    private RelativeLayout selfieHashtagsBox;
    private RelativeLayout selfieBottomBox;

    // Views and Hashtgs
    private TextView selfieViews;
    private ImageButton selfieViewsIcon;
    private TextView selfieHashtags;

    // Data
    private SelfieData data;
    private TextView selfieSelectedHashtag;
    private ProgressBar selfieProgressBar;

    // Vote Object
    private ImageButton selfieUpvote;
    private ImageButton selfieDownvote;
    private TextView selfieVote;
    private RelativeLayout selfieUpvoteBackground;
    private RelativeLayout selfieDownvoteBackground;
    private SelfieVote selfieVoteObject;

    // Flag Object
    private ImageButton selfieFlag;
    private SelfieFlag selfieFlagObject;

    // Username List
    private TextView slideText;
    private ImageButton slideArrow;
    private ImageButton slideArrowUp;
    private RelativeLayout animateOverlay;
    private ListView lv;
    private SelfieUsernameList selfieUsernameList;

    // Video
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
    private VideoService videoSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private RelativeLayout bufferIcon;

    // Image
    private ParseImageView selfieImage;


    /** VIDEO **/

    //connect to the service
    private ServiceConnection videoConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VideoBinder binder = (VideoBinder)service;
            //get service
            videoSrv = binder.getService();
            //pass list
            musicBound = true;
            videoSrv.setSurface(vidHolder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (activityReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(VideoService.ACTION_STRING_ACTIVITY);
            getActivity().registerReceiver(activityReceiver, intentFilter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {
        // Views
        v = inflater.inflate(R.layout.fragment_selfie, parent, false);
        selfie = (RelativeLayout) v.findViewById(R.id.selfie);
        selfie.setBackgroundColor(getArguments().getInt("color"));
        colora = getArguments().getInt("color");

        // Boxes 6
        selfieVoteBox = ((RelativeLayout) v.findViewById(R.id.selfieVoteBox));
        selfieHashtagsBox = ((RelativeLayout) v.findViewById(R.id.selfieHashtagsBox));
        selfieBottomBox = ((RelativeLayout) v.findViewById(R.id.selfieBottomBox));

        // Image
        selfieImage = ((ParseImageView) v.findViewById(R.id.selfieImage));

        // Video
        vidSurface = (SurfaceView) v.findViewById(R.id.surficeView);
        vidSurface.setBackgroundColor(getArguments().getInt("color"));
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);
        bufferIcon = (RelativeLayout) v.findViewById(R.id.loadingPanel);

        // Hashtags
        selfieHashtags = (TextView) v.findViewById(R.id.selfieHashtags);
        selfieHashtags.setTextColor(getArguments().getInt("color"));

        // Visits
        selfieViews = (TextView) v.findViewById(R.id.selfieViewsText);
        selfieViews.setTextColor(getArguments().getInt("color"));
        selfieViewsIcon = (ImageButton) v.findViewById(R.id.selfieViewsIcon);
        Drawable eye = getResources().getDrawable(R.drawable.icon_eye);
        eye.setColorFilter(colora, PorterDuff.Mode.SRC_ATOP);
        selfieViewsIcon.setImageDrawable(eye);

        // Exit
        selfieExit = ((ImageButton) v.findViewById(R.id.selfieExit));
        selfieExit.setEnabled(false);
        selfieExit.setColorFilter(Color.DKGRAY);
        selfieExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goExit();
            }
        });

        // Back
        selfieBack = ((ImageButton) v.findViewById(R.id.selfieBack));
        selfieBack.setColorFilter(Color.DKGRAY);
        selfieBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackward();
            }
        });

        /** DATA **/
        selfieSelectedHashtag = (TextView) v.findViewById(R.id.selfieSelectedHashtag);
        selfieProgressBar = (ProgressBar) v.findViewById(R.id.selfieProgressBar);
        data = new SelfieData(getActivity(), getArguments(), selfieSelectedHashtag, selfieProgressBar);
        data.loadContent(new TransactionAction() {
            @Override
            public void perform() {
                goForward();
            }
        });

        /** Vote Object **/
        selfieUpvote = ((ImageButton) v.findViewById(R.id.selfieUpvote));
        selfieDownvote = ((ImageButton) v.findViewById(R.id.selfieDownvote));
        selfieVote = ((TextView) v.findViewById(R.id.selfieVoteCount));
        selfieVote.setTextColor(colora);
        selfieUpvoteBackground = (RelativeLayout) v.findViewById(R.id.selfieUpvoteBackground);
        selfieDownvoteBackground = (RelativeLayout) v.findViewById(R.id.selfieDownvoteBackground);
        selfieVoteObject = new SelfieVote(getActivity(), selfieUpvote, selfieDownvote, selfieVote, selfieUpvoteBackground, selfieDownvoteBackground);

        /** Flag Object **/
        selfieFlag = ((ImageButton) v.findViewById(R.id.selfieFlag));
        selfieFlag.setColorFilter(Color.DKGRAY);
        selfieFlagObject = new SelfieFlag(getActivity(), selfieFlag);

        /** USERNAME LIST **/
        //slideText = (TextView) v.findViewById(R.id.slideText);
        //slideArrow = (ImageButton) v.findViewById(R.id.slideArrow);
        //slideArrowUp = (ImageButton) v.findViewById(R.id.slideArrowUp);
        animateOverlay = ((RelativeLayout) v.findViewById(R.id.animateOverlay));
        //lv = (ListView) v.findViewById(R.id.listviewUsername);
        //selfieUsernameList = new SelfieUsernameList(getActivity(), selfieImage, vidSurface, slideText, slideArrow, slideArrowUp, animateOverlay);
        //lv.setAdapter(selfieUsernameList.usernameAdapter);

        int bottomDistance = getActivity().getWindow().getDecorView().getHeight() - 200;
        animateOverlay.setOnTouchListener(new OnSwipeListener(getActivity(), bottomDistance) {
            public void onSwipeTop() {
                /**if (selfieUsernameList.didUserCreate() && selfieUsernameList.isUsernameListOpen()){
                    selfieUsernameList.slideUsernameList();
                    this.usernameListOpen(selfieUsernameList.isUsernameListOpen());
                    return;
                }**/
                goForward();
            }
            public void onSwipeRight() {
                goForward();
            }
            public void onSwipeLeft() {
                goForward();
            }
            public void onSwipeBottom() {
                /**if (selfieUsernameList.didUserCreate() && !selfieUsernameList.isUsernameListOpen()) {
                    selfieUsernameList.slideUsernameList();
                    this.usernameListOpen(selfieUsernameList.isUsernameListOpen());
                    return;
                }**/
                goForward();
            }
            public void onTap(){
                /**if (selfieUsernameList.didUserCreate() && selfieUsernameList.isUsernameListOpen()) {
                    selfieUsernameList.slideUsernameList();
                    this.usernameListOpen(selfieUsernameList.isUsernameListOpen());
                    return;
                }**/
                goForward();
            }

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        // Controls
        hideControls();

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (onActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        selfieExit.setEnabled(true);

        if(playIntent==null){
            playIntent = new Intent(getActivity(), VideoService.class);
            getActivity().bindService(playIntent, videoConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        videoSrv=null;
        super.onDestroy();
        getActivity().unregisterReceiver(activityReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        goExit();
    }


    /**
     *******************************************************
     **************   METHODS  *****************************
     ******************************************************
     */

    /** Movement ****************************************************************/

    public void goExit(){
        if (isAdded()) {
            mCallback.onHideFragment();
        }
    }

    public void goBackward(){
        Selfie media = data.dataBack();
        if (media != null){
            changeTo(media);
        }else{
            goExit();
        }
    }

    public void goForward(){
        if (isAdded()) {
            Selfie media = data.dataForward();
            if (media != null){
                changeTo(media);
            }else{
                goExit();
            }
            selfie.setBackgroundColor(Color.BLACK);
        }
    }

    public void changeTo(Selfie media){
        selfieVoteObject.resetWithSelfie(media);
        selfieFlagObject.resetWithSelfie(media);
        setMetaData(media);
        setPhoto(media);
        setVideo(media);
        //selfieUsernameList.resetWithSelfie(media);
        data.getImageData();
        data.getVideoData();
    }

    /** Movement ****************************************************************/

    /** Meta Data **/
    public void setMetaData(Selfie selfie){
        if(selfie != null){
            // Set Hashtags
            selfieHashtags.setText("");
            SortedHashtags sorted = new SortedHashtags();
            sorted.makeHashtagsClickable(selfieHashtags, selfie.getHashtags(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if (!selfieUsernameList.isUsernameListOpen()) {
                        String hashtag = String.valueOf(v.getTag());
                       // hashtag = hashtag.substring(1);
                        Bundle args = getArguments();
                        args.putString("hashtag", hashtag);
                        args.putString("selfieId", null);

                        //getTargetFragment().setArguments(args);
                        data = new SelfieData(getActivity(), args, selfieSelectedHashtag, selfieProgressBar);
                        Log.d("Data Load Content B: ",getArguments().getString("hashtag"));
                        data.loadContent(new TransactionAction() {
                            @Override
                            public void perform() {
                                Log.d("going forward","forward");
                                goForward();
                            }
                        });
                        //hideControls();
                        //mCallback.showSelfieWithClose(hashtag, colora, true);
                    //}
                }
            });

            selfieViews.setText(selfie.getVisits());
            selfieVote.setText(selfie.getLikes());
        }
    }

    /** Photo **/

    public void setPhoto(Selfie selfie){

        ParseFile photoFile = selfie.getImage();
        if (photoFile != null) {
            Bitmap b = selfie.getBmp();
            if(b != null) {
                selfieImage.setImageBitmap(b);
                showControls();
            }else{
                selfieImage.setParseFile(photoFile);
                selfieImage.loadInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        showControls();
                    }
                });
            }
        }
    }


    /** VIDEO **/

    public void setVideo(Selfie selfie){
        ParseFile videoFile = selfie.getVideo();
        vidSurface.setBackgroundColor(0);
        if (videoFile != null) {
            videoSrv.setVideo(selfie);
            videoSrv.playVideo();
        }else{
            videoSrv.pauseVideo();
            bufferIcon.setVisibility(View.INVISIBLE);
            selfieImage.setVisibility(View.VISIBLE);
        }
    }

    /** Video Recievier ****************************************************************/

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            switch(type) {
                case VideoService.VIDEO_SERVICE_BUFFERING:
                    Log.d("video: ","buffering");
                    bufferIcon.setVisibility(View.VISIBLE);
                    break;
                case VideoService.VIDEO_SERVICE_PLAYING:
                    Log.d("video: ","playing");
                    bufferIcon.setVisibility(View.INVISIBLE);
                    selfieImage.setImageBitmap(null);
                    break;
                case VideoService.VIDEO_SERVICE_ERROR:
                    Log.d("video: ","error");
                    //nextPhoto();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        //Log.d("video holder","surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        //setup
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        //Log.d("video holder","surfaceDestroyed");
    }


    /** Controls ****************************************************************/

    public void showControls(){
        selfieImage.setVisibility(View.VISIBLE);
        selfieFlag.setVisibility(View.VISIBLE);
        selfieBack.setVisibility(View.VISIBLE);
        selfieBottomBox.setVisibility(View.VISIBLE);
        selfieSelectedHashtag.setVisibility(View.INVISIBLE);
        selfieProgressBar.setVisibility(View.INVISIBLE);
    }

    public void hideControls(){
        selfieImage.setVisibility(View.INVISIBLE);
        selfieFlag.setVisibility(View.INVISIBLE);
        selfieBack.setVisibility(View.INVISIBLE);
        selfieBottomBox.setVisibility(View.INVISIBLE);
        selfieSelectedHashtag.setVisibility(View.VISIBLE);
        selfieProgressBar.setVisibility(View.VISIBLE);
    }

    public interface onActionListener {
        public void onHideFragment();
    }
}