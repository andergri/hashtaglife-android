package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

/**
 * Created by griffinanderson on 8/12/15.
 */
public class SelfieFlag {

    private Context context;
    private ImageButton selfieFlag;

    // Current Item
    private Selfie flagSelfie;
    private PopupMenu popupFlag;

    public SelfieFlag(Context context, ImageButton selfieFlag) {
        this.context = context;
        this.selfieFlag = selfieFlag;
        this.selfieFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapFlag();
            }
        });
    }

    public void resetWithSelfie(Selfie selfie){
        flagSelfie = selfie;
        selfieFlag.setImageResource(R.drawable.icon_openflag);
        selfieFlag.setTag("open");
    }

    public void tapFlag(){
        popupFlag = new PopupMenu(context, selfieFlag);
        if(selfieFlag.getTag().equals("open")){
            if(flagSelfie.didUserCreate()){
                popupFlag.getMenuInflater().inflate(R.menu.menu_flag_delete, popupFlag.getMenu());
            }else{
                popupFlag.getMenuInflater().inflate(R.menu.menu_flag, popupFlag.getMenu());
            }
        }else if(selfieFlag.getTag().equals("full")){
            popupFlag.getMenuInflater().inflate(R.menu.menu_flag_undo, popupFlag.getMenu() );
        }else{
        }
        popupFlag.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(context, "" + item.getTitle(), Toast.LENGTH_SHORT).show();
                if(!item.getTitle().equals("Undo Report")){
                    selfieFlag.setImageResource(R.drawable.icon_fullflag);
                    selfieFlag.setTag("full");
                    flagSelfie.addFlag(Boolean.TRUE, flagSelfie, String.valueOf(item.getTitle()));
                }else{
                    selfieFlag.setImageResource(R.drawable.icon_openflag);
                    selfieFlag.setTag("open");
                }
                return true;
            }
        });
        popupFlag.show();
    }
}
