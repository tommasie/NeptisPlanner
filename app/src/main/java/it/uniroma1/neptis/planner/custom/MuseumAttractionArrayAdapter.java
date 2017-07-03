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
import it.uniroma1.neptis.planner.model.museum.Area;

/**
 * Created by thomas on 03/07/17.
 */

public class MuseumAttractionArrayAdapter extends ArrayAdapter<Attraction> {

    private List<Attraction> attractions;

    public MuseumAttractionArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Attraction> objects) {
        super(context, resource, objects);
        this.attractions = objects;
    }

    @Override
    public int getCount() {
        return attractions.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Attraction attraction = getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.plans_list_item, parent, false);
        }
        TextView attractionName = (TextView)convertView.findViewById(R.id.textest);
        attractionName.setText("visit " + attraction.getName());
        return convertView;
    }
}