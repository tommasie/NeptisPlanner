/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.planning;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;

public class ChoiceFragment extends Fragment implements View.OnClickListener{

    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private ProgressDialog progress;
    private RadioGroup tourSelect;
    private Button nextButton;
    private String category;

    private TextView nameView;

    private MainInterface activity;
    private String city;
    private String region;
    public ChoiceFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = getArguments().getString("city");
        region = getArguments().getString("region");
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

        nameView = view.findViewById(R.id.cityName);
        nameView.setText(city + ", " + region);

        nextButton = view.findViewById(R.id.next_f);
        nextButton.setAlpha(.5f);
        nextButton.setOnClickListener(this);

        tourSelect = view.findViewById(R.id.tourRadioGroup);
        tourSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch(checkedId) {
                    case R.id.openTourRadio:
                        nextButton.setEnabled(true);
                        nextButton.setAlpha(1.0f);
                        category = "city";
                        break;
                    case R.id.museumTourRadio:
                        category = "museum";
                        nextButton.setEnabled(true);
                        nextButton.setAlpha(1.0f);
                        break;
                }
            }
        });
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
        Map<String,String> planningParameters = new HashMap<>();
        planningParameters.put("category", category);
        switch(category) {
            case "city":
                planningParameters.put("city", city);
                planningParameters.put("name", city);
                planningParameters.put("region", region);
                activity.selectVisits(planningParameters);
                break;
            case "museum":
                planningParameters.put("city", city);
                planningParameters.put("region", region);
                activity.selectMuseum(planningParameters);
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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }
}