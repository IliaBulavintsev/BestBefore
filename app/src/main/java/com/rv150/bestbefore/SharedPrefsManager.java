package com.rv150.bestbefore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Rudnev on 05.10.2016.
 */

class SharedPrefsManager {
    static List<StringWrapper> getFreshProducts(Context context) {
        List<StringWrapper> list = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; prefs.contains(String.valueOf(i)); ++i) {
            if (prefs.getString(String.valueOf(i), "").equals("") || i >= 500) {
                break;
            }
            final String title = prefs.getString(String.valueOf(i), "");
            final String date = prefs.getString(String.valueOf(i + 500), "0.0.0");
            final String createdAt = prefs.getString(String.valueOf(i + 1000), "0.0.0.0.0.0");

            StringWrapper temp = new StringWrapper(title, date, createdAt);
            list.add(temp);
        }
        return list;
    }

    static void saveFreshProducts(List<StringWrapper> list, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < list.size(); ++i) {
            Calendar temp = list.get(i).getDate();
            int myYear = temp.get(Calendar.YEAR);
            int myMonth = temp.get(Calendar.MONTH);
            int myDay = temp.get(Calendar.DAY_OF_MONTH);
            String str;
            if (myMonth < 9) {
                str = myDay + "." + "0" + myMonth + "." + myYear;
            } else {
                str = myDay + "." + myMonth + "." + myYear;
            }
            editor.putString(String.valueOf(i), list.get(i).getTitle());
            editor.putString(String.valueOf(i + 500), str);

            Calendar createdAt = list.get(i).createdAt();
            int DayCreated =  createdAt.get(Calendar.DAY_OF_MONTH);
            int MonthCreated = createdAt.get(Calendar.MONTH);
            int YearCreated = createdAt.get(Calendar.YEAR);
            int HourCreated = createdAt.get(Calendar.HOUR_OF_DAY);
            int MinuteCreated = createdAt.get(Calendar.MINUTE);
            int SecondCreated = createdAt.get(Calendar.SECOND);
            String createdAtStr = YearCreated + "." + MonthCreated + "." + DayCreated  + "." + HourCreated + "." + MinuteCreated + "." + SecondCreated;
            editor.putString(String.valueOf(i + 1000), createdAtStr);
        }
        editor.putString(String.valueOf(list.size()), ""); // признак конца списка
        editor.apply();
    }

    static List<StringWrapper> getOverdueProducts(Context context) {
        List<StringWrapper> list = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; prefs.contains("del" + String.valueOf(i)); ++i) {
            if (prefs.getString("del" + String.valueOf(i), "").equals("") || i >= 1000) {
                break;
            }
            final String title = prefs.getString("del" + String.valueOf(i), "");
            final String date = prefs.getString("del" + String.valueOf(i + 1000), "0.0.0");

            list.add(new StringWrapper(title, date));
        }
        return list;
    }


    static void saveOverdueProducts(List<StringWrapper> list, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < list.size(); ++i) {
            Calendar temp = list.get(i).getDate();
            int myYear = temp.get(Calendar.YEAR);
            int myMonth = temp.get(Calendar.MONTH);
            int myDay = temp.get(Calendar.DAY_OF_MONTH);
            String str;
            if (myMonth < 9) {
                str = myDay + "." + "0" + myMonth + "." + myYear;
            } else {
                str = myDay + "." + myMonth + "." + myYear;
            }
            editor.putString("del" + String.valueOf(i), list.get(i).getTitle());
            editor.putString("del" + String.valueOf(i + 1000), str);
        }
        editor.putString("del" + String.valueOf(list.size()), ""); // признак конца списка
        editor.apply();
    }
}
