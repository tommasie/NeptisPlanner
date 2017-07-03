package it.uniroma1.neptis.planner.planning;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import it.uniroma1.neptis.planner.model.city.CityAttraction;
import it.uniroma1.neptis.planner.model.Plan;
import it.uniroma1.neptis.planner.model.city.CityPlan;

/**
 * Created by thomas on 20/01/17.
 */

public abstract class JSONAsyncTask extends AsyncTask<String,String,Integer> {

    protected String readResponse(InputStream in){
        //reads from inputStream
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toString();
    }
}
