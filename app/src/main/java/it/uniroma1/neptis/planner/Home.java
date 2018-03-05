/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Request;
import it.uniroma1.neptis.planner.planning.AttractionsFragment;
import it.uniroma1.neptis.planner.planning.ChoiceFragment;
import it.uniroma1.neptis.planner.planning.ChooseMuseumFragment;
import it.uniroma1.neptis.planner.planning.VisitsFragment;
import it.uniroma1.neptis.planner.plans.CurrentPlanFragment;
import it.uniroma1.neptis.planner.plans.PlansListFragment;
import it.uniroma1.neptis.planner.plans.SelectedPlanFragment;
import it.uniroma1.neptis.planner.rating.CityAttractionTimeFragment;
import it.uniroma1.neptis.planner.rating.MuseumAttractionTimeFragment;
import it.uniroma1.neptis.planner.rating.RateAttractionFragment;
import it.uniroma1.neptis.planner.survey.SurveyFragment;
import it.uniroma1.neptis.planner.util.ConfigReader;
import it.uniroma1.neptis.planner.util.JSONAsyncTask;
import it.uniroma1.neptis.planner.util.ProfilePictureAsyncTask;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainInterface {

    private static final String TAG = Home.class.getName();
    private String apiURL;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private CoordinatorLayout coordLayout;
    private ProgressDialog progress;
    private ProgressBar progressBar;
    private Snackbar snackbar;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private Fragment fragment;

    private Request request;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private LocationManager locationManager;
    private Location location;
    public Address address;

    LocationRequest locationRequest;
    FusedLocationProviderClient locationClient;

    public static List<Attraction> attractionsList;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        this.apiURL = ConfigReader.getConfigValue(this, "serverURL");

        setContentView(R.layout.activity_home);
        coordLayout = findViewById(R.id.coordinator_layout);
        snackbar = Snackbar.make(coordLayout, "Attiva il GPS", Snackbar.LENGTH_LONG);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_planning);

        View header = navigationView.getHeaderView(0);
        TextView headerName =
                header.findViewById(R.id.headerNameView);
        headerName.setText(user.getDisplayName());
        TextView headerEmail =
                header.findViewById(R.id.headerEmailView);
        headerEmail.setText(user.getEmail());
        ImageView headerImg = (ImageView) header.findViewById(R.id.headerImageView);
        new ProfilePictureAsyncTask(headerImg).execute(user.getPhotoUrl().toString());

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage("Attendi la connessione del GPS");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            snackbar.show();
        }

        fragmentManager = getSupportFragmentManager();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        request = null;
        attractionsList = new ArrayList<>();

        progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        drawer.addView(progressBar,params);
        progressBar.setVisibility(View.GONE);     // To Hide ProgressBar

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // Update location every second

        boolean checkFineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean checkCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (checkCoarseLocation && checkFineLocation) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
            /*locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(@NonNull Location loc) {
                            Log.d("onsuccesslistener", "success");
                            location = loc;
                            long currTime = System.currentTimeMillis();
                            if(currTime <= location.getTime() + (10 * 60 * 1000)) {
                                locationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        location = locationResult.getLastLocation();
                                        Geocoder g = new Geocoder(getApplicationContext(), Locale.ITALIAN);
                                        try {
                                            List<Address> addresses = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                            address = addresses.get(0);
                                            Log.d("address", address.getLocality());
                                            Log.d("address", address.getAddressLine(0));
                                            progress.dismiss();
                                            initFragment();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        locationClient.removeLocationUpdates(new LocationCallback());
                                    }
                                }, null);
                            } else {
                                Geocoder g = new Geocoder(getApplicationContext(), Locale.ITALIAN);
                                try {
                                    List<Address> addresses = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    address = addresses.get(0);
                                    Log.d("address", address.getLocality());
                                    Log.d("address", address.getAddressLine(0));
                                    progress.dismiss();
                                    initFragment();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("onfaillistener", "fail");
                            e.printStackTrace();
                        }
                    });*/

            //LocationCallback cb

            locationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    locationClient.removeLocationUpdates(this);
                    location = locationResult.getLastLocation();
                    Geocoder g = new Geocoder(getApplicationContext(), Locale.ITALIAN);
                    try {
                        List<Address> addresses = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        address = addresses.get(0);
                        Log.d("address", address.getLocality());
                        Log.d("address", address.getAddressLine(0));
                        progressBar.setVisibility(View.GONE);  //To show ProgressBar
                        drawer.removeView(progressBar);
                        initFragment();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    progress.show();
                    locationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            location = locationResult.getLastLocation();
                            Geocoder g = new Geocoder(getApplicationContext(), Locale.ITALIAN);
                            try {
                                List<Address> addresses = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                address = addresses.get(0);
                                Log.d("address", address.getLocality());
                                Log.d("address", address.getAddressLine(0));
                                progress.dismiss();
                                initFragment();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            locationClient.removeLocationUpdates(new LocationCallback());
                        }
                    }, null);

                } else {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (getIntent().getStringExtra("computed_plan_file") != null) {
                setCurrentPlan(getIntent().getExtras());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
//        } else if (fragmentManager.getBackStackEntryCount() == 1) {
//            drawer.openDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        navigationView.setCheckedItem(id);
        Bundle bundle;
        switch (id) {
            case R.id.nav_planning:
                transaction = fragmentManager.beginTransaction();
                fragment = new ChoiceFragment();
                bundle = new Bundle();
                bundle.putString("city", address.getLocality());
                bundle.putString("region", address.getAdminArea());
                fragment.setArguments(bundle);
                transaction.replace(R.id.content_home, fragment);
                transaction.commit();
                toolbar.setTitle("Pianifica");
                break;
            case R.id.nav_plans:
                transaction = fragmentManager.beginTransaction();
                fragment = new PlansListFragment();
                transaction.replace(R.id.content_home, fragment);
                transaction.commit();
                getSupportActionBar().setTitle("Piani salvati");
                break;
            case R.id.city_attractions_sensing:
                transaction = fragmentManager.beginTransaction();
                fragment = new CityAttractionTimeFragment();
                bundle = new Bundle();
                bundle.putString("city", address.getLocality());
                bundle.putString("region", address.getAdminArea());
                fragment.setArguments(bundle);
                transaction.replace(R.id.content_home, fragment);
                transaction.commit();
                getSupportActionBar().setTitle("Attrazioni");
                break;
            case R.id.museum_attractions_sensing:
                transaction = fragmentManager.beginTransaction();
                fragment = new MuseumAttractionTimeFragment();
                bundle = new Bundle();
                bundle.putString("city", address.getLocality());
                bundle.putString("region", address.getAdminArea());
                fragment.setArguments(bundle);
                transaction.replace(R.id.content_home, fragment);
                transaction.commit();
                getSupportActionBar().setTitle("Attrazioni");
                break;
            case R.id.nav_survey:
                transaction = fragmentManager.beginTransaction();
                fragment = new SurveyFragment();
                transaction.replace(R.id.content_home, fragment);
                transaction.commit();
                getSupportActionBar().setTitle("Questionario");
                break;
            case R.id.nav_exit:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(Home.this, LoginActivity.class));
                                finish();
                            }
                        });
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void mainMenu() {
        //fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        fragment = new ChoiceFragment();
        Bundle bundle = new Bundle();
        bundle.putString("city", address.getLocality());
        bundle.putString("region", address.getAdminArea());
        fragment.setArguments(bundle);
        transaction.replace(R.id.content_home, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
        toolbar.setTitle("Pianifica");
        navigationView.setCheckedItem(R.id.nav_planning);
    }

    @Override
    public void selectVisits(Map<String, String> parameters) {
        if(request == null) request = new Request();
        request.addRequestParams(parameters);
        Fragment visitsFragment = new VisitsFragment();

        Bundle b = new Bundle();
        //Parameters needed to make the call in the AsyncTask
        b.putString("category", request.getRequestParameters().get("category"));
        b.putString("id", request.getRequestParameters().get("id"));
        b.putString("city", request.getRequestParameters().get("city"));
        b.putString("region", request.getRequestParameters().get("region"));
        visitsFragment.setArguments(b);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_home, visitsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void selectMuseum(Map<String, String> parameters) {
        request = new Request();
        request.addRequestParams(parameters);
        Fragment chooseMuseumFragment = new ChooseMuseumFragment();
        Bundle b = new Bundle();
        //Parameters needed to make the call in the AsyncTask
        b.putString("category", request.getRequestParameters().get("category"));
        b.putString("city", request.getRequestParameters().get("city"));
        b.putString("region", request.getRequestParameters().get("region"));
        chooseMuseumFragment.setArguments(b);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_home, chooseMuseumFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void selectIncludeExclude(Map<String, String> parameters) {
        request.addRequestParams(parameters);
        Fragment attractionsFragment = new AttractionsFragment();
        Bundle b = new Bundle();
        b.putString("category", request.getRequestParameters().get("category"));
        b.putString("city", request.getRequestParameters().get("city"));
        b.putString("region", request.getRequestParameters().get("region"));
        b.putString("id", request.getRequestParameters().get("id"));
        attractionsFragment.setArguments(b);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_home, attractionsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void computePlan(Map<String, List<Attraction>> params) {
        request.setMustVisit(params.get("must"));
        request.setExcludeVisit(params.get("exclude"));

        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            ComputePlanAsyncTask t = new ComputePlanAsyncTask(getApplicationContext(), progress, request, location, transaction,fragmentManager);
                            t.execute(apiURL + "/compute-plan-" + request.getRequestParameters().get("category"), idToken);
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
    }

    @Override
    public void selectPlan(Bundle bundle) {
        transaction = fragmentManager.beginTransaction();
        fragment = new SelectedPlanFragment();
//        fragment = new CurrentPlanFragment();
        fragment.setArguments(bundle);
        transaction.replace(R.id.content_home, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void setCurrentPlan(Bundle bundle) {
        transaction = fragmentManager.beginTransaction();
        fragment = new CurrentPlanFragment();
        fragment.setArguments(bundle);
        transaction.replace(R.id.content_home, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        getSupportActionBar().setTitle("Piano corrente");
    }

    @Override
    public void attractionDetail(Bundle bundle) {
        transaction = fragmentManager.beginTransaction();
        fragment = new RateAttractionFragment();
        fragment.setArguments(bundle);
        transaction.replace(R.id.content_home, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void popBackStack() {
        fragmentManager.popBackStack();
    }

    @Override
    public FirebaseUser getUser() {
        return user;
    }

    @Override
    public Address getLocation() {
        return this.address;
    }

    private static class ComputePlanAsyncTask extends JSONAsyncTask {

        private WeakReference<Context> context;
        private ProgressDialog progress;
        private Request request;
        private Location location;

        private FragmentTransaction transaction;
        private FragmentManager fragmentManager;

        private ComputePlanAsyncTask(Context context, ProgressDialog progress,
                                        Request request, Location location,
                                     FragmentTransaction transaction, FragmentManager fragmentManager) {
            this.context = new WeakReference<>(context);
            this.progress = progress;
            this.request = request;
            this.location = location;
            this.transaction = transaction;
            this.fragmentManager = fragmentManager;
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Calcolo del percorso, attendi");
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in;
            int code;
            String charset = "UTF-8";
            String urlString = params[0];
            String token = params[1];
            // HTTP post
            try {
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", token);
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Object json to send
                JSONObject json = new JSONObject();
                for(String param : request.getRequestParameters().keySet()) {
                    json.put(param,request.getRequestParameters().get(param));
                }

                //Add the list of must-see places
                JSONArray must = new JSONArray();
                for(Attraction a : request.getMustVisit())
                    must.put(a.getId());
                json.put("must", must);

                //Add the list of excluded places
                JSONArray exclude = new JSONArray();
                for(Attraction a : request.getExcludeVisit())
                    exclude.put(a.getId());
                json.put("exclude", exclude);

                json.put("lat", location.getLatitude());
                json.put("lng", location.getLongitude());

                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();
                printout.close();

                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }

            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {
                request.setPlan(readResponse(in));
                Log.d("LOG", "Your Plan: " + request.getPlan());
            }
            return code;
        }


        @Override
        protected void onPostExecute(Integer result) {
            progress.dismiss();
            if (result == 200) {
                String filename = savePlan();
                Fragment planFragment = new SelectedPlanFragment();
                Bundle b = new Bundle();
                b.putString("computed_plan_file", filename);
                planFragment.setArguments(b);
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.content_home, planFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }

        private String savePlan() {
            Calendar calendar = Calendar.getInstance();
            long ts = calendar.getTimeInMillis();
            String filename = request.getRequestParameters().get("name")  + "_" + ts;
            FileOutputStream outputStream;
            try {
                outputStream = context.get().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(request.getPlan().getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(context.get(), "Saved!", Toast.LENGTH_SHORT).show();
            return filename;
        }
    }

    public void initFragment() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (getIntent().getStringExtra("computed_plan_file") != null) {
                setCurrentPlan(getIntent().getExtras());
                return;
            }
        }
        mainMenu();
    }
}
