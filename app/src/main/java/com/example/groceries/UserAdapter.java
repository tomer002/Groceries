package com.example.groceries;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.List;

/**
 * array adapter for a list view of the users
 */
public class UserAdapter extends ArrayAdapter<UserAdapter.User> {
    LayoutInflater layoutInflater; // the layout inflater
    Drawable defaultPfp; // default pfp (profile picture) for a user that didn't upload a profile picture

    public static class User {
        Drawable image;
        String nickname;
        String username;
        String uid;
    }

    public UserAdapter(Activity activity, int resource, int textViewResourceId, List<User> users) {
        super(activity, resource, textViewResourceId, users);
        defaultPfp = AppCompatResources.getDrawable(activity, R.drawable.default_pfp);
        layoutInflater = activity.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = layoutInflater.inflate(R.layout.user_view, parent, false);
        }

        ImageView pfp = view.findViewById(R.id.pfp);
        User user = getItem(position);
        if (user.image == null)
            pfp.setImageDrawable(defaultPfp); // if user doesn't have an image, use default pfp
        else
            pfp.setImageDrawable(user.image); // if user has an image set the user's pfp as the drawable of the image view
        TextView displayName = view.findViewById(R.id.display_name);
        displayName.setText(user.nickname); // put the display name in the text view

        return view;
    }
}
