package it.uniroma1.neptis.planner.plans;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import it.uniroma1.neptis.planner.R;

public class PlansListFragment extends Fragment {

    public final static String EXTRA_MESSAGE = "key message";

    private TextView title;
    private ListView listView;
    ArrayAdapter<String> adapter;

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
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView)view.findViewById(R.id.textView_selectedPlan_f);
        title.setText("My plans");
        listView = (ListView)view.findViewById(R.id.listView_selectedPlan_f);
        ArrayList<String> filesList = new ArrayList<>();
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
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, filesList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_MESSAGE, item);
                activity.selectPlan(bundle);
                /*Intent intent = new Intent(getContext(), Selected_Plan.class);
                intent.putExtra(EXTRA_MESSAGE, item);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);*/
            }

        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(final AdapterView parent, View view, final int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                // DELETE FILES
                File dir = getContext().getFilesDir();
                final File file = new File(dir, item);

                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Delete");
                alertDialog.setMessage("Are you sure you want to remove it?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                boolean deleted = file.delete();
                                adapter.remove(item);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
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

}
