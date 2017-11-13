/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.rating;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.AttractionArrayAdapter;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.util.JSONAsyncTask;
import it.uniroma1.neptis.planner.util.ConfigReader;

public class CityAttractionsFragment extends Fragment {
    private static final String TAG = CityAttractionsFragment.class.getName();
    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private String apiURL;

//    private TextView title;
    private ListView listView;
    private AttractionArrayAdapter adapter;

    private int listPosition;
    private ArrayList<Attraction> attractionsList;

    private MainInterface activity;
    FirebaseUser user;
    public CityAttractionsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.apiURL = ConfigReader.getConfigValue(getContext(), "serverURL");
        apiURL += "/attractionc";
        apiURL += "?city=" + activity.getLocation().getLocality();
        apiURL += "&region=" + activity.getLocation().getAdminArea();
        user = activity.getUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attractions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        title = (TextView)view.findViewById(R.id.textView_attractions);
//        title.setText(getString(R.string.fragment_plans_list_title));
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            new GetAttractionsAsyncTask().execute(apiURL, idToken);
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
        listView = (ListView)view.findViewById(R.id.listView_attractions);
        attractionsList = new ArrayList<>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Attraction attraction = (Attraction)parent.getItemAtPosition(position);
                Bundle b = new Bundle();
                b.putString("id", attraction.getId());
                b.putString("name", attraction.getName());
                b.putString("type", "city");
                activity.attractionDetail(b);
            }

        });

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


    protected class GetAttractionsAsyncTask extends JSONAsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                        attractionsList.add(Attraction.parse(attraction));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                Log.d(TAG, attractionsList.toString());
                return 200;
            } else return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200) {
                adapter = new AttractionArrayAdapter(getContext(), R.layout.plans_list_item2, attractionsList);
                listView.setAdapter(adapter);
            } else
                Toast.makeText(getContext(), result + " There is an error", Toast.LENGTH_LONG).show();

        }
    }
}
