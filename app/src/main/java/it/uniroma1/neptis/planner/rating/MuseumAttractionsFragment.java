package it.uniroma1.neptis.planner.rating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.AttractionArrayAdapter;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.util.JSONAsyncTask;
import it.uniroma1.neptis.planner.util.ConfigReader;

public class MuseumAttractionsFragment extends Fragment {
    private static final String TAG = MuseumAttractionsFragment.class.getName();
    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private String apiURL;
    private String url_museum;

    private AutoCompleteTextView autocomplete;

    //    private TextView title;
    private ListView listView;
    private AttractionArrayAdapter adapter;

    private int listPosition;
    private ArrayList<Attraction> attractionsList;

    private MainInterface activity;
    FirebaseUser user;
    public MuseumAttractionsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.url_museum = ConfigReader.getConfigValue(getContext(), "serverURL") + "/museums";
        this.url_museum += "?city=" + getArguments().getString("city") + "&region=" + getArguments().getString("region");

        this.apiURL = ConfigReader.getConfigValue(getContext(), "serverURL");
        apiURL += "/museums/attractions/";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_museum_attractions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        autocomplete = (AutoCompleteTextView)view.findViewById(R.id.fragment_museum_attractions_autocomplete);
        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Element e = (Element) parent.getAdapter().getItem(position);
                String museumId = e.id;
                apiURL += museumId;
                activity.getUser().getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    String idToken = task.getResult().getToken();
                                    new GetAttractionsAsyncTask().execute(apiURL, idToken);
                                } else {
                                    // Handle error -> task.getException();
                                }
                            }
                        });

            }
        });
        user = activity.getUser();
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            new GetAttractionsAsyncTask().execute(apiURL, idToken);
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
        listView = (ListView)view.findViewById(R.id.listView_museum_attractions);
        attractionsList = new ArrayList<>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Attraction attraction = (Attraction)parent.getItemAtPosition(position);
                Bundle b = new Bundle();
                b.putString("id", attraction.getId());
                b.putString("name", attraction.getName());
                b.putString("type", "museum");
                activity.attractionDetail(b);
            }

        });

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


    protected class GetAttractionsAsyncTask extends JSONAsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                adapter = new AttractionArrayAdapter(getContext(), R.layout.plans_list_item2, attractionsList);
                listView.setAdapter(adapter);
            } else
                Toast.makeText(getContext(), result + " There is an error", Toast.LENGTH_LONG).show();

        }
    }

    private class MuseumListASyncTask extends JSONAsyncTask {

        private final String TAG = MuseumListASyncTask.class.getName();
        private List<Element> museumQuery;

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
                ArrayAdapter<Element> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        museumQuery
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
                //autocomplete.setVisibility(View.VISIBLE);
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

        public String toString() {
            return this.name;
        }
    }
}
