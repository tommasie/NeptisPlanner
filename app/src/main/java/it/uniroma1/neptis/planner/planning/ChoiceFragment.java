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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;

public class ChoiceFragment extends Fragment implements View.OnClickListener{

    private final static String url_city = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/cities";
    private final static String url_museum = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/museums";

    private ProgressDialog progress;
    private Button nextButton;
    private String mymail;
    private Spinner structureSpinner;
    private AutoCompleteTextView autocomplete;
    private List<Element> queryResults;
    private int position;

    private TextView text3;

    private PlanningFragmentsInterface activity;

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
        String[] structures = new String[]{getString(R.string.city),getString(R.string.museum)};
        ArrayAdapter<String> structuresAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, structures);
        structuresAdapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        structureSpinner.setAdapter(structuresAdapter);
        structureSpinner.setOnItemSelectedListener(new CategorySpinnerListener());

        text3 = (TextView)view.findViewById(R.id.textView_desc2);
        text3.setVisibility(View.INVISIBLE);

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

        else {
            Map<String,String> planningParameters = new HashMap<>();
            //pass structureSpinner selection {city, museum, opened air museum}
            int categoryIndex = structureSpinner.getSelectedItemPosition();
            String category;
            if(categoryIndex == 0)
                category = "City";
            else category = "Museum";
            planningParameters.put("category", category.toLowerCase());
            Element c = queryResults.get(position);
            planningParameters.put("type",c.name);
            planningParameters.put("id",c.id);
            position = -1;
            activity.requestTime(planningParameters);
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
        if (context instanceof PlanningFragmentsInterface) {
            activity = (PlanningFragmentsInterface) context;
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

            String selected = structureSpinner.getSelectedItem().toString();
            text3.setText(getString(R.string.fragment_choice_text3) + selected.toLowerCase() + ":");
            text3.setVisibility(View.VISIBLE);

            if(selected.equals(getString(R.string.city)))
                url = url_city;
            else url = url_museum;

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