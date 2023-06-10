package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.HAS_IMAGE;
import static com.example.groceries.Keys.NICKNAME;
import static com.example.groceries.Keys.USERNAME;
import static com.example.groceries.Keys.USERNAME_TO_ID;
import static com.example.groceries.Keys.USERS;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    /**
     * Register and sign in screens
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerScreenSetup();
    }

    /**
     * open the register screen and add listeners to the register and sign in buttons
     */
    public void registerScreenSetup() {
        setContentView(R.layout.register_layout);
        findViewById(R.id.register_submit_button).setOnClickListener(v -> createUser());
        findViewById(R.id.sign_in_button).setOnClickListener(v -> signInScreenSetup());
    }

    /**
     * open the sign in screen and add listener to the submit and register buttons
     */
    public void signInScreenSetup() {
        setContentView(R.layout.sign_in_layout);
        // Use the email and password in order to sign in the user
        findViewById(R.id.sign_in_submit_button).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, (task -> {
                                // If the sign in succeed open the landing screen
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, ListsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else { // If fail show proper message
                                    Toast.makeText(RegisterActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                                }
                            })
                    );
        });
        findViewById(R.id.register_button).setOnClickListener(v -> registerScreenSetup());
    }

    /**
     * Create a new user and insert him into the firebase database
     */
    public void createUser() {
        String email = ((EditText) findViewById(R.id.email)).getText().toString(); // entered email from the screen
        String password = ((EditText) findViewById(R.id.password)).getText().toString(); // entered password
        String username = ((EditText) findViewById(R.id.username)).getText().toString(); // entered username
        String nickname = ((EditText) findViewById(R.id.nickname)).getText().toString(); // entered nickname

        // check if the input is legal
        if (!isUsernameLegal(username)) {
            Toast.makeText(RegisterActivity.this, "Illegal username. Usernames must only contain lowercase letters, numbers and underscores.", Toast.LENGTH_LONG).show();
            return;
        }
        // First check if this user is already exist in the database
        database.getReference(USERNAME_TO_ID).child(username)
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(RegisterActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
                    if (task.getResult().exists())
                        Toast.makeText(RegisterActivity.this, "username already exists", Toast.LENGTH_SHORT).show();
                    else { // Create this user if he is not already exist
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task1 -> {
                                    if (!task1.isSuccessful()) { // If fail to create a user, then return
                                        Toast.makeText(RegisterActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    // If the user does not exist add their data into the database
                                    Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                    var thisUserRef = database.getReference(USERS).child(mAuth.getUid());
                                    thisUserRef.child(HAS_IMAGE).setValue(false);
                                    thisUserRef.child(USERNAME).setValue(username);
                                    thisUserRef.child(NICKNAME).setValue(nickname);

                                    database.getReference(USERNAME_TO_ID).child(username).setValue(mAuth.getUid());

                                    // Open the landing screen after the registration finished
                                    Intent intent = new Intent(RegisterActivity.this, ListsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                    }
                });
    }


    /**
     * Check if a username is legal
     *
     * @param username - the username
     * @return if the username is legal
     */
    public boolean isUsernameLegal(String username) {
        return username.matches("[a-z0-9_]+"); // regular expression that expresses what is legal in the username
    }
}