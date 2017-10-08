package com.alexan059.smarthome.bluetoothlightcontrol.bluetooth;


import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

class BluetoothStreamThread extends Thread {

    private static final String TAG = BluetoothStreamThread.class.getName();

    interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    private final BluetoothSocket mmSocket;
    //private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer;

    BluetoothStreamThread(BluetoothSocket socket) {
        mmSocket = socket;
        //InputStream tmpIn = null;
        OutputStream tmpOut = null;

            /*try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occured creating input stream", e);
            }*/

        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occured creating output stream", e);
        }

        //mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
            /*mmBuffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }*/
    }

    void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            //Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            //writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.d(TAG, "Error occurred when sending data", e);

            //Message writeErrorMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            //Bundle bundle = new Bundle();
            //bundle.putString("toast", "Couldn't send data to the other device");
            //writeErrorMsg.setData(bundle);
            //mHandler.sendMessage(writeErrorMsg);
        }
    }

    void cancel() {
        try {
            mmSocket.close();
        } catch (IOException closeException) {
            Log.e(TAG, "Unable to close client socket", closeException);
        }
    }

}
