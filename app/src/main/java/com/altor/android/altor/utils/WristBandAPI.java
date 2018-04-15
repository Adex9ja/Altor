package com.altor.android.altor.utils;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;
import com.zhaoxiaodan.miband.listeners.NotifyListener;
import com.zhaoxiaodan.miband.listeners.RealtimeStepsNotifyListener;
import com.zhaoxiaodan.miband.model.BatteryInfo;
import com.zhaoxiaodan.miband.model.LedColor;
import com.zhaoxiaodan.miband.model.UserInfo;
import com.zhaoxiaodan.miband.model.VibrationMode;

import java.util.Arrays;

/**
 * Created by ADEOLU on 3/28/2017.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WristBandAPI {
    private String TAG = "TAG";
    private Context mcontext;
    private MiBand miband;

    public WristBandAPI(Context vcontext){
        mcontext = vcontext;
        miband = new MiBand(mcontext);

    }



    final ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            BluetoothDevice device = result.getDevice();
            connectDevice(device);
            /*Log.d(TAG,
                    "name:" + device.getName() + ",uuid:"
            + device.getUuids() + ",add:"
            + device.getAddress() + ",type:"
            + device.getType() + ",bondState:"
            + device.getBondState() + ",rssi:" + result.getRssi());*/

        }

    };
    public void startScan (){
        MiBand.startScan(scanCallback);
    }
    public void stopScan(){
        MiBand.stopScan(scanCallback);
    }
    public void connectDevice(BluetoothDevice device){
        miband.connect(device, new ActionCallback() {

            @Override
            public void onSuccess(Object data)
            {
                Toast.makeText(mcontext,"connect success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode, String msg)
            {
                Toast.makeText(mcontext,"connect fail, code:"+errorCode+",mgs:"+msg,Toast.LENGTH_SHORT).show();
                Log.d(TAG,"connect fail, code:"+errorCode+",mgs:"+msg);
            }
        });

    }
    public void setDisconnectListener(){
      miband.setDisconnectedListener(new NotifyListener()
      {
          @Override
          public void onNotify(byte[] data)
          {

          }
      });
  }
    public void setUserInfo(){
        UserInfo userInfo = new UserInfo(20111111, 1, 32, 180, 55, "胖梁", 0);
        miband.setUserInfo(userInfo);
    }
    public void setHeartBeatRateListener(){
        miband.setHeartRateScanListener(new HeartRateNotifyListener()
        {
            @Override
            public void onNotify(int heartRate)
            {
                Log.d(TAG, "heart rate: "+ heartRate);
            }
        });

        miband.startHeartRateScan();
    }
    public void readSSI(){
        miband.readRssi(new ActionCallback() {

            @Override
            public void onSuccess(Object data)
            {
                Log.d(TAG, "rssi:"+(int)data);
            }

            @Override
            public void onFail(int errorCode, String msg)
            {
                Log.d(TAG, "readRssi fail");
            }
        });
    }
    public void getLowBAttryInfo(){
       miband.getBatteryInfo(new ActionCallback() {

           @Override
           public void onSuccess(Object data)
           {
               BatteryInfo info = (BatteryInfo)data;
               Log.d(TAG, info.toString());
               //cycles:4,level:44,status:unknow,last:2015-04-15 03:37:55
           }

           @Override
           public void onFail(int errorCode, String msg)
           {
               Log.d(TAG, "readRssi fail");
           }
       });
   }
    public void vibrationSet()   {

       miband.startVibration(VibrationMode.VIBRATION_WITH_LED);


       miband.startVibration(VibrationMode.VIBRATION_WITHOUT_LED);


       miband.startVibration(VibrationMode.VIBRATION_10_TIMES_WITH_LED);


       miband.stopVibration();
   }
    public void setNormalNotifierListener(){
        miband.setNormalNotifyListener(new NotifyListener() {

            @Override
            public void onNotify(byte[] data)
            {
                Log.d(TAG, "NormalNotifyListener:" + Arrays.toString(data));
            }
        });
    }
    public void setRealTimeStepsNotify(){
        miband.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {

            @Override
            public void onNotify(int steps)
            {
                Log.d(TAG, "RealtimeStepsNotifyListener:" + steps);
            }
        });


        miband.enableRealtimeStepsNotify();


        miband.disableRealtimeStepsNotify();

    }
    public void setLedColor(){
        miband.setLedColor(LedColor.ORANGE);
        miband.setLedColor(LedColor.BLUE);
        miband.setLedColor(LedColor.RED);
        miband.setLedColor(LedColor.GREEN);
    }
    public void sensorDataNotifier(){
        miband.setSensorDataNotifyListener(new NotifyListener()
        {
            @Override
            public void onNotify(byte[] data)
            {
                int i = 0;

                int index = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;  // 序号
                int d1 = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;
                int d2 = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;
                int d3 = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;

            }
        });

        miband.enableSensorDataNotify();
    }
    public void pairDevice(){
        miband.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data)
            {
                //  changeStatus("pair succ");
            }

            @Override
            public void onFail(int errorCode, String msg)
            {
                //  changeStatus("pair fail");
            }
        });
    }

}
