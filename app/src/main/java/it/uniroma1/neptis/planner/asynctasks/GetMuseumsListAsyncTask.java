/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.asynctasks;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Element;
import it.uniroma1.neptis.planner.planning.ChooseMuseumFragment;

public class GetMuseumsListAsyncTask extends JSONAsyncTask {

    private MainInterface activity;
    private SwipeRefreshLayout refreshLayout;
    private List<Element> museumQuery;
    private List<Element> filteredList;
    private ChooseMuseumFragment.MuseumRecyclerAdapter adapter;

    public GetMuseumsListAsyncTask(MainInterface activity, SwipeRefreshLayout layout,
                                   List<Element> museumQuery, List<Element> filteredList,
                                   ChooseMuseumFragment.MuseumRecyclerAdapter adapter) {
        this.activity = activity;
        this.refreshLayout = layout;
        this.museumQuery = museumQuery;
        this.filteredList = filteredList;
        this.adapter = adapter;
    }

    private final String TAG = GetMuseumsListAsyncTask.class.getName();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.showToolbarProgress();
        this.museumQuery.clear();
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream in;
        int code = 0;

        String urlString = params[0]; // URL to call
        String token = params[1];
        Log.d(TAG,token);
        // HTTP get

        try {
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", token);
            //get the response
            in = new BufferedInputStream(urlConnection.getInputStream());
            code = urlConnection.getResponseCode();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Log.e(TAG, ""+code);
            return -1;
        }
        if (code == 200) {
            String jsonResponse = readResponse(in);
            JSONObject item;
            JSONArray items;
            try {
                items = new JSONArray(jsonResponse);
                for (int i = 0; i < items.length(); ++i) {
                    item = items.getJSONObject(i);
                    museumQuery.add(new Element(item.getString("name"), item.getString("id")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return 400;
            }
        }
        return code;
    }

    @Override
    protected void onPostExecute(Integer result) {
        activity.hideToolbarProgress();
        refreshLayout.setRefreshing(false);
        if (result == 200) {
            if (!museumQuery.isEmpty()){
                Collections.sort(museumQuery);
                filteredList.clear();
                filteredList.addAll(museumQuery);
                adapter.notifyDataSetChanged();
            } else {
                activity.showSnackBar("Nessun museo disponibile nella tua area");
            }
        }else {
            activity.showSnackBar("Errore del sistema, riprova più tardi");
        }
    }
}
