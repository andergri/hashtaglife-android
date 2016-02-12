package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 12/17/14.
 */
public class PostAdapter<String> extends ArrayAdapter<String> {

    int[] rainbow;
    int picker;
    Random rn;
    List<String> list;
    NewPhotoFragment _newPhotoFragment;

    public PostAdapter(Context context, int resource, int textViewResourceId, List<String> objects, NewPhotoFragment fragment) {
        super(context, resource, textViewResourceId, objects);
        rainbow = this.getContext().getResources().getIntArray(R.array.colorPicker);
        rn = new Random();
        picker = rn.nextInt(rainbow.length);
        list = objects;
        this._newPhotoFragment = fragment;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        final ImageView exitImage = (ImageView)v.findViewById(R.id.image1);
        final ImageView cautionImage = (ImageView)v.findViewById(R.id.image2);

        if (_newPhotoFragment.shouldShowCaution((java.lang.String) list.get(position))){
            cautionImage.setImageResource(R.drawable.item_caution);
        }else{
            cautionImage.setImageBitmap(null);
        }

        exitImage.setImageResource(R.drawable.icon_exit);
        exitImage.setClickable(true);
        exitImage.setPadding(15, 15, 15, 15);
        exitImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);
                notifyDataSetChanged();
                _newPhotoFragment.checkHeader();
            }
        });

        int pickera = picker + position;
        if(pickera > rainbow.length - 1){
            pickera = pickera - rainbow.length;
        }

        v.setBackgroundColor(rainbow[pickera]);

        return v;

    }
}
