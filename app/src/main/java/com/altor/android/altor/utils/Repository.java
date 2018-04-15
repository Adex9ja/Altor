package com.altor.android.altor.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Created by ADEOLU on 4/18/2017.
 */
public class Repository {
    private DatabaseSchema mDbHelper;
    private SQLiteDatabase db;

    public Repository(Context context){
        mDbHelper = new DatabaseSchema(context);
        db =  mDbHelper.getWritableDatabase();
    }

    public long saveAddedDrinks(String [] data){
        ContentValues values = new ContentValues();
        values.put(DatabaseSchema.DrinksEntry.COLUMN_NAME_CATEGORY,data[0] );
        values.put(DatabaseSchema.DrinksEntry.COLUMN_NAME_TYPE, data[1]);
        values.put(DatabaseSchema.DrinksEntry.COLUMN_NAME_BRAND, data[2]);
        values.put(DatabaseSchema.DrinksEntry.COLUMN_NAME_SERVING, data[3]);
        values.put(DatabaseSchema.DrinksEntry.COLUMN_NAME_PRICE, data[4]);

         return db.insert(DatabaseSchema.DrinksEntry.TABLE_NAME, null, values);
    }
    public ArrayList<String[]> fetchAllAddedDrinks(){
        ArrayList<String[]> arr = new ArrayList<>();
        String [] result;
        String[] projection = {
                DatabaseSchema.DrinksEntry.COLUMN_NAME_CATEGORY,
                DatabaseSchema.DrinksEntry.COLUMN_NAME_TYPE,
                DatabaseSchema.DrinksEntry.COLUMN_NAME_BRAND,
                DatabaseSchema.DrinksEntry.COLUMN_NAME_SERVING,
                DatabaseSchema.DrinksEntry.COLUMN_NAME_PRICE};

        Cursor cursor = db.query(
                DatabaseSchema.DrinksEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                       // The columns for the WHERE clause
                null,                      // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        try {
            while(cursor.moveToNext()) {
                 result = new String[]{
                         cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DrinksEntry.COLUMN_NAME_CATEGORY)),
                         cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DrinksEntry.COLUMN_NAME_TYPE)),
                         cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DrinksEntry.COLUMN_NAME_BRAND)),
                         cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DrinksEntry.COLUMN_NAME_SERVING)),
                         cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DrinksEntry.COLUMN_NAME_PRICE))
                 };
                arr.add(result);
            }
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return  arr;
    }

    public long saveDailyDrinks(String [] data){

        ContentValues values = new ContentValues();
        values.put(DatabaseSchema.DailyRecord.COLUMN_NAME_VALUE,data[0] );
        values.put(DatabaseSchema.DailyRecord.COLUMN_NAME_UNIT, data[1]);
        values.put(DatabaseSchema.DailyRecord.COLUMN_NAME_DATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        long id = db.insert(DatabaseSchema.DailyRecord.TABLE_NAME, null, values);
        return id;
    }

    public ArrayList<String[]> fetchWeeklyDrinks(){
        ArrayList<String[]> arr = new ArrayList<>();
        String [] result;
        String[] projection = {
                DatabaseSchema.DailyRecord.COLUMN_NAME_VALUE,
                DatabaseSchema.DailyRecord.COLUMN_NAME_UNIT };
        String selection = DatabaseSchema.DailyRecord.COLUMN_NAME_DATE + " >= ? " ;
        long lastweek = System.currentTimeMillis()- (7 * 1000 * 60 * 60 * 24);
        String[] selectionArgs = { lastweek + "" };

        try {
            Cursor cursor = db.query(
                    DatabaseSchema.DailyRecord.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                    selection,                       // The columns for the WHERE clause
                    selectionArgs,                      // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );

            while(cursor.moveToNext()) {
                result = new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DailyRecord.COLUMN_NAME_VALUE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DailyRecord.COLUMN_NAME_UNIT))
                };
                arr.add(result);
            }
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return  arr;
    }

    public ArrayList<String[]> fetchMonthlyDrinks(int month){
        ArrayList<String[]> arr = new ArrayList<>();
        String [] result;
        String[] projection = {
                DatabaseSchema.DailyRecord.COLUMN_NAME_VALUE,
                DatabaseSchema.DailyRecord.COLUMN_NAME_UNIT ,
                DatabaseSchema.DailyRecord.COLUMN_NAME_DATE };
     /**   String selection = DatabaseSchema.DailyRecord.COLUMN_NAME_DATE + " > ? and " + DatabaseSchema.DailyRecord.COLUMN_NAME_DATE + " < ? ";
        long frommonth = System.currentTimeMillis()- (month * 31 * 1000 * 60 * 60 * 24);
        long tomonth = (System.currentTimeMillis()- (month * 31 * 1000 * 60 * 60 * 24)) + (31 * 1000 * 60 * 60 * 24);
        long today = System.currentTimeMillis();
        String[] selectionArgs = { frommonth + "", tomonth + "" };*/

        try {
            Cursor cursor = db.query(
                    DatabaseSchema.DailyRecord.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                    null,                       // The columns for the WHERE clause
                    null,                      // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );
            while(cursor.moveToNext()) {
                result = new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DailyRecord.COLUMN_NAME_VALUE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DailyRecord.COLUMN_NAME_UNIT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.DailyRecord.COLUMN_NAME_DATE))
                };
                if(calculateDate(month,result[2]))
                    arr.add(result);
            }
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return  arr;
    }

    public boolean calculateDate(int  month,String dateDB) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        Date date = new Date();
        c.setTime(date);
        c2.setTime(date);
        c.add(Calendar.DATE, - (30 * month));
        c2.add(Calendar.DATE, - ((30 * month) - 31)) ;
        String lastmonth = format.format(c.getTime());
        String followingmonth = format.format(c2.getTime());
        boolean res = false;
        try {
            Date first = format.parse(lastmonth);
            Date second = format.parse(followingmonth);
            Date dbDate =format.parse(dateDB);

            if(dbDate.after(first) && dbDate.before(second))
                res = true;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
