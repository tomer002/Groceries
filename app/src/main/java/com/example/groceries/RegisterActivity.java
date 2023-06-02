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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerScreenSetup();
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
                    .addOnCompleteListener(RegisterActivity.this, (task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, ListsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(RegisterActivity.this, "Illegal username. Usernames must only contain lowercase letters, numbers and underscores.", Toast.LENGTH_LONG).show();
            return;
        }
        database.getReference(USERNAME_TO_ID).child(username)
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(RegisterActivity.this, "error", Toast.LENGTH_SHORT).show();
                    if (task.getResult().exists())
                        Toast.makeText(RegisterActivity.this, "username already exists", Toast.LENGTH_SHORT).show();
                    else {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                        var thisUserRef = database.getReference(USERS).child(mAuth.getUid());
                                        thisUserRef.child(HAS_IMAGE).setValue(false);
                                        thisUserRef.child(USERNAME).setValue(username);
                                        thisUserRef.child(NICKNAME).setValue(nickname);

                                        database.getReference(USERNAME_TO_ID).child(username).setValue(mAuth.getUid());
                                        Intent intent = new Intent(RegisterActivity.this, ListsActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }


    public boolean isUsernameLegal(String username) {
        return username.matches("[a-z0-9_]+");
    }
}