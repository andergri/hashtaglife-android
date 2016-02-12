package com.hashtaglife.hashtaglife;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraUtils;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.IOException;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class VideoCameraFragment extends CameraFragment implements
        ScaleGestureDetector.OnScaleGestureListener {

    private View v;
    private int maxZoom = 0;
    private float mScaleFactor;
    private int mScaleResult;

    // Maybe
    private long lastFaceToast = 0L;
    String flashMode = null;

    onActionListener mCallback;

    private ScaleGestureDetector mScaleDetector;

    // Overlay
    private RelativeLayout cameraCover;
    private RelativeLayout cameraCoverAccept;

    // Camera controls
    private RelativeLayout photoBorder;
    private ImageButton photoButton;
    private ImageButton photoFlash;
    private ImageButton photoSwitch;
    private ImageButton leftButton;
    //private ImageButton rightButton;

    // Accept Deny
    private ImageButton postDeny;
    private ImageButton postAccept;
    private ImageView postImage;
    private TextureVideoView postVideo;

    // Image
    private Bitmap currentImage;
    private File currentFileImage;
    // Video
    private File currentVideo;
    // Color
    private int currentColor;
    private CircleView circle;
    private CurvedText curvedText;

    Handler VideoTimerHandler;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setHasOptionsMenu(true);

        SimpleCameraHost.Builder builder =
                new SimpleCameraHost.Builder(new VideoCameraHost(getActivity()));

        setHost(builder.useFullBleedPreview(true).build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View cameraView =
                super.onCreateView(inflater, container, savedInstanceState);
        v = inflater.inflate(R.layout.fragment_video_camera, container, false);

        addCameraView(cameraView);
        //((ViewGroup)v.findViewById(R.id.camera)).addView(cameraView);

        //lockToLandscape(true);

        cameraCover = (RelativeLayout) v.findViewById(R.id.camera_take);
        cameraCoverAccept = (RelativeLayout) v.findViewById(R.id.camera_accept);
        postImage = (ImageView) v.findViewById(R.id.post_image);
        postVideo = (TextureVideoView) v.findViewById(R.id.post_video);
        photoBorder = (RelativeLayout) v.findViewById(R.id.cameraBorder);

        // Video View
        postVideo.setLooping(true);
        postVideo.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);

        // New Methods
        final int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        final int picker = rn.nextInt(rainbow.length);

        // Photo button
        photoButton = (ImageButton) v.findViewById(R.id.camera_photo_button);
        photoButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("time test", String.valueOf(((Long) System.currentTimeMillis())));
                captureVideo(true);
                return false;
            }
        });

        final long[] athen = {0};
        photoButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    drawCenterColor();
                    athen[0] = System.currentTimeMillis();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (((Long) System.currentTimeMillis() - athen[0]) > 1000) {
                        Log.d("time testa", String.valueOf(((Long) System.currentTimeMillis() - athen[0])));
                        Log.d("time testa.", String.valueOf(((Long) System.currentTimeMillis())));
                        if (((Long) System.currentTimeMillis() - athen[0]) < 2000) {
                            Handler timeTemp = new Handler();
                            timeTemp.postDelayed(new Runnable() {
                                public void run() {
                                    captureVideo(false);
                                }
                            }, (2000 - ((Long) System.currentTimeMillis() - athen[0])));
                        }else{
                            captureVideo(false);
                        }
                    } else {
                        captureImage();
                    }
                    return true;
                }
                return false;
            }
        });

        // flash
        photoFlash = (ImageButton) v.findViewById(R.id.camera_photo_flash);
        photoFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlash();
            }
        });

        // camera switch
        photoSwitch = (ImageButton) v.findViewById(R.id.camera_photo_switch);
        photoSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSwitch();
            }
        });


        // left button
        leftButton = (ImageButton) v.findViewById(R.id.leftButton);
        Drawable dLeft = getResources().getDrawable(R.drawable.icon_exit);
        dLeft.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        leftButton.setImageDrawable(dLeft);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onHideFragment();
            }
        });

        // right button
        /**
        rightButton = (ImageButton) v.findViewById(R.id.rightButton);
        Drawable dRight = getResources().getDrawable(R.drawable.camera_image);
        dRight.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        rightButton.setImageDrawable(dRight);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onImageSelected();
            }
        });
         **/


        // Accept / Deny
        postDeny = (ImageButton) v.findViewById(R.id.post_deny);
        postAccept = (ImageButton) v.findViewById(R.id.post_accept);

        // Accept or deny photo
        Drawable dDeny = getResources().getDrawable(R.drawable.icon_exit_c);
        dDeny.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        postDeny.setImageDrawable(dDeny);
        postDeny.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cameraCoverTake();
            }
        });

        Drawable dAccept = getResources().getDrawable(R.drawable.icon_checkmark_c);
        dAccept.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        postAccept.setImageDrawable(dAccept);
        postAccept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cameraCoverTake();
                if (currentVideo != null)
                    mCallback.transitonToPosting(true, currentImage, currentVideo.getAbsolutePath());
                else
                    mCallback.transitonToPosting(true, currentImage, null);
            }
        });

        currentColor = rainbow[picker];

        // Scale

        mScaleFactor = 1;
        mScaleResult = 0;
        mScaleDetector = new ScaleGestureDetector(getActivity(), this);
        cameraCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

        setFlashMode(Camera.Parameters.FLASH_MODE_ON);

        // Curved Text
        curvedText =new CurvedText(getActivity());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pxToDp(230), pxToDp(230));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.bottomMargin = 0;
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d("animation", "begin");
                cameraCover.removeView(curvedText);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        curvedText.setAnimation(animation);
        cameraCover.addView(curvedText, params);


        return (v);
    }



    // Camera Covers
    private void cameraCoverTake() {
        try {
            restartPreview();
            mCallback.lockCameraFragment(false);
            postVideo.stop();
            photoButton.setEnabled(true);
            cameraCover.setVisibility(View.VISIBLE);
            cameraCoverAccept.setVisibility(View.INVISIBLE);
            postImage.setImageBitmap(null);
            postVideo.setBackgroundResource(0);
        }catch (Exception e){
            getActivity().recreate();
            Log.e(getClass().getSimpleName(),"Error: ", e);
            Toast.makeText(getActivity(), "Camera Restart",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void cameraCoverAccept(Boolean isImage) {
        postImage.setImageBitmap(null);
        cameraCover.setVisibility(View.INVISIBLE);
        cameraCoverAccept.setVisibility(View.VISIBLE);
        if (isImage) {
            postImage.setVisibility(View.VISIBLE);
            postVideo.setVisibility(View.INVISIBLE);
        } else {
            postImage.setVisibility(View.INVISIBLE);
            postVideo.setVisibility(View.VISIBLE);
        }
    }
    // End Camera Covers

    private void addCameraView(View view) {
        FrameLayout frame = (FrameLayout) v.findViewById(R.id.camera);
        frame.removeAllViews();
        CameraView cameraView = new CameraView(getActivity());
        cameraView.setHost(new VideoCameraHost(getActivity()));
        setCameraView(cameraView);
        frame.addView(cameraView);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                curvedText.animate();
            }
        }, 5000);
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
    public void onPause() {
        super.onPause();
    }

    /**
     * Scale Gesture Dector *
     */

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mCallback.lockCameraFragment(false);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {

        mCallback.lockCameraFragment(true);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        if (mScaleResult <= 0 && detector.getScaleFactor() < 1.0) {
            return true;
        }

        if (mScaleResult >= maxZoom && detector.getScaleFactor() > 1.0) {
            return true;
        }

        mScaleFactor *= Math.max(0.1f, Math.min(detector.getScaleFactor(), 5.0f));
        mScaleResult += (int) Math.round((mScaleFactor - 1.00) * (maxZoom / 5.0));

        if (mScaleResult < 0) {
            mScaleResult = 0;
        }

        if (mScaleResult > maxZoom) {
            mScaleResult = maxZoom;
        }

        zoomTo(mScaleResult).onComplete(new Runnable() {
            @Override
            public void run() {
            }
        }).go();

        return true;
    }

    /**
     * Flash and Switch *
     */

    private void toggleFlash() {
        if (photoFlash.isSelected()) {
            photoFlash.setImageResource(R.drawable.camera_flashoff);
            setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        } else {
            photoFlash.setImageResource(R.drawable.camera_flashon);
            setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }
        photoFlash.setSelected(!photoFlash.isSelected());
    }

    private void toggleSwitch() {
        photoSwitch.setSelected(!photoSwitch.isSelected());

        onPause();
        addCameraView(getView());
        onResume();
    }

    /**
     * Image and Video Methods *
     */

    private void captureVideo(boolean start) {

        if (isRecording() && !start) {
            Log.d("CAMERA:", "stop video");
            stopVideo();
        } else if (start) {
            try {
                Log.d("CAMERA:", "start video");
                drawCircleColor();
                record();
                mCallback.lockCameraFragment(true);
                startVideoTimer();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(),"Error: ", e);
                cameraCoverTake();
            }
        }
    }

    private void stopVideo(){
        try {
            if (isRecording()) {
                stopVideoTimer();
                stopRecording();
                removeDrawCircleColor();
                postVideo.setDataSource(currentVideo.getAbsolutePath());
                postVideo.setLooping(true);
                postVideo.play();
                currentImage = ThumbnailUtils.createVideoThumbnail(
                        currentVideo.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                cameraCoverAccept(false);
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error: ", e);
            removeDrawCircleColor();
            cameraCoverTake();
        }
    }

    private void captureImage() {

        try {
            photoButton.setEnabled(false);
            PictureTransaction pictureTransaction = new PictureTransaction(getHost());
            mCallback.lockCameraFragment(true);
            if (photoFlash != null && photoFlash.isSelected()) {
            } else {
            }
            Log.d("CAMERA:", "take a photo");
            takePicture(pictureTransaction);

            cameraCoverAccept(true);
        }catch (Exception e){
            cameraCoverTake();
        }
    }


    /**
     * CONTRACT *
     */

    Contract getContract() {
        return ((Contract) getActivity());
    }

    interface Contract {

    }

    /**
     * HOST *
     */

    class VideoCameraHost extends SimpleCameraHost implements
            Camera.FaceDetectionListener {

        private static final int DEFAULT_VIDEO_WIDTH = 720;
        private static final int DEFAULT_VIDEO_HEIGHT = 480;
        private static final int DEFAULT_VIDEO_FRAMERATE = 30;

        boolean supportsFaces = false;

        public VideoCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        public boolean useFrontFacingCamera() {
            return photoSwitch.isSelected();
        }

        @Override
        public boolean useSingleShotMode() {
            return true;
        }

        @Override
        public float maxPictureCleanupHeapUsage() {
            Log.d("max picture cleanup", String.valueOf(super.maxPictureCleanupHeapUsage())+" : 0.4f");
            return 0.4f;
        }

        @Override
        public void configureRecorderProfile(int cameraId, MediaRecorder recorder) {

            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            CamcorderProfile profile = null;
            /**if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P))
                profile = CamcorderProfile.get(cameraId,
                        CamcorderProfile.QUALITY_720P);
            else**/
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            //recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            if (profile == null) {
                recorder.setVideoSize(DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT);
                recorder.setVideoFrameRate(DEFAULT_VIDEO_FRAMERATE);
            } else {
                recorder.setVideoSize(profile.videoFrameWidth,
                        profile.videoFrameHeight);
                recorder.setVideoFrameRate(profile.videoFrameRate);
            }
            recorder.setVideoEncodingBitRate(1590910);//3000000);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            /**
             recorder.setProfile(profile);
             vision.videoBitRate = (PBJVideoBitRate640x480 / 2.2);
             vision.maximumCaptureDuration = CMTimeMakeWithSeconds(7.8, 600); // ~ 5 seconds
             */
        }

        @Override
        public void configureRecorderAudio(int cameraId, MediaRecorder recorder) {
            super.configureRecorderAudio(cameraId, recorder);
        }

        // PASSING

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            if (useSingleShotMode()) {

                BitmapFactory.Options opt = new BitmapFactory.Options();
                //opt.inSampleSize = 4;
                //opt.inDither = true;
                opt.inPreferredConfig = Bitmap.Config.ARGB_8888;//
                currentImage = BitmapFactory.decodeByteArray(image, 0, image.length, opt);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        postImage.setImageBitmap(currentImage);
                        Log.d("Video Camera Fragment", "showing image");
                    }
                });
            } else {
                super.saveImage(xact, image);
            }
        }

        @Override
        public void autoFocusAvailable() {
            if (supportsFaces)
                startFaceDetection();
        }

        @Override
        public void autoFocusUnavailable() {
            stopFaceDetection();
        }

        @Override
        public void onCameraFail(CameraHost.FailureReason reason) {
            super.onCameraFail(reason);

            Toast.makeText(getActivity(),
                    "Sorry, but you cannot use the camera now!",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public Parameters adjustPreviewParameters(Parameters parameters) {
            flashMode =
                    CameraUtils.findBestFlashModeMatch(parameters,
                            Camera.Parameters.FLASH_MODE_RED_EYE,
                            Camera.Parameters.FLASH_MODE_AUTO,
                            Camera.Parameters.FLASH_MODE_ON);

            if (doesZoomReallyWork() && parameters.getMaxZoom() > 0) {
                maxZoom = parameters.getMaxZoom();
            } else {
                maxZoom = 0;
            }

            if (parameters.getMaxNumDetectedFaces() > 0) {
                supportsFaces = true;
            } else {
            }

            return (super.adjustPreviewParameters(parameters));
        }

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
            if (faces.length > 0) {
                long now = SystemClock.elapsedRealtime();

                if (now > lastFaceToast + 10000) {
                    lastFaceToast = now;
                }
            }
        }

        @Override
        @TargetApi(16)
        public void onAutoFocus(boolean success, Camera camera) {
            super.onAutoFocus(success, camera);

            photoButton.setEnabled(true);
        }

        @Override
        public boolean mirrorFFC() {
            return (true);
        }


        @Override
        protected File getPhotoPath() {
            currentFileImage = super.getPhotoPath();
            return super.getPhotoPath();
        }

        @Override
        protected File getVideoPath() {

            File outputFile  = super.getVideoPath();
            File outputDir = getActivity().getCacheDir();

            try {
                outputFile = File.createTempFile(getVideoFilename(), null, outputDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentVideo = outputFile;

            System.out.println("**** CameraHost: , videoPath = " + outputFile);
            return outputFile;
        }

        @Override
        public Camera.ShutterCallback getShutterCallback() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawCenterFadeColor();
                    Log.d("Video Camera Fragment", "shutter callback");
                }
            });
            return super.getShutterCallback();
        }

        /**
         @Override public Camera.Size getPreferredPreviewSizeForVideo(int displayOrientation, int width, int height, Parameters parameters, Camera.Size deviceHint) {

         Camera.Size size = CameraUtils.getOptimalPreviewSize(displayOrientation,
         width, height,
         parameters);


         System.out.println("getPreviewSizeA: " + size.width+"x"+size.height);
         DisplayMetrics metrics = new DisplayMetrics();
         getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
         System.out.println("getPreviewSizeA: " + metrics.widthPixels+"x"+metrics.heightPixels);

         return size;

         //return super.getPreferredPreviewSizeForVideo(displayOrientation, width, height, parameters, deviceHint);
         }
         **/
    }

    public interface onActionListener {
        public void onImageSelected();

        public void onHideFragment();

        public void transitonToPosting(boolean fromCamera, Bitmap data, String videopath);

        public void lockCameraFragment(boolean lock);
    }


    public void startVideoTimer() {

        VideoTimerHandler = new Handler();
        VideoTimerHandler.postDelayed(new Runnable() {
            public void run () {
                stopVideo();
            }
        },7900);
    }

    public void stopVideoTimer() {

        VideoTimerHandler.removeCallbacksAndMessages(null);
    }

    public void drawCenterFadeColor() {

        final int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        final int picker = rn.nextInt(rainbow.length);

        Drawable backgrounds[] = new Drawable[2];

        Drawable arounded_button = getResources().getDrawable(R.drawable.rounded_button);
        arounded_button.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        backgrounds[0] = arounded_button;
        Drawable brounded_button = getResources().getDrawable(R.drawable.rounded_button);
        brounded_button.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        backgrounds[1] = brounded_button;

        TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
        photoButton.setBackground(crossfader);
        crossfader.startTransition(800);
    }

    public void drawCenterColor(){

        final int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        final int picker = rn.nextInt(rainbow.length);

        Drawable rounded_button = getResources().getDrawable(R.drawable.rounded_button);
        rounded_button.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        photoButton.setBackground(rounded_button);
    }

    public void drawCircleColor(){

        circle = new CircleView(getActivity());
        int side = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, getResources().getDisplayMetrics());
        int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                side,
                side);
        params.bottomMargin = bottom;
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        circle.setLayoutParams(params);
        circle.startAnimation();
        cameraCover.addView(circle);
    }

    public void removeDrawCircleColor(){
        drawCenterFadeColor();
        cameraCover.removeView(circle);
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public class CircleView extends View {

        int amargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        int awidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        private final int MARGIN = amargin;

        Handler handler = new Handler();
        Paint paint = new Paint();
        RectF rect = new RectF();

        boolean drawing = false;
        float sweep = 0;

        public CircleView(Context context)
        {
            this(context, null);
        }

        public CircleView(Context context, AttributeSet attrs)
        {
            super(context, attrs);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(awidth);
            paint.setColor(currentColor);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            canvas.drawArc(rect, 270, sweep, false, paint);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh)
        {
            super.onSizeChanged(w, h, oldw, oldh);
            rect.set(MARGIN, MARGIN, w - MARGIN, h - MARGIN);
        }

        public void startAnimation()
        {
            drawing = true;
            handler.postDelayed(runnable, 500);
        }

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                sweep += 1;
                if (!(sweep > 360))
                {
                    invalidate();
                    handler.postDelayed(this, 15);
                }
                else
                {
                    drawing = false;
                    sweep = 0;
                }
            }
        };

        public int pxToDp(int px) {
            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
            int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            return dp;
        }
    }
}