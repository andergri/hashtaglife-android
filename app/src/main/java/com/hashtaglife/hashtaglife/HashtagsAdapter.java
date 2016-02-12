package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 12/3/14.
 */
public class HashtagsAdapter extends ArrayAdapter<Hashtag> {

    private LayoutInflater inflater;
    private final List<Hashtag> hashtags;
    private boolean showIcons = false;
    private boolean _inverted;
    private Subscribe subscribe;

    public HashtagsAdapter(Context context, List<Hashtag> hashtags, boolean inverted) {
        super(context, 0);
        inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        showIcons = false;
        _inverted = inverted;
        this.hashtags = hashtags;
        subscribe = new Subscribe();
        subscribe.loadSubscribe();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        // If a view hasn't been provided inflate on
        if (view == null) {
            view = inflater.inflate(R.layout.item_list_hashtags, parent, false);
            // Cache view components into the view holder
            holder = new ViewHolder();
            holder.hashtagName = (TextView) view.findViewById(R.id.text1);
            holder.hashtagIcon = (ImageView) view.findViewById(R.id.image1);
            holder.hashtagBackground = (RelativeLayout) view.findViewById(R.id.background);
            holder.followButton = (RelativeLayout) view.findViewById(R.id.followButton);
            holder.followText = (TextView) view.findViewById(R.id.follow);
            holder.followCount = (TextView) view.findViewById(R.id.followCount);
            holder.inboxIcon = (Button) view.findViewById(R.id.inboxIcon);

            // Tag for lookup later
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Hashtag hashtag = getItem(position);

        holder.followButton.setVisibility(View.VISIBLE);
        ImageView imageView = holder.hashtagIcon;
        TextView nameText = holder.hashtagName;
        RelativeLayout backgound = holder.hashtagBackground;
        String name = hashtag.getNameWithHashtag();
        TextView followLabel = holder.followText;
        TextView followCount = holder.followCount;

        int[] rainbow = this.getContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length) + position;
        if(picker > rainbow.length - 1){
            picker = picker - rainbow.length;
        }

        if(name.equals("#popular") || name.equals("#recent") || name.equals("#my photos")){
            name = hashtag.getName();
            showIcons = true;
            holder.followButton.setVisibility(View.INVISIBLE);
        }
        //if (!((Hashtag)getItem(0)).getName().equals("popular")){
        //    showIcons = false;
        //}


        if (subscribe.isHashtagInSubscribe(name.substring(1, name.length()))){
            followCount.setTextColor(Color.WHITE);
            followLabel.setTextColor(Color.WHITE);
            followLabel.setText("following");
            followCount.setText(String.valueOf(hashtag.getFollowers() + 1));
        }else{
            followCount.setTextColor(getContext().getResources().getColor(R.color.transparentblack));
            followLabel.setTextColor(getContext().getResources().getColor(R.color.transparentblack));
            followLabel.setText("follow");
            followCount.setText(String.valueOf(hashtag.getFollowers()));
        }
        holder.inboxIcon.setVisibility(View.INVISIBLE);
        if (name != null) {
            imageView.setImageResource(0);
            nameText.setText(name);

            if (showIcons) {
                switch (name) {
                    case "popular":
                        imageView.setImageResource(R.drawable.design_star);
                        break;
                    case "recent":
                        imageView.setImageResource(R.drawable.design_clock);
                        break;
                    //case 2:
                    //    imageView.setImageResource(R.drawable.design_trending);
                    //    break;
                    //case 3:
                    //    imageView.setImageResource(R.drawable.design_trending);
                    //    break;
                    //case 4:
                    //    imageView.setImageResource(R.drawable.design_trending);
                    //    break;
                    default:
                        imageView.setImageResource(0);
                        break;
                }
            }
        }

        if(hashtag.isInbox()){
            holder.followButton.setVisibility(View.INVISIBLE);
            holder.inboxIcon.setText("1");
            holder.inboxIcon.setVisibility(View.VISIBLE);
            holder.inboxIcon.setTextColor(rainbow[picker]);
            ((GradientDrawable)holder.inboxIcon.getBackground()).setStroke(8, rainbow[picker]);
        }

        holder.followButton.setClickable(true);
        holder.followButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hashtag = String.valueOf(holder.hashtagName.getText());
                hashtag = hashtag.substring(1, hashtag.length());
                subscribe.toggleFollow(hashtag);
                notifyDataSetChanged();
            }
        });


        if (!_inverted) {
            backgound.setBackgroundColor(rainbow[picker]);
            nameText.setTextColor(Color.WHITE);
        }else{
            backgound.setBackgroundColor(Color.WHITE);
            nameText.setTextColor(rainbow[0]);
            imageView.setImageResource(R.drawable.design_globe);
            imageView.setColorFilter(rainbow[0]);
            imageView.setPadding(10,10,10,10);
        }
        return view;
    }

    private static class ViewHolder {
        TextView hashtagName;
        ImageView hashtagIcon;
        RelativeLayout hashtagBackground;
        RelativeLayout followButton;
        TextView followText;
        TextView followCount;
        Button inboxIcon;
    }
}
