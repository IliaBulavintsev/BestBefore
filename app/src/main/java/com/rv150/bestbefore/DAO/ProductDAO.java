package com.rv150.bestbefore.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.Resources;
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

    public List<Product> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME +
        " WHERE " + DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(0)});
        List<Product> products = new ArrayList<>();

        while (cursor.moveToNext()) {
            products.add(mapProduct(cursor));
        }
        cursor.close();
        return products;
    }

    public List<Product> getOverdued() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar now = new GregorianCalendar();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_DATE + " < ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query,  new String[]{
                String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
        List<Product> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            products.add(mapProduct(cursor));
        }
        cursor.close();
        return products;
    }

    public List<Product> getFresh() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar now = new GregorianCalendar();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_DATE + " > ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
        List<Product> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            products.add(mapProduct(cursor));
        }
        cursor.close();
        return products;
    }

    public List<Product> getRemoved() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_REMOVED + " = ? ORDER BY " +
                DBHelper.Product.COLUMN_NAME_REMOVED_AT + " DESC";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(1)});
        List<Product> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            products.add(mapProduct(cursor));
        }
        cursor.close();
        return products;
    }


    public List<Product> getFreshFromGroup (long groupId) {
        if (groupId == Resources.ID_MAIN_GROUP) {
            return getAll();              // Для основной группы вернуть все продукты
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar now = new GregorianCalendar();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_GROUP_ID + " = ? AND "
                + DBHelper.Product.COLUMN_NAME_DATE + " > ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(groupId), String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
        List<Product> products = new ArrayList<>();

        while (cursor.moveToNext()) {
            products.add(mapProduct(cursor));
        }
        cursor.close();
        return products;
    }




    private Product mapProduct(final Cursor cursor) {
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

        int viewed = cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_VIEWED));

        int removed = cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_REMOVED));
        long removedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_REMOVED_AT));


        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(dateInMillis);

        Calendar createdAt =  new GregorianCalendar();
        createdAt.setTimeInMillis(createdAtInMillis);

        Product product = new Product(name, date, createdAt, quantity, groupId);
        product.setId(id);
        product.setViewed(viewed);
        product.setmRemoved(removed);
        product.setmRemovedAt(removedAt);
        return product;
    }

    public void insertProducts (List<Product> products) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (Product product: products) {
            values.put(DBHelper.Product.COLUMN_NAME_NAME, product.getTitle());
            values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
            values.put(DBHelper.Product.COLUMN_NAME_CREATED_AT, product.getCreatedAt().getTimeInMillis());
            values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
            values.put(DBHelper.Product.COLUMN_NAME_GROUP_ID, product.getGroupId());
            values.put(DBHelper.Product.COLUMN_NAME_VIEWED, product.getViewed());
            values.put(DBHelper.Product.COLUMN_NAME_REMOVED, product.getmRemoved());
            values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, product.getmRemovedAt());
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
        values.put(DBHelper.Product.COLUMN_NAME_VIEWED, product.getViewed());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, product.getmRemoved());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, product.getmRemovedAt());
        return db.insert(DBHelper.Product.TABLE_NAME, null, values);
    }


    public void deleteProduct(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, 1);
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, Calendar.getInstance().getTimeInMillis());
        db.update(DBHelper.Product.TABLE_NAME, values,
                DBHelper.Product._ID + " = ?", new String[] {String.valueOf(id)});
    }

    public void removeProductFromTrash(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.Product.TABLE_NAME, DBHelper.Product._ID + " = ?", new String[] {String.valueOf(id)});
    }

    public void updateProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_NAME, product.getTitle());
        values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
        values.put(DBHelper.Product.COLUMN_NAME_GROUP_ID, product.getGroupId());
        values.put(DBHelper.Product.COLUMN_NAME_VIEWED, product.getViewed());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, product.getmRemoved());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, product.getmRemovedAt());
        db.update(DBHelper.Product.TABLE_NAME, values,
                DBHelper.Product._ID + " = ?", new String[] {String.valueOf(product.getId())});

    }


    public void deleteFreshFromGroup (long groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Calendar now = new GregorianCalendar();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, 1);
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, now.getTimeInMillis());
        db.update(DBHelper.Product.TABLE_NAME, values,
                DBHelper.Product.COLUMN_NAME_GROUP_ID + " = ? AND " +
                DBHelper.Product.COLUMN_NAME_DATE + " > ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?",
                new String[] {String.valueOf(groupId),
                        String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
    }

    public void deleteFresh() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Calendar now = new GregorianCalendar();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, 1);
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, now.getTimeInMillis());
        db.update(DBHelper.Product.TABLE_NAME, values,
                DBHelper.Product.COLUMN_NAME_DATE + " > ? AND " +
                        DBHelper.Product.COLUMN_NAME_REMOVED + " = ?",
                new String[] {String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
    }

    public void deleteOverdued() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Calendar now = new GregorianCalendar();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, 1);
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, now.getTimeInMillis());
        db.update(DBHelper.Product.TABLE_NAME, values,
                DBHelper.Product.COLUMN_NAME_DATE + " < ? AND " +
                        DBHelper.Product.COLUMN_NAME_REMOVED + " = ?",
                new String[] {String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
    }


    public void clearTrash() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        db.delete(DBHelper.Product.TABLE_NAME, whereClause, new String[] {String.valueOf(1)});
    }

    public void deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.Product.TABLE_NAME, null, null);
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
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(String.valueOf(i));
            editor.remove(String.valueOf(i+500));
            editor.remove(String.valueOf(i+1000));
            editor.remove(Resources.QUANTITY + String.valueOf(i));
            editor.apply();
        }
        return list;
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
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(String.valueOf("del" + String.valueOf(i)));
            editor.remove(String.valueOf("del" + String.valueOf(i + 1000)));
            editor.remove("del" + Resources.QUANTITY + String.valueOf(i));
            editor.apply();
        }
        return list;
    }
}
