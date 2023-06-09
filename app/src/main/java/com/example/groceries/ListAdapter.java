package com.example.groceries;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Adapter for the list names that the user is the editor of
 */
public class ListAdapter extends ArrayAdapter<String> {
    LayoutInflater layoutInflater; //  the layout inflater

    public ListAdapter(Activity activity, int resource, int textViewResourceId, List<String> listNames) {//, DatabaseReference listIdsRef){
        super(activity, resource, textViewResourceId, listNames);
        layoutInflater = activity.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = layoutInflater.inflate(R.layout.list_view, parent, false);
        }

        String listName = getItem(position);
        TextView listNameTextView = view.findViewById(R.id.list_name);
        listNameTextView.setText(listName); // edit the view with the list name
        return view;
    }
}
