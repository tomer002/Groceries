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
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;

/**
 * Setting of the user is the screen where you can edit the profile picture, nickname and also can sign out
 */
public class SettingsActivity extends AppCompatActivity {

    FirebaseStorage storage; // Instance to firebase storage
    String nickname; // nickname string
    boolean hasImage; // a flag indicating if the user upload a profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storage = FirebaseStorage.getInstance();

        findViewById(R.id.sign_out).setOnClickListener(v -> {
            mAuth.signOut(); // If sign out was pushed, make sign out using firebase authentication
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent); // Open the main activity
            finish();
        });
        database.getReference(USERS).child(mAuth.getUid()).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;
            DataSnapshot snapshot = task.getResult();
            nickname = (String) snapshot.child(NICKNAME).getValue(); // Get the user nickname from the database
            String username = (String) snapshot.child(USERNAME).getValue();// Get the username from the database
            ((TextView) findViewById(R.id.nickname)).setText("nickname: " + nickname); // put nickname in the text view
            ((TextView) findViewById(R.id.username)).setText("username: " + username); // put username in the text view
            hasImage = (Boolean) snapshot.child(HAS_IMAGE).getValue(); // check if image exist
            if (hasImage) { // if image exist, download it from the firebase database and put it in the screen
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
            Dialog dialog = new Dialog(this); // Dialog of edit nickname
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
                database.getReference(USERS + "/" + mAuth.getUid() + "/" + NICKNAME).setValue(newNickname); // put the nickname in the database
                nickname = newNickname;
                ((TextView) findViewById(R.id.nickname)).setText("nickname: " + nickname); // put the nickname in the UI
                dialog.cancel();
            });
            dialog.show();
            nicknameEditText.setSelection(nickname.length());
            nicknameEditText.requestFocus();
        });

        findViewById(R.id.edit_pfp).setOnClickListener(v -> {
            Dialog dialog = new Dialog(this); // Dialog for edit profile picture
            dialog.setTitle("Change profile picture");
            dialog.setContentView(R.layout.edit_pfp_layout);
            dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.cancel());
            dialog.findViewById(R.id.take_picture).setOnClickListener(view -> {
                takePicture(); // this method will enable to take a picture
                dialog.cancel();
            });
            dialog.show();
        });


    }

    /**
     * open the activity of taking a profile picture
     */
    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//go to camera
        startActivityForResult(intent, 0);
    }

    /**
     * Called when the user finished to take a picture or closed the take picture screen
     *
     * @param requestCode the request code of the activity that returned a result
     * @param resultCode  the result code of the activity
     * @param data        the data that the activity returned (contains the bitmap of the picture)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode != 0) return;

        Bitmap bitmap = (Bitmap) data.getExtras().get("data");

        // If the picture is too big, resize it according to the ratio
        if (bitmap.getWidth() > 360 || bitmap.getHeight() > 360) {
            int max = Math.max(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.getWidth() * 360) / max, (bitmap.getHeight() * 360) / max, true);
            // ratio is 360/max therefore I multiply by 360 and divide by max
        }
        ImageView pfp = findViewById(R.id.pfp);
        pfp.setImageBitmap(bitmap); // put the picture in the image view
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 98, baos); // put the picture in the database in jpeg compressed format
        storage.getReference("profile_pictures").child(mAuth.getUid()).putBytes(baos.toByteArray());
        if (!hasImage) { // edit the has_image property if needed
            hasImage = true;
            database.getReference(USERS + "/" + mAuth.getUid() + "/" + HAS_IMAGE).setValue(true);
        }
    }
}