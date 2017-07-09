package it.uniroma1.neptis.planner.plans;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.RecyclerAdapter;

public class PlansListFragment2 extends Fragment {

    public final static String EXTRA_MESSAGE = "key message";

    private TextView title;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayAdapter<String> adapter;

    private Menu menu;
    private int selectedPlan;
    private List<String> filesList;

    private PlansFragmentsInterface activity;
    public PlansListFragment2() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        filesList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plan2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView)view.findViewById(R.id.textView_selectedPlan_f);
        title.setText(getString(R.string.fragment_plans_list_title));
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

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

        recAdapter = new RecyclerAdapter(this,filesList);
        recyclerView.setAdapter(recAdapter);

        /*recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                String item = (String) filesList.get(position);
                Log.d("item", item);
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_MESSAGE, item);
                activity.selectPlan(bundle);
            }

            @Override
            public void onLongClick(View view, final int position) {
                final String item = (String) filesList.get(position);
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
                                filesList.remove(position);
                                recAdapter.notifyItemRemoved(position);
                                recAdapter.notifyItemRangeChanged(position,filesList.size());
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
            }
        }));

        /*recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
        });*/

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
        inflater.inflate(R.menu.plans_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.delete_plan).setVisible(false);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete_plan:
                File dir = getContext().getFilesDir();
                final File file = new File(dir, filesList.get(selectedPlan));
                boolean deleted = file.delete();
                filesList.remove(selectedPlan);
                recAdapter.notifyItemRemoved(selectedPlan);
                recAdapter.notifyItemRangeChanged(selectedPlan,filesList.size());
                return true;
        }
        return true;
    }

    public void deleteItem(int item) {
        menu.findItem(R.id.delete_plan).setVisible(true);
        selectedPlan = item;
    }

    public void hideDelete() {
        menu.findItem(R.id.delete_plan).setVisible(false);
    }

    public interface ClickListener {
        void onClick(View v, int position);
        void onLongClick(View v, int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child != null && clicklistener != null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
