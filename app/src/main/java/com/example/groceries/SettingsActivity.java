package com.example.groceries;

import static com.example.groceries.GlobalVariables.database;
import static com.example.groceries.GlobalVariables.mAuth;
import static com.example.groceries.Keys.HAS_IMAGE;
import static com.example.groceries.Keys.NICKNAME;
import static com.example.groceries.Keys.USERNAME;
import static com.example.groceries.Keys.USERS;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    FirebaseStorage storage;
    String nickname;
    boolean hasImage;

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
            hasImage = (Boolean) snapshot.child(HAS_IMAGE).getValue();
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

        findViewById(R.id.edit_pfp).setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Change profile picture");
            dialog.setContentView(R.layout.edit_pfp_layout);
            dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.cancel());
            dialog.findViewById(R.id.take_picture).setOnClickListener(view -> {
                takePicture();
                dialog.cancel();
            });
            dialog.findViewById(R.id.choose_from_gallery).setOnClickListener(view -> {
                chooseFromGallery();
                dialog.cancel();
            });
            dialog.show();
        });


    }

    public void chooseFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//go to camera
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        Bitmap bitmap;
        if (requestCode == 0) {
            bitmap = (Bitmap) data.getExtras().get("data");
        } else if (requestCode == 1) {
            Uri imageUri = data.getData();
            try {
                bitmap = GetImageFromGallery.getBitmap(this, imageUri);
            } catch (IOException e) {
                Toast.makeText(this, "error, try again", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            return;
        }
        if (bitmap.getWidth() > 360 || bitmap.getHeight() > 360) {
            int max = Math.max(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.getWidth() * 360) / max, (bitmap.getHeight() * 360) / max, true);
            // ratio is 360/max therefore I multiply by 360 and divide by max
        }
        ImageView pfp = findViewById(R.id.pfp);
        pfp.setImageBitmap(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        storage.getReference("profile_pictures").child(mAuth.getUid()).putBytes(baos.toByteArray());
        if (!hasImage) {
            hasImage = true;
            database.getReference(USERS + "/" + mAuth.getUid() + "/" + HAS_IMAGE).setValue(true);
        }
    }
}