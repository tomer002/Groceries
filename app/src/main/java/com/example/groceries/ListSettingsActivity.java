package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.HAS_IMAGE;
import static com.example.groceries.Keys.LISTS;
import static com.example.groceries.Keys.LIST_IDS;
import static com.example.groceries.Keys.NAME;
import static com.example.groceries.Keys.NICKNAME;
import static com.example.groceries.Keys.PARTICIPANTS;
import static com.example.groceries.Keys.USERNAME;
import static com.example.groceries.Keys.USERS;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListSettingsActivity extends AppCompatActivity {

    UserAdapter adapter;
    String listId;
    FirebaseStorage storage;
    DatabaseReference participantsRef;
    String listName;
    ChildEventListener editorsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            UserAdapter.User user = new UserAdapter.User();
            user.uid = (String) snapshot.getValue();
            database.getReference(USERS).child(user.uid).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) return;
                HashMap<String, Object> userMap = (HashMap<String, Object>) task.getResult().getValue();
                user.username = (String) userMap.get(USERNAME);
                user.nickname = (String) userMap.get(NICKNAME);
                if ((Boolean) userMap.get(HAS_IMAGE)) {
                    storage.getReference("profile_pictures").child(user.uid).getBytes(1024 * 1024)
                            .addOnCompleteListener(task1 -> {
                                if (!task1.isSuccessful()) {
                                    user.image = null;
                                } else {
                                    byte[] bytes = task1.getResult();
                                    user.image = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                }
                                adapter.add(user);
                            });
                } else {
                    user.image = null;
                    adapter.add(user);
                }
            });
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_settings);

        storage = FirebaseStorage.getInstance();

        ListView editorListView = findViewById(R.id.editors_list);
        ArrayList<UserAdapter.User> usersList = new ArrayList<>();

        listId = getIntent().getStringExtra("list_id");
        findViewById(R.id.leave_list).setOnClickListener(v -> leaveList(listId));

        findViewById(R.id.add_editor).setOnClickListener(v -> {
            Dialog addEditorDialog = new Dialog(this);
            addEditorDialog.setTitle("Add Editor");
            addEditorDialog.setContentView(R.layout.add_editor_layout);
            addEditorDialog.findViewById(R.id.cancel).setOnClickListener(v0 -> addEditorDialog.cancel());
            addEditorDialog.findViewById(R.id.add).setOnClickListener(v0 -> {
                EditText username = addEditorDialog.findViewById(R.id.username);
                Utils.addPersonToListByUsername(addEditorDialog, this, listId, username.getText().toString());
                addEditorDialog.cancel();
            });
            addEditorDialog.show();
        });


        participantsRef = database.getReference(LISTS).child(listId).child(PARTICIPANTS);
        adapter = new UserAdapter(this, 0, 0, usersList);
        editorListView.setAdapter(adapter);


        database.getReference(LISTS + "/" + listId + "/" + NAME).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;
            listName = (String) task.getResult().getValue();
            ((TextView) findViewById(R.id.list_name)).setText("List name: " + listName);
        });
        findViewById(R.id.edit_list_name).setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Edit list name");
            dialog.setContentView(R.layout.edit_list_name_layout);
            EditText listNameEditText = dialog.findViewById(R.id.list_name);
            listNameEditText.setText(listName);
            listNameEditText.setSelection(listName.length());
            dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.cancel());
            dialog.findViewById(R.id.confirm).setOnClickListener(view -> {
                String newName = listNameEditText.getText().toString();
                if (newName.equals(listName)) {
                    dialog.cancel();
                    return;
                }
                database.getReference(LISTS + "/" + listId + "/" + NAME).setValue(newName);
                listName = newName;

                ((TextView) findViewById(R.id.list_name)).setText("List name: " + listName);

                dialog.cancel();
            });
            dialog.show();
        });

    }
    public void leaveList(String listId){
        String uid=mAuth.getUid();
        database.getReference(USERS).child(uid).child(LIST_IDS).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful())return;
            List<Object> listIds= (List<Object>) task.getResult().getValue();
            listIds.remove(listId);
            task.getResult().getRef().setValue(listIds);

            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        });
        DatabaseReference listRef=database.getReference(LISTS).child(listId);
        listRef.child(PARTICIPANTS).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful())return;
            List<Object> participants = (List<Object>) task.getResult().getValue();
            participants.remove(uid);
            if (participants.isEmpty()){
                listRef.removeValue();
            }
            task.getResult().getRef().setValue(participants);
        });
    }

    @Override
    protected void onStart() {
        participantsRef.addChildEventListener(editorsListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        participantsRef.removeEventListener(editorsListener);
        super.onStop();
    }
}