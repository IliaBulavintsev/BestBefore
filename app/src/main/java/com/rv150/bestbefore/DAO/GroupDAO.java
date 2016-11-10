package com.rv150.bestbefore.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Services.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudnev on 05.11.2016.
 */

public class GroupDAO {
    private DBHelper dbHelper;

    public GroupDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Group> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Group.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        List<Group> groups = new ArrayList<>();

        while (cursor.moveToNext()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.Group.COLUMN_NAME_NAME));

            Long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBHelper.Group._ID));

            Group group = new Group(name);
            group.setId(id);
            groups.add(group);
        }
        cursor.close();
        return groups;
    }

    public Group get(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Group.TABLE_NAME +
                " WHERE " + DBHelper.Group._ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.Group.COLUMN_NAME_NAME));
        Group group = new Group(name);
        group.setId(id);
        cursor.close();
        return group;
    }

    public Group get (String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.Group.TABLE_NAME +
                " WHERE " + DBHelper.Group.COLUMN_NAME_NAME + " = \"" + name + "\"";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToNext();
        long id = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBHelper.Group._ID));
        Group group = new Group(name);
        group.setId(id);
        cursor.close();
        return group;
    }



    public long insertGroup(Group group) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Group.COLUMN_NAME_NAME, group.getName());
        try {
            return db.insertOrThrow(DBHelper.Group.TABLE_NAME, null, values);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteGroup(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.Group.TABLE_NAME, DBHelper.Group._ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateGroup(Group group) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.Group.COLUMN_NAME_NAME, group.getName());
        db.update(DBHelper.Group.TABLE_NAME, values,
                DBHelper.Group._ID + "=?", new String[]{String.valueOf(group.getId())});

    }

    public void deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.Group.TABLE_NAME, null, null);
    }
}
