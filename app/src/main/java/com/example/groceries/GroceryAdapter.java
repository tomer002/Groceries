package com.example.groceries;

import static com.example.groceries.Keys.CHECKED;
import static com.example.groceries.Keys.NAME;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The adapter to be used in the groceries list view
 */
public class GroceryAdapter extends ArrayAdapter<Map<String, Object>> {
    Activity activity; // current activity
    LayoutInflater layoutInflater; // the layout inflater of the activity
    DatabaseReference itemsRef; // database reference to the groceries array

    public GroceryAdapter(Activity activity, int resource, int textViewResourceId, @NonNull List<Map<String, Object>> objects, DatabaseReference itemsRef) {
        super(activity, resource, textViewResourceId, objects);
        this.activity = activity;
        this.itemsRef = itemsRef;
        layoutInflater = activity.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = layoutInflater.inflate(R.layout.element_view, parent, false);
        }

        HashMap<String, Object> element = (HashMap<String, Object>) getItem(position); // gets the required item

        TextView name = view.findViewById(R.id.name);
        CompoundButton isChecked = view.findViewById(R.id.isChecked);
        Button edit = view.findViewById(R.id.confirmButton);

        // Put values in UI
        name.setText((String) element.get(NAME));
        isChecked.setChecked((Boolean) element.get(CHECKED));

        // put listener to edit button and is checked button
        isChecked.setOnCheckedChangeListener(new OnCheckedChangeListener(element, itemsRef, position));
        edit.setOnClickListener(v -> ElementEditor.edit(activity, element, itemsRef.child(String.valueOf(position)), this));

        return view;
    }

    /**
     * A listener to when the user checks or unchecks an item
     */
    private static class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        HashMap<String, Object> element; // The grocery
        int index; // the index of the grocery in the adapter
        DatabaseReference itemsRef; // reference to the items

        public OnCheckedChangeListener(HashMap<String, Object> element, DatabaseReference itemsRef, int index) {
            this.element = element;
            this.itemsRef = itemsRef;
            this.index = index;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            itemsRef.child(String.valueOf(index)).child(CHECKED).setValue(b);// set the new value of the is checked
            element.put(CHECKED, b); // edit the element
        }
    }
}
