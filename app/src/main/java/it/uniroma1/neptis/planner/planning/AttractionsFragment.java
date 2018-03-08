/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.planning;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.Home;
import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.asynctasks.ComputePlanAsyncTask;
import it.uniroma1.neptis.planner.asynctasks.JSONAsyncTask;
import it.uniroma1.neptis.planner.firebase.FirebaseOnCompleteListener;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Request;

public class AttractionsFragment extends Fragment implements View.OnClickListener{

    private List<Attraction> attractions;
    private List<String> attractionsString;
    private int selectedPosition = -1;
    private List<Attraction> include;
    private List<Attraction> exclude;

    //Input variables
    private String category, id;
    private Request request;
    private AutoCompleteTextView autocomplete;
    private Button includeButton;
    private RecyclerView includeRecycler;
    private RecyclerView.Adapter includeAdapter;
    private Button excludeButton;
    private RecyclerView excludeRecycler;
    private RecyclerView.Adapter excludeAdapter;
    private Button next;

    private View prevView = null;
    private int position;

    private String computeURL;

    private MainInterface activity;

    public AttractionsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attractions = Home.attractionsList;
        attractionsString = new ArrayList<>();
        for(Attraction a : attractions) {
            attractionsString.add(a.getName());
        }

        request = (Request)getArguments().getSerializable("request");
        include = new ArrayList<>();
        exclude = new ArrayList<>();
        computeURL = Home.apiURL + "/compute-plan-" + request.getRequestParameters().get("category");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attractions, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        autocomplete = view.findViewById(R.id.filter_attractions_autocomplete);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line, attractionsString);
        autocomplete.setAdapter(adapter);
        autocomplete.setThreshold(1);
        includeButton = view.findViewById(R.id.filter_attractions_add_include);
        includeButton.setOnClickListener(this);
        includeRecycler = view.findViewById(R.id.filter_attractions_include_recycler);
        includeAdapter = new AttractionsAdapter(include);
        includeRecycler.setAdapter(includeAdapter);
        excludeButton = view.findViewById(R.id.filter_attractions_add_exclude);
        excludeButton.setOnClickListener(this);
        excludeRecycler = view.findViewById(R.id.filter_attractions_exclude_recycler);
        excludeAdapter = new AttractionsAdapter(exclude);
        excludeRecycler.setAdapter(excludeAdapter);
        next = view.findViewById(R.id.filter_attractions_next);
        next.setOnClickListener(this);

        autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                includeButton.setEnabled(true);
                excludeButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        Attraction selected;
        switch(v.getId()) {
            case R.id.filter_attractions_add_include:
                selected = attractions.get(selectedPosition);
                if(!exclude.contains(selected)) {
                    include.add(selected);
                    includeAdapter.notifyDataSetChanged();
                    autocomplete.setText("");
                }
                else {
                    Toast.makeText(getContext(), "Attrazione esclusa", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.filter_attractions_add_exclude:
                selected = attractions.get(selectedPosition);
                if(!include.contains(selected)) {
                    exclude.add(selected);
                    excludeAdapter.notifyDataSetChanged();
                    autocomplete.setText("");
                }
                else {
                    Toast.makeText(getContext(), "Attrazione inclusa", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.filter_attractions_next:
                request.setMustVisit(include);
                request.setExcludeVisit(exclude);
                JSONAsyncTask t = new ComputePlanAsyncTask(activity, getContext(), request, activity.getLocation());
                activity.getUser().getIdToken(true)
                        .addOnCompleteListener(new FirebaseOnCompleteListener(t, computeURL));
                break;
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

    private class AttractionsAdapter extends RecyclerView.Adapter<AttractionsAdapter.AttractionHolder> {

        private List<Attraction> attractions;

        private AttractionsAdapter(List<Attraction> attractions) {
            this.attractions = attractions;
        }

        @Override
        public AttractionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.museums_list_item, parent, false);
            return new AttractionHolder(v);
        }

        @Override
        public void onBindViewHolder(AttractionHolder holder, int position) {
            holder.getTextView().setText(attractions.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return attractions.size();
        }


        protected class AttractionHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

            private TextView museumName;

            private AttractionHolder(View v) {
                super(v);
                this.museumName = v.findViewById(R.id.museum_name);
                v.setOnLongClickListener(AttractionHolder.this);
            }

            private TextView getTextView() {
                return this.museumName;
            }

            @Override
            public boolean onLongClick(View v) {
                if(prevView != null) {
                    prevView.setBackgroundColor(Color.WHITE);
                }
                v.setBackgroundColor(Color.LTGRAY);
                prevView = v;
                position = getLayoutPosition();
                return true;
            }
        }
    }
}