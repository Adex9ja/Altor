package com.altor.android.altor.utils;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.altor.android.altor.ListOfDrinks;
import com.altor.android.altor.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ADEOLU on 3/20/2017.
 */
public class MyHandler extends Handler{
    private Context vcontext;
    private PrefManager mypref;
    private  ProgressDialog builder;
    private boolean isrolling = false;
    public MyHandler(Context mcontext){
        vcontext = mcontext;
        mypref = new PrefManager(vcontext);
    }

    @Override
    public void dispatchMessage(Message msg) {
        super.dispatchMessage(msg);
        switch (msg.what){
            case 0:
                mypref.saveLoginCookie((msg.obj != null) ? (String[]) msg.obj : new String[]{"","","",""});
                break;
            case 1:
                builder = new ProgressDialog(vcontext);
                builder.setCancelable(false);
                builder.setIndeterminate(true);
                builder.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                builder.setMessage((msg.obj != null) ? msg.obj.toString() : "Please wait...");
                builder.show();
                isrolling = true;
                TimeOut(30000);
                break;
            case 2:
                if(builder != null && isrolling)
                    builder.dismiss();
                break;
            case 3:
                Toast.makeText(vcontext, ((BluetoothDevice)msg.obj).getName(), Toast.LENGTH_SHORT).show();
                MyDevice device = new MyDevice();
                device.setDeviceAddress(((BluetoothDevice)msg.obj).getAddress());
                device.setDeviceName(((BluetoothDevice)msg.obj).getName());
                if(!BluetoothConnection._devicelists.contains(device))
                    BluetoothConnection._devicelists.add(device);
                break;
            case 4:
                    if(builder != null && builder.isShowing())
                    builder.dismiss();
                List<String> temp = new ArrayList<>();
                for(BluetoothDevice blue :  MainActivity.bAdapter.getBondedDevices()){
                        temp.add(blue.getName());
                 }
               // for(MyDevice mydevice : BluetoothConnection._devicelists)
                 //   temp.add(mydevice.getDeviceName());
                showAllBluetoothDevice(temp);
                break;
            case 5:
                builder = new ProgressDialog(vcontext);
                builder.setCancelable(false);
                builder.setIndeterminate(true);
                builder.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                builder.setMessage((msg.obj != null) ? msg.obj.toString() : "Please wait...");
                builder.show();
                TimeOut(60000);
                break;
        }
    }

   private void TimeOut(final long timeout){
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   Thread.sleep(timeout);
                   if(isrolling){
                       builder.dismiss();
                       Toast.makeText(vcontext,"Network Error!",Toast.LENGTH_SHORT).show();
                       isrolling = false;
                   }

               }catch (Exception e){e.printStackTrace();}
           }
       }).start();
   }
    public void showAllBluetoothDevice(List<String>devicename){
        String[] temp = new String[devicename.size()];
        int count = 0;
        for(String str : devicename)
            temp[count++] = str;
       AlertDialog.Builder alert = new AlertDialog.Builder(vcontext);
        alert.setTitle("Choose Device");
        alert.setItems(temp,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BluetoothDevice localdevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(BluetoothConnection._devicelists.get(i).getDeviceAddress());
                try {
                    //new WristBandAPI(vcontext).startScan();
                }
                catch (Exception e ){
                    e.printStackTrace();
                }

            }
        });
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }
}
