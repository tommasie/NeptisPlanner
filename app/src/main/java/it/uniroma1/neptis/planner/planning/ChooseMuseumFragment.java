/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.planning;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Element;
import it.uniroma1.neptis.planner.util.ConfigReader;
import it.uniroma1.neptis.planner.util.JSONAsyncTask;

public class ChooseMuseumFragment extends Fragment implements View.OnClickListener{

    private String museumUrl;
    private String city, region;
    private List<Element> museumQuery;
    private List<Element> filteredList;
    private int position;
    private View prevView;
    private MainInterface activity;

    private EditText nameSearch;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MuseumRecyclerAdapter adapter;
    private Button next;
    private ProgressDialog progress;

    public ChooseMuseumFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getArguments().getString("city");
        region = getArguments().getString("region");
        position = -1;
        prevView = null;
        museumQuery = new ArrayList<>();
        filteredList = new ArrayList<>();
        this.museumUrl = ConfigReader.getConfigValue(getContext(), "serverURL") + "/museums";
        this.museumUrl += "?city=" + city + "&region=" + region;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_museum, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        //progress.setMessage(" ");

        recyclerView = view.findViewById(R.id.choose_museum_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new MuseumRecyclerAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        activity.getUser().getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            new MuseumListASyncTask().execute(museumUrl ,idToken);
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });

        nameSearch = view.findViewById(R.id.choose_museum_filter);
        nameSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        next = view.findViewById(R.id.choose_museum_next);
        next.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        Element selected = filteredList.get(position);
        Map<String, String> planningParameters = new HashMap<>();
        planningParameters.put("id", selected.getId());
        planningParameters.put("name", selected.getName());
        activity.selectVisits(planningParameters);
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
            int code = 0;

            String urlString = params[0]; // URL to call
            String token = params[1];
            Log.d(TAG,token);
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
                Log.e(TAG, ""+code);
                return -1;
            }
            if (code == 200) {
                String jsonResponse = readResponse(in);
                JSONObject item;
                JSONArray items;
                try {
                    items = new JSONArray(jsonResponse);
                    for (int i = 0; i < items.length(); ++i) {
                        item = items.getJSONObject(i);
                        museumQuery.add(new Element(item.getString("name"), item.getString("id")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return 400;
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200) {
                if (!museumQuery.isEmpty()){
                    Collections.sort(museumQuery);
                    filteredList.addAll(museumQuery);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No items available now.\nPlease try later", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(getContext(), result+"There is a problem. \nTry later", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class MuseumRecyclerAdapter extends RecyclerView.Adapter<MuseumRecyclerAdapter.MuseumHolder> {

        private List<Element> museums;
        private CustomFilter filter;

        private MuseumRecyclerAdapter(List<Element> museums) {
            this.museums = museums;
            this.filter = new CustomFilter(MuseumRecyclerAdapter.this);
        }

        @Override
        public MuseumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.museums_list_item, parent, false);
            return new MuseumHolder(v);
        }

        @Override
        public void onBindViewHolder(MuseumHolder holder, int position) {
            holder.getTextView().setText(museums.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return museums.size();
        }

        public Filter getFilter() {
            return this.filter;
        }

        protected class MuseumHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private TextView museumName;

            private MuseumHolder(View v) {
                super(v);
                this.museumName = v.findViewById(R.id.museum_name);
                v.setOnClickListener(MuseumHolder.this);
            }

            private TextView getTextView() {
                return this.museumName;
            }

            @Override
            public void onClick(View v) {
                if(prevView != null) {
                    prevView.setBackgroundColor(Color.WHITE);
                }
                v.setBackgroundColor(Color.LTGRAY);
                prevView = v;
                position = getLayoutPosition();
                next.setEnabled(true);
                next.setAlpha(1.0f);
            }
        }

        private class CustomFilter extends Filter {

            private MuseumRecyclerAdapter adapter;
            private CustomFilter(MuseumRecyclerAdapter adapter) {
                super();
                this.adapter = adapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filteredList.clear();
                final FilterResults results = new FilterResults();
                if (constraint.length() == 0) {
                    filteredList.addAll(museumQuery);
                } else {
                    final String filterPattern = constraint.toString().toLowerCase().trim();
                    for (final Element mWords : museumQuery) {
                        if (mWords.getName().toLowerCase().startsWith(filterPattern)) {
                            filteredList.add(mWords);
                        }
                    }
                }
                System.out.println("Count Number " + filteredList.size());
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                this.adapter.notifyDataSetChanged();
            }
        }
    }
}
