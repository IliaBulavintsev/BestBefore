package com.rv150.bestbefore.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rv150.bestbefore.Preferences.SharedPrefsManager;
import com.rv150.bestbefore.Receivers.AlarmReceiver;
import com.rv150.bestbefore.MyNotification;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.StringWrapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Rudnev on 01.07.2016.
 */
public class MyIntentService extends IntentService {
    public MyIntentService() {
        super("MyIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        List<StringWrapper> wrapperList = SharedPrefsManager.getFreshProducts(this);
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        int ID = intent.getIntExtra("id", -1);
        switch (ID) {
            case 1: {
                int daysInFirst = sPrefs.getInt(Resources.PREF_DAYS_IN_FIRST_NOTIF, 0);
                makeNotification(wrapperList, daysInFirst, 1);
                break;
            }
            case 2: {
                int daysInSecond = sPrefs.getInt(Resources.PREF_DAYS_IN_SECOND_NOTIF, 1);
                makeNotification(wrapperList, daysInSecond, 2);
                break;
            }
            case 3: {
                int daysInThird = sPrefs.getInt(Resources.PREF_DAYS_IN_THIRD_NOTIF, 2);
                makeNotification(wrapperList, daysInThird, 3);
                break;
            }
            default: {
               Log.d("Exception", "ID is wrong!");
            }
        }

       AlarmReceiver.completeWakefulIntent(intent);
    }

    private void makeNotification(List<StringWrapper> wrapperList, int days_before, int ID) {
        String firstProduct = null;
        int count = 0;
        for (int i = 0; i < wrapperList.size(); ++i) {
            StringWrapper currentItem = wrapperList.get(i);
            Calendar date = currentItem.getDate();
            int year = date.get(Calendar.YEAR);
            int month = date.get(Calendar.MONTH);
            int day = date.get(Calendar.DAY_OF_MONTH);
            date.set(year, month, day, 23, 59);
            Calendar currentDate = new GregorianCalendar();
            long difference = date.getTimeInMillis()  - currentDate.getTimeInMillis();
            int days = (int) (difference / (24*60*60*1000));

            if (days == days_before && difference > 0) {
                if (firstProduct == null) {
                    firstProduct = currentItem.getTitle();
                }
                count++;
            }
        }

        if (count >= 2) {
            count--;
            firstProduct += " и еще " + count;

            if (count >= 10 && count <= 20)
                firstProduct += " продуктов";
            else if (count % 10 >= 2 && count % 10 <= 4)
                firstProduct += " продуктa";
            else if (count % 10 == 1)
                firstProduct += " продукт";
            else
                firstProduct += " продуктов";
        }

        if (firstProduct != null) {
            MyNotification myNotification = new MyNotification();
            myNotification.createNotification(this, ID, days_before, firstProduct);
        }
    }
}