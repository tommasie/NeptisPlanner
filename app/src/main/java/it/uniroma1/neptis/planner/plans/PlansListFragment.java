package it.uniroma1.neptis.planner.plans;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.PlansListAdapter;

public class PlansListFragment extends Fragment {

    public final static String EXTRA_MESSAGE = "key message";

    private TextView title;
    private ListView listView;
    private int listPosition = -1;
    private PlansListAdapter adapter;

    private ArrayList<String> filesList;
    private Menu menu;

    private PlansFragmentsInterface activity;
    public PlansListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        File[] dirFiles = fileDirectory.listFiles();
        for (File f : dirFiles) {
            String fileName = f.getName();
            //Needed for Android Studio with Instant Run enabled
            //The IDE creates an empty file named "instant-run", don't include it in the list
            if(!fileName.equals("instant-run"))
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
                String item = (String) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_MESSAGE, item);
                activity.selectPlan(bundle);
            }

        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(final AdapterView parent, View view, final int position, long id) {
                listPosition = position;
                menu.findItem(R.id.delete_plan).setVisible(true);
                return true;
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlansFragmentsInterface) {
            activity = (PlansFragmentsInterface) context;
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
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.plans_menu, menu);
        menu.findItem(R.id.delete_plan).setVisible(false);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        File dir = getContext().getFilesDir();
        File file = new File(dir, filesList.get(listPosition));
        boolean deleted = file.delete();
        filesList.remove(listPosition);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
        return true;
    }


}
