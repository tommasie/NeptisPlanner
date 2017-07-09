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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.model.Attraction;

/**
 * Created by thomas on 09/07/17.
 */

public class PlansListAdapter extends ArrayAdapter<String> {

    private List<String> fileNames;

    public PlansListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.fileNames = objects;
    }

    @Override
    public int getCount() {
        return fileNames.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String fileName = getItem(position);
        TextView name;
        TextView date;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.plans_list_item2, parent, false);
        }
        name = (TextView) convertView.findViewById(R.id.plan_name);
        date = (TextView) convertView.findViewById(R.id.plan_date);
        name.setText(fileName.split("_")[0]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(fileName.split("_")[1]));
        String d = DateFormat.getDateTimeInstance().format(calendar.getTime());
        date.setText(d);
        return convertView;
    }
}
