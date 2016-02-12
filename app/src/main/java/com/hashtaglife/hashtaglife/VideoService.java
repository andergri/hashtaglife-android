package com.hashtaglife.hashtaglife;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.parse.ParseFile;

import java.io.FileInputStream;

public class VideoService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    public static final String ACTION_STRING_ACTIVITY = "ToActivity";
    public static final String VIDEO_SERVICE_PLAYING = "VideoServicePlaying";
    public static final String VIDEO_SERVICE_ERROR = "VideoServiceError";
    public static final String VIDEO_SERVICE_BUFFERING = "VideoServiceBuffering";

    private MediaPlayer player;
    private Selfie currentVideo;
    private final IBinder videoBind = new VideoBinder();

    public VideoService() {
    }

    private void sendBroadcast(String type) {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_STRING_ACTIVITY);
        new_intent.putExtra("type", type);
        sendBroadcast(new_intent);
    }

    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        currentVideo=null;
        //create player
        player = new MediaPlayer();
        player.setLooping(true);

        initVideoPlayer();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return videoBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void initVideoPlayer(){
        //set player properties
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnInfoListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        sendBroadcast(VIDEO_SERVICE_ERROR);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        Handler myHandler = new Handler();
        myHandler.postDelayed(isNowPlaying, 200);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                break;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    public class VideoBinder extends Binder {
        VideoService getService() {
            return VideoService.this;
        }
    }

    // Player

    public void playVideo(){
        //play a song
        player.reset();
        player.setLooping(true);

        //get song
        Selfie playVideo = currentVideo;

        ParseFile file = playVideo.getVideo();
        String url = String.valueOf(file.getUrl());
        Log.d("set Video ", url);

        FileInputStream fis = playVideo.getFis();
        Log.d("FIS: ", String.valueOf(fis));
        try{
            if (fis != null && fis.getFD().valid()){
                Log.d("FIS: ", "YES");
                player.setDataSource(fis.getFD());
            }else{
                Log.d("FIS: ", "NO");
                sendBroadcast(VIDEO_SERVICE_BUFFERING);
                player.setDataSource(url);
            }
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }


    public void setVideo(Selfie selfie){
        currentVideo=selfie;
    }

    public void pauseVideo(){

        if (player.isPlaying())
            player.pause();
        player.stop();
    }

    public void setSurface(SurfaceHolder holder){
        player.setDisplay(holder);
    }

    private Runnable isNowPlaying = new Runnable()
    {
        @Override
        public void run() {
            sendBroadcast(VIDEO_SERVICE_PLAYING);
        }
    };
}
