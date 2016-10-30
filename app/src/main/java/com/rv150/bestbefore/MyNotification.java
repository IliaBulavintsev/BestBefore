package com.rv150.bestbefore;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import com.rv150.bestbefore.Activities.MainActivity;

/**
 * Created by Rudnev on 30.06.2016.
 */
public class MyNotification {
    public MyNotification() {}

    public void createNotification(Context context, int NOTIFY_ID, int days, String name) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);

        Resources res = context.getResources();
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String text;
        if (days == 0) {
            text = res.getString(R.string.today_is_the_last_day);
        }
        else {
            text = context.getResources().getQuantityString(R.plurals.numberOfDaysLeft, days, days);
        }

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notify)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.rrrr))
                .setTicker(res.getString(R.string.best_before_expires))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(name)
                .setContentText(text) // Текст уведомления
                .setSound(alarmSound);

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= 16)
            notification = builder.build();
        else
            notification = builder.getNotification();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
    }
}
