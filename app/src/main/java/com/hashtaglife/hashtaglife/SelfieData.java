package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by griffinanderson on 8/12/15.
 */

interface TransactionAction {
    public void perform();
}

public class SelfieData {

    private Context context;
    private Bundle arguments;
    private TextView selfieSelectedHashtag;
    private ProgressBar selfieProgressBar;
    private Integer currentPlace = 0;
    private List<Selfie> currentPhotos;

    public SelfieData(Context context, Bundle arguments, TextView selfieSelectedHashtag, ProgressBar selfieProgressBar){
        this.context = context;
        this.arguments = arguments;
        this.selfieSelectedHashtag = selfieSelectedHashtag;
        this.selfieProgressBar = selfieProgressBar;
        this.selfieProgressBar.setProgress(0);
        this.selfieProgressBar.setMax(100);
        currentPlace = 0;
        currentPhotos = null;

    }

    public void loadContent(TransactionAction transactionAction){
        String clickedHashtag = arguments.getString("hashtag");

        Log.d("Selife Fragment: hashtag=", clickedHashtag);
        selfieSelectedHashtag.setText(clickedHashtag);

        if (clickedHashtag.equals("popular")){
            loadPopularPhotos(transactionAction);
        }else if(clickedHashtag.equals("recent")){
            loadRecentPhotos(transactionAction);
        }else if(clickedHashtag.equals("my photos")){
            loadMyPhotos(transactionAction);
        }else if(arguments.getString("selfieId") != null){
            loadAPhoto(transactionAction);
        }else{
            loadHashtagPhotos(clickedHashtag.substring(1), transactionAction);
        }
    }

    public void loadAPhoto(final TransactionAction transactionAction){
        String selfieId = arguments.getString("selfieId");
        Selfie.loadObjectInBackground(selfieId, new FindCallback<Selfie>() {
            @Override
            public void done(List<Selfie> selfies, ParseException e) {
                if (selfies != null) {

                    currentPhotos = selfies;
                    transactionAction.perform();
                }
            }
        });
    }

    public void loadRecentPhotos(final TransactionAction transactionAction) {
        currentPlace = 0;
        boolean filter = arguments.getBoolean("filtered");
        Selfie.loadFreshInBackground(filter, new FindCallback<Selfie>() {

            @Override
            public void done(List<Selfie> selfies, ParseException e) {
                if (selfies != null) {

                    currentPhotos = selfies;
                    transactionAction.perform();
                }
            }
        });
    }

    public void loadPopularPhotos(final TransactionAction transactionAction) {
        currentPlace = 0;
        boolean filter = arguments.getBoolean("filtered");
        Selfie.loadPopularInBackground(filter, new FindCallback<Selfie>() {

            @Override
            public void done(List<Selfie> selfies, ParseException e) {

                if (selfies != null) {

                    currentPhotos = selfies;
                    transactionAction.perform();
                }
            }
        });
    }

    public void loadMyPhotos(final TransactionAction transactionAction) {
        currentPlace = 0;
        Selfie.loadUserPhotosInBackground(new FindCallback<Selfie>() {

            @Override
            public void done(List<Selfie> selfies, ParseException e) {
                if (selfies != null) {

                    currentPhotos = selfies;
                    transactionAction.perform();
                }
            }
        });
    }

    public void loadHashtagPhotos(String hashtag, final TransactionAction transactionAction) {
        currentPlace = 0;
        boolean filter = arguments.getBoolean("filtered");
        Selfie.loadHashtagInBackground(filter, hashtag, new FindCallback<Selfie>() {

            @Override
            public void done(List<Selfie> selfies, ParseException e) {
                if (selfies != null) {

                    currentPhotos = selfies;
                    transactionAction.perform();
                }
            }
        });
    }

    public Selfie dataBack(){
        currentPlace--;
        currentPlace--;
        Selfie temp = null;
        if(currentPlace >= 0) {
            temp = currentPhotos.get(currentPlace);
            currentPlace++;
        }
        return temp;
    }

    public Selfie dataForward(){
        Selfie temp = null;
        if (currentPlace < currentPhotos.size() && currentPhotos.size() > 0) {
            temp = currentPhotos.get(currentPlace);
            currentPlace++;
        }
        return temp;
    }

    // Load the image file
    public void getImageData() {

        try {
            //selfies.size()
            final int max = 4;
            final int[] currentProgress = {0};
            //temp
            selfieProgressBar.setProgress(0);

            if ((currentPlace + max) < currentPhotos.size()) {
                for (int i = currentPlace; i < (currentPlace + max); i++) {

                    Selfie selfie = currentPhotos.get(i);
                    ParseFile fileObject = selfie.getImage();
                    final int finalI = i;
                    fileObject.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                // Decode the Byte[] into
                                // Bitmap
                                /**if(!isAdded()) {
                                    return;
                                }**/
                                Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                                if (b != null && currentPhotos != null) {
                                    currentPhotos.get(finalI).setBmp(b);
                                }

                                currentProgress[0] += 25;
                                selfieProgressBar.setProgress(currentProgress[0]);
                            } else {
                                //currentPhotosImage.set(finalI, null);
                                Log.d("test",
                                        "There was a problem downloading the data.");
                            }
                        }
                    }, new ProgressCallback() {
                        @Override
                        public void done(Integer integer) {
                            selfieProgressBar.setProgress(integer);
                        }
                    });
                }
            }
            if (0 < (currentPlace - max)) {
                for (int i = 0; i < (currentPlace - max); i++) {
                    currentPhotos.get(i).setBmp(null);
                }
            }
            if ((currentPlace + max) < currentPhotos.size()) {
                for (int i = (currentPlace + max); i < currentPhotos.size(); i++) {
                    currentPhotos.get(i).setBmp(null);
                }
            }
        }catch (Exception e){
        }
    }

    // Video Data
    public void getVideoData() {

        try {
            //selfies.size()
            final int max = 4;

            if ((currentPlace + max) < currentPhotos.size()) {
                for (int i = currentPlace; i < (currentPlace + max); i++) {

                    Selfie selfie = currentPhotos.get(i);
                    if (selfie.getVideo() != null) {
                        ParseFile fileObject = selfie.getVideo();
                        final int finalI = i;
                        fileObject.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    // Decode the Byte[] into
                                    // Bitmap

                                    try {
                                        File tempMp4 = File.createTempFile("hashtaglifevideo" + String.valueOf(finalI),
                                                "mp4", context.getCacheDir());
                                        tempMp4.deleteOnExit();
                                        FileOutputStream fos = new FileOutputStream(tempMp4);
                                        fos.write(data);
                                        fos.close();

                                        FileInputStream fis = new FileInputStream(tempMp4);
                                        if (fis != null && currentPhotos != null) {
                                            currentPhotos.get(finalI).setFis(fis);
                                        }

                                    } catch (IOException ex) {
                                        String s = ex.toString();
                                        ex.printStackTrace();
                                    }


                                } else {
                                    //currentPhotosImage.set(finalI, null);
                                    Log.d("test",
                                            "There was a problem downloading the data.");
                                }
                            }
                        }, new ProgressCallback() {
                            @Override
                            public void done(Integer integer) {
                                selfieProgressBar.setProgress(integer);
                            }
                        });
                    }
                }
            }
            if (0 < (currentPlace - max)) {
                for (int i = 0; i < (currentPlace - max); i++) {
                    currentPhotos.get(i).setFis(null);
                }
            }
            if ((currentPlace + max) < currentPhotos.size()) {
                for (int i = (currentPlace + max); i < currentPhotos.size(); i++) {
                    currentPhotos.get(i).setFis(null);
                }
            }
        }catch (Exception e){
        }
    }

}
