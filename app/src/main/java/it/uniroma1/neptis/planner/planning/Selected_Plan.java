package it.uniroma1.neptis.planner.planning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.services.tracking.GeofencingService;

public class Selected_Plan extends AppCompatActivity {

    private TextView title;
    private ListView list;

    private String plan;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected__plan);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        initActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initActivity(intent);
    }

    private void initActivity(Intent intent) {
        plan = intent.getStringExtra(MyPlans.EXTRA_MESSAGE);
        if(plan != null)
            Log.d("plan_name",plan);
        title = (TextView) findViewById(R.id.textView_selectedPlan);
        list = (ListView) findViewById(R.id.listView_selectedPlan);

        builder = new AlertDialog.Builder(this);

        //READ FILE FROM INTERNAL STORAGE
        FileInputStream fis = null;
        try {
            fis = openFileInput(plan);
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




        // CONVERT FILE IN JSON OBJECT
        String jsonStr = sb.toString();

        ArrayList<String> routes  = new ArrayList<>();
        final ArrayList<String> coords = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();

        JSONArray jsonarray = null;
        try {
            jsonarray = new JSONArray(jsonStr);
            Log.d("JSON",jsonarray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = null;
            try {
                jsonobject = jsonarray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String route = null;
            String coord = null;
            String id = null;
            try {
                route = jsonobject.getString("route");
                String lat = jsonobject.getJSONObject("coordinates").getString("lat");
                String lng = jsonobject.getJSONObject("coordinates").getString("lng");
                id = jsonobject.getString("id");
                coord = lat + "," + lng;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            routes.add("GO TO " + route);
            coords.add(coord);
            ids.add(id);

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routes);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String dest_address = item.substring(6);
                initAlertDialog(coords.get(position));
                AlertDialog dialog = builder.create();
                dialog.show();
                Intent geofencingService = new Intent(getApplicationContext(), GeofencingService.class);
                geofencingService.putExtra("coordinates",coords.get(position));
                geofencingService.putExtra("id",ids.get(position));
                geofencingService.putExtra("name",dest_address);
                geofencingService.putExtra("current_plan",plan);
                startService(geofencingService);
            }

        });
    }

    // Add the buttons

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    private void initAlertDialog(String dest_address) {
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + dest_address + "&mode=w"));
        builder.setMessage(R.string.open_gps);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });


    }
}
