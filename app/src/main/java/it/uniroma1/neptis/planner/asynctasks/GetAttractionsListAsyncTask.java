/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.asynctasks;

import android.util.Log;
import android.widget.Button;
import android.widget.NumberPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.Home;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Attraction;

public class GetAttractionsListAsyncTask extends JSONAsyncTask {

    private MainInterface activity;
    private NumberPicker picker;
    private Button top10, top20, top30;

    public GetAttractionsListAsyncTask(MainInterface activity, NumberPicker picker, Button... buttons) {
        this.activity = activity;
        this.picker = picker;
        this.top10 = buttons[0];
        this.top20 = buttons[1];
        this.top30 = buttons[2];
    }

    @Override
    protected void onPreExecute() {
        activity.showToolbarProgress();
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream in;
        int code;
        String urlString = params[0];
        String token = params[1];

        try {
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", token);

            in = new BufferedInputStream(urlConnection.getInputStream());
            code = urlConnection.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }

        if (code == 200) {
            String jsonResponse = readResponse(in);
            Log.d("response", jsonResponse);
            JSONArray attractions;
            try {
                attractions = new JSONArray(jsonResponse);
                JSONObject attraction;
                for (int i = 0; i < attractions.length(); ++i) {
                    attraction = attractions.getJSONObject(i);
                    Home.attractionsList.add(Attraction.parse(attraction));
                }
                return 200;
            } catch (JSONException e) {
                e.printStackTrace();
                return 500;
            }

        } else return code;
    }

    @Override
    protected void onPostExecute(Integer result) {
        activity.hideToolbarProgress();
        if (result == 200) {
            int maxAttractions = Home.attractionsList.size();
            picker.setMaxValue(maxAttractions);
            if(maxAttractions >= 10)
                top10.setEnabled(true);
            if(maxAttractions >= 20)
                top20.setEnabled(true);
            if(maxAttractions >= 30)
                top30.setEnabled(true);

        } else activity.showSnackBar("Errore");
    }
}
