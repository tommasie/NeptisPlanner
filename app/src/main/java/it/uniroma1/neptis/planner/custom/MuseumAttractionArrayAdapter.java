/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.custom;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.museum.MuseumAttraction;

public class MuseumAttractionArrayAdapter extends AttractionArrayAdapter {

    public MuseumAttractionArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Attraction> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MuseumAttraction attraction = (MuseumAttraction) getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.plans_item_new, parent, false);
        }
        ImageView attractionImage = convertView.findViewById(R.id.museum_attraction_image);
        TextView attractionName = convertView.findViewById(R.id.plan_n);
        attractionName.setText(attraction.getName());
        TextView areaName = convertView.findViewById(R.id.museum_attraction_description);
        areaName.setText("Room: " + attraction.getArea());
        TextView attractionTime = convertView.findViewById(R.id.museum_attraction_visit_time);
        attractionTime.setText(getContext().getString(R.string.attraction_minutes, 1));

        Button beginVisit = convertView.findViewById(R.id.museum_attraction_begin_timer);
        Button endVisit =  convertView.findViewById(R.id.museum_attraction_end_timer);

        Button rateAttraction = convertView.findViewById(R.id.museum_attraction_rate);
        //TODO il pulsante di rating e di inizio/fine della visita
        return convertView;
    }
}