package it.uniroma1.neptis.planner.planning;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class Your_Plan extends AppCompatActivity {

    private final static String apiURL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/compute-plan-";

    private ListView yourPlan;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your__plan);

        //intiat your shared pref
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        //retrieve data from pref
        travel_mode = pref.getString("travel_mode", null);
        data_mode = pref.getString("data_mode", null);

        yourPlan = (ListView) findViewById(R.id.listView_yourPlan);

        progress= new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage("I am thinking..");

        message = new ArrayList<>();
        lmust = new ArrayList<>();
        lexclude = new ArrayList<>();
        lrating = new ArrayList<>();

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        int callingActivity = b.getInt("calling-activity");
        switch (callingActivity) {

            case Best_Time_Plan.ACTIVITY_1:

                message = b.getStringArrayList(Best_Time_Plan.EXTRA_MESSAGE);
                lmust = b.getStringArrayList(Best_Time_Plan.MUST);
                lexclude = b.getStringArrayList(Best_Time_Plan.EXCLUDE);
                Toast.makeText(getApplicationContext(), "Best time Planning", Toast.LENGTH_LONG).show();
                break;

            case Best_Rate_Plan.ACTIVITY_2:
                message = b.getStringArrayList(Best_Rate_Plan.EXTRA_MESSAGE);
                lmust = b.getStringArrayList(Best_Rate_Plan.MUST);
                lexclude = b.getStringArrayList(Best_Rate_Plan.EXCLUDE);
                lrating = b.getStringArrayList(Best_Rate_Plan.RATING);
                Toast.makeText(getApplicationContext(), "lrating intent: "+lrating.toString(), Toast.LENGTH_LONG).show();
                break;
        }

        Toast.makeText(getApplicationContext(), "lmust intent: "+lmust.toString(), Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "lexclude intent: "+lexclude.toString(), Toast.LENGTH_LONG).show();

        //Passing Parameters
        idMail = message.get(0); //id-mail user
        category = message.get(1); //{city,museum,oam}
        category = category.toLowerCase();
        type = message.get(2); //{rome, lecce, ..}
        idType = message.get(3);
        hh = message.get(4);
        mm = message.get(5);

        Toast.makeText(getApplicationContext(), "Node route:" + apiURL + category, Toast.LENGTH_LONG).show();


        //qui gps


        // ** MAKE GET Request **

        new CallAPI().execute(apiURL+category);

    }


    @Override
    public void onBackPressed() {
    }

    public void exit_to_menu(View view) {
        Intent intent = new Intent(Your_Plan.this, Welcome.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

    }


    private class CallAPI extends AsyncTask<String, String, String> {


        protected void onPreExecute() {
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {
            InputStream in = null;
            int code = -1;
            String charset = "UTF-8";


            String urlURL = params[0]; // URL to call

            // HTTP post
            try {
                URL url = new URL(urlURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // set like post request
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



                JSONObject jo;
                JSONArray ja = new JSONArray();
                for(int i=0; i<lmust.size();i++){
                    jo = new JSONObject();
                    jo.put("name", lmust.get(i));
                    ja.put(jo);
                }

                JSONObject mainObj = new JSONObject();
                mainObj.put("must", ja);


                json.put("must",mainObj); //invio al server



                JSONObject oex;
                JSONArray aex = new JSONArray();
                for(int i=0; i<lexclude.size();i++){
                    oex = new JSONObject();
                    oex.put("name", lexclude.get(i));
                    aex.put(oex);
                }

                JSONObject moex = new JSONObject();
                moex.put("exclude", aex);

                json.put("exclude",moex); //invio al server




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
                // --

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

                //urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }


            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {

                plan = readResponse(in);
                Log.d("LOG", "Your Plan: " + plan);

                }
            return code+"";
        }


        protected void onPostExecute(String result) {


            Geocoder geocoder = new Geocoder(Your_Plan.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(Welcome.lat, Welcome.lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(result.equals("200")){

            // CONVERT FILE IN JSON OBJECT
            ArrayList<String> routes  = new ArrayList<>();

            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(plan);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Log.d("plan: ",plan);

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



            ArrayAdapter<String> adapter = new ArrayAdapter<String >(Your_Plan.this, android.R.layout.simple_list_item_1, routes);

            yourPlan.setAdapter(adapter);

             if(!category.equals("museum")) {

                 yourPlan.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                         /*Intent i = new Intent(getApplicationContext(), GeofencingService.class);
                         i.putExtra("latitude", Welcome.lat);
                         i.putExtra("latitude", Welcome.lon);
                         startService(i);*/

                     }

                 });
             }

               progress.dismiss();

        // ---SIMPLE ALERT FOR DEBUG---

            new AlertDialog.Builder(Your_Plan.this)
                .setTitle("Alert")
                .setMessage("Do you want to save your plan?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // continue with saving

                        // SALVATAGGIO  FILE 1
                        String filename = type+"_"+ts;
                        //String string = "[{\"route\":\"colosseo\"},{\"route\":\"fontana di trevi\"}]";
                        FileOutputStream outputStream;

                        try {
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(plan.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                        // fine saving


                        Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();

                    }
                })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                .show();


        //--- END ----

            }else {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "An error occured during plan computation.\nTry later..", Toast.LENGTH_LONG).show();

            }

        }


    }  // end CallAPI


    private String readResponse(InputStream in){
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
