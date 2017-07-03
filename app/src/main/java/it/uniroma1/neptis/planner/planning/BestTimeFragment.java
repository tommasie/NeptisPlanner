package it.uniroma1.neptis.planner.planning;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.must_visit;

public class BestTimeFragment extends Fragment implements View.OnClickListener{

    public static final int ACTIVITY_1 = 1001;

    private final static String attraction_c_URL = "http://"+ LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/get_attraction";

    private ProgressDialog progress;

    protected TextView title;
    private NumberPicker hours;
    private NumberPicker minutes;

    private EditText multi_must;
    private EditText multi_exclude;

    private Button next;

    private List<String> lmust;
    private List<String> lexclude;

    private List<String> areaList;

    private String category;
    private String id;

    protected PlanningFragmentsInterface activity;

    public BestTimeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        category = getArguments().getString("category");
        id = getArguments().getString("id");
        lmust = new ArrayList<>();
        lexclude = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_best_time, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = (TextView)view.findViewById(R.id.textView1_f);
        title.setText("Best Time Planning");

        hours = (NumberPicker) view.findViewById(R.id.hh_picker_f);
        hours.setMaxValue(24);
        hours.setMinValue(0);
        hours.setValue(2);
        hours.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        minutes = (NumberPicker) view.findViewById(R.id.min_picker_f);
        minutes.setMaxValue(60);
        minutes.setMinValue(0);
        minutes.setValue(30);
        minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        multi_must = (EditText) view.findViewById(R.id.multiAutoCompleteTextView_must_f);
        multi_exclude = (EditText) view.findViewById(R.id.multiAutoCompleteTextView_exclude_f);

        next = (Button)view.findViewById(R.id.btn_next2_f);
        next.setOnClickListener(this);

        progress = new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        progress.setMessage(" ");

        new GetAreasAsyncTask().execute(attraction_c_URL);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_next2_f)
            next2();
    }

    public void next2() {

        if(!lmust.isEmpty() && !lexclude.isEmpty() && lexclude.containsAll(lmust)) {

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
            String hh = String.valueOf(hours.getValue());
            String mm = String.valueOf(minutes.getValue());

            Map<String,String> parameters = new HashMap<>();
            parameters.put("hh",hh);
            parameters.put("mm",mm);

            Bundle b = new Bundle();
            //FIXME remember best rate planning
            b.putInt("calling-activity", ACTIVITY_1);

            Map<String,List<String>> extraParams = new HashMap<>();
            extraParams.put(PlanningActivity.MUST, lmust);
            extraParams.put(PlanningActivity.EXCLUDE, lexclude);
            //activity.computePlan(b, parameters, extraParams);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlanningFragmentsInterface) {
            activity = (PlanningFragmentsInterface) context;
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

    protected class GetAreasAsyncTask extends JSONAsyncTask {

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
                //TODO change to GET
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

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }

            //****  CHECK the ResponseCode FIRST! ****
            if (code == 200) {
                    String jsonResponse = readResponse(in);
                    JSONObject area = null;
                    JSONArray areas = null;
                    try {
                        areas = new JSONArray(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int arrSize = areas.length();
                    areaList = new ArrayList<>(arrSize);

                    for (int i = 0; i < arrSize; ++i) {

                        try {
                            area = areas.getJSONObject(i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        try {
                            areaList.add(area.getString("name"));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
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
                        i.putStringArrayListExtra(PlanningActivity.EXTRA_MESSAGE, (ArrayList<String>) areaList);
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
                        Intent i = new Intent(getContext(), must_visit.class);
                        i.putStringArrayListExtra(PlanningActivity.EXTRA_MESSAGE, (ArrayList<String>) areaList);
                        i.putExtra("calling","exclude");
                        // must_visit.title_t10.setText("What you don't want to  visit");
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

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                lmust = data.getStringArrayListExtra("result-must");
                multi_must.setText(lmust.toString());

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                lexclude = data.getStringArrayListExtra("result-exclude");
                multi_exclude.setText(lexclude.toString());
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
