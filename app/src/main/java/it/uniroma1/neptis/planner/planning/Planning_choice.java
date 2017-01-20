package it.uniroma1.neptis.planner.planning;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import it.uniroma1.neptis.planner.report.Report;

public class Planning_choice extends AppCompatActivity implements View.OnClickListener{

    private ProgressDialog progress;
    private Button next;
    private String mymail;
    private Spinner structureSpinner;
    private AutoCompleteTextView autocomplete;
    private RadioGroup rg;
    private List<Element> mresult = null;
    private int position;

    private List<String> message;

    public final static String EXTRA_MESSAGE = "key message";

    private final static String url_city = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/get_city_Android";
    private final static String url_museum = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/get_museum";
    private final static String url_opened = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/get_oam";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_choice);

        position = -1;

        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage(" ");

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        mymail = pref.getString("mail", null);

        structureSpinner = (Spinner) findViewById(R.id.spinner1);
        structureSpinner.setPrompt(getString(R.string.planning_choice_structure_selection));

        String[] structures = new String[]{"City","Museum","Opened Air Museum"};
        ArrayAdapter<String> structuresAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, structures);
        structuresAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        structureSpinner.setAdapter(structuresAdapter);
        structureSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        autocomplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autocomplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autocomplete.setThreshold(1);
        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = (String)adapter.getItemAtPosition(pos);
                for (int i = 0; i < mresult.size(); i++) {
                    if (mresult.get(i).getName().equals(selected)) {
                        position = i;
                        break;
                    }

                }
            }

        });

        autocomplete.setVisibility(View.INVISIBLE);
        next = (Button) findViewById(R.id.next1);
        next.setVisibility(View.INVISIBLE);
        next.setOnClickListener(this);
        mresult = null;

        rg = (RadioGroup) findViewById(R.id.radioGroup);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.next1:
                next1();
                break;
        }
    }

    public void next1() {

        if(position == -1)
            for(int i = 0; i < mresult.size(); i++)
                if(autocomplete.getText().toString().equals(mresult.get(i).name)){
                    position = i;
                    break;
                }
        if(position == -1)
            Toast.makeText(getApplicationContext(), "Invalid selection for " + structureSpinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();

           else if (rg.getCheckedRadioButtonId() == -1)
            Toast.makeText(this, "Please select one modality", Toast.LENGTH_LONG).show();
            else if (rg.getCheckedRadioButtonId() == R.id.radioButton_best_time) { //best time planning

            progress.show();

            Intent intent = new Intent(getApplicationContext(), Best_Time_Plan.class);
            ArrayList l = new ArrayList();

            l.add(mymail); // add id-mail user

            //pass structureSpinner selection {city, museum, opened air museum}
            l.add(structureSpinner.getSelectedItem().toString());

            Element c = mresult.get(position);
            //Toast.makeText(this, "position: "+position, Toast.LENGTH_LONG).show();

            l.add(c.name);
            l.add(c.id);


            position = -1;
            intent.putStringArrayListExtra(EXTRA_MESSAGE, l);
            startActivity(intent);


        }


        else if(rg.getCheckedRadioButtonId() == R.id.radioButton_best_rate) { //best rate planning

            Intent intent = new Intent(getApplicationContext(), Best_Rate_Plan.class);
            ArrayList l = new ArrayList();

            l.add(mymail); // add id-mail user

            //pass structureSpinner selection {city, museum, opened air museum}
            l.add(structureSpinner.getSelectedItem().toString());

            Element c = mresult.get(position);

            l.add(c.name);
            l.add(c.id);


            position = -1;
            intent.putStringArrayListExtra(EXTRA_MESSAGE, l);
            startActivity(intent);
        }

            /*
                        else if(rg.getCheckedRadioButtonId() == 3) { //free planning
                            Toast.makeText(this, "selected:" + rg.getCheckedRadioButtonId(), Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(getApplicationContext(), Best_Time_Plan.class);
                            ArrayList l = new ArrayList();

                            l.add(message.get(0)); // add id-mail user

                            //pass structureSpinner selection {city, museum, opened air museum}
                            l.add(structureSpinner.getSelectedItem().toString());
                            //pass spinner2 selection
                            int pos = spinner2.getSelectedItemPosition();
                            City c = mresult.get(pos);
                            l.add(c.name);
                            l.add(c.id);

                            intent.putStringArrayListExtra(EXTRA_MESSAGE, l);
                            startActivity(intent);
                        }

            */



    }



    public void guide(View v){
        // ---SIMPLE ALERT FOR DEBUG---

        new AlertDialog.Builder(Planning_choice.this)
                .setTitle("Guide")
                .setMessage("Best Time Planning: visit as many attractions as possible\n\nBest Rate Planning: visit the attractions with most rating")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // continue with saving
                        //Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();

                    }
                })

                .setIcon(R.drawable.ic_info)
                .show();

        //--- END ----

    }


    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        progress.dismiss();
        //autocomplete.setText("");
        position=-1;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // show menu when menu button is pressed
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_options_menu, menu);
        return true;
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

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            if (structureSpinner.getSelectedItem().toString().equals("City")) {
                //** reset **//
                autocomplete.getText().clear();
                mresult = null;
                position = -1;
                autocomplete.setVisibility(View.INVISIBLE);
                next.setVisibility(View.INVISIBLE);
                // Make GET request
                new CallAPI().execute(url_city, "city");

            }

            if (structureSpinner.getSelectedItem().toString().equals("Museum")) {
                //** reset **//
                autocomplete.getText().clear();
                mresult = null;
                position = -1;
                autocomplete.setVisibility(View.INVISIBLE);
                next.setVisibility(View.INVISIBLE);
                // Make GET request
                new CallAPI().execute(url_museum, "museum");

            }

            if (structureSpinner.getSelectedItem().toString().equals("Opened Air Museum")) {
                //** reset **//
                autocomplete.getText().clear();
                mresult = null;
                position = -1;
                autocomplete.setVisibility(View.INVISIBLE);
                next.setVisibility(View.INVISIBLE);
                // Make GET request
                new CallAPI().execute(url_opened, "oam");

            }


           }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }


    private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {
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
                return e.getMessage();
            }


            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200){
                //Log.d("LOG", "code: " + code);
                if (category.equals("city")) {
                    mresult = new ArrayList<>();


                    // Parse XML -------------------------------------------------------
                    XmlPullParserFactory pullParserFactory;
                    try {
                        pullParserFactory = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = pullParserFactory.newPullParser();

                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(in, null);
                        mresult = parseXML(parser);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // -----------------------------------------------------------------


                } else if (category.equals("museum")) {

                    String jsonResponse = readResponse(in);

                    JSONObject o = null;
                    JSONArray b = null;
                    try {
                        b = new JSONArray(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return code + "(1): ";
                    }

                    int arrSize = b.length();
                    mresult = new ArrayList<Element>(arrSize);

                    for (int i = 0; i < arrSize; ++i) {

                        try {
                            o = b.getJSONObject(i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            return code + "(2): ";
                        }

                        try {
                            mresult.add(new Element(o.getString("name"), o.getString("id")));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            return code + "(3): ";
                        }
                    }


                } else if (category.equals("oam")) {


                    String jsonResponse = readResponse(in);


                    JSONObject o = null;
                    JSONArray b = null;

                    try {
                        b = new JSONArray(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return code + "(1): ";
                    }

                    int arrSize = b.length();
                    mresult = new ArrayList<Element>(arrSize);

                    for (int i = 0; i < arrSize; ++i) {

                        try {
                            o = b.getJSONObject(i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            return code + "(2): ";
                        }

                        try {
                            mresult.add(new Element(o.getString("name"), o.getString("id")));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            return code + "(3): ";
                        }
                    }

                }

            return "ok";
        }else return code+" ";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equals("ok")){
                if (mresult.size()>0){

                List<String> list2 = new ArrayList<String>();
                for (Element c : mresult) {
                    list2.add(c.name);

                }


                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        Planning_choice.this,
                        android.R.layout.simple_dropdown_item_1line,
                        list2
                );
                autocomplete.setAdapter(adapter);



                autocomplete.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        // TODO Auto-generated method stub
                        autocomplete.showDropDown();

                        // autocomplete.requestFocus();
                        return false;
                    }
                });



                autocomplete.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);


                progress.dismiss();

                }else{
                        Toast.makeText(getApplicationContext(), "No items available now.\nPlease try later", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                        return;
                }
            }else{
                Toast.makeText(getApplicationContext(), result+"There is a problem. \nTry later", Toast.LENGTH_LONG).show();
                progress.dismiss();
                return;
            }
        }


        // START OF PARSER SECTION - - - - -
        private List<Element> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

            int eventType = parser.getEventType();
            List<Element> res = new ArrayList<Element>();
            Element c = new Element();
            String id = null;
            String name = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();

                        if (tag.equals("id")) {

                            id = parser.nextText();

                            break;
                        }

                        if (tag.equals("name")) {
                            name = parser.nextText();

                            c = new Element(name, id);
                            res.add(c);

                            id = null;
                            name = null;
                            c = new Element();

                            break;
                        }

                    case XmlPullParser.END_TAG:
                        break;
                } // end switch

                eventType = parser.next();
            } // end while

            return res;
        }


        private String readResponse(InputStream in) {
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


    }  // end CallAPI


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
}
