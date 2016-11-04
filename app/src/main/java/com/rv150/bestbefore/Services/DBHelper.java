package com.rv150.bestbefore.Services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Rudnev on 30.10.2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    
    public static class AutoCompletedProducts implements BaseColumns {
        public static final String TABLE_NAME = "user_products";
        public static final String COLUMN_NAME_NAME = "name";
    }

    public static class Group implements BaseColumns {
        public static final String TABLE_NAME = "groups";
        public static final String COLUMN_NAME_NAME = "name";
    }

    public static class Product implements BaseColumns {
        public static final String TABLE_NAME = "product";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_GROUP_ID = "group_id";
    }

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "BestBefore.db";

    private static final String SQL_CREATE_AUTOCOMPLETED_TABLE  =
            "CREATE TABLE " + AutoCompletedProducts.TABLE_NAME + " (" +
                    AutoCompletedProducts._ID + " INTEGER PRIMARY KEY," +
                    AutoCompletedProducts.COLUMN_NAME_NAME + " VARCHAR(100) UNIQUE)";
    private static final String SQL_CREATE_GROUP_TABLE  =
            "CREATE TABLE " + Group.TABLE_NAME + " (" +
                    Group._ID + " INTEGER PRIMARY KEY," +
                    Group.COLUMN_NAME_NAME + " VARCHAR(50))";
    private static final String SQL_CREATE_PRODUCT_TABLE  =
            "CREATE TABLE " + Product.TABLE_NAME + " (" +
                    Product._ID + " INTEGER PRIMARY KEY," +
                    Product.COLUMN_NAME_NAME + " VARCHAR(100) NOT NULL, " +
                    Product.COLUMN_NAME_DATE + " INTEGER NOT NULL," +
                    Product.COLUMN_NAME_CREATED_AT + " INTEGER NOT NULL," +
                    Product.COLUMN_NAME_QUANTITY + " INTEGER DEFAULT 1," +
                    Product.COLUMN_NAME_GROUP_ID + " INTEGER," +
                    "FOREIGN KEY (" + Product.COLUMN_NAME_GROUP_ID + ") REFERENCES " +
                    Group.TABLE_NAME + "(" + Group._ID + ") ON DELETE CASCADE)";


    private static final String SQL_DELETE_AUTOCOMPLETED_TABLE =
            "DROP TABLE IF EXISTS " + AutoCompletedProducts.TABLE_NAME;
    private static final String SQL_DELETE_GROUP_TABLE =
            "DROP TABLE IF EXISTS " + Group.TABLE_NAME;
    private static final String SQL_DELETE_PRODUCT_TABLE =
            "DROP TABLE IF EXISTS " + Product.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_AUTOCOMPLETED_TABLE);
        db.execSQL(SQL_CREATE_GROUP_TABLE);
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_AUTOCOMPLETED_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}

