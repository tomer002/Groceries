package com.example.groceries;

import static com.example.groceries.Keys.NAME;

import android.app.Activity;
import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;

/**
 * A class used when the user wants to edit or add an item to a shopping list
 */
public class ElementEditor {
    /**
     * open a dialog to edit an existing item in a list
     * @param activity current activity
     * @param element the element that should be edit
     * @param elementRef reference to the element in the database
     * @param adapter the adapter that contains the items
     */
    public static void edit(Activity activity, Map<String, Object> element, DatabaseReference elementRef, ArrayAdapter<Map<String, Object>> adapter) {
        Dialog dialog = new Dialog(activity);
        dialog.setTitle("Edit grocery");
        dialog.setContentView(R.layout.edit_element_layout);
        dialog.setCancelable(true);
        TextView name = dialog.findViewById(R.id.NameEditText);
        name.setText((String) element.get(NAME));
        dialog.findViewById(R.id.confirmButton).setOnClickListener(v -> {
            String nameStr = name.getText().toString(); // get the new item name

            elementRef.child(NAME).setValue(nameStr); //change the name in the database

            dialog.cancel();
        });
        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.cancel());
        dialog.findViewById(R.id.deleteButton).setOnClickListener(v -> {
            adapter.remove(element); // remove the item
            Utils.uploadElementsToCloud(adapter, elementRef.getParent());// upload the list without the item that was removed
            dialog.cancel();
        });
        dialog.show();
        name.requestFocus();
    }

    /**
     * Opem a dialog for adding a new element to the list
     *
     * @param activity current activity
     * @param itemsRef reference to the array of the groceries in the list
     * @param adapter  the adapter that contains the items
     */
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
                    );// add the item to the database
            dialog.cancel();
        });
        dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.cancel());
        dialog.show();
        dialog.findViewById(R.id.NameEditText).requestFocus();
    }
}
