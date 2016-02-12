package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.parse.ParseFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CaptureFragment extends Fragment {

    public static final String TAG = "CaptureFragment";

    private ParseFile photoFile;

    private Camera camera;
    private SurfaceHolder holder;
    private boolean cameraFront = false;

    private SurfaceView surfaceView;
    private ImageButton photoButton;
    private ImageButton photoFlash;
    private ImageButton photoSwitch;
    private ImageButton photoExit;
    private RelativeLayout cameraCover;
    private RelativeLayout cameraCoverAccept;
    private ImageButton postDeny;
    private ImageButton postAccept;
    private ZoomControls zoomControls;
    private ScaleGestureDetector scaleGestureDetector;
    private Bitmap currentImage;

    onActionListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, parent, false);


        final int[] rainbow = getActivity().getApplicationContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        final int picker = rn.nextInt(rainbow.length);

        photoButton = (ImageButton) v.findViewById(R.id.camera_photo_button);
        cameraCover = (RelativeLayout) v.findViewById(R.id.camera_take);
        cameraCoverAccept = (RelativeLayout) v.findViewById(R.id.camera_accept);


        if (camera == null) {
            Log.d("aaa","camera should be null");
            try {
                camera = Camera.open();
                Log.d("aaa","open");
                photoButton.setEnabled(true);
            } catch (Exception e) {
                Log.e(TAG, "No camera with exception: " + e.getMessage());
                photoButton.setEnabled(false);
                Toast.makeText(getActivity(), "No camera detected",
                        Toast.LENGTH_LONG).show();
            }
        }
        Log.d("aaa","passed");

        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (camera == null)
                    return;
                camera.takePicture(new Camera.ShutterCallback() {

                    @Override
                    public void onShutter() {
                        // nothing to do
                        Drawable rounded_button = getResources().getDrawable(R.drawable.rounded_button);
                        rounded_button.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
                        photoButton.setBackground(rounded_button);

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

                }, null, new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        cameraCoverAccept();

                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 4;
                        opt.inDither = true;
                        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;//
                        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length, opt);

                        image = rotateImage(image);
                        currentImage = image;
                    }
                });
            }
        });

        surfaceView = (SurfaceView) v.findViewById(R.id.camera_surface_view);
        holder = surfaceView.getHolder();
        holder.addCallback(new Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (camera != null) {
                        camera.setDisplayOrientation(90);
                        camera.setPreviewDisplay(holder);
                        camera.startPreview();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error setting up preview", e);
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                // nothing to do here
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // nothing here
            }

        });

        scaleGestureDetector = new ScaleGestureDetector(this.getActivity().getApplicationContext(), new PinchToZoomGestureListener());

        cameraCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        hasFlash();

        photoFlash = (ImageButton) v.findViewById(R.id.camera_photo_flash);
        photoFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera == null)
                    return;
                Log.d("Flash Camera", "Toggle");
                toggleFlash();
            }
        });

        photoSwitch = (ImageButton) v.findViewById(R.id.camera_photo_switch);
        photoSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera == null)
                    return;
                Log.d("flip camera", "yep");
                flipCamera();
            }
        });

        photoExit = (ImageButton) v.findViewById(R.id.camera_photo_exit);
        Drawable dExit = getResources().getDrawable(R.drawable.icon_exit_c);
        dExit.setColorFilter(rainbow[picker], PorterDuff.Mode.SRC_ATOP);
        photoExit.setImageDrawable(dExit);

        photoExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera == null)
                    return;
                mCallback.onHideFragment();
            }
        });

        postDeny = (ImageButton) v.findViewById(R.id.post_deny);
        postAccept  = (ImageButton) v.findViewById(R.id.post_accept);

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
                mCallback.transitonToPosting(true , currentImage, null);
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
    public void onResume() {
        super.onResume();
        if (camera == null) {
            try {
                camera = Camera.open();
                photoButton.setEnabled(true);
            } catch (Exception e) {
                Log.i(TAG, "No camera: " + e.getMessage());
                photoButton.setEnabled(false);
                Toast.makeText(getActivity(), "No camera detected",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;

        }
        super.onPause();
    }


    public void flipCamera() {
        releaseCamera();
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                camera = Camera.open(cameraId);
                //mPicture = getPictureCallback();
                refreshCamera(camera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                camera = Camera.open(cameraId);
                //mPicture = getPictureCallback();
                refreshCamera(camera);
            }
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void refreshCamera(Camera camera) {
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        //camera = camera;z
        try {
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up preview A", e);
        }
    }

    // FRONT & BACK FACING CAMERA IDS

    private int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    // FLASH

    public boolean hasFlash() {
        if (camera == null) {
            return false;
        }
        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }
        return true;
    }

    private void toggleFlash(){
        try {
            if (getActivity().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {

                if (photoFlash.getTag().equals("flashon")){
                    Parameters p = camera.getParameters();
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    photoFlash.setTag("flashoff");
                    photoFlash.setImageResource(R.drawable.camera_flashoff);
                }else if(photoFlash.getTag().equals("flashoff")){
                    Parameters p = camera.getParameters();
                    p.setFlashMode(Parameters.FLASH_MODE_ON);
                    camera.setParameters(p);
                    photoFlash.setTag("flashon");
                    photoFlash.setImageResource(R.drawable.camera_flashon);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getBaseContext(), "Exception flashLightOn()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cameraCoverTake(){
        releaseCamera();
        if (cameraFront) {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                camera = Camera.open(cameraId);
                //mPicture = getPictureCallback();
                refreshCamera(camera);
            }
        } else {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                camera = Camera.open(cameraId);
                //mPicture = getPictureCallback();
                refreshCamera(camera);
            }
        }
        cameraCover.setVisibility(View.VISIBLE);
        cameraCoverAccept.setVisibility(View.INVISIBLE);
    }

    private void cameraCoverAccept(){
        cameraCover.setVisibility(View.INVISIBLE);
        cameraCoverAccept.setVisibility(View.VISIBLE);
    }


    private Bitmap rotateImage(Bitmap data){

        int cameraId;
        if (cameraFront) {
            cameraId = findFrontFacingCamera();
        }else{
            cameraId = findBackFacingCamera();
        }

        int angleToRotate = getRoatationAngle(getActivity(), cameraId);
        data = rotate(data, angleToRotate, cameraId);

        return data;

    }

    public static Bitmap rotate(Bitmap bitmap, int degree, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mtx.setScale(-1, 1);
        }
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static int getRoatationAngle(Activity mContext, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

// ZOOM // Class

    public class PinchToZoomGestureListener extends SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (detector.getScaleFactor() >= 1.0){
                zoomCamera(true);
            }else{
                zoomCamera(false);
            }

            return true;
        }
    }

    public void zoomCamera(boolean zoomInOrOut) {
        if(camera!=null) {
            Parameters parameter = camera.getParameters();

            if(parameter.isZoomSupported()) {
                int MAX_ZOOM = parameter.getMaxZoom();
                int currnetZoom = parameter.getZoom();
                if(zoomInOrOut && (currnetZoom <MAX_ZOOM && currnetZoom >=0)) {
                    parameter.setZoom(++currnetZoom);
                }
                else if(!zoomInOrOut && (currnetZoom <=MAX_ZOOM && currnetZoom >0)) {
                    parameter.setZoom(--currnetZoom);
                }
            }
            else
                Toast.makeText(getActivity(), "Zoom Not Avaliable", Toast.LENGTH_LONG).show();

            camera.setParameters(parameter);
        }
    }

    public interface onActionListener {
        public void onHideFragment();
        public void transitonToPosting(boolean fromCamera, Bitmap data, String videopath);
    }
}