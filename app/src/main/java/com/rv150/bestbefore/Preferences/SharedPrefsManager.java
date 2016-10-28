package com.rv150.bestbefore.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudnev on 05.10.2016.
 */

public class SharedPrefsManager {
    public static List<Product> getFreshProducts(Context context) {
        List<Product> list = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; prefs.contains(String.valueOf(i)); ++i) {
            if (prefs.getString(String.valueOf(i), "").equals("") || i >= 500) {
                break;
            }
            final String title = prefs.getString(String.valueOf(i), "");
            final String date = prefs.getString(String.valueOf(i + 500), "0.0.0");
            final String createdAt = prefs.getString(String.valueOf(i + 1000), "0.0.0.0.0.0");
            final int quantity = prefs.getInt(Resources.QUANTITY + String.valueOf(i), 1);
            Product temp = new Product(title, date, createdAt, quantity);
            list.add(temp);
        }
        return list;
    }

    public static void saveFreshProducts(List<Product> list, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < list.size(); ++i) {
            Product item = list.get(i);
            editor.putString(String.valueOf(i), item.getTitle());
            editor.putString(String.valueOf(i + 500), item.getDateStr());
            editor.putString(String.valueOf(i + 1000), item.getCreatedAtStr());
            editor.putInt(Resources.QUANTITY + String.valueOf(i), item.getQuantity());
        }
        editor.putString(String.valueOf(list.size()), ""); // признак конца списка
        editor.apply();
    }

    public static List<Product> getOverdueProducts(Context context) {
        List<Product> list = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; prefs.contains("del" + String.valueOf(i)); ++i) {
            if (prefs.getString("del" + String.valueOf(i), "").equals("") || i >= 1000) {
                break;
            }
            final String title = prefs.getString("del" + String.valueOf(i), "");
            final String date = prefs.getString("del" + String.valueOf(i + 1000), "0.0.0");
            final int quantity = prefs.getInt("del" + Resources.QUANTITY + String.valueOf(i), 1);
            list.add(new Product(title, date, quantity));
        }
        return list;
    }


    public static void saveOverdueProducts(List<Product> list, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < list.size(); ++i) {
            Product item = list.get(i);
            editor.putString("del" + String.valueOf(i), item.getTitle());
            editor.putString("del" + String.valueOf(i + 1000), item.getDateStr());
            editor.putInt("del" + Resources.QUANTITY + String.valueOf(i), item.getQuantity());
        }
        editor.putString("del" + String.valueOf(list.size()), ""); // признак конца списка
        editor.apply();
    }
}
