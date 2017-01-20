package it.uniroma1.neptis.planner.planning;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;

public class ChoiceFragment extends Fragment implements View.OnClickListener{

    public final static String EXTRA_MESSAGE = "key message";
    private final static String url_city = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/get_city";
    private final static String url_museum = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/get_museum";
    private final static String url_opened = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/get_oam";

    private ProgressDialog progress;
    private Button nextButton;
    private String mymail;
    private Spinner structureSpinner;
    private AutoCompleteTextView autocomplete;
    private RadioGroup rg;
    private List<Element> queryResults;
    private int position;

    private PlanningFragments activity;

    public ChoiceFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = -1;
        SharedPreferences pref = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        mymail = pref.getString("mail", null);
        queryResults = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choice, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        progress.setMessage(" ");

        structureSpinner = (Spinner) view.findViewById(R.id.spinner_f);
        structureSpinner.setPrompt(getString(R.string.planning_choice_structure_selection));
        //TODO i18n
        String[] structures = new String[]{"City","Museum","Opened Air Museum"};
        ArrayAdapter<String> structuresAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, structures);
        structuresAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        structureSpinner.setAdapter(structuresAdapter);
        structureSpinner.setOnItemSelectedListener(new CategorySpinnerListener());

        autocomplete = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView_f);
        autocomplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autocomplete.setThreshold(1);
        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = (String)adapter.getItemAtPosition(pos);
                for (int i = 0; i < queryResults.size(); i++) {
                    if (queryResults.get(i).getName().equals(selected)) {
                        position = i;
                        break;
                    }
                }
            }

        });
        autocomplete.setVisibility(View.INVISIBLE);

        nextButton = (Button) view.findViewById(R.id.next_f);
        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setOnClickListener(this);

        rg = (RadioGroup) view.findViewById(R.id.radioGroup_f);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.next_f:
                next();
                break;
        }
    }

    public void next() {
        if(position == -1)
            for(int i = 0; i < queryResults.size(); i++)
                //TODO can it be brought to O(1) rather than O(n)?
                //HashMap rather than list?
                if(autocomplete.getText().toString().equals(queryResults.get(i).name)){
                    position = i;
                    break;
                }
        if(position == -1)
            Toast.makeText(getContext(), "Invalid selection for " + structureSpinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();

        else if (rg.getCheckedRadioButtonId() == -1)
            Toast.makeText(getContext(), "Please select one modality", Toast.LENGTH_LONG).show();
        else if (rg.getCheckedRadioButtonId() == R.id.radioButton_best_time_f) { //best time planning

            //TODO perhaps set variables in the hosting activity rather than sending bundles of data between fragments?
            ArrayList<String> l = new ArrayList<>();
            l.add(mymail); // add id-mail user

            //pass structureSpinner selection {city, museum, opened air museum}
            l.add(structureSpinner.getSelectedItem().toString());
            Element c = queryResults.get(position);
            l.add(c.name);
            l.add(c.id);

            position = -1;
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(EXTRA_MESSAGE,l);
            activity.requestTime(bundle);
        }


        else if(rg.getCheckedRadioButtonId() == R.id.radioButton_best_rate_f) { //best rate planning
            //TODO best rate implementation
            Intent intent = new Intent(getContext(), Best_Rate_Plan.class);
            ArrayList<String> l = new ArrayList<>();

            l.add(mymail); // add id-mail user

            //pass structureSpinner selection {city, museum, opened air museum}
            l.add(structureSpinner.getSelectedItem().toString());

            Element c = queryResults.get(position);

            l.add(c.name);
            l.add(c.id);


            position = -1;
            intent.putStringArrayListExtra(EXTRA_MESSAGE, l);
            startActivity(intent);
        }

    }

    public void guide(View v){
        new AlertDialog.Builder(getContext())
                .setTitle("Guide")
                .setMessage("Best Time Planning: visit as many attractions as possible\n\nBest Rate Planning: visit the attractions with best rating")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.ic_info)
                .show();
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

    private class CategorySpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            String url = null;
            queryResults = null;
            position = -1;
            autocomplete.getText().clear();
            autocomplete.setVisibility(View.INVISIBLE);
            nextButton.setVisibility(View.INVISIBLE);

            switch(structureSpinner.getSelectedItem().toString()) {
                case "City":
                    url = url_city;
                    break;
                case "Museum":
                    url = url_museum;
                    break;
                case "Opened Air Museum":
                    url = url_opened;
                    break;
            }
            new SpinnerAsyncTask().execute(url);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class SpinnerAsyncTask extends JSONAsyncTask {
        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in;
            int code;

            String urlString = params[0]; // URL to call

            // HTTP get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //get the response
                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }
            //TODO Fix error codes
            if (code == 200) {
                String jsonResponse = readResponse(in);
                JSONObject item;
                JSONArray items;
                try {
                    items = new JSONArray(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return 400;
                }
                int arrSize = items.length();
                queryResults = new ArrayList<>(arrSize);

                for (int i = 0; i < arrSize; ++i) {
                    try {
                        item = items.getJSONObject(i);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        return 400;
                    }

                    try {
                        queryResults.add(new Element(item.getString("name"), item.getString("id")));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        return 400;
                    }
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200) {
                if (!queryResults.isEmpty()){
                    List<String> autocompleteList = new ArrayList<>();
                    for (Element e : queryResults) {
                        autocompleteList.add(e.name);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            autocompleteList
                    );
                    autocomplete.setAdapter(adapter);
                    autocomplete.setOnTouchListener(new View.OnTouchListener() {

                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                            autocomplete.showDropDown();
                            return false;
                        }
                    });
                    autocomplete.setVisibility(View.VISIBLE);

                    nextButton.setVisibility(View.VISIBLE);
                    progress.dismiss();
                } else {
                    progress.dismiss();
                    Toast.makeText(getContext(), "No items available now.\nPlease try later", Toast.LENGTH_LONG).show();
                    return;
                }
            }else {
                progress.dismiss();
                Toast.makeText(getContext(), result+"There is a problem. \nTry later", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }  // end SpinnerAsyncTask

    private class Element {
        String name;
        String id;

        Element(String n, String id) {
            this.name = n;
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}