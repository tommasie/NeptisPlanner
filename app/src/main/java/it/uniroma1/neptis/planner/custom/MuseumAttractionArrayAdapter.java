package it.uniroma1.neptis.planner.custom;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.museum.MuseumAttraction;

/**
 * Created by thomas on 03/07/17.
 */

public class MuseumAttractionArrayAdapter extends AttractionArrayAdapter {

    public MuseumAttractionArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Attraction> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MuseumAttraction attraction = (MuseumAttraction) getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.plans_item_3, parent, false);
        }
        TextView attractionName = (TextView)convertView.findViewById(R.id.museum_attraction_name);
        attractionName.setText(attraction.getName());
        TextView areaName = (TextView)convertView.findViewById(R.id.museum_room_name);
        areaName.setText("Room: " + attraction.getArea());
        return convertView;
    }
}