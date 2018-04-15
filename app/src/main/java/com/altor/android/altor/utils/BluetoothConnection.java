package com.altor.android.altor.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ADEOLU on 4/21/2017.
 */
public class BluetoothConnection extends MyHandler {
    public static List<MyDevice>_devicelists;
    public BluetoothConnection(Context mcontext){
        super(mcontext);
        _devicelists = new ArrayList<>();
        MyBluetoothReceiver _receiver = new MyBluetoothReceiver(this);
        IntentFilter _intentFilter = new IntentFilter();
        _intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        _intentFilter.addAction(BluetoothDevice.EXTRA_DEVICE);
        _intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        _intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mcontext.registerReceiver(_receiver, _intentFilter);
    }
    public class MyBluetoothReceiver extends BroadcastReceiver{
        private MyHandler handler;
        public MyBluetoothReceiver(MyHandler mhandler){
            handler = mhandler;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.EXTRA_DEVICE))
            {
                BluetoothDevice bluetoothdevice = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                handler.obtainMessage(3, bluetoothdevice).sendToTarget();
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
            {
                handler.obtainMessage(4, null).sendToTarget();
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
            {
                handler.obtainMessage(5,"Searching for bluetooth device(s)...").sendToTarget();
            }
        }
    }
}
