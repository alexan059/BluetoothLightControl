package com.alexan059.smarthome.bluetoothlightcontrol.bluetooth;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BluetoothService extends IntentService implements BluetoothConnectThread.OnBluetoothSocketConnectedListener {

    public static final String TAG = BluetoothService.class.getName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectThread connection = null;
    private BluetoothStreamThread stream;

    private String deviceName;

    public BluetoothService() {
        super(BluetoothService.class.getName());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            deviceName = intent.getStringExtra(BluetoothConstants.EXTRA_DEVICE_NAME);
            startBluetoothService();

            return START_STICKY;
        }

        return super.onStartCommand(null, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (connection != null)
            connection.cancel();

        unregisterAllReceivers();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "Bluetooth enabled.");
                        requestBluetoothConnection();
                        break;
                }
            } else if (BluetoothConstants.ACTION_WRITE_MESSAGE.equals(action)) {
                String message = intent.getStringExtra(BluetoothConstants.EXTRA_WRITE_MESSAGE);
                writeMessage(message);
            }
        }
    };

    private void startBluetoothService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Device doesn't support bluetooth.");
        }

        if (!mBluetoothAdapter.isEnabled()) {
           BluetoothHelper.requestBluetoothEnable(getApplicationContext(), mReceiver);
        } else {
            requestBluetoothConnection();
        }
    }

    private void requestBluetoothConnection() {
        BluetoothDevice device = BluetoothHelper.getPairedDeviceByName(deviceName, mBluetoothAdapter);

        if (device != null) {
            Log.d(TAG, device.getName());

            connection = new BluetoothConnectThread(device);
            connection.setOnBluetoothSocketConnectedListener(this);
            connection.start();
        }
    }

    private void unregisterAllReceivers() {

        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Receiver not registred.", e);
        }

    }

    @Override
    public void onBluetoothSocketConnected(BluetoothSocket socket) {
        Log.d(TAG, "Connection established.");

        Intent localIntent = new Intent(BluetoothConstants.BROADCAST_ACTION);
        localIntent.putExtra(BluetoothConstants.EXTENDED_DATA_STATUS, "Connection established.");
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothConstants.ACTION_WRITE_MESSAGE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        stream = new BluetoothStreamThread(socket);
        stream.start();
    }

    private void writeMessage(String message) {
        stream.write(message.getBytes());
    }
}
