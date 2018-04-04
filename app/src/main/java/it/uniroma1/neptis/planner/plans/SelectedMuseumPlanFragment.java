/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.plans;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.museum.MuseumAttraction;

public class SelectedMuseumPlanFragment extends SelectedPlanFragment {


    @Override
    protected void setAdapter() {
        mAdapter = new MuseumAttractionRecyclerAdapter(plan.getAttractions());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void startTour() {

    }

    public class MuseumAttractionRecyclerAdapter extends SelectedPlanFragment.AttractionRecyclerAdapter {

        public MuseumAttractionRecyclerAdapter(List<Attraction> attractions) {
            super(attractions, "museum");
        }

        @Override
        public MuseumAttractionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selected_plan_item, parent, false);
            MuseumAttractionHolder vh;
            vh = new MuseumAttractionHolder(v);
            return vh;
        }

        public class MuseumAttractionHolder extends SelectedPlanFragment.AttractionRecyclerAdapter.AttractionHolder{

            private MuseumAttractionHolder(View v) {
                super(v);
            }

            @Override
            protected void enableButtons() {

            }

            @Override
            protected void setSpecificData(Attraction attraction) {
                attractionDescription.setText(((MuseumAttraction)attraction).getArea());
            }

        }

    }
}
