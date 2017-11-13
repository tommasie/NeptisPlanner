/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.plans;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;

public class CurrentPlanFragment extends Fragment {

    private MainInterface activity;

    public CurrentPlanFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
