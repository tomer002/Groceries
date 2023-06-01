package com.example.groceries;

import static com.example.groceries.Keys.NAME;

import android.app.Activity;
import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;

public class ElementEditor {
    public static void edit(Activity activity, Map<String, Object> element, DatabaseReference elementRef, ArrayAdapter<Map<String, Object>> adapter) {
        Dialog dialog = new Dialog(activity);
        dialog.setTitle("Edit grocery");
        dialog.setContentView(R.layout.edit_element_layout);
        dialog.setCancelable(true);
        TextView name = dialog.findViewById(R.id.NameEditText);
        name.setText((String) element.get(NAME));
        dialog.findViewById(R.id.confirmButton).setOnClickListener(v -> {
            //Log.d("txt", name.getText().toString());
            String nameStr = name.getText().toString();

            elementRef.child(NAME).setValue(nameStr);

            dialog.cancel();
        });
        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.cancel());
        dialog.findViewById(R.id.deleteButton).setOnClickListener(v -> {
            adapter.remove(element);
            Utils.uploadElementsToCloud(adapter, elementRef.getParent());
            dialog.cancel();
        });
        dialog.show();
        name.requestFocus();
    }

    public static void add(Activity activity, DatabaseReference itemsRef, ArrayAdapter<Map<String, Object>> adapter) {
        Dialog dialog = new Dialog(activity);
        dialog.setTitle("Add grocery");
        dialog.setContentView(R.layout.add_element_layout);
        dialog.setCancelable(true);
        dialog.findViewById(R.id.confirmButton).setOnClickListener(v -> {
            TextView name = dialog.findViewById(R.id.NameEditText);
            itemsRef.child(Integer.toString(adapter.getCount()))
                    .setValue(
                            Utils.createItem(name.getText().toString(), false)
                    );
            dialog.cancel();
        });
        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.cancel());
        dialog.show();
        dialog.findViewById(R.id.NameEditText).requestFocus();
    }
}
