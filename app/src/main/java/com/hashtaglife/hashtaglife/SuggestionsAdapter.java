package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by griffinanderson on 5/6/15.
 */
public class SuggestionsAdapter extends ArrayAdapter<Hashtag> {

    private LayoutInflater inflater;
    private final List<Hashtag> hashtags;
    private boolean showIcons = false;
    private boolean _inverted;

    public SuggestionsAdapter(Context context, List<Hashtag> hashtags, boolean inverted) {
        super(context, 0);
        inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        showIcons = false;
        _inverted = inverted;
        this.hashtags = hashtags;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        // If a view hasn't been provided inflate on
        if (view == null) {
            view = inflater.inflate(R.layout.item_grid_hashtags, parent, false);
            // Cache view components into the view holder
            holder = new ViewHolder();
            holder.hashtagName = (TextView) view.findViewById(R.id.text1);

            // Tag for lookup later
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Hashtag hashtag = getItem(position);

        TextView nameText = holder.hashtagName;
        String name = hashtag.getNameWithHashtag();
        nameText.setText(name);

        return view;
    }

    private static class ViewHolder {
        TextView hashtagName;
    }

}
