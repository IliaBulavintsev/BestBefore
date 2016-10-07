package com.rv150.bestbefore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
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
            StringWrapper item = list.get(i);
            editor.putString(String.valueOf(i), item.getTitle());
            editor.putString(String.valueOf(i + 500), item.getDateStr());
            editor.putString(String.valueOf(i + 1000), item.getCreatedAtStr());
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
            StringWrapper item = list.get(i);
            editor.putString("del" + String.valueOf(i), item.getTitle());
            editor.putString("del" + String.valueOf(i + 1000), item.getDateStr());
        }
        editor.putString("del" + String.valueOf(list.size()), ""); // признак конца списка
        editor.apply();
    }
}
