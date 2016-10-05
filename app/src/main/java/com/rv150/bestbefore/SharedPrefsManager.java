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

public class SharedPrefsManager {
    public static List<StringWrapper> getFreshProducts(Context context) {
        List<StringWrapper> list = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; prefs.contains(String.valueOf(i)); ++i) {
            if (prefs.getString(String.valueOf(i), "").equals("") || i >= 500) {
                break;
            }
            final String title = prefs.getString(String.valueOf(i), "");
            final String date = prefs.getString(String.valueOf(i + 500), "0.0.0");
            String[] array = date.split("\\.");
            int myDay = Integer.parseInt(array[0]);
            int myMonth = Integer.parseInt(array[1]);
            int myYear = Integer.parseInt(array[2]);

            final String createdAtStr = prefs.getString(String.valueOf(i + 1000), "0.0.0.0.0.0");
            String[] createdAtSplit = createdAtStr.split("\\.");
            int YearCreated = Integer.parseInt(createdAtSplit[0]);
            int MonthCreated = Integer.parseInt(createdAtSplit[1]);
            int DayCreated = Integer.parseInt(createdAtSplit[2]);
            int HourCreated = Integer.parseInt(createdAtSplit[3]);
            int MinuteCreated = Integer.parseInt(createdAtSplit[4]);
            int SecondCreated = Integer.parseInt(createdAtSplit[5]);

            StringWrapper temp = new StringWrapper(title, new GregorianCalendar(myYear, myMonth, myDay), new GregorianCalendar(YearCreated, MonthCreated, DayCreated, HourCreated, MinuteCreated, SecondCreated));
            list.add(temp);
        }
        return list;
    }

    public static void saveFreshProducts(List<StringWrapper> list, Context context) {
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
}
