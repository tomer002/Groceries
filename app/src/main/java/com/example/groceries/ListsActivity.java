package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.LISTS;
import static com.example.groceries.Keys.LIST_IDS;
import static com.example.groceries.Keys.NAME;
import static com.example.groceries.Keys.USERS;

import android.app.Dialog;
import android.content.Intent;
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

public class ListsActivity extends AppCompatActivity {
    private ValueEventListener listLoader;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        listLoader = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadLists((List<Object>) snapshot.getValue(), adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        listsScreenSetup();
    }


    public void loadLists(List<Object> listIds, ListAdapter adapter) {
        DatabaseReference listsRef = database.getReference(LISTS);
        if (listIds == null) listIds = Collections.EMPTY_LIST;
        AtomicInteger listsLeft = new AtomicInteger(listIds.size());
        String[] names = new String[listIds.size()];
        for (int i = 0; i < listIds.size(); i++) {
            String idStr = (String) listIds.get(i);
            final int index = i;
            listsRef.child(idStr).child(NAME).get().addOnCompleteListener(
                    task -> {
                        if (!task.isSuccessful()) return;
                        names[index] = (String) task.getResult().getValue();
                        listsLeft.decrementAndGet();
                        if (listsLeft.get() == 0) {
                            adapter.clear();
                            adapter.addAll(names);
                        }
                    });
        }
    }

    public void listsScreenSetup() {
        setContentView(R.layout.activity_lists);
        ListAdapter adapter = new ListAdapter(this, 0, 0, new ArrayList<>());
        this.adapter = adapter;
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS + "/" + i)
                        .get().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) return;
                            String listId = (String) task.getResult().getValue();
                            editList(listId);
                        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS).addValueEventListener(listLoader);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS).removeEventListener(listLoader);
        }
    }

    public void editList(String id) {
        Intent intent = new Intent(ListsActivity.this, EditListActivity.class);
        intent.putExtra("list_id", id);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.create_list) {
            Dialog addDialog = new Dialog(this);
            addDialog.setTitle("Create list");
            addDialog.setContentView(R.layout.add_list_layout);
            addDialog.findViewById(R.id.cancel_button).setOnClickListener(v -> addDialog.cancel());

            addDialog.findViewById(R.id.create_button).setOnClickListener(v -> {
                String listName = ((EditText) addDialog.findViewById(R.id.list_name)).getText().toString();
                Utils.createNewList(listName);
                addDialog.cancel();
            });
            addDialog.show();
            return true;
        }
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}