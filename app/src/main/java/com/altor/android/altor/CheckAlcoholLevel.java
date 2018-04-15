package com.altor.android.altor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.altor.android.altor.utils.MyViewPagerAdapter;
import com.altor.android.altor.utils.PrefManager;
import com.altor.android.altor.utils.Repository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;

public class CheckAlcoholLevel extends AppCompatActivity {

    private ViewPager viewPager;
    private CustomPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private PrefManager pref;
    private Repository repo;
    private TextView txtcalloriesconsumed,txtamountspent,txtunitconsumed;
    private BarChart barChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_alcohol_level);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);

        layouts = new int[]{
                R.layout.daily_alcohol_monitor,
                R.layout.weekly_alcohol_monitor,
                R.layout.monthly_alcohol_monitor};

        addBottomDots(0);

         myViewPagerAdapter = new CustomPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        pref = new PrefManager(this);
        repo = new Repository(this);

    }

    private void calculateDailyProfile(View view) {
        txtamountspent = (TextView)view.findViewById(R.id.txtamountspent);
        txtcalloriesconsumed = (TextView)view.findViewById(R.id.txtcalloriesconsumed);
        txtunitconsumed = (TextView)view.findViewById(R.id.txtunitconsumed);
        String s1 = pref.getTodayDrinksValues();
        String s2 = pref.getTodayDrinksUnitAndPercent();
        double amountspent,calloryconsume,unitconsume;
        amountspent = calloryconsume = unitconsume = 0;
        if( s1 != null && s2 != null){
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
            txtamountspent.setText("£" + amountspent +"");
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
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(Color.GRAY);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.WHITE);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
    public class CustomPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public CustomPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            if(position == 0)
                calculateDailyProfile(view);
            else if(position == 1)
                calculateWeeklyProfile(view);
            else if(position == 2)
                calculateMonthlyProfile(view);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private void calculateMonthlyProfile(View view) {
        barChart = (BarChart) view.findViewById(R.id.chart);
        //Creating dataset
       /** ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(4, 0));
        entries.add(new BarEntry(8, 1));
        entries.add(new BarEntry(6, 2));
        entries.add(new BarEntry(12, 3));
        entries.add(new BarEntry(18, 4));
        entries.add(new BarEntry(9, 5));*/

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(getMonthData(6), 0));
        entries.add(new BarEntry(getMonthData(5), 1));
        entries.add(new BarEntry(getMonthData(4), 2));
        entries.add(new BarEntry(getMonthData(3), 3));
        entries.add(new BarEntry(getMonthData(2), 4));
        entries.add(new BarEntry(getMonthData(1), 5));

        BarDataSet dataset = new BarDataSet(entries, "6 Months Report");

        // creating labels
        ArrayList<String> labels = new ArrayList<>();
        labels.add(getMonthInWord(Calendar.MONTH -5));
        labels.add(getMonthInWord(Calendar.MONTH -4));
        labels.add(getMonthInWord(Calendar.MONTH -3));
        labels.add(getMonthInWord(Calendar.MONTH -2));
        labels.add(getMonthInWord(Calendar.MONTH -1));
        labels.add(getMonthInWord(Calendar.MONTH));

        BarData data = new BarData(labels, dataset);
        barChart.setData(data);

        barChart.setDescription("Description");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
    }

    private float getMonthData(int j) {
        ArrayList<String[]> monthlyprofile = repo.fetchMonthlyDrinks(j);
        double amountspent,calloryconsume,unitconsume;
        amountspent = calloryconsume = unitconsume = 0;
        if( monthlyprofile != null){
            for(String [] str : monthlyprofile){
                for (int i =0; i< str[0].split(",").length; i++){
                    String []todaydrinkvalue =  str[0].split(",");
                    String[] todaydrinkunitandpercent = str[1].split(",");
                    amountspent += Double.parseDouble(todaydrinkvalue[i]) * Double.parseDouble(todaydrinkunitandpercent[i].split("%")[2]);
                    unitconsume += Double.parseDouble(todaydrinkunitandpercent[i].split("%")[1]) * Double.parseDouble(todaydrinkvalue[i]);
                    Double alcoholpercent = Double.parseDouble(todaydrinkunitandpercent[i].split("%")[0]);
                    calloryconsume += calculateCallories(calculateExactVolume(alcoholpercent,todaydrinkunitandpercent[i].split("%")[1]),alcoholpercent) * Double.parseDouble(todaydrinkvalue[i]);
                }
            }
        }
        return (float)calloryconsume;
    }

    private String getMonthInWord(int i){
        switch (i){
            case 0:
            case -12:
                return "January";
            case 1:
            case -11:
                return "February";
            case 2:
            case -10:
                return "March";
            case 3:
            case -9:
                return "April";
            case 4:
            case -8:
                return "May";
            case 5:
            case -7:
                return "June";
            case 6:
            case -6:
                return "July";
            case 7:
            case -5:
                return "August";
            case 8:
            case -4:
                return "September";
            case 9:
            case -3:
                return "October";
            case 10:
            case -2:
                return "November";
            case 11:
            case -1:
                return "December";
            default:
                return "";
        }
    }

    private void calculateWeeklyProfile(View view) {
        txtamountspent = (TextView)view.findViewById(R.id.txtamountspent);
        txtcalloriesconsumed = (TextView)view.findViewById(R.id.txtcalloriesconsumed);
        txtunitconsumed = (TextView)view.findViewById(R.id.txtunitconsumed);

        ArrayList<String[]> weeklyprofile = repo.fetchWeeklyDrinks();
        double amountspent,calloryconsume,unitconsume;
        amountspent = calloryconsume = unitconsume = 0;
         if( weeklyprofile != null){
            for(String [] str : weeklyprofile){
                for (int i =0; i< str[0].split(",").length; i++){
                    String []todaydrinkvalue =  str[0].split(",");
                    String[] todaydrinkunitandpercent = str[1].split(",");
                    amountspent += Double.parseDouble(todaydrinkvalue[i]) * Double.parseDouble(todaydrinkunitandpercent[i].split("%")[2]);
                    unitconsume += Double.parseDouble(todaydrinkunitandpercent[i].split("%")[1]) * Double.parseDouble(todaydrinkvalue[i]);
                    Double alcoholpercent = Double.parseDouble(todaydrinkunitandpercent[i].split("%")[0]);
                    calloryconsume += calculateCallories(calculateExactVolume(alcoholpercent,todaydrinkunitandpercent[i].split("%")[1]),alcoholpercent) * Double.parseDouble(todaydrinkvalue[i]);
                }
            }
            txtamountspent.setText("£" + String.format("%.2f",amountspent));
            txtunitconsumed.setText(String.format("%.2f",unitconsume));
            txtcalloriesconsumed.setText(String.format("%.2f",calloryconsume));
        }
        else{
            txtamountspent.setText("£" + amountspent +"");
            txtunitconsumed.setText(unitconsume + "");
            txtcalloriesconsumed.setText(calloryconsume + "");
        }

    }
}
