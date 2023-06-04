package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.USERNAME_TO_ID;

import android.app.Activity;
import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    public static void uploadElementsToCloud(ArrayAdapter<Map<String, Object>> adapter, DatabaseReference itemsRef) {
        var arr = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < adapter.getCount(); i++) {
            arr.add(adapter.getItem(i));
        }
        itemsRef.setValue(arr);
    }

    public static Map<String, Object> createItem(String name, boolean checked) {
        Map<String, Object> item = new HashMap<>(2);
        item.put(Keys.NAME, name);
        item.put(Keys.CHECKED, checked);
        return item;
    }

    public static void addToDatabaseList(DatabaseReference listRef, Object value) {
        listRef.get().addOnCompleteListener(
                task -> {
                    if (!task.isSuccessful())return;

                    List<Object> list = (List<Object>) task.getResult().getValue();
                    if (list==null)list = Collections.singletonList(value);
                    else list.add(value);
                    listRef.setValue(list);
                });
    }

    public static void createNewList(String name) {
        DatabaseReference listRef = database.getReference(Keys.LISTS).push();
        listRef.child(Keys.NAME).setValue(name);
        listRef.child(Keys.PARTICIPANTS).setValue(Collections.singletonList(mAuth.getUid()));

        DatabaseReference listIdsRef = database.getReference(Keys.USERS).child(mAuth.getUid()).child(Keys.LIST_IDS);
        Utils.addToDatabaseList(listIdsRef, listRef.getKey());
    }

    public static void addPersonToListByUsername(Dialog dialogToCloseOnSuccess, Activity activity, String listId, String username){
        database.getReference(USERNAME_TO_ID).child(username)
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        Toast.makeText(activity, "user does not exist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = (String) task.getResult().getValue();
                    addPersonToList(dialogToCloseOnSuccess, activity, listId, uid);
                });
    }

    public static void addPersonToList(Dialog dialogToCloseOnSuccess,Activity activity,String listId, String personId) {
        DatabaseReference participantsRef = database.getReference(Keys.LISTS)
                .child(listId)
                .child(Keys.PARTICIPANTS);

        participantsRef.get().addOnCompleteListener(
                task -> {
                    if (!task.isSuccessful())return;

                    List<Object> list = (List<Object>) task.getResult().getValue();
                    if (list==null)list=Collections.singletonList(personId);
                    else if (!list.contains(personId)) {
                        list.add(personId);
                    }else{
                        Toast.makeText(activity, "user already in list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    participantsRef.setValue(list);
                    dialogToCloseOnSuccess.cancel();
                });

        DatabaseReference listIdsRef = database.getReference(Keys.USERS)
                .child(personId)
                .child(Keys.LIST_IDS);
        Utils.addToDatabaseList(listIdsRef, listId);
    }
}
