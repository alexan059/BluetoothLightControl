package com.alexan059.smarthome.bluetoothlightcontrol;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alexan059.smarthome.bluetoothlightcontrol.bluetooth.BluetoothConstants;
import com.alexan059.smarthome.bluetoothlightcontrol.bluetooth.BluetoothService;
import com.alexan059.smarthome.bluetoothlightcontrol.bluetooth.BluetoothStateReceiver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String BT_DEVICE = this.getString(R.string.device_name);

    private Intent bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBluetoothService();

        findViewById(R.id.btOn).setOnClickListener(this);
        findViewById(R.id.btOff).setOnClickListener(this);

        final TextView tvStatus = (TextView) findViewById(R.id.tvStatus);

        IntentFilter statusIntentFilter = new IntentFilter(BluetoothConstants.BROADCAST_ACTION);
        BluetoothStateReceiver mBluetoothStateReceiver = new BluetoothStateReceiver() {
            @Override
            public void setText(String text) {
                tvStatus.setText(text);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothStateReceiver, statusIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(bluetoothService);
    }

    private void startBluetoothService() {
        bluetoothService = new Intent(this, BluetoothService.class);
        bluetoothService.putExtra(BluetoothConstants.EXTRA_DEVICE_NAME, BT_DEVICE);
        startService(bluetoothService);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btOn:
                sendMessage(BT_DEVICE + "on");
                break;
            case R.id.btOff:
                sendMessage(BT_DEVICE + "off");
                break;
        }
    }

    private void sendMessage(String message) {
        Intent localIntent = new Intent(BluetoothConstants.ACTION_WRITE_MESSAGE);
        localIntent.putExtra(BluetoothConstants.EXTRA_WRITE_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
