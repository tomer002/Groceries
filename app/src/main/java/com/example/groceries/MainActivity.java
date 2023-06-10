package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    /**
     * on create initialize the super
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance(); // Initialize firebase authentication
        database = FirebaseDatabase.getInstance(); // Initialize firebase database


        if (mAuth.getCurrentUser() == null) {//if user is not signed in, open the registration screen
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {//if user is signed in open the landing screen
            Intent intent = new Intent(this, ListsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}