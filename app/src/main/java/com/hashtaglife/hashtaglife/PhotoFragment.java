package com.hashtaglife.hashtaglife;


import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseFile;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends Fragment{

//  Cursor used to access the results from querying for images on the SD card.
    private Cursor cursor;
    private Button pickCancel;
    // Column index for the Thumbnails Image IDs.
    private int columnIndex;
    private int columnID;
    private int width;
    private ParseFile photoFile;
    onActionListener mCallback;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_photo, container, false);


        // Set up an array of the Thumbnail Image ID column we want
        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID};
        // Create the cursor pointing to the SDCard
        cursor = getActivity().managedQuery( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                MediaStore.Images.Thumbnails._ID + " DESC");
        // Get the column index of the Thumbnails Image ID
        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        columnID = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID);


        GridView sdcardImages = (GridView) v.findViewById(R.id.gridView1);
        sdcardImages.setAdapter(new PhotoAdapter());
        sdcardImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageView prompt = (ImageView)view;
                Drawable d = prompt.getDrawable();
                //Bitmap bitmap = ((BitmapDrawable)d).getBitmap();

                try {

                    final String[] projectionPhotos = {
                            MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.BUCKET_ID,
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media.DATE_TAKEN,
                            MediaStore.Images.Media.ORIENTATION
                    };

                    Bitmap bitmap = null;
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor images =
                            MediaStore.Images.Media.query(getActivity().getContentResolver(),
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                    , projectionPhotos, MediaStore.Images.Media._ID + "=?",
                                    new String[]{String.valueOf(prompt.getTag())},
                                    MediaStore.Images.Media.DATE_TAKEN + " DESC");

                    //selection, selection args
                            /**getActivity().getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            filePathColumn, MediaStore.Images.Media._ID + "=?",
                            new String[]{String.valueOf(prompt.getTag())}, null);
                             **/

                    if (images != null && images.moveToFirst()) {
                        // Your file-path will be here
                        String filePath = images.getString(images.getColumnIndex(filePathColumn[0]));

                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = calculateInSampleSize(options, 360, 600) + 1;
                        options.inDither = true;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        bitmap = BitmapFactory.decodeFile(filePath, options);
                    }
                    if (bitmap != null) {

                        if (bitmap.getWidth() > bitmap.getHeight()) {
                            bitmap = rotate(bitmap, 90);

                        }

                        mCallback.transitonToPosting(false, bitmap, null);

                    } else {
                        Toast.makeText(getActivity(), "fail", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                }
            }
        });
//asdf
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
        width = displaymetrics.widthPixels - (int) px;

        int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int pickera = rn.nextInt(rainbow.length);

        pickCancel = ((Button) v.findViewById(R.id.pick_cancel));
        pickCancel.setBackgroundColor(rainbow[pickera]);
        pickCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onHideFragment();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public class PhotoAdapter extends BaseAdapter {

        public PhotoAdapter() {
            super();
        }

        public int getCount() {
            return cursor.getCount();
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(getActivity());
                // Move cursor to current position
            }
            else {
                picturesView = (ImageView)convertView;
            }
            cursor.moveToPosition(position);
            String imagePath = cursor.getString(columnIndex);
            int id = cursor.getInt(columnID);

            picturesView.setImageURI(Uri.parse(imagePath));
            picturesView.setTag(id);

            picturesView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //picturesView.setPadding(10, 10, 10, 10);
            picturesView.setBackgroundColor(Color.WHITE);
            picturesView.setLayoutParams(new GridView.LayoutParams(width / 3, width /3));

            return picturesView;
        }
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

    // Image Helper Class

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public interface onActionListener {
        public void onHideFragment();
        public void transitonToPosting(boolean fromCamera, Bitmap data, String videopath);
    }
}
