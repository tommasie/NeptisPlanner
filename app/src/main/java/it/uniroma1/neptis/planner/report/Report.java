package it.uniroma1.neptis.planner.report;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Settings;
import it.uniroma1.neptis.planner.planning.JSONAsyncTask;
import it.uniroma1.neptis.planner.plans.PlansActivity;

public class Report extends AppCompatActivity {

    private ProgressDialog progress;

    private final static String url_city = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/cities";
    private final static String url_museum = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/museums";
    private final static String url_opened = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/get_oam";

    private final static String attraction_c_URL = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/get_attraction";
    private final static String area_m_URL = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/get_aream";
    private final static String area_oam_URL = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/get_areaOam";

    private final static String report_URL = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/report_queue";

    private String type="";
    private String ucategory = "";
    private String nameId = "";
    private String attractionId ="";
    private String sminutes="";

    private EditText minutesEdit;

    private TextView minutesTextView;
    private Spinner typeSpinner;
    private Spinner categorySpinner;
    private Button reportButton;

    private List<Element> queryResults;
    private List<Element> mlist2;

    private  AutoCompleteTextView structureAutoComplete;
    private  AutoCompleteTextView areaAutoComplete;

   // private int position=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);


        progress= new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage(" ");

        typeSpinner = (Spinner) findViewById(R.id.spinner_type);
        List<String> list = new ArrayList<>();
        list.add("What are you reporting?");
        list.add("Queue");
        list.add("Visit");
        //list.add("Moving");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter(this,
                android.R.layout.select_dialog_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        typeSpinner.setAdapter(dataAdapter);
        typeSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        categorySpinner = (Spinner) findViewById(R.id.spinner_category);
        categorySpinner.setVisibility(View.INVISIBLE);
        categorySpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        List<String> categorySpinnerData = new ArrayList<>();

        structureAutoComplete = (AutoCompleteTextView) findViewById(R.id.autocomplete_structure);
        structureAutoComplete.setThreshold(1);
        structureAutoComplete.setVisibility(View.INVISIBLE);

        structureAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                areaAutoComplete.getText().clear();
                areaAutoComplete.setVisibility(View.INVISIBLE);
                //structureAutoComplete.setKeyListener(true);

                String selected = (String) adapter.getItemAtPosition(pos);
                for (int i = 0; i < queryResults.size(); i++)
                    if (queryResults.get(i).getName().equals(selected)) {
                        nameId = queryResults.get(i).getId();
                        Log.d("NameId",nameId);
                        //structureAutoComplete.setKeyListener(null);
                        if(ucategory.equals("City"))
                            new CallAPI2().execute(attraction_c_URL);
                        else if(ucategory.equals("Museum"))
                           new CallAPI2().execute(area_m_URL);
                        else new CallAPI2().execute(area_oam_URL);
                        break;
                    }
            }
        });


        areaAutoComplete = (AutoCompleteTextView) findViewById(R.id.autocomplete_area);
        areaAutoComplete.setThreshold(1);
        areaAutoComplete.setVisibility(View.INVISIBLE);

        areaAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = (String) adapter.getItemAtPosition(pos);
                for (int i = 0; i < mlist2.size(); i++)
                    if (mlist2.get(i).getName().equals(selected)) {
                        attractionId = mlist2.get(i).getId();
                        break;
                    }
                minutesEdit.setVisibility(View.VISIBLE);
                minutesTextView.setVisibility(View.VISIBLE);
                reportButton.setVisibility(View.VISIBLE);

            }

        });

        minutesEdit = (EditText) findViewById(R.id.editText_minutes);
        minutesEdit.setVisibility(View.INVISIBLE);

        minutesTextView = (TextView) findViewById(R.id.textView_minutes);
        minutesTextView.setVisibility(View.INVISIBLE);

        reportButton = (Button) findViewById(R.id.button_report);
        reportButton.setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        else if (id == R.id.myplans) {
            Intent intent = new Intent(this, PlansActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    public void reportIt(View view) {
        //** check spinners **
        sminutes = minutesEdit.getText().toString();
        //(!spinner1.getSelectedItem().equals("Select the category"))
        if(sminutes.isEmpty())
            Toast.makeText(getApplicationContext(), "minutesEdit is empty ", Toast.LENGTH_LONG).show();
        else if(attractionId.isEmpty())
            Toast.makeText(getApplicationContext(), "There is an error attractionId", Toast.LENGTH_LONG).show();
        else {
            //  ** make post request **
            Toast.makeText(getApplicationContext(), "type: " + type + " category: " + ucategory + " nameId: " + nameId + " attractionId: " + attractionId + " min: " + sminutes, Toast.LENGTH_LONG).show();
            new CallAPIReport().execute(report_URL);
        }

    }

    // reports the collected data
    private class CallAPIReport extends AsyncTask<String, String, Integer> {

        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in = null;
            int code = -1;
            String charset = "UTF-8";


            String urlURL = params[0]; // URL to call

            //Log.d("LOG",urlURL);

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
                json.put("type", type);
                json.put("category", ucategory);
                json.put("nameId", nameId);
                json.put("attractionId", attractionId);
                json.put("minutes", sminutes);

                // get current date
                Calendar calendar = Calendar.getInstance();
                java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
                String ts = ourJavaTimestampObject.toString().replace(' ','T');
                Log.d("timestamp: ",ts);
                // --

                json.put("data",ts);


                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();

                printout.close();


                in = new BufferedInputStream(urlConnection.getInputStream());

                code = urlConnection.getResponseCode();
                Log.d("code report",code+"");

                //urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }


            return code;
        }


        protected void onPostExecute(Integer result) {

            if (result== 204) {
                Toast.makeText(getApplicationContext(), "Report succesful! \nThank you!", Toast.LENGTH_LONG).show();
                progress.dismiss();
                return;
            }

            else Toast.makeText(getApplicationContext(), result+"Error on reporting..", Toast.LENGTH_LONG).show();
            progress.dismiss();

        }


    }// end CallAPI

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            //Toast.makeText(parent.getContext(), "On Item Select : \n" + spinner1.getSelectedItem().toString(), Toast.LENGTH_LONG).show();

            Spinner spinner = (Spinner) parent;
            //If the spinner triggering the function is the first one
            if(spinner.getId() == R.id.spinner_type) {
                type = spinner.getSelectedItem().toString();

                structureAutoComplete.getText().clear();
                structureAutoComplete.setVisibility(View.INVISIBLE);
                areaAutoComplete.getText().clear();
                areaAutoComplete.setVisibility(View.INVISIBLE);
                minutesEdit.getText().clear();
                minutesEdit.setVisibility(View.INVISIBLE);
                minutesTextView.setVisibility(View.INVISIBLE);
                reportButton.setVisibility(View.INVISIBLE);


                if (!type.equals("What are you reporting?")) {
                    List<String> categorySpinnerData = new ArrayList();
                    if(type.equals("Visit")) {
                        categorySpinnerData.add("Select the structure");
                        categorySpinnerData.add("City");
                        categorySpinnerData.add("Opened Air Museum");
                    } else {
                        categorySpinnerData.add("Select the structure");
                        categorySpinnerData.add("City");
                        categorySpinnerData.add("Museum");
                        categorySpinnerData.add("Opened Air Museum");
                    }
                    ArrayAdapter<String> dataAdapter2 = new ArrayAdapter(Report.this,
                            android.R.layout.simple_spinner_item, categorySpinnerData);
                    dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(dataAdapter2);

                    categorySpinner.setVisibility(View.VISIBLE);
                }
            }

            else if(spinner.getId() == R.id.spinner_category) {

                structureAutoComplete.getText().clear();
                structureAutoComplete.setVisibility(View.INVISIBLE);
                areaAutoComplete.getText().clear();
                areaAutoComplete.setVisibility(View.INVISIBLE);
                minutesEdit.getText().clear();
                minutesEdit.setVisibility(View.INVISIBLE);
                minutesTextView.setVisibility(View.INVISIBLE);
                reportButton.setVisibility(View.INVISIBLE);

                String selected = categorySpinner.getSelectedItem().toString();
                ucategory = selected;
                if (selected.equals("City"))
                    // Make GET request
                    new CallAPI().execute(url_city, "City");
                else if (selected.equals("Museum"))
                    // Make GET request
                    new CallAPI().execute(url_museum, "Museum");
                else if (selected.equals("Opened Air Museum"))
                    // Make GET request
                    new CallAPI().execute(url_opened, "Opened Air Museum");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {}
    }


    // call to get items of selected category
    private class CallAPI extends JSONAsyncTask {

        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in = null;
            int code = -1;
            String charset = "UTF-8";

            String urlString = params[0]; // URL to call
            String category = params[1];

            // HTTP get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //get the response
                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();
                //urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }

            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200){
                //Log.d("LOG", "code: " + code);

                if (category.equals("City")) {
                    queryResults = new ArrayList<>();

                    String jsonResponse = readResponse(in);
                    JSONObject o = null;
                    JSONArray b = null;
                    try {
                        b = new JSONArray(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int arrSize = b.length();
                    queryResults = new ArrayList<Element>(arrSize);
                    for (int i = 0; i < arrSize; ++i) {
                        try {
                            o = b.getJSONObject(i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        try {
                            queryResults.add(new Element(o.getString("name"), o.getString("id")));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                } else if (category.equals("Museum")) {
                    String jsonResponse = readResponse(in);
                    JSONObject o = null;
                    JSONArray b = null;
                    try {
                        b = new JSONArray(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int arrSize = b.length();
                    queryResults = new ArrayList<Element>(arrSize);
                    for (int i = 0; i < arrSize; ++i) {
                        try {
                            o = b.getJSONObject(i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        try {
                            queryResults.add(new Element(o.getString("name"), o.getString("id")));
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
                    queryResults = new ArrayList<Element>(arrSize);

                    for (int i = 0; i < arrSize; ++i) {

                        try {
                            o = b.getJSONObject(i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        try {
                            queryResults.add(new Element(o.getString("name"), o.getString("id")));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                }
            return 200;

            }else
                return code;

        }


        protected void onPostExecute(Integer result) {

            if (result == 200) {
                if (queryResults.size() > 0) {
                    List<String> list2 = new ArrayList<String>();
                    for (Element c : queryResults) {
                        list2.add(c.name);

                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            Report.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list2
                    );
                    structureAutoComplete.setAdapter(adapter);
                    structureAutoComplete.setVisibility(View.VISIBLE);
                    if (ucategory.equals("Museum"))
                        areaAutoComplete.setHint("Digit the area");

                    /*
                    structureAutoComplete.setOnTouchListener(new View.OnTouchListener() {

                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                            // TODO Auto-generated method stub
                            structureAutoComplete.showDropDown();
                            // autocomplete.requestFocus();
                            return false;
                        }
                    });
                    */

                    //autocomplete.setVisibility(View.VISIBLE);


                } else
                    Toast.makeText(getApplicationContext(), "No items available now. \nPlease try later", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getApplicationContext(), result + "There is a problem. \nPlease try later", Toast.LENGTH_LONG).show();

            progress.dismiss();
            return;


        }


    }  // end CallAPI


    //call to get items of TYPE selected
    private class CallAPI2 extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {
            InputStream in = null;
            int code = -1;
            String charset = "UTF-8";

            String urlURL = params[0]; // URL to call

            //Log.e("LOG",urlURL);

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
                json.put("category", ucategory); //{city, museum, .. }
                json.put("id", nameId); //esempio id di city
                Log.d("CallAPI2", "sending category " + ucategory + " and id " + nameId);

                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();

               // printout.close();


                in = new BufferedInputStream(urlConnection.getInputStream());

                code = urlConnection.getResponseCode();

                //urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }


            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {

                if (ucategory.equals("City")) {

                    String jsonResponse = readResponse(in);

                    //Log.d("LOG", "jsonResponse City: " + jsonResponse);

                    JSONObject o = null;
                    JSONArray js = null;

                    try {
                        js = new JSONArray(jsonResponse);


                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        return code + "(1): ";
                    }


                        int arrSize = js.length();
                        mlist2 = new ArrayList<Element>(arrSize);

                        for (int i = 0; i < arrSize; ++i) {

                            try {
                                o = js.getJSONObject(i);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                return code + "(2): ";
                            }

                            try {
                                mlist2.add(new Element(o.getString("name"), o.getString("id")));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                return code + "(3): ";
                            }
                        }


                } else if (ucategory.equals("Museum")) {

                    String jsonResponse = readResponse(in);

                    //Log.d("LOG", "jsonResponse: " + jsonResponse);

                    JSONObject o = null;
                    JSONArray b = null;

                    try {
                        b = new JSONArray(jsonResponse);


                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        return code + "(1): ";
                    }

                        int arrSize = b.length();
                        mlist2 = new ArrayList<Element>(arrSize);

                        for (int i = 0; i < arrSize; ++i) {

                            try {
                                o = b.getJSONObject(i);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                return code + "(2): ";
                            }

                            try {
                                mlist2.add(new Element(o.getString("name"), o.getString("id")));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                return code + "(3): ";
                            }
                        }



                } else if (ucategory.equals("Opened Air Museum")) {

                    String jsonResponse = readResponse(in);

                    //Log.d("LOG", "jsonResponse: " + jsonResponse);

                    JSONObject o = null;
                    JSONArray b = null;

                    try {
                        b = new JSONArray(jsonResponse);


                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        return code + "(1): ";
                    }


                        int arrSize = b.length();
                        mlist2 = new ArrayList<Element>(arrSize);
                        for (int i = 0; i < arrSize; ++i) {

                            try {
                                o = b.getJSONObject(i);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                return code + "(2): ";
                            }

                            try {
                                mlist2.add(new Element(o.getString("name"), o.getString("id")));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                return code + "(3): ";
                            }
                        }

                }
            return "ok";
            } else
                return "Code: "+code;
        }


        protected void onPostExecute(String result) {

            if (result.equals("ok")) {
                if (mlist2.size() > 0) {

                    List<String> list = new ArrayList<String>();
                    for (Element c : mlist2) {
                        list.add(c.name);

                    }

                    ArrayAdapter adapter1 = new ArrayAdapter(Report.this, android.R.layout.simple_list_item_1, list);
                    areaAutoComplete.setAdapter(adapter1);

                    /*
                    areaAutoComplete.setOnTouchListener(new View.OnTouchListener() {

                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                            // TODO Auto-generated method stub
                            areaAutoComplete.showDropDown();
                            // autocomplete.requestFocus();
                            return false;
                        }
                    });
                    */

                    areaAutoComplete.setVisibility(View.VISIBLE);

                    progress.dismiss();
                } else Toast.makeText(getApplicationContext(), "No items available now. \nPlease try later", Toast.LENGTH_LONG).show();
            }else
                Toast.makeText(getApplicationContext(), result+"There is a problem. \nPlease try later", Toast.LENGTH_LONG).show();

                progress.dismiss();
                return;

        }

    }// end CallAPI2


    protected class Element {
        String name;
        String id;

        Element(String n, String id) {
            this.name = n;
            this.id = id;
        }

        Element() {}

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
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
