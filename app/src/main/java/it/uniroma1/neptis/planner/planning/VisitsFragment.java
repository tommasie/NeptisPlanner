/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.planning;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.Home;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.util.ConfigReader;
import it.uniroma1.neptis.planner.asynctasks.JSONAsyncTask;

public class VisitsFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = VisitsFragment.class.getName();
    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private String attractionURL;

    private ProgressDialog progress;

    private Button top10, top20, top30;

    private NumberPicker numPicker;

    private Button next;

    private String category;
    private String id;

    protected MainInterface activity;

    public VisitsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Home.attractionsList.clear();
        category = getArguments().getString("category");
        attractionURL = ConfigReader.getConfigValue(getContext(), "serverURL");
        switch(category) {
            case "city":
                attractionURL += "/attractionc";
                attractionURL += "?city=" + getArguments().getString("city");
                attractionURL += "&region=" + getArguments().getString("region");
                break;
            case "museum":
                attractionURL += "/museums/attractions/";
                attractionURL += getArguments().getString("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visits, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        top10 = view.findViewById(R.id.button_top10);
        top10.setOnClickListener(this);
        top20 = view.findViewById(R.id.button_top20);
        top20.setOnClickListener(this);
        top30 = view.findViewById(R.id.button_top30);
        top30.setOnClickListener(this);

        numPicker = view.findViewById(R.id.numberPicker);
        numPicker.setMinValue(1);
        numPicker.setMaxValue(20);
        numPicker.setValue(1);

        next = view.findViewById(R.id.visits_button_next);
        next.setOnClickListener(this);

        progress = new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        progress.setMessage(" ");
        //TODO bisogno dell'upper bound sulle attrazioni
        activity.getUser().getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            new GetAttractionsAsyncTask().execute(attractionURL, idToken);
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.visits_button_next:
                logEvent = new LogEvent(getActivity().getClass().getName(),"compute plan", "next_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                String numVisits = String.valueOf(numPicker.getValue());
                Map<String,String> parameters = new HashMap<>();
                parameters.put("visits",numVisits);
                activity.selectIncludeExclude(parameters);
                //next2();
                break;

            case R.id.button_top10:
                numPicker.setValue(10);
                break;
            case R.id.button_top20:
                numPicker.setValue(20);
                break;
            case R.id.button_top30:
                numPicker.setValue(30);
                break;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainInterface) {
            activity = (MainInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IBestTime");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    protected class GetAttractionsAsyncTask extends JSONAsyncTask {

        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in;
            int code;
            String urlString = params[0];
            String token = params[1];
            // HTTP GET
            try {

                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", token);

                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }

            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {
                String jsonResponse = readResponse(in);
                Log.d("response", jsonResponse);
                JSONArray attractions = null;
                try {
                    attractions = new JSONArray(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject attraction;
                for (int i = 0; i < attractions.length(); ++i) {
                    try {
                        attraction = attractions.getJSONObject(i);
                        Home.attractionsList.add(Attraction.parse(attraction));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                Log.d(TAG, Home.attractionsList.toString());
                return 200;
            } else return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200) {
                progress.dismiss();
                int maxAttractions = Home.attractionsList.size();
                numPicker.setMaxValue(maxAttractions);
                if(maxAttractions >= 10)
                    top10.setEnabled(true);
                if(maxAttractions >= 20)
                    top20.setEnabled(true);
                if(maxAttractions >= 30)
                    top30.setEnabled(true);

            } else
                Toast.makeText(getContext(), result + " There is an error", Toast.LENGTH_LONG).show();

        }
    }

}
