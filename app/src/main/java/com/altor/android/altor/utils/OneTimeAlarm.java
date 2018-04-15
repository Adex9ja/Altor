package com.altor.android.altor.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.altor.android.altor.ListOfDrinks;
import com.altor.android.altor.MainActivity;
import com.altor.android.altor.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ADEOLU on 3/14/2017.
 */
public class OneTimeAlarm extends BroadcastReceiver {
    public void onReceive(Context context, Intent vintent) {

        Intent intent = new Intent(context, ListOfDrinks.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification n  = new Notification.Builder(context)
                .setContentTitle("Alcohol Limit")
                .setContentText("You have exeeded your alcohol limit.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
    }

}
