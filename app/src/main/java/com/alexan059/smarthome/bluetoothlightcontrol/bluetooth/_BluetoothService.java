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

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class _BluetoothService extends IntentService {

    public static final String TAG = _BluetoothService.class.getName();

    private BluetoothAdapter mBluetoothAdapter;

    private String deviceName;

    private boolean pairingReceiverRegistered = false;
    private boolean discoveryReceiverRegistered = false;

    public _BluetoothService() {
        super(_BluetoothService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created.");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        if (intent != null) {
            deviceName = intent.getStringExtra(BluetoothConstants.EXTRA_DEVICE_NAME);

            Log.d(TAG, "Searching: " + deviceName);

            startBluetoothDetection();

            return START_STICKY;
        }

        return super.onStartCommand(null, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
            mBluetoothAdapter.cancelDiscovery();
        }

        if (discoveryReceiverRegistered)
            unregisterReceiver(mDiscoveryReceiver);

        if (pairingReceiverRegistered)
            unregisterReceiver(mPairingReceiver);

        Log.d(TAG, "Service destroyed.");
    }

    private final BroadcastReceiver mPairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.d(TAG, "Pairing request sent.");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                startBluetoothConnection(device);
            }
        }
    };

    private final BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery enabled.");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery disabled.");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "Searching: " + deviceName + "|Found: " + device.getName());

                if (deviceName.equals(device.getName())) {
                    Log.d(TAG, "Device found! - " + device.getName());

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

                    registerReceiver(mPairingReceiver, filter);
                    pairingReceiverRegistered = true;

                    device.createBond();

                }
            }
        }
    };

    private void findBluetoothDevice() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        boolean deviceFound = false;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceHardwareAddress = device.getAddress();

                if (device.getName().equals(deviceName)) {

                    Log.d(TAG, "Device: " + deviceName + " | " + deviceHardwareAddress);

                    deviceFound = true;

                    startBluetoothConnection(device);
                }
            }


        }

        /*if (!deviceFound)
            startDiscovery();*/
    }

    private void startBluetoothDetection() {
        Intent localIntent = new Intent(BluetoothConstants.BROADCAST_ACTION);
        localIntent.putExtra(BluetoothConstants.EXTENDED_DATA_STATUS, "Some status here.");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Device doesn't support bluetooth.");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();

            Log.d(TAG, "Bluetooth enabled.");
        }

        findBluetoothDevice();

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void startBluetoothConnection(BluetoothDevice device) {
        ConnectThread connect = new ConnectThread(device);
        connect.start();
    }

    private void startDiscovery() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mDiscoveryReceiver, filter);
        discoveryReceiverRegistered = true;

        if (mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Active discovery disabled.");
            mBluetoothAdapter.cancelDiscovery();
        }

        // TODO add user permission request for permission location
        while (!mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Trying to active discovery mode...");
            mBluetoothAdapter.startDiscovery();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                Log.d(TAG, "Connecting...");

                byte[] pin = ("").getBytes();
                device.setPin(pin);
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(BluetoothConstants.CONNECTION_UUID));

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }

            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

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

            Log.d(TAG, "Successfully connected to " + mmDevice.getName());
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Unable to close client socket", closeException);
            }
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


}
