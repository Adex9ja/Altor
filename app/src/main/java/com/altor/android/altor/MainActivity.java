package com.altor.android.altor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.altor.android.altor.pedometer.StepDetector;
import com.altor.android.altor.pedometer.StepListener;
import com.altor.android.altor.utils.BluetoothConnection;
import com.altor.android.altor.utils.DatabaseSchema;
import com.altor.android.altor.utils.MyDevice;
import com.altor.android.altor.utils.MyHandler;
import com.altor.android.altor.utils.OneTimeAlarm;
import com.altor.android.altor.utils.PrefManager;
import com.altor.android.altor.utils.Repository;
import com.altor.android.altor.utils.WristBandAPI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener, StepListener {
    private MyHandler handler;
    public static final BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView txtcalloriesconsumed,txtamountspent,txtunitconsumed;
    private PrefManager pref;
    private  double amountspent,calloryconsume,unitconsume;
    private Repository repo;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    private TextView risklevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        handler = new MyHandler(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        txtamountspent = (TextView)findViewById(R.id.txtamountspent);
        txtcalloriesconsumed = (TextView)findViewById(R.id.txtcalloriesconsumed);
        txtunitconsumed = (TextView)findViewById(R.id.txtunitconsumed);
        risklevel = (TextView)findViewById(R.id.risklevel);

        pref = new PrefManager(this);
        repo = new Repository(this);

    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLastStep();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLastStep();
    }

    private void saveLastStep() {
        pref.saveLastStep(numSteps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateDailyProfile();
        riskLevelCheck();
        double sex = pref.getSexValue();
        if(unitconsume > sex)
            DailylimitExceeded();
        startStepCounter();
    }

    private void startStepCounter() {
        numSteps = pref.getLastStepCount();
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void DailylimitExceeded() {
        Intent intent = new Intent(this, OneTimeAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, 10  , pendingIntent);
    }
    private void riskLevelCheck(){
        ArrayList<String[]> weeklyprofile = repo.fetchWeeklyDrinks();
        double unitconsume;
        unitconsume = 0;
        if( weeklyprofile != null){
            for(String [] str : weeklyprofile){
                for (int i =0; i< str[0].split(",").length; i++){
                    String []todaydrinkvalue =  str[0].split(",");
                    String[] todaydrinkunitandpercent = str[1].split(",");
                    unitconsume += Double.parseDouble(todaydrinkunitandpercent[i].split("%")[1]) * Double.parseDouble(todaydrinkvalue[i]);
                }
            }
            interpretRiskLevel(pref.getSexValue(),unitconsume);
        }
        else{
            risklevel.setText("Low Risk Level");
        }

    }

    private void interpretRiskLevel(double sexValue, double unitconsume) {
       if(sexValue == 2.0){
           if(unitconsume <= 14)
               risklevel.setText("Low Risk Level");
           else if(unitconsume <= 49)
               risklevel.setText("Increasing Risk Level");
           else if(unitconsume > 49)
               risklevel.setText("High Risk Level");
       }
       else{
           if(unitconsume <= 11.5)
               risklevel.setText("Low Risk Level");
           else if(unitconsume <= 34)
               risklevel.setText("Increasing Risk Level");
           else if(unitconsume > 35)
               risklevel.setText("High Risk Level");
       }
    }

    private void calculateDailyProfile() {
        String s1 = pref.getTodayDrinksValues();
        String s2 = pref.getTodayDrinksUnitAndPercent();
        amountspent = calloryconsume = unitconsume = 0;
        if( s1 != null && s2 != null && !TextUtils.isEmpty(s1) && !TextUtils.isEmpty(s2)){
            String []todaydrinkvalue =  s1.split(",");
            String[] todaydrinkunitandpercent = s2.split(",");
            for(int i= 0; i< todaydrinkvalue.length; i++){
                amountspent += Double.parseDouble(todaydrinkvalue[i]) * Double.parseDouble(todaydrinkunitandpercent[i].split("%")[2]);
                unitconsume += Double.parseDouble(todaydrinkunitandpercent[i].split("%")[1]) * Double.parseDouble(todaydrinkvalue[i]);
                Double alcoholpercent = Double.parseDouble(todaydrinkunitandpercent[i].split("%")[0]);
                calloryconsume += calculateCallories(calculateExactVolume(alcoholpercent,todaydrinkunitandpercent[i].split("%")[1]),alcoholpercent) * Double.parseDouble(todaydrinkvalue[i]);
            }
            txtamountspent.setText("£" + String.format("%.2f",amountspent));
            txtunitconsumed.setText(String.format("%.2f",unitconsume));
            txtcalloriesconsumed.setText(String.format("%.2f",calloryconsume));
        }
        else{
            txtamountspent.setText(amountspent +"");
            txtunitconsumed.setText(unitconsume + "");
            txtcalloriesconsumed.setText(calloryconsume + "");
        }

    }
    private Double calculateExactVolume(Double alcoholpercent,String unit){
        return (Double.parseDouble(unit) * 1000) / alcoholpercent;
    }
    private Double calculateCallories(Double volume, Double alcoholpercent){
        return (alcoholpercent/1000) * volume * 0.8 * 7;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings)
            startActivity(new Intent(this,SettingsActivity.class));
        else if(id == R.id.action_connect)
           connectWithDevice();

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_nearestpubs)
            showNearestPubs();
        else if(id == R.id.nav_get_taxi)
            getATaxi();
        else if(id == R.id.nav_logout)
            logout();
        else if(id == R.id.nav_contat_us)
            contactUs();
        else if(id == R.id.nav_about)
            aboutApp();
        else if(id == R.id.nav_share)
            share();
        else if(id == R.id.nav_drinks)
            showListOfDrinks();
        else if(id == R.id.nav_check_alcohol)
            startActivity(new Intent(this,CheckAlcoholLevel.class));
        else if(id == R.id.nav_health_advice)
            startActivity(new Intent(this,HealthAdvice.class));


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showListOfDrinks() {
        startActivity(new Intent(this,ListOfDrinks.class));
    }
    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Checkout Altor app!");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
    private void aboutApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.about));
        builder.setTitle("About");
        builder.setCancelable(true);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private void contactUs() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent sendMail = new Intent(Intent.ACTION_SEND);
                    sendMail.putExtra(Intent.EXTRA_EMAIL,new String[]{ "info@altor.com"});
                    sendMail.putExtra(Intent.EXTRA_SUBJECT, "Customer's Feedback");
                    sendMail.setType("message/rfc822");
                    startActivity(sendMail);
                }catch (Exception e){
                    Toast.makeText(MainActivity.this,"No email provider found",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                handler.obtainMessage(0,null).sendToTarget();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });
        builder.show();
    }
    private void getATaxi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=taxi");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Please install a map on your device",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showNearestPubs() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=pubs,bars");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Please install a map on your device",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void connectWithDevice() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!bAdapter.isEnabled())
                    bAdapter.enable();
                new BluetoothConnection(MainActivity.this);
                bAdapter.startDiscovery();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        isCalloryBurnt();

    }

    private void isCalloryBurnt() {
        if(numSteps == 10000){
            pref.saveNoofCallorie((pref.getNoofCallorie() - 3500) < 0 ? 0 : pref.getNoofCallorie() - 3500);
            numSteps = 0;
        }

    }


}
