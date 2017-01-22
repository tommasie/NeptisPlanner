package it.uniroma1.neptis.planner.planning;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Welcome;
import it.uniroma1.neptis.planner.plans.NewPlanFragment;

public class PlanningActivity extends AppCompatActivity implements PlanningFragmentsInterface {

    public final static String EXTRA_MESSAGE = "key message";
    public static final String MUST = "must";
    public static final String EXCLUDE = "exclude";
    public static final String RATING = "rating";

    private final static String apiURL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/compute-plan-";

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private ChoiceFragment choiceFragment;
    private ProgressDialog progress;

    private Map<String,String> planningParameters;
    private List<String> mustVisit;
    private List<String> excludeVisit;
    private List<String> ratingList;
    private String plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage("I am thinking..");

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        choiceFragment = new ChoiceFragment();
        transaction.add(R.id.activity_planning,choiceFragment);
        transaction.commit();

        planningParameters = new HashMap<>();
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        planningParameters.put("mail", pref.getString("mail", null));
        planningParameters.put("travel_mode", pref.getString("travel_mode", null));
        planningParameters.put("data_mode", pref.getString("data_mode", null));

        //FIXME for best rating planning
        ratingList = new ArrayList<>();
    }

    //TODO move it to its fragment
    public void guide(View v) {
        choiceFragment.guide(v);
    }

    @Override
    public void requestTime(Map<String,String> parameters, Class fragmentClass) {
        planningParameters.putAll(parameters);
        //Use Java Reflection API to determine which fragment to call
        Fragment bestTimeFragment = null;
        try {
            bestTimeFragment = (Fragment) Class.forName(fragmentClass.getName()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bundle b = new Bundle();
        //Parameters needed to make the call in the AsyncTask
        b.putString("category", planningParameters.get("category"));
        b.putString("id", planningParameters.get("id"));
        bestTimeFragment.setArguments(b);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.activity_planning, bestTimeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void computePlan(Bundle bundle, Map<String,String> parameters, Map<String,List<String>> extraParams) {
        //FIXME bundle needed currently for type of planning (best rate/best time)
        planningParameters.putAll(parameters);
        mustVisit = extraParams.get(MUST);
        excludeVisit = extraParams.get(EXCLUDE);

        new ComputePlanAsyncTask().execute(apiURL + planningParameters.get("category"));
    }

    @Override
    public void exitToMenu() {
        Intent intent = new Intent(this, Welcome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void askSavePlan(final String ts) {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Do you want to save your plan?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with saving
                        String filename = planningParameters.get("type") + "_" + ts;
                        FileOutputStream outputStream;
                        try {
                            outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(plan.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private class ComputePlanAsyncTask extends JSONAsyncTask {

        private String ts;

        @Override
        protected void onPreExecute() {
            progress.show();
            Toast.makeText(getApplicationContext(), "lmust intent: "+ mustVisit.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "lexclude intent: "+ excludeVisit.toString(), Toast.LENGTH_LONG).show();
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
                for(String param : planningParameters.keySet()) {
                    if(param.equals("category"))
                        json.put(param,planningParameters.get(param).toLowerCase());
                    else
                        json.put(param,planningParameters.get(param));
                }
                /*json.put("mail", planningParameters.get("mail"));
                json.put("category", planningParameters.get("category").toLowerCase());
                json.put("type", planningParameters.get("type")); //
                json.put("id", planningParameters.get("id"));
                json.put("hh", planningParameters.get("hh"));
                json.put("mm", planningParameters.get("mm"));
                json.put("travel_mode", planningParameters.get("travel_mode"));
                json.put("data_mode", planningParameters.get("data_mode"));*/

                //Add the list of must-see places
                JSONObject jo;
                JSONArray ja = new JSONArray();
                for (int i = 0; i < mustVisit.size(); i++) {
                    jo = new JSONObject();
                    jo.put("name", mustVisit.get(i));
                    ja.put(jo);
                }
                JSONObject mainObj = new JSONObject();
                mainObj.put("must", ja);
                json.put("must", mainObj);

                //Add the list of excluded places
                JSONObject oex;
                JSONArray aex = new JSONArray();
                for (int i = 0; i < excludeVisit.size(); i++) {
                    oex = new JSONObject();
                    oex.put("name", excludeVisit.get(i));
                    aex.put(oex);
                }
                JSONObject moex = new JSONObject();
                moex.put("exclude", aex);
                json.put("exclude", moex);

                JSONObject jor;
                JSONArray jar = new JSONArray();
                for (int i = 0; i < ratingList.size(); i = i + 2) {
                    jor = new JSONObject();
                    jor.put("name", ratingList.get(i));
                    jor.put("rating", ratingList.get(i + 1));

                    jar.put(jor);
                }
                JSONObject mainJor = new JSONObject();
                mainJor.put("rating", jar);
                json.put("rating", mainJor); //invio al server
                Log.d("json rating", mainJor.toString());

                // get current date
                Calendar calendar = Calendar.getInstance();
                java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
                ts = ourJavaTimestampObject.toString().replace(' ', 'T');
                Log.d("timestamp: ", ts);
                json.put("data", ts);

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
                e.printStackTrace();
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
            progress.dismiss();
            if (result == 200) {
                Fragment planFragment = new NewPlanFragment();
                Bundle b = new Bundle();
                b.putString(EXTRA_MESSAGE, plan);
                planFragment.setArguments(b);
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.activity_planning, planFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                //Ask the user wether he wants to save the plan
                askSavePlan(ts);
            }
        }
    }
}
