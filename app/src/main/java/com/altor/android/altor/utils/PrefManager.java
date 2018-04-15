package com.altor.android.altor.utils;

/**
 * Created by ADEOLU on 3/15/2017.
 */
import android.content.Context;
import android.content.SharedPreferences;

import com.altor.android.altor.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "SPLASHSCREEN";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");




    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(_context.getString(R.string.firstlaunch), isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(_context.getString(R.string.firstlaunch), true);
    }
    public void saveLoginCookie(String[] data) {
        editor.putString(_context.getString(R.string.username),data[0]);
        editor.putString(_context.getString(R.string.age),data[1]);
        editor.putString(_context.getString(R.string.weight),data[2]);
        editor.putString(_context.getString(R.string.sex),data[3]);
        editor.commit();
    }
   public String isUserLoggedIn() {
        return pref.getString(_context.getString(R.string.username), null);
    }
    public void saveDailyDrinks(String values,String units){
        editor.putString(_context.getString(R.string.presentdate), sdf.format(new Date().getTime()));
        editor.putString(_context.getString(R.string.drinkvalues),values);
        editor.putString(_context.getString(R.string.drinkunits),units);
        editor.commit();
    }
    public String getTodayDrinksValues() {
        String value = null;
        Date date1,date2;
        date1 = date2 = null;
        String lastdate = pref.getString(_context.getString(R.string.presentdate),null);
       try {
            date1 = sdf.parse(lastdate);
            date2 = sdf.parse(sdf.format(new Date().getTime()));
       }
       catch (Exception e){
           e.printStackTrace();
           return null;
       }
        if(lastdate!=null && date1.compareTo(date2) == 0)
            return pref.getString(_context.getString(R.string.drinkvalues),null);
        else
            return null;
    }
    public String getTodayDrinksUnitAndPercent() {
        String value = null;
        Date date1,date2;
        date1 = date2 = null;
        String lastdate = pref.getString(_context.getString(R.string.presentdate),null);
        try {
            date1 = sdf.parse(lastdate);
            date2 = sdf.parse(sdf.format(new Date().getTime()));
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        if(lastdate!=null && date1.compareTo(date2) == 0)
            return pref.getString(_context.getString(R.string.drinkunits),null);
        else
            return null;
    }

    public void saveLastStep(int numSteps) {
        editor.putInt(_context.getString(R.string.noofsteps),numSteps);
        editor.commit();
    }

    public int getLastStepCount() {
        return pref.getInt(_context.getString(R.string.noofsteps),0);
    }

    public void saveNoofCallorie(int i) {
        editor.putInt(_context.getString(R.string.noofcallorie),i);
        editor.commit();
    }

    public int getNoofCallorie() {
        return pref.getInt(_context.getString(R.string.noofcallorie),0);
    }

    public double getSexValue() {
        return pref.getString(_context.getString(R.string.sex),"male").equalsIgnoreCase("male") ? 2.0 : 1.5;
    }
}