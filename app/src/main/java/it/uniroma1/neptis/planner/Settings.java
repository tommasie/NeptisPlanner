package it.uniroma1.neptis.planner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.plans.PlansActivity;
import it.uniroma1.neptis.planner.report.Report;

public class Settings extends AppCompatActivity {

    SharedPreferences settings;

    private Spinner spinner;
    private Spinner spinner_TravelMode;
    private TextView userView;
    private Button logout;

    private String message;
    private String mymail;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE); // 0 - for private mode
        mymail = pref.getString("mail", null);

        logout = (Button) findViewById(R.id.button_logout);
        userView = (TextView) findViewById(R.id.textView_logout);
        spinner = (Spinner) findViewById(R.id.spinner_settings);
        List<String> list = new ArrayList<>();
        list.add("Real-Time data");
        list.add("Average data");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinner.setAdapter(dataAdapter);

        spinner_TravelMode = (Spinner) findViewById(R.id.spinner_travelMode);
        List<String> mode = new ArrayList<String>();
        mode.add("Walking");
        mode.add("Public Tranpost");
        mode.add("Driving");
        ArrayAdapter<String> dataAdapter_mode = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, mode);
        dataAdapter_mode.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinner_TravelMode.setAdapter(dataAdapter_mode);



        userView.setText("You are logged in as: " + mymail);




        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                SharedPreferences.Editor editor = pref.edit();

                //store data to pref
                editor.putString("data_mode", spinner.getSelectedItem().toString());


                editor.commit();
                //Toast.makeText(getApplicationContext(), "Travel Mode setted: " + spinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });



        spinner_TravelMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                SharedPreferences.Editor editor = pref.edit();

                //store data to pref
                editor.putString("travel_mode", spinner_TravelMode.getSelectedItem().toString());
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });



    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        else if (id == R.id.report) {
            Intent intent = new Intent(this, Report.class);
            startActivity(intent);
        }
        else if (id == R.id.myplans) {
            Intent intent = new Intent(this, PlansActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    public void logOut(View view) {
        settings = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (settings.getString("mail", null) != null) {
            //Removing single preference:
            SharedPreferences settings = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            settings.edit().remove("mail").commit();
            settings.edit().remove("password").commit();
        }

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);


    }
}
