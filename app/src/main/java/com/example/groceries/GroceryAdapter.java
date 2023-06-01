package com.example.groceries;

import static com.example.groceries.Keys.CHECKED;
import static com.example.groceries.Keys.NAME;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceryAdapter extends ArrayAdapter<Map<String, Object>> {
    Activity activity;
    LayoutInflater layoutInflater;
    DatabaseReference itemsRef;

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

        HashMap<String, Object> element = (HashMap<String, Object>) getItem(position);

        TextView name = view.findViewById(R.id.name);
        CompoundButton isChecked = view.findViewById(R.id.isChecked);
        View edit = view.findViewById(R.id.confirmButton);

        if (name != null) name.setText((String) element.get(NAME));

        if (isChecked != null) {
            isChecked.setOnCheckedChangeListener(new OnCheckedChangeListener(element, itemsRef, position));
            isChecked.setChecked((Boolean) element.get("checked"));
        }
        if (edit != null) {
            edit.setOnClickListener(v -> ElementEditor.edit(activity, element, itemsRef.child(String.valueOf(position)), this));
        }
        return view;
    }

    private static class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        HashMap<String, Object> element;
        int index;
        DatabaseReference itemsRef;

        public OnCheckedChangeListener(HashMap<String, Object> element, DatabaseReference itemsRef, int index) {
            this.element = element;
            this.itemsRef = itemsRef;
            this.index = index;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            itemsRef.child(String.valueOf(index)).child(CHECKED).setValue(b);
            element.put(CHECKED, b);
        }
    }
}
