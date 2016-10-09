package com.rv150.bestbefore.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.MyIntentService;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ivan on 29.06.2016.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MyIntentService.class);

        int ID = intent.getIntExtra("id", -1);
        service.putExtra("id", ID);
        switch (ID) {
            case 1: service.setAction("first");
                break;
            case 2: service.setAction("second");
                break;
            case 3: service.setAction("third");
                break;
        }
        startWakefulService(context, service);
    }

    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);


        SharedPreferences sPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);


        // Отмена всех трех
        Intent i1 = new Intent(context, AlarmReceiver.class);
        i1.putExtra("id", 1);
        i1.setAction("first");
        PendingIntent pi1 = PendingIntent.getBroadcast(context, 1, i1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i2 = new Intent(context, AlarmReceiver.class);
        i2.putExtra("id", 2);
        i2.setAction("second");
        PendingIntent pi2 = PendingIntent.getBroadcast(context, 2, i2, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i3 = new Intent(context, AlarmReceiver.class);
        i3.putExtra("id", 3);
        i3.setAction("third");
        PendingIntent pi3 = PendingIntent.getBroadcast(context, 3, i3, PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(pi1);
        am.cancel(pi2);
        am.cancel(pi3); // Тормозим все напоминания, а потом устанавливаем только те, что действительно включены


        boolean firstNotif = sPrefs.getBoolean(Resources.PREF_FIRST_NOTIF, true);
        boolean secondNotif = sPrefs.getBoolean(Resources.PREF_SECOND_NOTIF, false);
        boolean thirdNotif = sPrefs.getBoolean(Resources.PREF_THIRD_NOTIF, false);

        // Если активировано 1-ое напоминание
        if (firstNotif) {
            int hour = sPrefs.getInt(Resources.PREF_FIRST_HOUR, 17);
            int minute = sPrefs.getInt(Resources.PREF_FIRST_MINUTE, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date dat = new Date();// initializes to now
            Calendar cal_now = Calendar.getInstance();
            cal_now.setTime(dat);
            if (calendar.before(cal_now)) {// if its in the past increment
                calendar.add(Calendar.DATE, 1);
            }

            long delay = 24 * 60 * 60 * 1000;
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    delay, pi1);
        }

        // Второе
        if (secondNotif) {
            int hour = sPrefs.getInt(Resources.PREF_SECOND_HOUR, 17);
            int minute = sPrefs.getInt(Resources.PREF_SECOND_MINUTE, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date dat = new Date();// initializes to now
            Calendar cal_now = Calendar.getInstance();
            cal_now.setTime(dat);
            if (calendar.before(cal_now)) {// if its in the past increment
                calendar.add(Calendar.DATE, 1);
            }

            long delay = 24 * 60 * 60 * 1000;
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    delay, pi2);
        }


        // Третье
        if (thirdNotif) {
            int hour = sPrefs.getInt(Resources.PREF_THIRD_HOUR, 17);
            int minute = sPrefs.getInt(Resources.PREF_THIRD_MINUTE, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            Date dat = new Date();// initializes to now
            Calendar cal_now = Calendar.getInstance();
            cal_now.setTime(dat);
            if (calendar.before(cal_now)) {// if its in the past increment
                calendar.add(Calendar.DATE, 1);
            }

            long delay = 24 * 60 * 60 * 1000;
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    delay, pi3);
        }
    }

    public void cancelAlarm (Context context) {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i1 = new Intent(context, AlarmReceiver.class);
        i1.setAction("first");
        PendingIntent pi1 = PendingIntent.getBroadcast(context, 1, i1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i2 = new Intent(context, AlarmReceiver.class);
        i2.setAction("second");
        PendingIntent pi2 = PendingIntent.getBroadcast(context, 2, i2, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent i3 = new Intent(context, AlarmReceiver.class);
        i3.setAction("third");
        PendingIntent pi3 = PendingIntent.getBroadcast(context, 3, i3, PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(pi1);
        am.cancel(pi2);
        am.cancel(pi3);
    }
}



