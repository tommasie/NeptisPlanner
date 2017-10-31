package it.uniroma1.neptis.planner.planning;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.util.ConfigReader;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;

public class ChoiceFragment extends Fragment implements View.OnClickListener{

    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private String url_museum;

    private ProgressDialog progress;
    private RadioGroup tourSelect;
    private Button nextButton;
    private AutoCompleteTextView autocomplete;
    private List<Element> museumQuery;
    private int position;

    private String category;

    private TextView nameView;

    private TextView selectMuseumTextView;

    private MainInterface activity;
    private String city;
    private String region;
    public ChoiceFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getArguments().getString("city");
        region = getArguments().getString("region");
        position = -1;
        museumQuery = null;

        this.url_museum = ConfigReader.getConfigValue(getContext(), "serverURL") + "/museums";
        this.url_museum += "?city=" + city + "&region=" + region;
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

        nameView = (TextView)view.findViewById(R.id.cityName);
        nameView.setText(city + ", " + region);

        autocomplete = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView_f);
        //autocomplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autocomplete.setThreshold(1);
        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                logEvent = new LogEvent(getActivity().getClass().getName(),"select attraction", "attraction_autocomplete", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                nextButton.setEnabled(true);
                nextButton.setAlpha(1.0f);
                String selected = (String)adapter.getItemAtPosition(pos);
                for (int i = 0; i < museumQuery.size(); i++) {
                    if (museumQuery.get(i).getName().equals(selected)) {
                        position = i;
                        break;
                    }
                }
            }

        });
        autocomplete.setVisibility(View.INVISIBLE);

        nextButton = (Button) view.findViewById(R.id.next_f);
        nextButton.setAlpha(.5f);
        nextButton.setOnClickListener(this);

        tourSelect = (RadioGroup)view.findViewById(R.id.tourRadioGroup);
        tourSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch(checkedId) {
                    case R.id.openTourRadio:
                        selectMuseumTextView.setVisibility(View.INVISIBLE);
                        autocomplete.setVisibility(View.INVISIBLE);
                        nextButton.setEnabled(true);
                        nextButton.setAlpha(1.0f);
                        category = "city";
                        break;
                    case R.id.museumTourRadio:
                        category = "museum";
                        activity.getUser().getIdToken(true)
                                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                                        if (task.isSuccessful()) {
                                            String idToken = task.getResult().getToken();
                                            new MuseumListASyncTask().execute(url_museum ,idToken);
                                        } else {
                                            // Handle error -> task.getException();
                                        }
                                    }
                                });

                        break;
                }
            }
        });

        selectMuseumTextView = (TextView)view.findViewById(R.id.textView_desc2);
        selectMuseumTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.next_f:
                logEvent = new LogEvent(getActivity().getClass().getName(),"next step", "next_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                next();
                break;
            case R.id.spinner_f:
                logEvent = new LogEvent(getActivity().getClass().getName(),"select structure", "structure_spinner", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                break;
        }
    }

    public void next() {
        Map<String,String> planningParameters = new HashMap<>();
        planningParameters.put("category", category);
        switch(category) {
            case "city":
                planningParameters.put("city", city);
                planningParameters.put("name", city);
                planningParameters.put("region", region);
                break;
            case "museum":
                Element c = museumQuery.get(position);
                planningParameters.put("museum",c.name);
                planningParameters.put("name",c.name);
                planningParameters.put("id",c.id);
                break;
        }
        activity.requestTime(planningParameters);
}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainInterface) {
            activity = (MainInterface) context;
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

    private class MuseumListASyncTask extends JSONAsyncTask {

        private final String TAG = MuseumListASyncTask.class.getName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in;
            int code;

            String urlString = params[0]; // URL to call
            String token = params[1];
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
                return -1;
            }
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
                museumQuery = new ArrayList<>(arrSize);

                for (int i = 0; i < arrSize; ++i) {
                    try {
                        item = items.getJSONObject(i);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return 400;
                    }

                    try {
                        museumQuery.add(new Element(item.getString("name"), item.getString("id")));
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return 400;
                    }
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200) {
                if (!museumQuery.isEmpty()){
                    List<String> autocompleteList = new ArrayList<>();
                    for (Element e : museumQuery) {
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
                    selectMuseumTextView.setVisibility(View.VISIBLE);
                    autocomplete.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "No items available now.\nPlease try later", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(getContext(), result+"There is a problem. \nTry later", Toast.LENGTH_LONG).show();
            }
        }
    }



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