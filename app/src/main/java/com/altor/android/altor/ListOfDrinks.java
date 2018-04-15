package com.altor.android.altor;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.altor.android.altor.utils.PrefManager;
import com.altor.android.altor.utils.Repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListOfDrinks extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private List<String[]> data;
    private ListView listView;
    private Switch toggle;
    private Button btncancel,btnsave;
    private Repository repo;
    private PrefManager pref;
    private String [] todaydrink,dataholder;
    private boolean isOffDrinkDay = false;
    private MyAdapter adapter;
    private String values = "";
    private String units = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_of_drinks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);


        toggle = (Switch)findViewById(R.id.toggle);
           toggle.setOnCheckedChangeListener(this);


        listView = (ListView)findViewById(R.id.listview);
        repo = new Repository(this);
        getDailyDrinks();
        populateDataWithDefaultDrinks();
        loadAddedDrinks();
        filldataholder();


        btncancel = (Button) findViewById(R.id.btncancel);
        btnsave = (Button)findViewById(R.id.btnsave);

        btncancel.setOnClickListener(this);
        btnsave.setOnClickListener(this);

    }

    private void filldataholder() {
        dataholder = new String[listView.getCount()];
        Arrays.fill(dataholder,"0");
        if(todaydrink != null)
            for(int i = 0; i< todaydrink.length; i++)
                if(dataholder.length > i)
                 dataholder[i] = todaydrink[i];
    }
    private void getDailyDrinks() {
        pref = new PrefManager(this);
        String values = pref.getTodayDrinksValues();
        if(values != null)
            todaydrink = values.split(",");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_of_drinks_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void populateDataWithDefaultDrinks() {
        data = new ArrayList<>();
        String []defaultdrinks  = getResources().getStringArray(R.array.defaultdrinks);
        for(String str : defaultdrinks)
            data.add(str.split(","));
    }
    private void loadAddedDrinks(){
        ArrayList<String []> defaultdrinks  = repo.fetchAllAddedDrinks();
        for(String [] str : defaultdrinks){
                String stripename = str[2].split("[(]")[1].replace(")","");
                double calculatedunit = (Double.parseDouble(stripename.replace("%","")) * getExactVoulume(str[3])) /1000;
                String [] temp = new String[]{str[2].split("[(]")[0], stripename , str[3] + " - " + String.format("%.2f",calculatedunit) + "Units", "£" + str[4]};
                data.add(temp);
        }
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
    }
    private double getExactVoulume(String s) {
        double res = 0.0;
        try {
            if(s.contains("ml")&& !s.contains("[(]"))
                res = Double.parseDouble(s.replace("ml","").trim());
            else if(s.contains("Litre"))
                res = Double.parseDouble(s.toCharArray()[0] +"") * 1000;
            else if(s.contains("Pint"))
                res = 473.176;
            else if(s.contains("Half Pint"))
                res = 473.176/2;
            else if(s.contains("ml")&& s.contains("[(]"))
                res = Double.parseDouble(s.substring(s.indexOf("(")+1,3));
        }catch (Exception e){
            res = 1;
        }
        return res;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.nav_add:
                startActivity(new Intent(ListOfDrinks.this,AddDrink.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        View v;
        if(b){
            isOffDrinkDay = true;
            Arrays.fill(dataholder,"0");
            adapter.notifyDataSetChanged();
        }
        else
            isOffDrinkDay = false;
    }
    @Override
    public void onClick(View view) {
        if(view == btncancel)
            finish();
        else if(view == btnsave){
            saveToSharedPreference();
            saveToDB();
            saveCallorieGained();
        }

    }

    private void saveCallorieGained() {
        int callorie = 0;
        for(int i =0; i<dataholder.length; i++)
            callorie += ((Double.parseDouble(data.get(i)[1].replace("%","")) * 0.8 * getExactVoulume(data.get(i)[2].split("-")[0])) / 10) * Double.parseDouble(dataholder[i]);
        pref.saveNoofCallorie(pref.getNoofCallorie() + callorie);
    }

    private void saveToDB() {
        String [] temp = new String[]{values,units};
        repo.saveDailyDrinks(temp);
    }

    private void saveToSharedPreference() {
        for(int i =0; i<dataholder.length; i++){
            values += values.equals("") ? dataholder[i] : "," + dataholder[i];
            String rightside = data.get(i)[2].split("-")[1].trim();
            String concatenated = data.get(i)[1] + rightside.substring(0,rightside.indexOf("U")) + "%" + data.get(i)[3].replace("£","");
            units += TextUtils.isEmpty(units) ? concatenated : "," + concatenated;
        }
        pref = new PrefManager(this);
        pref.saveDailyDrinks(values,units);
        Toast.makeText(this,"Successfully saved!",Toast.LENGTH_SHORT).show();
    }
    public class MyAdapter extends ArrayAdapter {
        public MyAdapter(Context vcontext){
            super(vcontext,R.layout.list_item_cardview,data);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final  Holder holder = new Holder();
            View view = convertView;
            if(view == null)
                view = getLayoutInflater().inflate(R.layout.list_item_cardview,null);

            ((TextView)view.findViewById(R.id.drinkname)).setText(data.get(position)[0]);
            ((TextView)view.findViewById(R.id.drinkpercent)).setText(data.get(position)[1]);
            ((TextView)view.findViewById(R.id.drinkunit)).setText(data.get(position)[2] + "\t" + data.get(position)[3]);
            holder.txtqty = (TextView)view.findViewById(R.id.value);
            holder.btnminus = (ImageButton) view.findViewById(R.id.minus);
            holder.btnplus = (ImageButton) view.findViewById(R.id.plus);
            if(holder.btnminus!=null)
                holder.btnminus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        opertation(holder.txtqty,0,position);
                    }
                });
            if(holder.btnplus != null)
                holder.btnplus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        opertation(holder.txtqty,1,position);
                    }
                });
               holder.txtqty.setText(dataholder[position]);
            return view;
        }

        private void opertation(View view,int i,int position) {
            if(!isOffDrinkDay){
                int initvalue = 0;
                try {
                    initvalue = Integer.parseInt(((TextView)view).getText().toString());
                }
                catch (Exception e){
                    initvalue =0;
                }
                switch (i){
                    case 0:
                        ((TextView)view).setText(initvalue != 0 ? "" + --initvalue:"0");
                        break;
                    case 1:
                        ((TextView)view).setText(""+ (++initvalue));
                        break;
                }
                dataholder[position] = ((TextView)view).getText().toString();
            }
        }
    }
    public class Holder {
        ImageButton btnplus;
        ImageButton btnminus;
        TextView txtqty;
    }
}
