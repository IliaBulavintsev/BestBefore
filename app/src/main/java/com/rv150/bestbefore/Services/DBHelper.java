package com.rv150.bestbefore.Services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Rudnev on 30.10.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance;
    
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
        public static final String COLUMN_NAME_PRODUCED = "produced";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_GROUP_ID = "group_id";
        public static final String COLUMN_NAME_VIEWED = "viewed";
        public static final String COLUMN_NAME_REMOVED = "removed";
        public static final String COLUMN_NAME_REMOVED_AT = "removed_at";
        public static final String COLUMN_NAME_MEASURE = "measure";
        public static final String COLUMN_NAME_PHOTO = "photo";
    }

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "BestBefore.db";

    private static final String SQL_CREATE_AUTOCOMPLETED_TABLE  =
            "CREATE TABLE " + AutoCompletedProducts.TABLE_NAME + " (" +
                    AutoCompletedProducts._ID + " INTEGER PRIMARY KEY," +
                    AutoCompletedProducts.COLUMN_NAME_NAME + " VARCHAR(100) UNIQUE)";

    private static final String SQL_CREATE_GROUP_TABLE  =
            "CREATE TABLE " + Group.TABLE_NAME + " (" +
                    Group._ID + " INTEGER PRIMARY KEY," +
                    Group.COLUMN_NAME_NAME + " VARCHAR(50) UNIQUE)";

    private static final String SQL_CREATE_PRODUCT_TABLE  =
            "CREATE TABLE " + Product.TABLE_NAME + " (" +
                    Product._ID + " INTEGER PRIMARY KEY," +
                    Product.COLUMN_NAME_NAME + " VARCHAR(100) DEFAULT NULL, " +
                    Product.COLUMN_NAME_PRODUCED + " INTEGER DEFAULT 0," +
                    Product.COLUMN_NAME_DATE + " INTEGER NOT NULL," +
                    Product.COLUMN_NAME_CREATED_AT + " INTEGER NOT NULL," +
                    Product.COLUMN_NAME_QUANTITY + " INTEGER DEFAULT 1," +
                    Product.COLUMN_NAME_GROUP_ID + " INTEGER," +
                    Product.COLUMN_NAME_VIEWED + " INTEGER DEFAULT 0," +
                    Product.COLUMN_NAME_REMOVED + " INTEGER DEFAULT 0," +
                    Product.COLUMN_NAME_REMOVED_AT + " INTEGER DEFAULT 0," +
                    Product.COLUMN_NAME_MEASURE + " INTEGER DEFAULT 0," +
                    Product.COLUMN_NAME_PHOTO + " INTEGER DEFAULT 0," +
                    "FOREIGN KEY (" + Product.COLUMN_NAME_GROUP_ID + ") REFERENCES " +
                    Group.TABLE_NAME + "(" + Group._ID + ") ON DELETE CASCADE)";

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_AUTOCOMPLETED_TABLE);
        db.execSQL(SQL_CREATE_GROUP_TABLE);
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
            case 2:
            case 3: {
                db.execSQL(SQL_CREATE_GROUP_TABLE);
                db.execSQL(SQL_CREATE_PRODUCT_TABLE);
            }
            case 4:
                db.execSQL("ALTER TABLE " + Product.TABLE_NAME +
                        " ADD COLUMN " + Product.COLUMN_NAME_REMOVED + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Product.TABLE_NAME +
                        " ADD COLUMN " + Product.COLUMN_NAME_REMOVED_AT + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Product.TABLE_NAME +
                        " ADD COLUMN " + Product.COLUMN_NAME_PRODUCED + " INTEGER DEFAULT 0");
            case 5:
                db.execSQL("ALTER TABLE " + Product.TABLE_NAME +
                        " ADD COLUMN " + Product.COLUMN_NAME_MEASURE + " INTEGER DEFAULT 0");
            case 6: {
                db.beginTransaction();
                String tempTable = "temp_table";
                try {
                    db.execSQL("ALTER TABLE " + Product.TABLE_NAME + " RENAME TO " + tempTable);
                    db.execSQL(SQL_CREATE_PRODUCT_TABLE);
                    db.execSQL("INSERT INTO " + Product.TABLE_NAME + " (" +
                            Product.COLUMN_NAME_NAME + ", " +
                            Product.COLUMN_NAME_PRODUCED + ", " +
                            Product.COLUMN_NAME_DATE + ", " +
                            Product.COLUMN_NAME_CREATED_AT + ", " +
                            Product.COLUMN_NAME_QUANTITY + ", " +
                            Product.COLUMN_NAME_GROUP_ID + ", " +
                            Product.COLUMN_NAME_VIEWED + ", " +
                            Product.COLUMN_NAME_REMOVED + ", " +
                            Product.COLUMN_NAME_REMOVED_AT + ", " +
                            Product.COLUMN_NAME_MEASURE + ") SELECT " +
                                    Product.COLUMN_NAME_NAME + ", " +
                                    Product.COLUMN_NAME_PRODUCED + ", " +
                                    Product.COLUMN_NAME_DATE + ", " +
                                    Product.COLUMN_NAME_CREATED_AT + ", " +
                                    Product.COLUMN_NAME_QUANTITY + ", " +
                                    Product.COLUMN_NAME_GROUP_ID + ", " +
                                    Product.COLUMN_NAME_VIEWED + ", " +
                                    Product.COLUMN_NAME_REMOVED + ", " +
                                    Product.COLUMN_NAME_REMOVED_AT + ", " +
                                    Product.COLUMN_NAME_MEASURE + " FROM " + tempTable);
                    db.execSQL("DROP TABLE " + tempTable);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            default:
                break;
        }
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

