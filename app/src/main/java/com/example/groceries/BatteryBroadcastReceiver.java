package com.example.groceries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Broadcast receiver that listen to the battery level and puts it in a text view
 */
public class BatteryBroadcastReceiver extends BroadcastReceiver {
    private final TextView textView; // the battery level text view

    public BatteryBroadcastReceiver(TextView textView) {
        this.textView = textView;
    }

    /**
     * Called when the battery level changes
     *
     * @param context the current context
     * @param intent  the intent that contain the data about the battery level
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        int battery = intent.getIntExtra("level", 0);
        textView.setText("battery level = " + battery + "%");
    }
}
