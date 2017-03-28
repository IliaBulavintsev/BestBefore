package com.rv150.bestbefore.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    private static ProductDAO instance;

    private ProductDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public static ProductDAO getInstance(Context context) {
        if (instance == null) {
            instance = new ProductDAO(context);
        }
        return instance;
    }


    public List<Product> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Product.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        List<Product> products = new ArrayList<>();
        while (cursor.moveToNext()) {
            products.add(mapProduct(cursor));
        }
        cursor.close();
        return products;
    }

    public List<Product> getAllNotRemoved() {
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
        Calendar now = Calendar.getInstance();
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
            return getAllNotRemoved();              // Для основной группы вернуть все продукты
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

        String name = null;
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_NAME))) {
            name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_NAME));
        }


        long dateInMillis = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_DATE));

        long createdAtInMillis = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_CREATED_AT));

        long producedInMillis = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_PRODUCED));

        int quantity = cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_QUANTITY));

        int measure = cursor.getInt(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_MEASURE));

        long groupId;
        if (cursor.isNull(cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_GROUP_ID))) {
            groupId = -1;
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

        long photo = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBHelper.Product.COLUMN_NAME_PHOTO));


        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dateInMillis);

        Calendar createdAt =  Calendar.getInstance();
        createdAt.setTimeInMillis(createdAtInMillis);

        Calendar produced = Calendar.getInstance();
        produced.setTimeInMillis(producedInMillis);

        Product product = new Product(name, date, createdAt, quantity, groupId);
        product.setId(id);
        product.setViewed(viewed);
        product.setRemoved(removed);
        product.setRemovedAt(removedAt);
        product.setProduced(produced);
        product.setMeasure(measure);
        product.setPhoto(photo);
        return product;
    }


    public long insertProduct (Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String name = product.getTitle();
        if (name != null && !name.isEmpty()) {
            values.put(DBHelper.Product.COLUMN_NAME_NAME, name);
        }

        values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_CREATED_AT, product.getCreatedAt().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
        values.put(DBHelper.Product.COLUMN_NAME_MEASURE, product.getMeasure());
        long groupId = product.getGroupId();
        if (groupId == -1) {
            values.putNull(DBHelper.Product.COLUMN_NAME_GROUP_ID);
        }
        else {
            values.put(DBHelper.Product.COLUMN_NAME_GROUP_ID, groupId);
        }
        values.put(DBHelper.Product.COLUMN_NAME_VIEWED, product.getViewed());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, product.getRemoved());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, product.getRemovedAt());
        values.put(DBHelper.Product.COLUMN_NAME_PRODUCED, product.getProduced().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_PHOTO, product.getPhoto());
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

    public void markRestored(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, 0);
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

        String name = product.getTitle();
        if (name == null || name.isEmpty()) {
            values.putNull(DBHelper.Product.COLUMN_NAME_NAME);
        }
        else {
            values.put(DBHelper.Product.COLUMN_NAME_NAME, name);
        }

        values.put(DBHelper.Product.COLUMN_NAME_DATE, product.getDate().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_QUANTITY, product.getQuantity());
        values.put(DBHelper.Product.COLUMN_NAME_MEASURE, product.getMeasure());
        long groupId = product.getGroupId();
        if (groupId == -1) {
            values.putNull(DBHelper.Product.COLUMN_NAME_GROUP_ID);
        }
        else {
            values.put(DBHelper.Product.COLUMN_NAME_GROUP_ID, groupId);
        }
        values.put(DBHelper.Product.COLUMN_NAME_VIEWED, product.getViewed());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED, product.getRemoved());
        values.put(DBHelper.Product.COLUMN_NAME_REMOVED_AT, product.getRemovedAt());
        values.put(DBHelper.Product.COLUMN_NAME_PRODUCED, product.getProduced().getTimeInMillis());
        values.put(DBHelper.Product.COLUMN_NAME_PHOTO, product.getPhoto());
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

    public int getFreshCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar now = Calendar.getInstance();
        String query = "SELECT " + DBHelper.Product._ID + " FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_DATE + " > ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getOverduedCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar now = Calendar.getInstance();
        String query = "SELECT " + DBHelper.Product._ID + " FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_DATE + " < ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query,  new String[]{
                String.valueOf(now.getTimeInMillis()), String.valueOf(0)});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }



    public int getRemovedCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DBHelper.Product._ID + " FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " + DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(1)});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }



    // Возвращает кол-во свежих продуктов в данной группе
    public int getCountForGroup (long groupId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar now = Calendar.getInstance();

        String query = "SELECT " + DBHelper.Product._ID + " FROM " + DBHelper.Product.TABLE_NAME +
                " WHERE " +
                DBHelper.Product.COLUMN_NAME_GROUP_ID + " = ? AND " +
                DBHelper.Product.COLUMN_NAME_DATE + " > ? AND " +
                DBHelper.Product.COLUMN_NAME_REMOVED + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(groupId),
                String.valueOf(now.getTimeInMillis()),
                String.valueOf(0)
        });

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

}
