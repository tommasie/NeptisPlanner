package it.uniroma1.neptis.planner.plans;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.CityAttractionArrayAdapter;
import it.uniroma1.neptis.planner.custom.MuseumAttractionArrayAdapter;
import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Plan;
import it.uniroma1.neptis.planner.model.city.CityAttraction;
import it.uniroma1.neptis.planner.model.city.CityPlan;
import it.uniroma1.neptis.planner.model.museum.Area;
import it.uniroma1.neptis.planner.model.museum.MuseumPlan;
import it.uniroma1.neptis.planner.services.tracking.GeofencingService;

public abstract class AbstractPlanFragment extends Fragment {

    protected TextView title;
    protected ListView listView;

    protected String planString;
    protected Plan plan;

    private String type;

    protected CityAttractionArrayAdapter cityAdapter;
    protected MuseumAttractionArrayAdapter museumAdapter;

    protected AlertDialog.Builder builder;

    public AbstractPlanFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        plan = parsePlan(planString);
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
        if(type.equals("city")) {
            CityPlan cPlan = (CityPlan)plan;
            cityAdapter = new CityAttractionArrayAdapter(getContext(), R.layout.plans_list_item, cPlan.getAttractions());
            listView.setAdapter(cityAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    CityAttraction attraction = (CityAttraction) parent.getItemAtPosition(position);
                    initAlertDialog(attraction.getLatitude(), attraction.getLongitude());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Intent geofencingService = new Intent(getContext(), GeofencingService.class);
                    geofencingService.putExtra("id", attraction.getId());
                    geofencingService.putExtra("name", attraction.getName());
                    geofencingService.putExtra("latitude", attraction.getLatitude());
                    geofencingService.putExtra("longitude", attraction.getLongitude());
                    //geofencingService.putExtra("current_plan", planFileName);
                    //TODO start service from activity and update notification pendingintent class
                    getActivity().startService(geofencingService);
                }

            });
        } else if(type.equals("museum")) {
            MuseumPlan mPlan = (MuseumPlan)plan;
            museumAdapter = new MuseumAttractionArrayAdapter(getContext(), R.layout.plans_list_item, mPlan.getAttractions());
            listView.setAdapter(museumAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Attraction attraction = (Attraction) parent.getItemAtPosition(position);
                    Intent geofencingService = new Intent(getContext(), GeofencingService.class);
                    geofencingService.putExtra("id", attraction.getId());
                    geofencingService.putExtra("name", attraction.getName());
                    //geofencingService.putExtra("current_plan", planFileName);
                    //TODO start service from activity and update notification pendingintent class
                    getActivity().startService(geofencingService);
                }
            });
        }

    }

    private void initAlertDialog(String latitude, String longitude) {
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=w"));
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

    private Plan parsePlan(String planString) {
        //Plan plan = null;
        JSONObject obj;
        try {
            obj = new JSONObject(planString);
            type = obj.getString("type");
            if(type.equals("city")) {
                //FIXME
                CityPlan plan = new CityPlan("name");
                JSONArray route = obj.getJSONArray("route");
                for (int i = 0; i < route.length(); i++) {
                    JSONObject attraction = route.getJSONObject(i);
                    String name = attraction.getString("name");
                    String id = attraction.getString("id");
                    String lat = attraction.getJSONObject("coordinates").getString("latitude");
                    String lng = attraction.getJSONObject("coordinates").getString("longitude");
                    CityAttraction a = new CityAttraction(id, name, (byte)50, lat, lng);
                    plan.addAttraction(a);
                }
                return plan;
            } else if(type.equals("museum")) {
                MuseumPlan plan = new MuseumPlan("name");
                JSONArray route = obj.getJSONArray("route");
                for (int i = 0; i < route.length(); i++) {
                    //Lista delle stanze
                    JSONObject area = route.getJSONObject(i);
                    String areaName = area.getString("name");
                    String areaId = area.getString("id");
                    Area a = new Area(areaId, areaName);
                    //Lista delle attrazioni
                    JSONArray attractions = area.getJSONArray("attractions");
                    for(int j = 0; j < attractions.length(); j++) {
                        JSONObject att = attractions.getJSONObject(j);
                        String attractionName = att.getString("name");
                        String attractionId = att.getString("id");
                        byte attractionRating = (byte)att.getInt("rating");
                        Attraction at = new Attraction(attractionId,attractionName,attractionRating);
                        a.addAttraction(at);
                    }
                    plan.addArea(a);
                }
                return plan;
            }

        } catch (JSONException e) {
            plan = null;
        }

        return plan;
    }
}
