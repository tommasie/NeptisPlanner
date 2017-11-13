/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.plans;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.AttractionArrayAdapter;
import it.uniroma1.neptis.planner.custom.MuseumAttractionArrayAdapter;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Plan;
import it.uniroma1.neptis.planner.model.city.CityAttraction;
import it.uniroma1.neptis.planner.model.museum.MuseumAttraction;
import it.uniroma1.neptis.planner.services.tracking.FINDService;
import it.uniroma1.neptis.planner.services.tracking.GeofencingService;

public class SelectedPlanFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = SelectedPlanFragment.class.getName();

    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    protected TextView title;
    protected ListView listView;
    private LinearLayout currentLinearLayout;
    private TextView current;
    protected Button startButton;
    protected Button pauseButton;
    protected String planFileName;
    private int index;

    protected String planString;
    protected Plan plan;

    private AttractionArrayAdapter adapter;

    private ArrayList<Attraction> attractions;

    protected AlertDialog.Builder builder;

    private MainInterface activity;

    public SelectedPlanFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        planFileName = getArguments().getString("computed_plan_file");
        planString = readFile(planFileName);
        index = getArguments().getInt("index");
        plan = parsePlan(planString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        builder = new AlertDialog.Builder(getContext());
        title = (TextView)view.findViewById(R.id.textView_selectedPlan_f);
        title.setText(plan.getName());
        listView = (ListView)view.findViewById(R.id.listView_selectedPlan_f);

        if(plan.getType().equals("city"))
            adapter = new AttractionArrayAdapter(getContext(), R.layout.plans_list_item, plan.getAttractions());
        else
            adapter = new MuseumAttractionArrayAdapter(getContext(), R.layout.plans_item_3, plan.getAttractions());
        listView.setAdapter(adapter);

        currentLinearLayout = (LinearLayout)view.findViewById(R.id.current_plan_ll);
        current = (TextView)view.findViewById(R.id.current_plan_current_attraction);
        startButton = (Button)view.findViewById(R.id.button_plan_start);
        startButton.setOnClickListener(this);
        pauseButton = (Button)view.findViewById(R.id.button_plan_pause);

        Log.d("index",""+index);
        if(index != -1) {
            startButton.setVisibility(View.GONE);
            currentLinearLayout.setVisibility(View.VISIBLE);
            current.setText(attractions.get(index).getName());
        }
        else {
            startButton.setVisibility(View.VISIBLE);
        }

        if(index != -1 && plan.getType().equals("city")) {
            CityAttraction a = (CityAttraction) (attractions.get(index));
            initAlertDialog(a.getLatitude(),a.getLongitude());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initAlertDialog(String latitude, String longitude) {
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=w"));
        builder.setMessage(R.string.open_gps);
        builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
    }

    private Plan parsePlan(String planString) {
        //Plan plan = null;
        JSONObject obj;
        try {
            obj = new JSONObject(planString);
            String name = obj.getString("name");
            String type = obj.getString("type");
            Plan plan = new Plan(name, type);
            if(type.equals("city")) {
                JSONArray route = obj.getJSONArray("route");
                for (int i = 0; i < route.length(); i++) {
                    JSONObject attraction = route.getJSONObject(i);
                    String attrName = attraction.getString("name");
                    String id = attraction.getString("id");
                    String lat = attraction.getJSONObject("coordinates").getString("latitude");
                    String lng = attraction.getJSONObject("coordinates").getString("longitude");
                    CityAttraction a = new CityAttraction(id, attrName, (byte)50, lat, lng, 10.0);
                    plan.addAttraction(a);
                }
                attractions = plan.getAttractions();
                return plan;
            } else if(type.equals("museum")) {
                JSONArray route = obj.getJSONArray("route");
                for (int i = 0; i < route.length(); i++) {
                    //Lista delle stanze
                    JSONObject area = route.getJSONObject(i);
                    String areaName = area.getString("name");
                    String areaId = area.getString("id");
                    //Lista delle attrazioni
                    JSONArray attractions = area.getJSONArray("attractions");
                    for(int j = 0; j < attractions.length(); j++) {
                        JSONObject att = attractions.getJSONObject(j);
                        String attractionName = att.getString("name");
                        String attractionId = att.getString("id");
                        byte attractionRating = (byte)att.getInt("rating");
                        MuseumAttraction at = new MuseumAttraction(attractionId,attractionName,attractionRating, areaName);
                        plan.addAttraction(at);
                    }
                }
                return plan;
            }
        } catch (JSONException e) {
            return null;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_plan_start) {
            Log.d("start button", "tour started");
            v.setEnabled(false);
            startButton.setVisibility(View.INVISIBLE);
            currentLinearLayout.setVisibility(View.VISIBLE);
            current.setText(attractions.get(0).getName());
            /*Intent queueService = new Intent(getContext(), QueueRecognitionService.class);
            queueService.putExtra("attractions", plan.getAttractions());
            queueService.putExtra("index",index);
            getActivity().startService(queueService);*/
            switch (plan.getType()) {
                case "city":
                    Intent geofencingService = new Intent(getContext(), GeofencingService.class);
                    geofencingService.putExtra("attractions", plan.getAttractions());
                    geofencingService.putExtra("current_plan", planFileName);
                    geofencingService.putExtra("index",index);
                    CityAttraction attraction = (CityAttraction) plan.getAttractions().get(0);
                    initAlertDialog(attraction.getLatitude(), attraction.getLongitude());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    getActivity().startService(geofencingService);
                    break;
                case "museum":
                    int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_WIFI_STATE);
                    if(permissionCheck != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_WIFI_STATE},2);
                    else {
                        Intent findService = new Intent(getContext(), FINDService.class);
                        findService.putExtra("museum_name", plan.getName());
                        findService.putExtra("attractions", plan.getAttractions());
                        findService.putExtra("user","");
                        findService.putExtra("current_plan", planFileName);
                        findService.putExtra("index",index);
                        getActivity().startService(findService);
                    }
                    break;
            }
        }

        if(v.getId() == R.id.button_plan_pause) {
            Log.d("start button", "tour paused");
            pauseButton.setVisibility(View.GONE);
            startButton.setEnabled(true);
            Intent intent;
            if(plan.getType().equals("city"))
                intent = new Intent(getContext(), GeofencingService.class);
            else
                intent = new Intent(getContext(), FINDService.class);
            getActivity().stopService(intent);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainInterface) {
            activity = (MainInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1:
                Log.d("Permission length:", ""+grantResults.length);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //start FINDService
                    Intent findService = new Intent(getContext(), FINDService.class);
                    findService.putExtra("museum_name", plan.getName());
                    findService.putExtra("attractions", plan.getAttractions());
                    //TODO add user name
                    findService.putExtra("user","");
                    findService.putExtra("current_plan", planFileName);
                    findService.putExtra("index",index);
                    getActivity().startService(findService);
                }
        }
    }

    private String readFile(String filename) {
        FileInputStream fis = null;
        try {
            fis = getContext().openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
