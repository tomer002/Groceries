package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.HAS_IMAGE;
import static com.example.groceries.Keys.NICKNAME;
import static com.example.groceries.Keys.USERNAME;
import static com.example.groceries.Keys.USERS;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class SettingsActivity extends AppCompatActivity {

    FirebaseStorage storage;
    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storage = FirebaseStorage.getInstance();

        findViewById(R.id.sign_out).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        database.getReference(USERS).child(mAuth.getUid()).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;
            DataSnapshot snapshot = task.getResult();
            nickname = (String) snapshot.child(NICKNAME).getValue();
            String username = (String) snapshot.child(USERNAME).getValue();
            ((TextView) findViewById(R.id.nickname)).setText("nickname: " + nickname);
            ((TextView) findViewById(R.id.username)).setText("username: " + username);
            boolean hasImage = (Boolean) snapshot.child(HAS_IMAGE).getValue();
            if (hasImage) {
                ImageView pfp = findViewById(R.id.pfp);

                storage.getReference("profile_pictures").child(mAuth.getUid()).getBytes(1024 * 1024)
                        .addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful()) return;
                            byte[] bytes = task1.getResult();
                            Drawable image = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            pfp.setImageDrawable(image);
                        });
            }
        });
        findViewById(R.id.edit_nickname).setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Edit nickname");
            dialog.setContentView(R.layout.edit_nickname_layout);
            EditText nicknameEditText = dialog.findViewById(R.id.nickname);
            nicknameEditText.setText(nickname);
            dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.cancel());
            dialog.findViewById(R.id.confirm).setOnClickListener(v -> {
                String newNickname = nicknameEditText.getText().toString();
                if (nickname.equals(newNickname)) {
                    dialog.cancel();
                    return;
                }
                database.getReference(USERS + "/" + mAuth.getUid() + "/" + NICKNAME).setValue(newNickname);
                nickname = newNickname;
                ((TextView) findViewById(R.id.nickname)).setText("nickname: " + nickname);
                dialog.cancel();
            });
            dialog.show();
            nicknameEditText.setSelection(nickname.length());
            nicknameEditText.requestFocus();
        });
    }
}