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

/**
 * The utils class is used throughout the program and contains methods that are useful to the program
 */
public class Utils {
    /**
     * Upload all items in a grocery adapter and put it in the firebase database
     *
     * @param adapter  adapter that contains the groceries that should be uploaded to the database
     * @param itemsRef
     */
    public static void uploadElementsToCloud(ArrayAdapter<Map<String, Object>> adapter, DatabaseReference itemsRef) {
        var arr = new ArrayList<Map<String, Object>>(); // Array of the groceries
        for (int i = 0; i < adapter.getCount(); i++) {
            arr.add(adapter.getItem(i));
        }
        itemsRef.setValue(arr);
    }

    /**
     * Create a hashmap that represents an item that can later be uploaded to the database
     *
     * @param name    grocery name
     * @param checked grocery picked or not
     * @return a hashmap that represents an item that can later be uploaded to the database
     */
    public static Map<String, Object> createItem(String name, boolean checked) {
        Map<String, Object> item = new HashMap<>(2); // item hashmap
        item.put(Keys.NAME, name);
        item.put(Keys.CHECKED, checked);
        return item;
    }

    /**
     * Add item to existing ot not existing array in the database
     *
     * @param listRef - array reference
     * @param value   - the value that should be added
     */
    public static void addToDatabaseList(DatabaseReference listRef, Object value) {
        listRef.get().addOnCompleteListener( // get the list from the database
                task -> {
                    if (!task.isSuccessful()) return;

                    List<Object> list = (List<Object>) task.getResult().getValue();
                    if (list == null)
                        list = Collections.singletonList(value); // if list is empty create a new list
                    else list.add(value); // if the list exist add the value to the list
                    listRef.setValue(list); // put the list in the cloud database
                });
    }

    /**
     * create a new list in the database with the current user as the only editor
     *
     * @param name the list name
     */
    public static void createNewList(String name) {
        DatabaseReference listRef = database.getReference(Keys.LISTS).push(); // create a reference to the list with a random key, this key is the list id
        listRef.child(Keys.NAME).setValue(name); // set the name of the list
        listRef.child(Keys.PARTICIPANTS).setValue(Collections.singletonList(mAuth.getUid())); // set the participants list

        DatabaseReference listIdsRef = database.getReference(Keys.USERS).child(mAuth.getUid()).child(Keys.LIST_IDS); // reference to the list_ids of the user
        Utils.addToDatabaseList(listIdsRef, listRef.getKey()); // Add the list id to the array
    }

    /**
     * Add a participant to the list by his username
     *
     * @param dialogToCloseOnSuccess Dialog that will be close if the user is successfully added to the list
     * @param activity               the current activity
     * @param listId                 the id of the list
     * @param username               the username of the user
     */
    public static void addPersonToListByUsername(Dialog dialogToCloseOnSuccess, Activity activity, String listId, String username) {
        database.getReference(USERNAME_TO_ID).child(username)
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) { //if the user does not exist show a message and return
                        Toast.makeText(activity, "user does not exist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = (String) task.getResult().getValue(); // get the user id that matches the username
                    addPersonToList(dialogToCloseOnSuccess, activity, listId, uid); // Add the user to the list
                });
    }

    /**
     * add a user to a list
     *
     * @param dialogToCloseOnSuccess Dialog that will be close if the user is successfully added to the list
     * @param activity               the current activity
     * @param listId                 the id of the list
     * @param personId               the username of the user
     */
    public static void addPersonToList(Dialog dialogToCloseOnSuccess, Activity activity, String listId, String personId) {
        DatabaseReference participantsRef = database.getReference(Keys.LISTS) // reference to the list participants
                .child(listId)
                .child(Keys.PARTICIPANTS);

        participantsRef.get().addOnCompleteListener(
                task -> {
                    if (!task.isSuccessful()) return;

                    List<Object> list = (List<Object>) task.getResult().getValue(); // get the participants list
                    if (list == null) list = Collections.singletonList(personId);
                    else if (!list.contains(personId)) {
                        list.add(personId);
                    } else {
                        Toast.makeText(activity, "user already in list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    participantsRef.setValue(list);// add to database
                    dialogToCloseOnSuccess.cancel();

                    DatabaseReference listIdsRef = database.getReference(Keys.USERS)
                            .child(personId)
                            .child(Keys.LIST_IDS);
                    Utils.addToDatabaseList(listIdsRef, listId); // add the list id to the user's list_ids
                });
    }
}
