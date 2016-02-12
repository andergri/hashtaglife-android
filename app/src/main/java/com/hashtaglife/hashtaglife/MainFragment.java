package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 4/21/15.
 */
public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private HashtagsAdapter hashtagAdapter;
    private EditText searchText;
    private ListView lv;
    public Clickable clickable;
    public boolean filter = false;

    private SwipeRefreshLayout swipeLayout;
    onActionListener mCallback;
    private View v;

    // Footer
    private ImageButton leftButton;
    private ImageButton centerButton;
    private ImageButton rightButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {

        v = inflater.inflate(R.layout.fragment_main, parent, false);
        clickable = new Clickable();

        lv = (ListView) v.findViewById(R.id.listview);
        searchText = (EditText) v.findViewById(R.id.searchview);

        // Search Background Color
        int[] rainbow = getActivity().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);
        searchText.setBackgroundColor(rainbow[picker]);

        // Search Test
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        searchText.setCursorVisible(false);


        // Subclass of ParseQueryAdapter
        ArrayList<Hashtag> entries = new ArrayList<Hashtag>();
        hashtagAdapter = new HashtagsAdapter(getActivity(), entries, false);
        lv.setAdapter(hashtagAdapter);

        defaultHashtags();

        // ListView touches
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Hashtag hashtag = (Hashtag) lv.getAdapter().getItem(position);
                showPhotos(hashtag, position, view);
            }
        });
        lv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                searchText.setCursorVisible(false);

                return false;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                if (searchText.length() > 0) {
                    String query = searchText.getText().toString();
                    searchHashtags(query);
                } else {
                    defaultHashtags();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        searchText.setOnTouchListener(new OnTouchListener() {
            //@Override
            public boolean onTouch(View v, MotionEvent event) {

                searchText.setCursorVisible(true);
                return false;
            }
        });

        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(rainbow[picker]);


        // Footer //
        RelativeLayout bottomBar = (RelativeLayout) v.findViewById(R.id.bottomBar);
        bottomBar.setBackgroundColor(rainbow[picker]);
        leftButton = (ImageButton) v.findViewById(R.id.leftButton);
        leftButton.setColorFilter(rainbow[picker]);
        ((GradientDrawable)leftButton.getBackground()).setStroke(7, rainbow[picker]);
        rightButton = (ImageButton) v.findViewById(R.id.rightButton);
        rightButton.setColorFilter(rainbow[picker]);
        ((GradientDrawable)rightButton.getBackground()).setStroke(7, rainbow[picker]);
        centerButton = (ImageButton) v.findViewById(R.id.centerButton);
        centerButton.setColorFilter(rainbow[picker]);
        ((GradientDrawable)centerButton.getBackground()).setStroke(8, rainbow[picker]);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSwipeSelected();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onImageSelected();
            }
        });
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCameraSelected();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        setLocation();
        setUserPhoto();
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
    public void onRefresh() {

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 1000);

        if(searchText.length() > 0){
            String query = searchText.getText().toString();
            searchHashtags(query);
        }else{
            defaultHashtags();
        }
    }

    /** Search For Hashtags **/

    public void defaultHashtags() {

        if (!filter) {

            Hashtag.popularInBackground(new FindCallback<Hashtag>() {

                @Override
                public void done(List<Hashtag> hashtags, ParseException e) {

                    if (hashtags != null) {
                        if (hashtagAdapter != null) {
                            hashtagAdapter.setNotifyOnChange(true);
                            hashtagAdapter.clear();
                            Hashtag popular = new Hashtag();
                            popular.setName("popular");
                            Hashtag recent = new Hashtag();
                            recent.setName("recent");
                            //Hashtag photos = new Hashtag();
                            //photos.setName("my photos");
                            hashtagAdapter.add(popular);
                            hashtagAdapter.add(recent);
                            //hashtagAdapter.add(photos);
                            for (Hashtag hashtag : hashtags) {

                                hashtagAdapter.add(hashtag);
                            }
                        }
                    }
                    loadInbox();
                }
            });
        }else{

            Tag.popularInBackground(new FindCallback<Tag>() {

                @Override
                public void done(List<Tag> tags, ParseException e) {

                    Hashtag hash = new Hashtag();
                    List<Hashtag> hashtags = hash.getCleanHashtags(tags);

                    if (hashtags != null) {
                        if (hashtagAdapter != null) {
                            hashtagAdapter.setNotifyOnChange(true);
                            hashtagAdapter.clear();
                            Hashtag popular = new Hashtag();
                            popular.setName("popular");
                            Hashtag recent = new Hashtag();
                            recent.setName("recent");
                            //Hashtag photos = new Hashtag();
                            //photos.setName("my photos");
                            hashtagAdapter.add(popular);
                            hashtagAdapter.add(recent);
                            //hashtagAdapter.add(photos);
                            for (Hashtag hashtag : hashtags) {

                                hashtagAdapter.add(hashtag);
                            }
                        }
                    }
                    loadInbox();
                }
            });
        }
    }

    public void loadInbox(){
        Inbox.loadInboxInBackground(new FindCallback<Inbox>() {
            @Override
            public void done(List<Inbox> list, ParseException e) {
                Hashtag hash = new Hashtag();
                List<Hashtag> hashtags = hash.getCleanInbox(list);
                if (hashtags != null) {
                    hashtagAdapter.setNotifyOnChange(true);
                    if (hashtagAdapter != null) {
                        for (Hashtag hashtag : hashtags) {
                            hashtagAdapter.insert(hashtag, 0);
                        }
                    }
                }
            }
        });
    }

    public void searchHashtags(String query) {

        if (!filter) {

            Hashtag.searchInBackground(query, new FindCallback<Hashtag>() {

                @Override
                public void done(List<Hashtag> hashtags, ParseException e) {
                    if (hashtags != null) {
                        if (hashtagAdapter != null) {
                            hashtagAdapter.setNotifyOnChange(true);
                            hashtagAdapter.clear();
                            for (Hashtag hashtag : hashtags) {

                                hashtagAdapter.add(hashtag);
                            }
                        }
                    }
                }
            });

        }else{

            Tag.searchInBackground(query, new FindCallback<Tag>() {

                @Override
                public void done(List<Tag> tags, ParseException e) {

                    Hashtag hash = new Hashtag();
                    List<Hashtag> hashtags = hash.getCleanHashtags(tags);

                    if (hashtags != null) {
                        if (hashtagAdapter != null) {
                            hashtagAdapter.setNotifyOnChange(true);
                            hashtagAdapter.clear();
                            for (Hashtag hashtag : hashtags) {

                                hashtagAdapter.add(hashtag);
                            }
                        }
                    }
                }
            });
        }
    }

    // Footer //

    private void setUserPhoto(){

        try {

            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Thumbnails._ID};
            Cursor cursor = getActivity().managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    projection, // Which columns to return
                    null,       // Return all rows
                    null,
                    MediaStore.Images.Thumbnails._ID + " DESC");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToPosition(0);
                // Get the current value for the requested column
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String imagePath = cursor.getString(columnIndex);
                // Set the content of the image based on the provided URI
                Log.d("main imagePath", imagePath);
                if (imagePath != null) {
                    //  rightButton.setImageURI(null);
                    //  rightButton.setImageURI(Uri.fromFile(new File(imagePath)));
                    //rightButton.setImageURI(Uri.parse(imagePath));
                    //rightButton.setPadding(3, 3, 3, 3);
                    //Drawable d = rightButton.getDrawable();
                    //   Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                    //   bitmap = getRoundedCornerBitmap(bitmap, 1000);
                    //   rightButton.setImageBitmap(bitmap);
                    //rightButton.setScaleType(ImageView.ScaleType.CENTER_CROP);

                }
            }
        }catch (Exception e){
            Log.e("failted", Log.getStackTraceString(e));
        }
    }

    public Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 170, 170);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void showPhotos(Hashtag hashtag, int position, View view) {
        String clickedHashtag;
        int clickedColor;

        if(((hashtag.getName().equals("popular")) ||
                (hashtag.getName().equals("recent"))) && searchText.length() == 0){
            clickedHashtag = hashtag.getName();
        }else {
            clickedHashtag = hashtag.getNameWithHashtag();
        }
        clickedColor = ((ColorDrawable) view.getBackground()).getColor();
        Log.d("++hashtag ",clickedHashtag);
        mCallback.showSelfie(clickedHashtag, clickedColor, filter);
    }

    public void setLocation() {

        try {

        final ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                ParseObject location = parseObject.getParseObject("location");
                if (location != null) {
                    location.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject locationObject, ParseException e) {
                            if(locationObject != null){
                                Boolean defauted = locationObject.getBoolean("default");
                                if (defauted){
                                    filter = true;
                                }else{
                                    filter = false;
                                }
                                return;
                            }else{
                                filter = false;
                            }
                        }
                    });
                }else{
                    filter = false;
                }
            }
        });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface onActionListener {
        public void onCameraSelected();
        public void onSwipeSelected();
        public void onImageSelected();
        public void showSelfie(String hashtag, int color, boolean filterd);
    }
}
