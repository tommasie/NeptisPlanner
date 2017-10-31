package it.uniroma1.neptis.planner.plans;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.PlansListAdapter;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;

public class PlansListFragment extends Fragment {

    private static final String TAG = PlansListFragment.class.getName();
    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    public final static String EXTRA_MESSAGE = "key message";

    private TextView title;
    private ListView listView;
    private int listPosition = -1;
    private PlansListAdapter adapter;

    private ArrayList<String> filesList;
    private Menu menu;

    private MainInterface activity;
    public PlansListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plans_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView)view.findViewById(R.id.textView_selectedPlan_f);
        title.setText(getString(R.string.fragment_plans_list_title));
        listView = (ListView)view.findViewById(R.id.listView_selectedPlan_f);
        filesList = new ArrayList<>();
        //Get the list of plans in the folder and display them in the ListView
        File fileDirectory = getContext().getFilesDir();
        File[] dirFiles = fileDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Log.d(TAG, name);
                return name.matches("\\w+\\_\\d+");
            }
        });
        for (File f : dirFiles) {
            filesList.add(f.getName());
        }
        adapter = new PlansListAdapter(getContext(), R.layout.plans_list_item2, filesList);
        adapter.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Long l1 = Long.parseLong(o1.split("_")[1]);
                Long l2 = Long.parseLong(o2.split("_")[1]);
                return l1.compareTo(l2) * -1;
            }
        });
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                logEvent = new LogEvent(getActivity().getClass().getName(),"select plan", "listview_selectedPlan_item", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                String item = (String) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putString("computed_plan_file", item);
                activity.selectPlan(bundle);
            }

        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(final AdapterView parent, View view, final int position, long id) {
                logEvent = new LogEvent(getActivity().getClass().getName(),"open list menu", "listview_selectedPlan_item", System.currentTimeMillis());
                eventLogger.info(logEvent.toJSONString());
                listPosition = position;
                menu.findItem(R.id.delete_plan).setVisible(true);
                return true;
            }
        });

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.plans_menu, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.delete_plan) {
            logEvent = new LogEvent(getActivity().getClass().getName(),"delete eplan", "delete_plan_options_button", System.currentTimeMillis());
            eventLogger.info(logEvent.toJSONString());
            File dir = getContext().getFilesDir();
            File file = new File(dir, filesList.get(listPosition));
            boolean deleted = file.delete();
            filesList.remove(listPosition);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

}
