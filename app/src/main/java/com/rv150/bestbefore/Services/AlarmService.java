package com.rv150.bestbefore.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rv150.bestbefore.Receivers.AlarmReceiver;

/**
 * Created by Rudnev on 30.06.2016.
 */public class AlarmService extends Service
{
    AlarmReceiver alarm = new AlarmReceiver();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        alarm.setAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
