package it.uniroma1.neptis.planner.plans;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.planning.PlanningFragmentsInterface;
import it.uniroma1.neptis.planner.services.tracking.GeofencingService;

public abstract class AbstractPlanFragment extends Fragment {

    protected TextView title;
    protected ListView listView;

    protected String plan;

    protected List<String> routes, coords, ids;
    protected ArrayAdapter<String> adapter;

    protected AlertDialog.Builder builder;

    public AbstractPlanFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routes  = new ArrayList<>();
        coords = new ArrayList<>();
        ids = new ArrayList<>();

        JSONArray jsonarray = null;
        try {
            jsonarray = new JSONArray(plan);
            Log.d("JSON",jsonarray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = null;
            try {
                jsonobject = jsonarray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String route = null;
            String coord = null;
            String id = null;
            try {
                route = jsonobject.getString("route");
                String lat = jsonobject.getJSONObject("coordinates").getString("lat");
                String lng = jsonobject.getJSONObject("coordinates").getString("lng");
                id = jsonobject.getString("id");
                coord = lat + "," + lng;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            routes.add("GO TO " + route);
            coords.add(coord);
            ids.add(id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        builder = new AlertDialog.Builder(getContext());
        title = (TextView)view.findViewById(R.id.textView_selectedPlan_f);
        listView = (ListView)view.findViewById(R.id.listView_selectedPlan_f);
        //adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, routes);
        adapter = new ArrayAdapter<>(getContext(),R.layout.plans_list_item,R.id.textest, routes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String dest_address = item.substring(6);
                initAlertDialog(coords.get(position));
                AlertDialog dialog = builder.create();
                dialog.show();
                Intent geofencingService = new Intent(getContext(), GeofencingService.class);
                geofencingService.putExtra("coordinates",coords.get(position));
                geofencingService.putExtra("id",ids.get(position));
                geofencingService.putExtra("name",dest_address);
                //geofencingService.putExtra("current_plan", planFileName);
                //TODO start service from activity and update notification pendingintent class
                getActivity().startService(geofencingService);
            }

        });
    }

    private void initAlertDialog(String dest_address) {
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + dest_address + "&mode=w"));
        builder.setMessage(R.string.open_gps);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
    }
}
