package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 7/15/15.
 */
public class UsernameAdapter extends ArrayAdapter<Vote> {

    private LayoutInflater inflater;
    private final List<Vote> votes;

    public UsernameAdapter(Context context, List<Vote> usernames) {
        super(context, 0);
        inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        votes = usernames;
    }

    @Override
    public int getCount() {
        return votes.size();
    }

    @Override
    public Vote getItem(int position) {
        return votes.get(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_list_username, parent, false);
            holder = new ViewHolder();
            holder.hashtagName = (TextView) view.findViewById(R.id.text1);
            holder.hashtagIcon = (ImageView) view.findViewById(R.id.image1);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final Vote vote = getItem(position);
        ImageView imageView = holder.hashtagIcon;
        TextView nameText = holder.hashtagName;

        int[] rainbow = this.getContext().getResources().getIntArray(R.array.colorPicker);
        Random rn = new Random();
        int picker = rn.nextInt(rainbow.length) + position;
        if(picker > rainbow.length - 1){
            picker = picker - rainbow.length;
        }

        imageView.setImageResource(0);
        if (vote.getVoterReaction()){
            imageView.setImageResource(R.drawable.icon_upvote);
        }else{
            imageView.setImageResource(R.drawable.icon_downvote);
        }
        imageView.setColorFilter(rainbow[picker]);
        nameText.setText(vote.getVoterName());
        nameText.setTextColor(rainbow[picker]);

        return view;
    }

    private static class ViewHolder {
        TextView hashtagName;
        ImageView hashtagIcon;
    }
}
