package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.HAS_IMAGE;
import static com.example.groceries.Keys.LISTS;
import static com.example.groceries.Keys.LIST_IDS;
import static com.example.groceries.Keys.NAME;
import static com.example.groceries.Keys.NICKNAME;
import static com.example.groceries.Keys.USERNAME;
import static com.example.groceries.Keys.USERNAME_TO_ID;
import static com.example.groceries.Keys.USERS;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private ValueEventListener listLoader;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();


        database = FirebaseDatabase.getInstance();
        listLoader = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadLists((List<Object>) snapshot.getValue(), adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        if (mAuth.getCurrentUser() == null) {//user is not signed in
            registerScreenSetup();
        } else {//user is signed in
            homeScreenSetup();
        }
    }

    public void loadLists(List<Object> listIds, ListAdapter adapter) {
        DatabaseReference listsRef = database.getReference(LISTS);
        if (listIds==null)listIds = Collections.EMPTY_LIST;
        AtomicInteger listsLeft = new AtomicInteger(listIds.size());
        String[] names = new String[listIds.size()];
        for (int i = 0; i < listIds.size(); i++) {
            String idStr = (String) listIds.get(i);
            final int index = i;
            listsRef.child(idStr).child(NAME).get().addOnCompleteListener(
                    task -> {
                        if (!task.isSuccessful())return;
                        names[index] = (String) task.getResult().getValue();
                        listsLeft.decrementAndGet();
                        if (listsLeft.get()==0){
                            adapter.clear();
                            adapter.addAll(names);
                        }
                    });
        }
    }

    public void homeScreenSetup() {
        setContentView(R.layout.activity_main);
        ListAdapter adapter = new ListAdapter(this, 0, 0, new ArrayList<>());
        this.adapter = adapter;
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS + "/" + i)
                        .get().addOnCompleteListener(task -> {
                            if (!task.isSuccessful())return;
                            String listId = (String) task.getResult().getValue();
                            editList(listId);
                        }));
        //database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS)
        //        .addValueEventListener(listLoader);
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
        Intent intent = new Intent(MainActivity.this, EditListActivity.class);
        intent.putExtra("list_id", id);
        startActivity(intent);
    }

    public void registerScreenSetup() {
        setContentView(R.layout.register_layout);
        findViewById(R.id.register_submit_button).setOnClickListener(v -> createUser());
        findViewById(R.id.sign_in_button).setOnClickListener(v -> signInScreenSetup());
    }

    public void signInScreenSetup() {
        setContentView(R.layout.sign_in_layout);
        findViewById(R.id.sign_in_submit_button).setOnClickListener(v -> {
            EditText email = findViewById(R.id.email);
            EditText password = findViewById(R.id.password);
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, (task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                    homeScreenSetup();
                                    database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS).addValueEventListener(listLoader);
                                } else {
                                    Toast.makeText(MainActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                                }
                            })
                    );
        });
        findViewById(R.id.register_button).setOnClickListener(v -> registerScreenSetup());
    }

    public void createUser() {
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String nickname = ((EditText) findViewById(R.id.nickname)).getText().toString();

        if (!isUsernameLegal(username)) {
            Toast.makeText(this, "Illegal username. Usernames must only contain lowercase letters, numbers and underscores.", Toast.LENGTH_LONG).show();
            return;
        }
        database.getReference(USERNAME_TO_ID).child(username)
                        .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful())Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                        if (task.getResult().exists())Toast.makeText(MainActivity.this, "username already exists", Toast.LENGTH_SHORT).show();
                        else{
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                                var thisUserRef = database.getReference(USERS).child(mAuth.getUid());
                                                thisUserRef.child(HAS_IMAGE).setValue(false);
                                                thisUserRef.child(USERNAME).setValue(username);
                                                thisUserRef.child(NICKNAME).setValue(nickname);

                                                database.getReference(USERNAME_TO_ID).child(username).setValue(mAuth.getUid());
                                                homeScreenSetup();
                                                database.getReference(USERS + "/" + mAuth.getUid() + "/" + LIST_IDS).addValueEventListener(listLoader);
                                            } else {
                                                Toast.makeText(MainActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    public boolean isUsernameLegal(String username) {
        return username.matches("[a-z0-9_]+");
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
            addDialog.findViewById(R.id.cancel_button).setOnClickListener(v-> addDialog.cancel());

            addDialog.findViewById(R.id.create_button).setOnClickListener(v->{
                String listName = ((EditText)addDialog.findViewById(R.id.list_name)).getText().toString();
                Utils.createNewList(listName);
                addDialog.cancel();
            });
            addDialog.show();
            return true;
        }
        if (item.getItemId() == R.id.settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}