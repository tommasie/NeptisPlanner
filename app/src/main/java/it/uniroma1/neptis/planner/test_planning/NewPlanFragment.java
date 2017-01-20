package it.uniroma1.neptis.planner.test_planning;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Welcome;
import it.uniroma1.neptis.planner.planning.Best_Rate_Plan;
import it.uniroma1.neptis.planner.planning.Best_Time_Plan;

public class NewPlanFragment extends Fragment implements View.OnClickListener{

    private final static String apiURL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/compute-plan-";

    private TextView title;
    private ListView listView;
    private Button button;

    private ProgressDialog progress;
    
    private List<String> message;
    private List<String> lmust;
    private List<String> lexclude;
    private List<String> lrating;

    private String travel_mode;
    private String data_mode;
    private SharedPreferences pref;

    private String ts;
    private String category = "";
    private String type = "";
    private String hh = "";
    private String mm = "";

    private String idMail = "";
    private String idType = "";

    private String plan = "";
    
    private PlanningFragments activity;
    
    public NewPlanFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        travel_mode = pref.getString("travel_mode", null);
        data_mode = pref.getString("data_mode", null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView)view.findViewById(R.id.textView_selectedPlan_f);
        title.setText("Your Plan");
        listView = (ListView)view.findViewById(R.id.listView_selectedPlan_f);
        button = (Button)view.findViewById(R.id.button_exit_f);
        button.setOnClickListener(this);

        progress= new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        progress.setMessage("I am thinking..");

        message = new ArrayList<>();
        lmust = new ArrayList<>();
        lexclude = new ArrayList<>();
        lrating = new ArrayList<>();

        Bundle b = getArguments();
        int callingActivity = b.getInt("calling-activity");
        switch (callingActivity) {
            case Best_Time_Plan.ACTIVITY_1:
                message = b.getStringArrayList(Best_Time_Plan.EXTRA_MESSAGE);
                lmust = b.getStringArrayList(Best_Time_Plan.MUST);
                lexclude = b.getStringArrayList(Best_Time_Plan.EXCLUDE);
                Toast.makeText(getContext(), "Best time Planning", Toast.LENGTH_LONG).show();
                break;
            case Best_Rate_Plan.ACTIVITY_2:
                message = b.getStringArrayList(Best_Rate_Plan.EXTRA_MESSAGE);
                lmust = b.getStringArrayList(Best_Rate_Plan.MUST);
                lexclude = b.getStringArrayList(Best_Rate_Plan.EXCLUDE);
                lrating = b.getStringArrayList(Best_Rate_Plan.RATING);
                Toast.makeText(getContext(), "lrating intent: "+lrating.toString(), Toast.LENGTH_LONG).show();
                break;
        }
        Toast.makeText(getContext(), "lmust intent: "+lmust.toString(), Toast.LENGTH_LONG).show();
        Toast.makeText(getContext(), "lexclude intent: "+lexclude.toString(), Toast.LENGTH_LONG).show();

        //Passing Parameters
        idMail = message.get(0); //id-mail user
        category = message.get(1); //{city,museum,oam}
        category = category.toLowerCase();
        type = message.get(2); //{rome, lecce, ..}
        idType = message.get(3);
        hh = message.get(4);
        mm = message.get(5);

        Toast.makeText(getContext(), "Node route:" + apiURL + category, Toast.LENGTH_LONG).show();

        new ComputePlanAsyncTask().execute(apiURL+category);
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlanningFragments) {
            activity = (PlanningFragments) context;
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
    public void onClick(View v) {
        if(v.getId() == R.id.button_exit_f)
            activity.exitToMenu();
    }

    private class ComputePlanAsyncTask extends JSONAsyncTask {

        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in;
            int code;
            String charset = "UTF-8";
            String urlURL = params[0]; // URL to call

            // HTTP post
            try {
                URL url = new URL(urlURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Object json to send
                JSONObject json = new JSONObject();
                json.put("mail", idMail);
                json.put("type", type); //
                json.put("id",idType);
                json.put("category", category.toLowerCase());
                json.put("hh", hh);
                json.put("mm", mm);
                json.put("travel_mode", travel_mode);
                json.put("data_mode", data_mode);

                //Add the list of must-see places
                JSONObject jo;
                JSONArray ja = new JSONArray();
                for(int i=0; i<lmust.size();i++){
                    jo = new JSONObject();
                    jo.put("name", lmust.get(i));
                    ja.put(jo);
                }
                JSONObject mainObj = new JSONObject();
                mainObj.put("must", ja);
                json.put("must",mainObj);

                //Add the list of excluded places
                JSONObject oex;
                JSONArray aex = new JSONArray();
                for(int i=0; i<lexclude.size();i++){
                    oex = new JSONObject();
                    oex.put("name", lexclude.get(i));
                    aex.put(oex);
                }
                JSONObject moex = new JSONObject();
                moex.put("exclude", aex);
                json.put("exclude", moex);

                JSONObject jor;
                JSONArray jar = new JSONArray();
                for(int i=0; i<lrating.size();i=i+2){
                    jor = new JSONObject();
                    jor.put("name", lrating.get(i));
                    jor.put("rating", lrating.get(i+1));

                    jar.put(jor);
                }
                JSONObject mainJor = new JSONObject();
                mainJor.put("rating", jar);
                json.put("rating",mainJor); //invio al server
                Log.d("json rating",mainJor.toString());

                // get current date
                Calendar calendar = Calendar.getInstance();
                java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
                ts = ourJavaTimestampObject.toString().replace(' ','T');
                Log.d("timestamp: ", ts);
                json.put("data",ts);

                //lat = Welcome.gps.getLatitude();
                //lon = Welcome.gps.getLongitude();
                Welcome.lat = 42.100335;
                Welcome.lon = 12.159988;
                json.put("lat", Welcome.lat);
                json.put("lon", Welcome.lon);

                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();
                printout.close();

                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }

            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {
                plan = readResponse(in);
                Log.d("LOG", "Your Plan: " + plan);
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result == 200) {
                // CONVERT FILE IN JSON OBJECT
                ArrayList<String> routes  = new ArrayList<>();
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(plan);
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
                    try {
                        route = jsonobject.getString("route");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(category.equals("museum"))
                        routes.add("GO TO AREA: " + route);
                    else
                        routes.add("GO TO " + route);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, routes);
                listView.setAdapter(adapter);

                if(!category.equals("museum")) {

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, final View view,
                                                int position, long id) {
                            String item = (String) parent.getItemAtPosition(position);

                            // ** CALL MAPS **/
                            String dest_address = item.substring(6);

                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    //Uri.parse("http://maps.google.com/maps?daddr=41.890026,12.492370"));

                                    Uri.parse("google.navigation:q=" + dest_address));

                            startActivity(intent);
                         /*Intent i = new Intent(getContext(), GeofencingService.class);
                         i.putExtra("latitude", Welcome.lat);
                         i.putExtra("latitude", Welcome.lon);
                         startService(i);*/

                        }

                    });
                }
                progress.dismiss();

                //Ask the user wether he wants to save the plan
                new AlertDialog.Builder(getContext())
                        .setTitle("Alert")
                        .setMessage("Do you want to save your plan?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with saving
                                String filename = type+"_"+ts;
                                FileOutputStream outputStream;
                                try {
                                    outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                                    outputStream.write(plan.getBytes());
                                    outputStream.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getContext(), "Saved!", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            } else {
                progress.dismiss();
                Toast.makeText(getContext(), "An error occured during plan computation.\nTry later..", Toast.LENGTH_LONG).show();
            }
        }
    }
}
