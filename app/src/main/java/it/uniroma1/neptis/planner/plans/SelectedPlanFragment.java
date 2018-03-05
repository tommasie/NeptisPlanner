/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.plans;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.asynctasks.DownloadImageAsyncTask;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Plan;
import it.uniroma1.neptis.planner.model.city.CityAttraction;
import it.uniroma1.neptis.planner.model.museum.MuseumAttraction;
import it.uniroma1.neptis.planner.services.tracking.GeofencingService;

public class SelectedPlanFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = SelectedPlanFragment.class.getName();

    protected TextView title;
    private TextView totalDuration;
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LinearLayout currentLinearLayout;
    protected TextView current;
    protected Button startButton;
    protected String planFileName;

    protected String planString;
    protected Plan plan;

    private ArrayList<Attraction> attractions;

    protected MainInterface activity;

    public SelectedPlanFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        planFileName = getArguments().getString("computed_plan_file");
        planString = readFile(planFileName);
        plan = parsePlan(planString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.tv_area_name);
        title.setText(plan.getName());
        totalDuration = view.findViewById(R.id.tv_duration);
        int time = 0;
        for(Attraction a : plan.getAttractions())
            time += a.getTime();
        totalDuration.setText("Durata: " + time + "m");
        recyclerView = view.findViewById(R.id.recycler_view_nas);

        /*if(plan.getType().equals("city"))
            adapter = new AttractionArrayAdapter(getContext(), R.layout.plans_list_item, plan.getAttractions());
        else
            adapter = new MuseumAttractionArrayAdapter(getContext(), R.layout.plans_item_new, plan.getAttractions());*/

        mAdapter = new AttractionRecyclerAdapter(plan.getAttractions(), plan.getType());
        recyclerView.setAdapter(mAdapter);

        currentLinearLayout = view.findViewById(R.id.current_plan_ll);
        current = view.findViewById(R.id.current_plan_current_attraction);
        startButton = view.findViewById(R.id.tour_start);
        startButton.setOnClickListener(this);
        if(plan.getType().equals("museum"))
            startButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private Plan parsePlan(String planString) {
        //Plan plan = null;
        JSONObject obj;
        try {
            obj = new JSONObject(planString);
            Log.d("object string", obj.toString());
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
                    int time = attraction.getInt("time");
                    CityAttraction a = new CityAttraction(id, attrName, "", (byte)50, time, "", lat, lng, 10.0);
                    plan.addAttraction(a);
                }
                attractions = plan.getAttractions();
                return plan;
            } else if(type.equals("museum")) {
                //startButton.setVisibility(View.GONE);
                JSONArray attractions = obj.getJSONArray("route");
                for(int j = 0; j < attractions.length(); j++) {
                    JSONObject att = attractions.getJSONObject(j);
                    String attractionName = att.getString("name");
                    String attractionId = att.getString("id");
                    String room = att.getString("room");
                    byte attractionRating = (byte)att.getInt("rating");
                    String url = att.getString("picture");
                    MuseumAttraction at = new MuseumAttraction(attractionId,attractionName, "", attractionRating, 2, url, room);
                    plan.addAttraction(at);
                }
                return plan;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tour_start) {
            Log.d(TAG, "click");
            /*Intent queueService = new Intent(getContext(), QueueRecognitionService.class);
            queueService.putExtra("attractions", plan.getAttractions());
            queueService.putExtra("index",index);
            getActivity().startService(queueService);*/
            switch (plan.getType()) {
                case "city":
                    Intent geofencingService = new Intent(getContext(), GeofencingService.class);
                    Bundle serviceBundle = new Bundle();
                    serviceBundle.putSerializable("attractions", plan.getAttractions());
                    serviceBundle.putString("current_plan", planFileName);
                    serviceBundle.putInt("index",0);
                    geofencingService.putExtras(serviceBundle);
                    CityAttraction attraction = (CityAttraction) plan.getAttractions().get(0);
                    //initAlertDialog(attraction.getLatitude(), attraction.getLongitude());
                    //AlertDialog dialog = builder.create();
                    //dialog.show();
                    getActivity().startService(geofencingService);
                    Bundle fragmentBundle = new Bundle();
                    fragmentBundle.putString("computed_plan_file", planFileName);
                    fragmentBundle.putInt("index",0);
                    activity.setCurrentPlan(fragmentBundle);
                    break;
                case "museum":
                    /*int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_WIFI_STATE);
                    if(permissionCheck != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_WIFI_STATE},2);
                    else {
                        Intent findService = new Intent(getContext(), FINDService.class);
                        findService.putExtra("museum_name", plan.getName());
                        findService.putExtra("attractions", plan.getAttractions());
                        findService.putExtra("user","");
                        findService.putExtra("current_plan", planFileName);
                        findService.putExtra("index",0);
                        getActivity().startService(findService);
                    }*/
                    break;
            }
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

    public class AttractionRecyclerAdapter extends RecyclerView.Adapter<AttractionRecyclerAdapter.AttractionHolder> {

        private List<Attraction> mDataset;
        private String category;

        public AttractionRecyclerAdapter(List<Attraction> attractions, String category) {
            mDataset = attractions;
            Log.d("category",category);
            this.category = category;
        }

        @Override
        public AttractionHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.plans_item_new, parent, false);
            AttractionHolder vh;
            vh = new AttractionHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(AttractionHolder holder, int position) {
            holder.bind(mDataset.get(position));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public class AttractionHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            private TextView attractionName;
            private TextView attractionDescription;
            private TextView attractionTime;
            private ImageView attractionImage;
            private Attraction curr;
            private TextView visitLabel;
            private Button start;
            private Button stop;
            private Button rate;

            public AttractionHolder(View v) {
                super(v);
                this.attractionName = v.findViewById(R.id.plan_n);
                this.attractionDescription = v.findViewById(R.id.museum_attraction_description);
                this.attractionTime = v.findViewById(R.id.attraction_time_short);
                this.attractionImage = v.findViewById(R.id.museum_attraction_image);
                this.visitLabel = v.findViewById(R.id.visit_label);
                this.start = v.findViewById(R.id.museum_attraction_begin_timer);
                this.stop = v.findViewById(R.id.museum_attraction_end_timer);
                if(category.equals("city")) {
                    this.start.setVisibility(View.GONE);
                    this.stop.setVisibility(View.GONE);
                    this.visitLabel.setVisibility(View.GONE);
                }
                this.rate = v.findViewById(R.id.museum_attraction_rate);
                this.rate.setOnClickListener(this);
            }

            public void bind(Attraction attraction) {
                curr = attraction;
                this.attractionName.setText(attraction.getName());
                this.attractionDescription.setText(attraction.getDescription());
                this.attractionTime.setText(attraction.getTime() + "m");
                new DownloadImageAsyncTask(this.attractionImage).execute(attraction.getImageURL());
            }

            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.museum_attraction_rate:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("attraction",curr);
                        bundle.putString("type", category);
                        activity.attractionDetail(bundle);
                        break;
                }
            }
        }

    }
}
