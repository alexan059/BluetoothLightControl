package com.alexan059.smarthome.bluetoothlightcontrol.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class BluetoothStateReceiver extends BroadcastReceiver {

    public static final String TAG = BluetoothStateReceiver.class.getName();

    public BluetoothStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String extra = intent.getStringExtra(BluetoothConstants.EXTENDED_DATA_STATUS);

        Log.d(TAG, context.getClass().getName() + " " + extra);

        setText(extra);
    }

    public abstract void setText(String text);
}
