package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.LISTS;
import static com.example.groceries.Keys.LIST_IDS;
import static com.example.groceries.Keys.NAME;
import static com.example.groceries.Keys.USERS;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The landing screen (after the user is logged in) where we can see all lists belong to the logged in user
 */
public class ListsActivity extends AppCompatActivity {
    private ValueEventListener listLoader; // listens to the value of the user's list_ids in order to load the lists to the adapter
    private ListAdapter adapter; // The array adapter of the lists
    private BatteryBroadcastReceiver batteryBroadcastReceiver; // a broadcast receiver that listens to the battery level and puts it in a text view
    private DatabaseReference listIdsRef; // Reference to the list_ids of the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        listIdsRef = database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS);

        listLoader = new ValueEventListener() {
            @Override
            // When something changes (usually when you are added to a list) in list_ids, reload the list_ids to the adapter.
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadLists((List<Object>) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        listsScreenSetup();
        // create new battery instance
        batteryBroadcastReceiver = new BatteryBroadcastReceiver(findViewById(R.id.battery_level));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the battery receiver to get the battery level when it changes
        registerReceiver(batteryBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryBroadcastReceiver); // unregister the battery receiver
    }

    /**
     * Adds the lists to the adapter
     *
     * @param listIds -- The list of the shopping list id's that the user belong to
     */
    public void loadLists(List<Object> listIds) {
        DatabaseReference listsRef = database.getReference(LISTS); // reference to the lists
        if (listIds == null) listIds = Collections.EMPTY_LIST;
        AtomicInteger listsLeft = new AtomicInteger(listIds.size()); // keep number of lists
        String[] names = new String[listIds.size()]; // The ordered lists names to be put in the adapter
        for (int i = 0; i < listIds.size(); i++) {
            String listId = (String) listIds.get(i); // The list id
            final int index = i;
            listsRef.child(listId).child(NAME).get().addOnCompleteListener(
                    task -> {
                        if (!task.isSuccessful()) return;
                        names[index] = (String) task.getResult().getValue();
                        listsLeft.decrementAndGet();
                        if (listsLeft.get() == 0) { // If it is the last list, add all list names to the adapter
                            adapter.clear();
                            adapter.addAll(names);
                        }
                    });
        }
    }

    /**
     * Setup the lists screen and register to know when one of the lists was clicked by the user
     */
    public void listsScreenSetup() {
        setContentView(R.layout.activity_lists);
        adapter = new ListAdapter(this, 0, 0, new ArrayList<>()); // Initialize the list adapter
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        // Set listener when one of the lists is clicked
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                listIdsRef.child(Integer.toString(i))
                        .get().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) return;
                            String listId = (String) task.getResult().getValue();
                            editList(listId); // Open the list edit screen
                        }));
    }

    /**
     * add the listLoader listener to list_ids
     */
    @Override
    protected void onStart() {
        super.onStart();
        listIdsRef.addValueEventListener(listLoader);
    }

    /**
     * remove the listLoader listener
     */
    @Override
    protected void onStop() {
        super.onStop();
        listIdsRef.removeEventListener(listLoader);
    }

    /**
     * Open a list editing screen
     *
     * @param id - The list id
     */
    public void editList(String id) {
        Intent intent = new Intent(ListsActivity.this, EditListActivity.class);
        intent.putExtra("list_id", id);// put the list id in the intent extras
        startActivity(intent);
    }


    /**
     * loads the main menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * called when the user select one of the options in the main menu and handles the selection
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.create_list) { // If the user selected to create a new list, open the dialog for creating a new list
            Dialog addDialog = new Dialog(this);
            addDialog.setTitle("Create list");
            addDialog.setContentView(R.layout.add_list_layout);
            addDialog.findViewById(R.id.cancel_button).setOnClickListener(v -> addDialog.cancel());

            addDialog.findViewById(R.id.create_button).setOnClickListener(v -> {
                String listName = ((EditText) addDialog.findViewById(R.id.list_name)).getText().toString();
                Utils.createNewList(listName); // Creating the new list when the create button is clicked
                addDialog.cancel();
            });
            addDialog.show();
            return true;
        }
        if (item.getItemId() == R.id.settings) { // If the user selected settings, open the setting screen
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}