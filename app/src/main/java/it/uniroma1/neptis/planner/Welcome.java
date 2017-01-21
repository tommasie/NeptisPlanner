package it.uniroma1.neptis.planner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.uniroma1.neptis.planner.plans.PlansActivity;
import it.uniroma1.neptis.planner.report.Report;
import it.uniroma1.neptis.planner.planning.PlanningActivity;

public class Welcome extends AppCompatActivity implements View.OnClickListener{

    public static final String EMAIL = "mail";
    public static final String UNAME = "name";

    private TextView userView;
    private Button planner;
    private Button report;
    private Button plans;
    private Button settings;

    public static GPSTracker gps;
    public static double lat;
    public static double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        userView = (TextView) findViewById(R.id.textView_user);
        planner = (Button)findViewById(R.id.button_planning);
        planner.setOnClickListener(this);
        report = (Button)findViewById(R.id.button2_report);
        report.setOnClickListener(this);
        plans = (Button)findViewById(R.id.button_myPlan);
        plans.setOnClickListener(this);
        settings = (Button)findViewById(R.id.button3_settings);
        settings.setOnClickListener(this);

        Intent intent = getIntent();
        String name = intent.getStringExtra(UNAME);
        if(name != null) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            userView.setText("Welcome " + name + "!");
        }
        gps = new GPSTracker(this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            lat = gps.getLatitude();
            lon = gps.getLongitude();

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            String address;
            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
                address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                address = "";
            }

            // ---SIMPLE ALERT FOR DEBUG---
            String dialogMessage = String.format(getString(R.string.alert_welcome), address);
            new AlertDialog.Builder(Welcome.this)
                    .setTitle(getString(R.string.alert_information_title))
                    .setMessage(dialogMessage)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // continue with saving
                            //Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();

                        }
                    })
                    .setIcon(R.drawable.ic_info)
                    .show();

            //--- END ----

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings

            //GPS coordinates of my home
            gps.showSettingsAlert();
            lat = 42.100335;
            lon = 12.159988;
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()) {
            case R.id.button_planning:
                intent = new Intent(this, PlanningActivity.class);
                startActivity(intent);
                break;
            case R.id.button2_report:
                intent = new Intent(this, Report.class);
                startActivity(intent);
                break;
            case R.id.button_myPlan:
//                intent = new Intent(this, MyPlans.class);
                intent = new Intent(this, PlansActivity.class);
                startActivity(intent);
                break;
            case R.id.button3_settings:
                intent = new Intent(this, Settings.class);
                startActivity(intent);
        }
    }

    private boolean exit = false;
    //Tap twice to close the app, give at most 3 seconds to record the second tap
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, getString(R.string.toast_close),
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
}