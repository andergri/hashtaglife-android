package com.hashtaglife.hashtaglife;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by griffinanderson on 12/18/14.
 */
public class LocationAdapter<String> extends ArrayAdapter<String>  implements Filterable  {

    int[] rainbow;
    int picker;
    Random rn;
    List<String> list;
    List<Location> listLocations;
    Location loc;
    LocationFragment fragment;
    String filtered;
    EditText search;
    private ItemFilter mFilter = new ItemFilter();

    public LocationAdapter(Context context, int resource, int textViewResourceId, List<String> objects, LocationFragment locationFragment, EditText searchText) {
        super(context, resource, textViewResourceId, objects);
        rainbow = this.getContext().getResources().getIntArray(R.array.colorPicker);
        rn = new Random();
        picker = rn.nextInt(rainbow.length);
        list = objects;
        loc = new Location();
        listLocations = null;
        fragment = locationFragment;
        search = searchText;

    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("location", " " + position);
                if(list.get(position).equals("College / University") || list.get(position).equals("High School") || list.get(position).equals("Other") ){
                    filtered = list.get(position);
                    getLocation(list.get(position), (String) "");
                    turnOnText();
                }else{
                    if (list.get(position).equals("Back")){
                        list.clear();
                        list.add((String) "College / University");
                        list.add((String)"High School");
                        list.add((String)"Other");
                        filtered = null;
                        turnOffText();
                    }else {
                        turnOffText();
                        saveLocation(position);
                    }
                }
                //list.remove(position);
                notifyDataSetChanged();
            }
        });

        int pickera = picker + position;
        if(pickera > rainbow.length - 1){
            pickera = pickera - rainbow.length;
        }

        try {
            v.setBackgroundColor(rainbow[pickera]);
        }catch (Exception e){}

        return v;
    }

    private void  turnOnText(){
        search.setEnabled(true);
        search.setHint("Search for your school:");
        search.setFocusableInTouchMode(true);
        search.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
        search.setCursorVisible(true);
    }

    private void  turnOffText(){
        search.setText("");
        search.setHint("Choose your school:");
        search.setEnabled(false);
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.setCursorVisible(false);
    }

    private void getLocation(String filter, String search){

        if(filtered == null ) {
            return;
        }

            if (((java.lang.String) search).length() > 0) {
            search = (String) capitalizeLetter((java.lang.String) search);
        }

        loc.locationInBackground((java.lang.String) filter, (java.lang.String) search, new FindCallback<Location>() {
            @Override
            public void done(List<Location> locations, ParseException e) {
                list.clear();
                listLocations = null;
                listLocations = locations;
                for (int i=0; i<locations.size(); i++) {
                    list.add((String) locations.get(i).getName());
                }
                list.add((String) "Back");
                notifyDataSetChanged();
            }
        });
    }

    private void saveLocation(int position){

        Location location = listLocations.get(position);
        loc.updateLocation(location);

        fragment.mCallback.onHideFragment();
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = (String) constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<String> nlist = new ArrayList<String>(1);
            nlist.add(filterString);

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<String> filteredData = (ArrayList<String>) results.values;
            getLocation(filtered, filteredData.get(0));
            notifyDataSetChanged();
        }
    }

    private java.lang.String capitalizeLetter(java.lang.String searchResult){

        StringBuffer res = new StringBuffer();

        searchResult = searchResult.toLowerCase();
        java.lang.String[] strArr = searchResult.split(" ");
        for (java.lang.String str : strArr) {
            str = str.trim();
            if (!str.equals("of") &&
                    !str.equals("for") &&
                    !str.equals("the") &&
                    str.length() > 1 ) {
                char[] stringArray = str.toCharArray();
                stringArray[0] = Character.toUpperCase(stringArray[0]);
                str = new java.lang.String(stringArray);
            }
            res.append(str).append(" ");
        }
        res = res.replace(res.length() - 1, res.length(), "");
        System.out.println("Text [" + res + "]");
        return java.lang.String.valueOf(res);
    }
}
