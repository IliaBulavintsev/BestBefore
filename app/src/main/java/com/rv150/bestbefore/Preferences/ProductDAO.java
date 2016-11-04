package com.rv150.bestbefore.Preferences;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Product;
import com.rv150.bestbefore.Services.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Rudnev on 05.10.2016.
 */

public class ProductDAO {
    private DBHelper dbHelper;

    public ProductDAO(Context context) {
        dbHelper = new DBHelper(context);
    }


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
            Product temp = new Product(title, date, createdAt, quantity, null);
            list.add(temp);
        }
        return list;
    }

    public List<Product> getAllFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        List<Product> products = new ArrayList<>();

        while (cursor.moveToNext()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_NAME));
            long dateInMillis = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_DATE));

            long createdAtInMillis = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_CREATED_AT));

            int quantity = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_QUANTITY));

            Long groupId;
            if (cursor.isNull(cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_GROUP_ID))) {
                groupId = null;
            }
            else {
                groupId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_GROUP_ID));
            }

            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBHelper.Product._ID));

            Calendar date = new GregorianCalendar();
            date.setTimeInMillis(dateInMillis);

            Calendar createdAt =  new GregorianCalendar();
            createdAt.setTimeInMillis(createdAtInMillis);

            Product product = new Product(name, date, createdAt, quantity, groupId);
            product.setId(id);
            products.add(product);
        }
        cursor.close();
        return products;
    }


    public void insertProducts (List<Product> products) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (Product product: products) {
            values.put(DBHelper.Product.COLUMN_NAME_NAME, product.getTitle());
            values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
            values.put(DBHelper.Product.COLUMN_NAME_CREATED_AT, product.getCreatedAt().getTimeInMillis());
            values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
            values.putNull(DBHelper.Product.COLUMN_NAME_GROUP_ID);
            db.insert(DBHelper.Product.TABLE_NAME, null, values);
        }
    }


    public long insertProduct (Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_NAME, product.getTitle());
        values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_CREATED_AT, product.getCreatedAt().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
        values.put(DBHelper.Product.COLUMN_NAME_GROUP_ID, product.getGroupId());
        return db.insert(DBHelper.Product.TABLE_NAME, null, values);
    }

    public void deleteProduct(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.Product.TABLE_NAME, DBHelper.Product._ID + "=?", new String[] {String.valueOf(id)});
    }

    public void updateProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_NAME, product.getTitle());
        values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
        values.put(DBHelper.Product.COLUMN_NAME_GROUP_ID, product.getGroupId());
        db.update(DBHelper.Product.TABLE_NAME, values,
                DBHelper.Product._ID + "=?", new String[] {String.valueOf(product.getId())});

    }

    public void deleteProducts (List<Product> products) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Product product: products) {
            db.delete(DBHelper.Product.TABLE_NAME, DBHelper.Product._ID + "=?", new String[] {String.valueOf(product.getId())});
        }
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
            list.add(new Product(title, date, quantity, null));
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
