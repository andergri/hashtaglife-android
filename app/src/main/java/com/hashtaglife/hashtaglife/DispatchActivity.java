package com.hashtaglife.hashtaglife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.parse.ParseUser;

@DeepLink("hashtaglife://hashtaglife.com")
public class DispatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
/**
        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                }else{

                    Log.i("MyApp", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
**/

        if (ParseUser.getCurrentUser() != null) {
            // Start an intent for the logged in activity

            Intent intent = new Intent(this, ContainerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            // Start and intent for the logged out activity
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
/**
    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }**/

    @Override
    public void onResume() {
        super.onResume();

        if (ParseUser.getCurrentUser() != null) {
            // Start an intent for the logged in activity

            Intent intent = new Intent(this, ContainerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            // Start and intent for the logged out activity
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
