package it.uniroma1.neptis.planner.planning;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.must_visit;
import it.uniroma1.neptis.planner.util.ConfigReader;
import it.uniroma1.neptis.planner.util.JSONAsyncTask;

public class VisitsFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = VisitsFragment.class.getName();

    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private String attractionURL;

    private ProgressDialog progress;

    private EditText visits;
    private Button increaseVisits;
    private Button decreaseVisits;

    private ArrayList<Attraction> attractionsList;
    private EditText multi_must;
    private EditText multi_exclude;

    private Button next;

    private List<Attraction> attMust;
    private List<Attraction> attExclude;

    private String category;
    private String id;

    protected MainInterface activity;

    public VisitsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        category = getArguments().getString("category");
        attractionURL = ConfigReader.getConfigValue(getContext(), "serverURL");
        switch(category) {
            case "city":
                attractionURL += "/attractionc";
                attractionURL += "?city=" + getArguments().getString("city");
                attractionURL += "&region=" + getArguments().getString("region");
                break;
            case "museum":
                attractionURL += "/museums/attractions/";
                attractionURL += getArguments().getString("id");
        }
        attractionsList = new ArrayList<>();
        attMust = new ArrayList<>();
        attExclude = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visits, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        visits = (EditText)view.findViewById(R.id.number_visits);

        increaseVisits = (Button)view.findViewById(R.id.button_increase_visits);
        increaseVisits.setOnClickListener(this);
        decreaseVisits = (Button)view.findViewById(R.id.button_decrease_visits);
        decreaseVisits.setOnClickListener(this);

        multi_must = (EditText) view.findViewById(R.id.multiAutoCompleteTextView_must_f);
        multi_exclude = (EditText) view.findViewById(R.id.multiAutoCompleteTextView_exclude_f);

        next = (Button)view.findViewById(R.id.btn_next2_f);
        next.setOnClickListener(this);

        progress = new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        progress.setMessage(" ");
        activity.getUser().getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            new GetAttractionsAsyncTask().execute(attractionURL, idToken);
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if(visits.getText().toString().equals(""))
            visits.setText("1");
        int value = Integer.parseInt(visits.getText().toString());

        switch(v.getId()) {
            case R.id.btn_next2_f:
                logEvent = new LogEvent(getActivity().getClass().getName(),"compute plan", "next_button", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                next2();
                break;
            case R.id.button_increase_visits:
                logEvent = new LogEvent(getActivity().getClass().getName(),"increase visits", "button_increase_visits", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                if(value == attractionsList.size())
                    break;
                value++;
                visits.setText(String.valueOf(value));
                break;
            case R.id.button_decrease_visits:
                logEvent = new LogEvent(getActivity().getClass().getName(),"decrease visits", "button_decrease_visits", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                if(visits.getText().toString().equals("0"))
                    break;
                value--;
                visits.setText(String.valueOf(value));
        }
    }

    public void next2() {

        if(!attMust.isEmpty() && !attExclude.isEmpty() && attExclude.containsAll(attMust)) {
            new AlertDialog.Builder(getContext())
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
        } else {
            String numVisits = visits.getText().toString();
            Map<String,String> parameters = new HashMap<>();
            parameters.put("visits",numVisits);

            Map<String,List<Attraction>> extraParams = new HashMap<>();
            extraParams.put("must", attMust);
            extraParams.put("exclude", attExclude);
            activity.computePlan(parameters, extraParams);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainInterface) {
            activity = (MainInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IBestTime");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    protected class GetAttractionsAsyncTask extends JSONAsyncTask {

        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream in;
            int code;
            String urlString = params[0];
            String token = params[1];
            // HTTP GET
            try {
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", token);

                in = new BufferedInputStream(urlConnection.getInputStream());
                code = urlConnection.getResponseCode();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }

            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {
                String jsonResponse = readResponse(in);
                JSONArray attractions = null;
                try {
                    attractions = new JSONArray(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject attraction;
                for (int i = 0; i < attractions.length(); ++i) {
                    try {
                        attraction = attractions.getJSONObject(i);
                        attractionsList.add(Attraction.parse(attraction));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                Log.d(TAG, attractionsList.toString());
                return 200;
            } else return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200) {
                multi_must.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        //multi_must.showDropDown();
                        multi_must.setEnabled(false);
                        Intent i = new Intent(getContext(), must_visit.class);
                        Bundle b = new Bundle();
                        b.putSerializable("list",attractionsList);
                        i.putExtra("list",b);
                        i.putExtra("calling","must");
                        startActivityForResult(i, 1);
                        return false;
                    }
                });

                multi_exclude.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        //   multi_exclude.showDropDown();
                        multi_exclude.setEnabled(false);
                        Intent i = new Intent(getContext(), must_visit.class);
                        Bundle b = new Bundle();
                        b.putSerializable("list",attractionsList);
                        i.putExtra("list",b);
                        i.putExtra("calling","exclude");
                        startActivityForResult(i, 2);
                        return false;
                    }
                });
                progress.dismiss();
            } else
                Toast.makeText(getContext(), result + " There is an error", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    attMust = (ArrayList<Attraction>) data.getBundleExtra("list").getSerializable("list");
                    Log.d("MUST", attMust.toString());
                    multi_must.setText(attMust.toString());

                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    //Write your code if there's no result
                }
                break;
            case 2:
                if(resultCode == Activity.RESULT_OK){
                    attExclude = (ArrayList<Attraction>) data.getBundleExtra("list").getSerializable("list");
                    multi_exclude.setText(attExclude.toString());
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    //Write your code if there's no result
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        multi_must.setEnabled(true);
        multi_exclude.setEnabled(true);
    }

}
