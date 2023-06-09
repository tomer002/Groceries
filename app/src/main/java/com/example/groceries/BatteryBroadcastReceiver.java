package com.example.groceries;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    private final TextView textView;

    public BatteryBroadcastReceiver(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int battery = intent.getIntExtra("level", 0);
        textView.setText("battery level = " + battery + "%");
    }
}
