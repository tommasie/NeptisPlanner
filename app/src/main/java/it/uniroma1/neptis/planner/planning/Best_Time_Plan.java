package it.uniroma1.neptis.planner.planning;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Settings;
import it.uniroma1.neptis.planner.must_visit;
import it.uniroma1.neptis.planner.report.Report;

public class Best_Time_Plan extends AppCompatActivity {

    public static final int ACTIVITY_1 = 1001;

    public final static String EXTRA_MESSAGE = "key message";
    public final static String MUST = "must message";
    public final static String EXCLUDE = "exclude message";

    private final static String attraction_c_URL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/get_attraction";


    private ProgressDialog progress;
    private ProgressDialog progress2;

    private NumberPicker hours;
    private NumberPicker minutes;


    private EditText multi_must;
    private EditText multi_exclude;

    private List<String> message;

    private ArrayList lmust;
    private ArrayList lexclude;



    private List<String> mlist;

    private String category = "";
    private String type = "";
    private String id = "";
    private String idMail = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best__time__plan);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        message = intent.getStringArrayListExtra(Planning_choice.EXTRA_MESSAGE);
        lmust = new ArrayList<>();
        lexclude = new ArrayList<>();

        hours = (NumberPicker) findViewById(R.id.hh_picker);
        hours.setMaxValue(24);
        hours.setMinValue(0);
        hours.setValue(2);
        hours.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        minutes = (NumberPicker) findViewById(R.id.min_picker);
        minutes.setMaxValue(60);
        minutes.setMinValue(0);
        minutes.setValue(30);
        minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        multi_must = (EditText) findViewById(R.id.multiAutoCompleteTextView_must);

        multi_exclude = (EditText) findViewById(R.id.multiAutoCompleteTextView_exclude);


        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage(" ");

        progress2 = new ProgressDialog(this);
        progress2.setIndeterminate(true);
        progress2.setMessage("I am thinking..");


        //Passing Parameters
        idMail = message.get(0);
        category = message.get(1); //{city,museum,oam}
        type =  message.get(2); // {rome, lecce, ..}
        id = message.get(3); // id of the city or museom or oam



        // ** MAKE GET Request **
        new CallAPI().execute(attraction_c_URL);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        else if (id == R.id.report) {
            Intent intent = new Intent(this, Report.class);
            startActivity(intent);
        }
        else if (id == R.id.myplans) {
            Intent intent = new Intent(this, MyPlans.class);
            startActivity(intent);
        }
        else if (id == R.id.settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    public void next2(View view) {

            if(!lmust.isEmpty() && !lexclude.isEmpty() && lexclude.containsAll(lmust)) {

                new AlertDialog.Builder(Best_Time_Plan.this)
                        .setTitle("Alert")
                        .setMessage("Your MustList and ExcludeList are incompatible!")

                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User pressed ok button. Write Logic Here
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();


            }else{

                progress2.show();
                String hh = ""+hours.getValue();
                String mm = ""+minutes.getValue();

                Intent intent = new Intent(Best_Time_Plan.this, Your_Plan.class);
                ArrayList l = new ArrayList();


                l.add(idMail); //id-mail user
                l.add(category);
                l.add(type);
                l.add(id); // id of city or museuom or oam
                l.add(hh);
                l.add(mm);


                Bundle b = new Bundle();
                b.putInt("calling-activity", Best_Time_Plan.ACTIVITY_1);
                b.putStringArrayList(Best_Time_Plan.EXTRA_MESSAGE, l);
                b.putStringArrayList(Best_Time_Plan.MUST, lmust);
                b.putStringArrayList(Best_Time_Plan.EXCLUDE, lexclude);

                intent.putExtras(b);
                startActivity(intent);


                }


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
                json.put("category", category);
                json.put("id", id);


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

                 if (category.equals("City")) {

                     String jsonResponse = readResponse(in);

                     //Log.d("LOG", "jsonResponse City: " + jsonResponse);

                     JSONObject o = null;
                     JSONArray b = null;
                     try {
                         b = new JSONArray(jsonResponse);
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }


                     int arrSize = b.length();
                     mlist = new ArrayList<String>(arrSize);

                     for (int i = 0; i < arrSize; ++i) {

                         try {
                             o = b.getJSONObject(i);
                         } catch (JSONException e1) {
                             e1.printStackTrace();
                         }

                         try {
                             mlist.add(o.getString("name"));
                         } catch (JSONException e1) {
                             e1.printStackTrace();
                         }

                     }

                 } else if (category.equals("Museum")) {

                     String jsonResponse = readResponse(in);

                     //Log.d("LOG", "jsonResponse: " + jsonResponse);

                     JSONObject o = null;
                     JSONArray b = null;
                     try {
                         b = new JSONArray(jsonResponse);
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }


                     int arrSize = b.length();
                     mlist = new ArrayList<String>(arrSize);

                     for (int i = 0; i < arrSize; ++i) {

                         try {
                             o = b.getJSONObject(i);
                         } catch (JSONException e1) {
                             e1.printStackTrace();
                         }

                         try {
                             mlist.add(o.getString("name"));
                         } catch (JSONException e1) {
                             e1.printStackTrace();
                         }
                     }


                 } else if (category.equals("Opened Air Museum")) {

                     String jsonResponse = readResponse(in);

                     //Log.d("LOG", "jsonResponse: " + jsonResponse);

                     JSONObject o = null;
                     JSONArray b = null;
                     try {
                         b = new JSONArray(jsonResponse);
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }


                     int arrSize = b.length();
                     mlist = new ArrayList<String>(arrSize);

                     for (int i = 0; i < arrSize; ++i) {

                         try {
                             o = b.getJSONObject(i);
                         } catch (JSONException e1) {
                             e1.printStackTrace();
                         }

                         try {
                             mlist.add(o.getString("name"));
                         } catch (JSONException e1) {
                             e1.printStackTrace();
                         }
                     }

                 }

                return "ok";
             }else return "Code: "+code;
        }


        protected void onPostExecute(String result) {

            if (result.equals("ok")) {




                multi_must.setOnTouchListener(new View.OnTouchListener() {


                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        // TODO Auto-generated method stub
                        //multi_must.showDropDown();
                        multi_must.setEnabled(false);
                        Intent i = new Intent(Best_Time_Plan.this, must_visit.class);
                        i.putStringArrayListExtra(EXTRA_MESSAGE, (ArrayList<String>) mlist);
                        i.putExtra("calling","must");
                       // must_visit.title_t10.setText("What you don't want to  visit");
                        startActivityForResult(i, 1);

                        return false;
                    }
                });



                multi_exclude.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        // TODO Auto-generated method stub
                     //   multi_exclude.showDropDown();
                        multi_exclude.setEnabled(false);
                        Intent i = new Intent(Best_Time_Plan.this, must_visit.class);
                        i.putStringArrayListExtra(EXTRA_MESSAGE, (ArrayList<String>) mlist);
                        i.putExtra("calling","exclude");
                       // must_visit.title_t10.setText("What you don't want to  visit");
                        startActivityForResult(i, 2);
                        return false;
                    }
                });


                progress.dismiss();
            } else
                Toast.makeText(getApplicationContext(), result + " There is an error", Toast.LENGTH_LONG).show();

        }




        }// end CallAPI


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                lmust = (ArrayList) data.getStringArrayListExtra("result-must");
                multi_must.setText(lmust.toString());

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                lexclude = (ArrayList) data.getStringArrayListExtra("result-exclude");
                multi_exclude.setText(lexclude.toString());
               }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }





    }//onActivityResult


    @Override
    protected void onResume() {
        super.onResume();

        multi_must.setEnabled(true);
        multi_exclude.setEnabled(true);
    }





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