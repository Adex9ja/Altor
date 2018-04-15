package com.altor.android.altor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.altor.android.altor.utils.Repository;

import java.util.ArrayList;
import java.util.List;

public class AddDrink extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Spinner category,type,brand,servng;
    private Button btnsave;
    private EditText price;
    private String [] select,temp;
    private List templist,pricelist;
    private int selectedbrand;
    private Repository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drink);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        select = new String []{"Select..."};
        category = (Spinner) findViewById(R.id.cbcategory);
            category.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.category)));
            category.setOnItemSelectedListener(this);
        type = (Spinner) findViewById(R.id.cbtype);
            type.setOnItemSelectedListener(this);
            type.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,select));
        brand = (Spinner) findViewById(R.id.cbbrand);
            brand.setOnItemSelectedListener(this);
            brand.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,select));
        servng = (Spinner) findViewById(R.id.cbserving);
            servng.setOnItemSelectedListener(this);
            servng.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,select));
        price = (EditText) findViewById(R.id.txtprice);
        btnsave = (Button)findViewById(R.id.btnsave);
         btnsave.setOnClickListener(this);
        repo = new Repository(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.cbcategory:
                if(!doesItHasAType(i)){
                    selectedbrand = getbrand(i);
                    temp = getResources().getStringArray(selectedbrand);
                    templist = new ArrayList();
                    for (String str : temp){
                        templist.add(str.split("-")[0] + " (" + str.split("-")[1] + "%)");
                    }
                    brand.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,templist));
                }
                break;
            case R.id.cbtype:
                if(category.getSelectedItemPosition() == 1){
                    if(i == 0){
                        selectedbrand = R.array.Ale_Stout;
                        temp = getResources().getStringArray(selectedbrand);
                    }
                    else{
                        selectedbrand = R.array.Larger;
                        temp = getResources().getStringArray(selectedbrand);
                    }
                    templist = new ArrayList();
                    for (String str : temp)
                        templist.add(str.split("-")[0] + " (" + str.split("-")[1] + "%)");
                    brand.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,templist));
                }
            case R.id.cbbrand:
                temp = getResources().getStringArray(selectedbrand)[i].split("-");
                templist = new ArrayList();
                pricelist = new ArrayList();
                for(String s: temp[2].split(",")){
                    templist.add(s.split("@")[0]);
                    pricelist.add(s.split("@")[1]);
                }
                servng.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,templist));
                break;
            case R.id.cbserving:
                price.setText(pricelist.get(servng.getSelectedItemPosition())+"");
                price.setEnabled(true);
                break;
        }
    }
    private boolean doesItHasAType(int i) {
        boolean response = false;
        if(i == 1 ){
            type.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.Beer)));
            response = true;
        }
        else{
            type.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,select));
            response = false;
        }
        return response;
    }
    private int getbrand(int i) {
        int ret = -1;
        switch (i){
            case 0:
                brand.setEnabled(true);
                ret = R.array.Alcopops;
                break;
            case 2:
                brand.setEnabled(true);
                ret = R.array.Champagne;
                break;
            case 3:
                brand.setEnabled(true);
                ret = R.array.Cider;
            break;
        }
        return ret;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    @Override
    public void onClick(View view) {
        String newdrink[] = new String[]{
                category.getSelectedItem().toString(),
                type.getSelectedItem().toString(),
                brand.getSelectedItem().toString(),
                servng.getSelectedItem().toString(),
                price.getText().toString()
        };
        long res = repo.saveAddedDrinks(newdrink);
        if(res > 0)
            Toast.makeText(this,"Drink added successfully!",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,"Error occur!",Toast.LENGTH_SHORT).show();
    }
}
