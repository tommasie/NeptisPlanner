/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.plans;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.asynctasks.DownloadImageAsyncTask;
import it.uniroma1.neptis.planner.asynctasks.JSONAsyncTask;
import it.uniroma1.neptis.planner.asynctasks.ReportAsyncTask;
import it.uniroma1.neptis.planner.firebase.FirebaseOnCompleteListener;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Plan;
import it.uniroma1.neptis.planner.services.tracking.MuseumVisitService;
import it.uniroma1.neptis.planner.util.LocalStorage;

public abstract class SelectedPlanFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = SelectedPlanFragment.class.getName();

    protected TextView title;
    protected TextView totalDuration;
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected Button startButton;
    protected String planFileName;

    protected String planString;
    protected Plan plan;

    protected MainInterface activity;

    protected int visitTime = 0;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("visitTime")) {
                visitTime = intent.getIntExtra("visitTime",0);
            }
        }
    };

    public SelectedPlanFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        planFileName = getArguments().getString("computed_plan_file");
        planString = LocalStorage.readFile(getContext(), planFileName);
        plan = Plan.parse(planString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.tv_area_name);
        title.setText(plan.getName());
        totalDuration = view.findViewById(R.id.tv_duration);
        int time = 0;
        for(Attraction a : plan.getAttractions())
            time += a.getTime();
        totalDuration.setText(getString(R.string.attraction_minutes, time));
        recyclerView = view.findViewById(R.id.recycler_view_nas);
        setAdapter();

        startButton = view.findViewById(R.id.tour_start);
        startButton.setOnClickListener(this);
    }

    protected abstract void setAdapter();

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(receiver, new IntentFilter("visitTime"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tour_start) {
            Log.d(TAG, "click");
            /*Intent queueService = new Intent(getContext(), QueueRecognitionService.class);
            queueService.putExtra("attractions", plan.getAttractions());
            queueService.putExtra("index",index);
            getActivity().startService(queueService);*/
            startTour();
        }
    }

    protected abstract void startTour();

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

    public abstract class AttractionRecyclerAdapter extends RecyclerView.Adapter<AttractionRecyclerAdapter.AttractionHolder> {

        private List<Attraction> mDataset;
        private String category;

        public AttractionRecyclerAdapter(List<Attraction> attractions, String category) {
            mDataset = attractions;
            Log.d("category",category);
            this.category = category;
        }

        @Override
        public void onBindViewHolder(AttractionHolder holder, int position) {
            holder.bind(mDataset.get(position));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public abstract class AttractionHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            private TextView attractionName;
            protected TextView attractionDescription;
            private TextView attractionTime;
            private ImageView attractionImage;
            private Attraction curr;
            protected TextView visitLabel;
            protected Button start;
            protected Button stop;
            protected Button rate;

            protected AttractionHolder(View v) {
                super(v);
                this.attractionName = v.findViewById(R.id.plan_n);
                this.attractionDescription = v.findViewById(R.id.museum_attraction_description);
                this.attractionTime = v.findViewById(R.id.attraction_time_short);
                this.attractionImage = v.findViewById(R.id.museum_attraction_image);
                this.visitLabel = v.findViewById(R.id.visit_label);
                this.start = v.findViewById(R.id.museum_attraction_begin_timer);
                this.stop = v.findViewById(R.id.museum_attraction_end_timer);
                this.stop.setEnabled(false);
                enableButtons();
                this.start.setOnClickListener(this);
                this.stop.setOnClickListener(this);
                this.rate = v.findViewById(R.id.museum_attraction_rate);
                this.rate.setOnClickListener(this);
            }

            protected abstract void enableButtons();

            private void bind(Attraction attraction) {
                curr = attraction;
                this.attractionName.setText(attraction.getName());
                setSpecificData(attraction);
                this.attractionTime.setText(getString(R.string.attraction_minutes_shortened, attraction.getTime()));
                new DownloadImageAsyncTask(this.attractionImage).execute(attraction.getImageURL());
            }

            protected abstract void setSpecificData(Attraction attraction);

            @Override
            public void onClick(View v) {
                Intent intent;
                switch(v.getId()) {
                    case R.id.museum_attraction_begin_timer:
                        intent = new Intent(getContext(), MuseumVisitService.class);
                        intent.putExtra("attractionName", curr.getName());
                        //intent.putExtra("attractionIndex", curr.)
                        getActivity().startService(intent);
                        this.start.setEnabled(false);
                        this.stop.setEnabled(true);
                        break;
                    case R.id.museum_attraction_end_timer:
                        intent = new Intent(getContext(), MuseumVisitService.class);
                        getActivity().stopService(intent);
                        JSONAsyncTask task = new ReportAsyncTask(getContext());
                        activity.getUser().getIdToken(true)
                                .addOnCompleteListener(new FirebaseOnCompleteListener(task, "visit", "museum", curr.getId(), String.valueOf(visitTime)));
                        this.stop.setEnabled(false);
                        break;
                    case R.id.museum_attraction_rate:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("attraction",curr);
                        bundle.putString("type", category);
                        activity.attractionDetail(bundle);
                        break;
                }
            }
        }

    }
}
