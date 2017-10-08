package com.alexan059.smarthome.bluetoothlightcontrol.bluetooth;


import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

class BluetoothConnectThread extends Thread {

    private static final String TAG = BluetoothConnectThread.class.getName();
    private final BluetoothSocket mmSocket;

    private OnBluetoothSocketConnectedListener mCallback;

    interface OnBluetoothSocketConnectedListener {
        public void onBluetoothSocketConnected(BluetoothSocket socket);
    }

    BluetoothConnectThread(BluetoothDevice device) {
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(BluetoothConstants.CONNECTION_UUID));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }

        mmSocket = tmp;
    }

    public void run() {

        // Try until connected
        while (!mmSocket.isConnected()) {
            Log.d(TAG, "Connecting...");

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e(TAG, "Unable to connect", connectException);

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Unable to close client socket", closeException);
                }

                return;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


        mCallback.onBluetoothSocketConnected(mmSocket);
    }

    void setOnBluetoothSocketConnectedListener(IntentService service) {
        mCallback = (OnBluetoothSocketConnectedListener) service;
    }

    void cancel() {
        try {
            mmSocket.close();
            Log.d(TAG, "Disconnected.");
        } catch (IOException closeException) {
            Log.e(TAG, "Unable to close client socket", closeException);
        }
    }
}
