package it.uniroma1.neptis.planner.plans;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.PlansListAdapter;


public class RateAttractionFragment extends Fragment implements View.OnClickListener{
    public final static String EXTRA_MESSAGE = "key message";

    private TextView attractionName;
    private ImageView attractionPicture;
    private RatingBar ratingBar;
    private Button button;

    private String attractionId;
    private String type;

    private PlansFragmentsInterface activity;

    public RateAttractionFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rate_attraction, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attractionId = getArguments().getString("id");
        type = getArguments().getString("type");
        attractionName = (TextView)view.findViewById(R.id.attraction_rating_name);
        attractionName.setText(getArguments().getString("name"));
        attractionPicture = (ImageView)view.findViewById(R.id.attraction_rating_image);
        //Rating bar can get the values {1.0, 2.0, 3.0} (floats)
        ratingBar = (RatingBar)view.findViewById(R.id.attraction_rating_bar);
        button = (Button)view.findViewById(R.id.attraction_rating_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlansFragmentsInterface) {
            activity = (PlansFragmentsInterface) context;
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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.attraction_rating_button) {
            new RatingAsyncTask().execute(type, attractionId, String.valueOf((int)ratingBar.getRating()));
        }
    }

    private class RatingAsyncTask extends AsyncTask<String, String, String> {

        private String report_URL = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/report_rating";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            String id = params[1];
            byte rating = Byte.parseByte(params[2]);
            try {
                URL url = new URL(report_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("type", type);
                json.put("attractionId", id);
                json.put("rating", rating);

                // get current date
                Calendar calendar = Calendar.getInstance();
                java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
                String ts = ourJavaTimestampObject.toString().replace(' ','T');
                Log.d("timestamp: ",ts);
                json.put("data",ts);
                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();
                printout.close();
                //BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
                int code = urlConnection.getResponseCode();
                Log.d("code report",code+"");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getContext(), "Rating saved",Toast.LENGTH_SHORT).show();
            activity.popBackStack();
        }

    }
}
