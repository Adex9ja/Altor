package com.altor.android.altor.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by ADEOLU on 4/18/2017.
 */
public class DatabaseSchema extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "AltorDB.db";
    private PrefManager pref;

    //dRINKS ADD
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DrinksEntry.TABLE_NAME + " (" +
                    DrinksEntry._ID + " INTEGER PRIMARY KEY," +
                    DrinksEntry.COLUMN_NAME_CATEGORY + " TEXT," +
                    DrinksEntry.COLUMN_NAME_TYPE + " TEXT ," +
                    DrinksEntry.COLUMN_NAME_BRAND + " TEXT," +
                    DrinksEntry.COLUMN_NAME_SERVING + " TEXT," +
                    DrinksEntry.COLUMN_NAME_PRICE + " TEXT" + ")";
    private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + DrinksEntry.TABLE_NAME;

    //dAILY RECORD
    private static final String SQL_CREATE_ENTRIES2 =
            "CREATE TABLE " + DailyRecord.TABLE_NAME + " (" +
                    DailyRecord._ID + " INTEGER PRIMARY KEY," +
                    DailyRecord.COLUMN_NAME_VALUE + " TEXT," +
                    DailyRecord.COLUMN_NAME_UNIT + " TEXT," +
                    DailyRecord.COLUMN_NAME_DATE + " TEXT," +
                    " UNIQUE (" + DailyRecord.COLUMN_NAME_DATE + ") ON CONFLICT REPLACE);";

    private static final String SQL_DELETE_ENTRIES2 =
            "DROP TABLE IF EXISTS " + DailyRecord.TABLE_NAME;


    public DatabaseSchema(Context mcontext){
        super(mcontext, DATABASE_NAME, null, DATABASE_VERSION);
        pref = new PrefManager(mcontext);
       }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES2);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTRIES2);
        pref.saveNoofCallorie(0);
        pref.saveDailyDrinks("","");
        pref.saveLastStep(0);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public static class DrinksEntry implements BaseColumns {
        public static final String TABLE_NAME = "drinks_entity";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_BRAND = "brand";
        public static final String COLUMN_NAME_SERVING = "serving";
        public static final String COLUMN_NAME_PRICE = "price";
    }

    public static class DailyRecord implements BaseColumns {
        public static final String TABLE_NAME = "dailyrecord_entity";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_UNIT = "unit";
        public static final String COLUMN_NAME_DATE = "recorddate";
    }
}
