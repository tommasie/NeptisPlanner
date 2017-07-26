package it.uniroma1.neptis.planner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.planning.PlanningActivity;
import it.uniroma1.neptis.planner.plans.PlansActivity;

public class Welcome extends AppCompatActivity implements View.OnClickListener{

    private static final Logger logger = LoggerFactory.getLogger(Welcome.class);
    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    public static final String EMAIL = "mail";
    public static final String UNAME = "name";

    private TextView planner;
    private TextView plans;
    private TextView settings;
    private TextView exit;
    private TextView survey;

    private Menu menu;
    private String name;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public static GPSTracker gps;
    public static double lat;
    public static double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);
        //getActionBar().setDisplayShowHomeEnabled(true);
        planner = (TextView)findViewById(R.id.planner_button);
        planner.setOnClickListener(this);
        plans = (TextView)findViewById(R.id.plans_list_button);
        plans.setOnClickListener(this);
        //settings = (TextView)findViewById(R.id.settings_button);
        //settings.setOnClickListener(this);
        exit = (TextView)findViewById(R.id.exit_button);
        exit.setOnClickListener(this);
        survey = (TextView)findViewById(R.id.survey_button);
        survey.setOnClickListener(this);


        Intent intent = getIntent();
        name = intent.getStringExtra(UNAME);
        if(name != null)
            name = name.substring(0, 1).toUpperCase() + name.substring(1);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        int permissionCheck = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
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

                String dialogMessage = String.format(getString(R.string.alert_welcome), address);
                new AlertDialog.Builder(Welcome.this)
                        .setTitle(getString(R.string.alert_information_title))
                        .setMessage(dialogMessage)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .setIcon(R.drawable.ic_info)
                        .show();

            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings

                //GPS coordinates of my home
                gps.showSettingsAlert();
                lat = 42.100335;
                lon = 12.159988;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1:
                Log.d("Permission length:", ""+grantResults.length);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

                        String dialogMessage = String.format(getString(R.string.alert_welcome), address);
                        new AlertDialog.Builder(Welcome.this)
                                .setTitle(getString(R.string.alert_information_title))
                                .setMessage(dialogMessage)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                })
                                .setIcon(R.drawable.ic_info)
                                .show();

                    } else {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings

                        //GPS coordinates of my home
                        gps.showSettingsAlert();
                        lat = 42.100335;
                        lon = 12.159988;
                    }
                } else {
                    gps = null;
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_user_menu, menu);
        MenuItem item = menu.findItem(R.id.option_user_name);
        if(user != null)
            item.setTitle(user.getDisplayName());
        else
            item.setTitle(name);
        /*Drawable profilePic;
        try {
            InputStream inputStream = getContentResolver().openInputStream(user.getPhotoUrl());
            profilePic = Drawable.createFromStream(inputStream, user.getPhotoUrl().toString() );
        } catch (FileNotFoundException e) {
            profilePic = getResources().getDrawable(R.drawable.ic_user);
        }
        item.setIcon(profilePic);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.user_option_item:
                return true;
            case R.id.option_settings:
                logEvent = new LogEvent(this.getClass().getName(),"settings", "settings_option_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.option_logout:
                logEvent = new LogEvent(this.getClass().getName(),"log out", "logout_option_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(this,"Logout option", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()) {
            case R.id.planner_button:
                logEvent = new LogEvent(this.getClass().getName(),"start planning", "planner_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                intent = new Intent(this, PlanningActivity.class);
                //logger.in
                startActivity(intent);
                break;
            case R.id.plans_list_button:
                logEvent = new LogEvent(this.getClass().getName(),"list plans", "plans_list_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                intent = new Intent(this, PlansActivity.class);
                startActivity(intent);
                break;
            /*case R.id.settings_button:
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;*/
            case R.id.survey_button:

                intent = new Intent(this, SurveyActivity.class);
                startActivity(intent);
            case R.id.exit_button:
                logEvent = new LogEvent(this.getClass().getName(),"exit app", "exit_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                finish();
                //intent = new Intent(this, Settings.class);
                //startActivity(intent);
        }
    }

    /*private boolean exit = false;
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

    }*/
}