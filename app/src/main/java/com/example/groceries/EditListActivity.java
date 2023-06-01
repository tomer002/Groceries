package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.Keys.ITEMS;
import static com.example.groceries.Keys.LISTS;
import static com.example.groceries.Keys.NAME;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class EditListActivity extends AppCompatActivity {

    ArrayAdapter<Map<String, Object>> adapter;
    String listId;
    DatabaseReference listRef;
    DatabaseReference itemsRef;
    ValueEventListener itemListListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            load(snapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };
    ValueEventListener setTitleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            setTitle((String) snapshot.getValue());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        listId = getIntent().getStringExtra("list_id");
        database = FirebaseDatabase.getInstance();
        listRef = database.getReference(LISTS).child(listId);
        itemsRef = listRef.child(ITEMS);
        ListView listView = findViewById(R.id.listView);

        adapter = new GroceryAdapter(this, 0, 0, new ArrayList<>(), listRef.child(ITEMS));
        listView.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        itemsRef.addValueEventListener(itemListListener);
        listRef.child(NAME).addValueEventListener(setTitleEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        itemsRef.removeEventListener(itemListListener);
        listRef.child(NAME).removeEventListener(setTitleEventListener);
    }

    public void load(DataSnapshot dataSnapshot) {
        adapter.clear();
        for (DataSnapshot item : dataSnapshot.getChildren()) {
            adapter.add(
                    (Map<String, Object>) item.getValue()
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.element_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            ElementEditor.add(this, itemsRef, adapter);
            return true;
        }else if (item.getItemId() == R.id.list_settings){
            Intent intent = new Intent(this,ListSettingsActivity.class);
            intent.putExtra("list_id",listId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}