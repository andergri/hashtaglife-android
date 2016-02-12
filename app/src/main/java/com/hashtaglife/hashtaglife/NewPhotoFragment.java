package com.hashtaglife.hashtaglife;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPhotoFragment extends Fragment {


    private EditText postText;
    private ListView postHashtags;
    private LinearLayout postDoneContainer;
    private RelativeLayout postTags;
    private RelativeLayout postVisual;
    private ImageView postImage;
    private ImageButton postDeny;
    private ImageButton postAccept;
    private Button postCancel;
    private LinearLayout cautionHeader;
    private RelativeLayout suggestedHeader;

    private ViewGroup header;
    private ArrayList<String> hashtags = new ArrayList<String>();
    private PostAdapter<String> adapter;

    private ParseFile photoFile;
    private ParseFile videoFile;

    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;
    enum Direction {LEFT, RIGHT;}
    onActionListener mCallback;

    private String[] BAD_WORDS = {"anal", "anorexia", "bitch", "bomb", "boner", "boob", "breast", "butt", "chode", "cock", "clit", "dyke", "deepthroat",
            "dick", "faggot", "fat", "fuck", "gay", "hoe", "jizz", "kill", "orgasm", "pussy", "rape", "sloot", "slut", "suck", "threat",
            "thot", "ugly", "whore", "xx", "virgin"};

    private static final String[] EXACT_BAD_WORDS  = new String[] { "ass", "gangbang", "cleavage", "crack", "cumshot", "cunt", "dead", "dildo",
    "dumbass", "fag", "fake", "fat", "fetish", "freak", "hoe", "jizz", "joog", "jugs", "milf", "nigger", "nsfw",
    "nude", "orgasm", "penis", "porn", "prostitute", "pussy", "rack", "slayer", "slyaer", "sloot", "slut", "testicle",
            "thot", "threat", "ugly", "vagina", "virgin", "weed", "whore", "xx"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle SavedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_photo, parent, false);
        header = (ViewGroup)inflater.inflate(R.layout.item_header, postHashtags, false);

        photoFile = null;
        videoFile = null;

        postHashtags = (ListView) v.findViewById(R.id.post_hashtags);

        postTags = (RelativeLayout) v.findViewById(R.id.post_tags);
        postVisual = (RelativeLayout) v.findViewById(R.id.post_visual);
        postImage = (ImageView) v.findViewById(R.id.post_image);
        postDeny = (ImageButton) v.findViewById(R.id.post_deny);
        postAccept  = (ImageButton) v.findViewById(R.id.post_accept);

        adapter = new PostAdapter<String>(getActivity().getApplicationContext(),
                R.layout.item_list_hashtags, R.id.text1, hashtags, this);
        try {
            postHashtags.addHeaderView(header, null, false);
        }catch (Exception e){
        }
        postHashtags.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        cautionHeader = (LinearLayout) header.findViewById(R.id.post_caution_container);
        cautionHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllBadHashtags();
            }
        });

        int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length);

        /**
        suggestedHeader = (RelativeLayout) header.findViewById(R.id.post_suggested);
        suggestedHeader.setBackgroundColor(rainbow[rn.nextInt(rainbow.length)]);
        suggestedHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                mCallback.onSuggestions();
            }
        });**/

        // yep
        // Accept or deny photo
        Drawable dDeny = getResources().getDrawable(R.drawable.icon_exit_c);
        dDeny.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        postDeny.setImageDrawable(dDeny);
        postDeny.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mCallback.onTransitionFromPostion(getArguments().getBoolean("from_camera", true));
                photoFile = null;
                videoFile = null;
            }
        });

        Drawable dAccept = getResources().getDrawable(R.drawable.icon_checkmark_c);
        dAccept.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        postAccept.setImageDrawable(dAccept);
        postAccept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Bitmap data = getArguments().getParcelable("image");
                String videoPath = getArguments().getString("video", null);
                if (data != null) {
                    saveScaledPhoto(data, videoPath);
                    hideImage();
                }
            }
        });


        postCancel  = (Button) v.findViewById(R.id.post_cancel);
        postCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showImage();
            }
        });

        // Hashtags
        postText = ((EditText) v.findViewById(R.id.post_text));
        postText.setBackgroundColor(rainbow[picker]);
        postText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (postText.length() > 0 && s.toString().contains(" ")){
                    String hashing = String.valueOf(postText.getText());
                    postText.setText("");
                    checkHashtag(hashing);
                }
            }
        });
        postText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (postText.length() > 0) {
                        String hashing = String.valueOf(postText.getText());
                        postText.setText("");
                        checkHashtag(hashing);
                        return true;
                    }
                }
                return false;
            }
        });

        postText.setFocusableInTouchMode(true);
        postText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(postText, InputMethodManager.SHOW_IMPLICIT);
        postText.setCursorVisible(true);

        int[] rainbowa = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rna = new Random();
        int pickera = rn.nextInt(rainbow.length);

        postDoneContainer = ((LinearLayout) header.findViewById(R.id.post_done_container));
        postDoneContainer.setBackgroundColor(rainbow[rn.nextInt(rainbow.length)]);
        postDoneContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postDoneContainer.setEnabled(false);
                String hashing = String.valueOf(postText.getText());
                postText.setText("");
                checkHashtag(hashing);

                // Saving Hashtags
                ArrayList<String> cleanHashtags = cleanHashtags(hashtags);

                Hashtag hashtag = new Hashtag();

                for (int i = 0; i < cleanHashtags.size(); i++) {
                    String hash = (String) cleanHashtags.get(i);
                    hashtag.saveHashtag(hash);//
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(postText.getWindowToken(), 0);

                // Saving Selfie
                Selfie selfie = new Selfie();

                ParseObject location = ParseUser.getCurrentUser().getParseObject("location");

                if (photoFile == null) {
                    Toast.makeText(getActivity(),
                            "Error saving: Not going to save properly",
                            Toast.LENGTH_LONG).show();
                }

                if (videoFile != null){
                    selfie.setNewSelfie(photoFile, ParseUser.getCurrentUser(), location, cleanHashtags, videoFile);
                }else{
                    selfie.setNewSelfie(photoFile, ParseUser.getCurrentUser(), location, cleanHashtags, null);
                }

                // Save the meal and return
                selfie.saveInBackground(new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                        } else {
                            Log.d("Error saving: ",e.getMessage());
                        }
                    }

                });
                postDoneContainer.setEnabled(true);
                mCallback.onHideFragment();

            }
        });

        postHashtags.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(postText.getWindowToken(), 0);
                postText.setCursorVisible(false);
                String hashing = String.valueOf(postText.getText());
                postText.setText("");
                checkHashtag(hashing);

                return false;
            }
        });

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(postText.getWindowToken(), 0);
                postText.setCursorVisible(false);
                String hashing = String.valueOf(postText.getText());
                postText.setText("");
                checkHashtag(hashing);
                return false;
            }
        });

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                setPlaceholder();
            }
        });

        checkHeader();

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

    private void hideImage(){
        postVisual.setVisibility(View.INVISIBLE);
        postTags.setVisibility(View.VISIBLE);
    }
    private void showImage(){
        postVisual.setVisibility(View.VISIBLE);
        postTags.setVisibility(View.INVISIBLE);
    }

    /*
     * All data entry about a Meal object is managed from the NewMealActivity.
     * When the user wants to add a photo, we'll start up a custom
     * CaptureFragment that will let them take the photo and save it to the Meal
     * object owned by the NewMealActivity. Create a new CaptureFragment, swap
     * the contents of the fragmentContainer (see activity_new_meal.xml), then
     * add the NewMealFragment to the back stack so we can return to it when the
     * camera is finished.
     */
    /**
    public void startCamera() {
        Fragment cameraFragment = new CaptureFragment();
        FragmentTransaction transaction = getActivity().getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragmentContainer, cameraFragment);
        transaction.addToBackStack("NewPhotoFragment");
        transaction.commit();
    }**/

    /*
     * On resume, check and see if a meal photo has been set from the
     * CaptureFragment. If it has, load the image in this fragment and make the
     * preview image visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        Bitmap img = getArguments().getParcelable("image");


        if (img != null) {
            postDoneContainer.setEnabled(true);
            postImage.setImageBitmap(img);
            postImage.setVisibility(View.VISIBLE);

        }
        boolean fromCamera = getArguments().getBoolean("from_camera", true);
        String videoPath = getArguments().getString("video", null);

        Log.d("fromCamera", String.valueOf(fromCamera));

        String hashtagSelected = getArguments().getString("hashtag_selected");
        if (hashtagSelected != null){
            Log.d("hashtagSelected", hashtagSelected);
        }

        if(fromCamera){
            hideImage();
            saveScaledPhoto(img, videoPath);
        }
    }

    public void setHashtag(String hashtag) {
        checkHashtag(hashtag);
    }

    private void checkHashtag(String hashtag){

        hashtag = hashtag.replace("#", "");
        hashtag = hashtag.replace(" ", "");

        if (hashtag.length() > 0) {

            if (hashtags.size() < 5.0){

                Log.d("postText final ", hashtag);

                if (hashtags.size() == 0){
                }
                hashtags.add(0, "#" + hashtag);

            }else{
                Toast.makeText(getActivity().getApplicationContext(), "Only 5 Hashtags allowed.",
                                Toast.LENGTH_SHORT).show();
            }

            postText.setText("");
            checkHeader();
        }
        adapter.notifyDataSetChanged();
    }

    public void setPlaceholder(){

        switch (hashtags.size()){
            case 0:
                postText.setHint("Add a few hashtags");
                break;
            case 1:
                postText.setHint("4 hashtags left");
                break;
            case 2:
                postText.setHint("3 hashtags left");
                break;
            case 3:
                postText.setHint("2 hashtags left");
                break;
            case 4:
                postText.setHint("1 hashtags left");
                break;
            case 5:
                postText.setHint("Touch 'Done'");
                break;
        }
    }

    private ArrayList<String> cleanHashtags(ArrayList<String> dirtyHashtags){

        ArrayList<String> cleanHashtags = new ArrayList<String>();
        for(int i = 0; i < dirtyHashtags.size(); i++){
            String tag = dirtyHashtags.get(i).replaceAll("#","");
            cleanHashtags.add(tag);
        }
        return cleanHashtags;
    }

    private void saveScaledPhoto(Bitmap finalImage, String videoPath) {

        Bitmap imageScaled = Bitmap.createScaledBitmap(finalImage, 380, 380
                * finalImage.getHeight() / finalImage.getWidth(), false);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();

        // Save the scaled image to Parse
        photoFile = new ParseFile("photo.jpg", scaledData);
        photoFile.saveInBackground(new SaveCallback() {

            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(),
                            "Error saving image: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                }
            }
        });

        if (videoPath != null && videoPath.length() > 0){

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(videoPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            byte[] buf = new byte[1024];
            int n;
            try {
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] videoBytes = baos.toByteArray();

            Log.d("VIDEO:", "byte size: "+videoBytes.length/1024);

            videoFile = new ParseFile("video.mov", videoBytes);
            videoFile.saveInBackground(new SaveCallback() {

                public void done(ParseException e) {
                    if (e != null) {
                        Log.d("VIDEO:", "Failed to Save");
                        Toast.makeText(getActivity(),
                                "Error saving video: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("VIDEO:", "Saved Video");
                    }
                }
            });
        }
    }


    // Caution Hashtags
    public boolean shouldShowCaution(String hashtag){

        hashtag = hashtag.toLowerCase();
        hashtag = hashtag.replace("#", "");
        hashtag = hashtag.replace(" ", "");
        for (String bad_word : BAD_WORDS) {
            if (hashtag.contains(bad_word)){
                return true;
            }
        }
        Log.d("new photo fragment bad", hashtag +" = "+ String.valueOf(Arrays.asList(EXACT_BAD_WORDS)));
        if (Arrays.asList(EXACT_BAD_WORDS).contains(hashtag)) {
            return true;
        }
        String[] badWordList = getResources().getStringArray(R.array.badwordPicker);
        if (Arrays.asList(badWordList).contains(hashtag)) {
            return true;
        }
        return false;
    }

    private boolean showWarning() {
        for (String hashtag : hashtags) {
            if (shouldShowCaution(hashtag)) {
                return true;
            }
        }
        return false;
    }

    private void removeAllBadHashtags (){

        // Find the things to remove
        ArrayList<String> toDelete = new ArrayList<String>();
        for (String hashtag : hashtags){
            if (shouldShowCaution(hashtag)) {
                toDelete.add(hashtag);
            }
        }
        hashtags.removeAll(toDelete);
        checkHeader();
        adapter.notifyDataSetChanged();
    }

    public void checkHeader(){
        Log.d("hashtag size", String.valueOf(hashtags.size()));
        if (hashtags.size() == 0.0) {
                //suggestedHeader.setVisibility(View.VISIBLE);
                postDoneContainer.setVisibility(View.GONE);
                cautionHeader.setVisibility(View.GONE);
        }else{
            //suggestedHeader.setVisibility(View.GONE);
            if (showWarning()) {
                postDoneContainer.setVisibility(View.GONE);
                cautionHeader.setVisibility(View.VISIBLE);
            } else {
                postDoneContainer.setVisibility(View.VISIBLE);
                cautionHeader.setVisibility(View.GONE);
            }
        }
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(postText.getWindowToken(), 0);
    }

    public interface onActionListener {
        public void onHideFragment();
        public void onTransitionFromPostion(boolean from_camera);
        public void  onSuggestions();
    }
}