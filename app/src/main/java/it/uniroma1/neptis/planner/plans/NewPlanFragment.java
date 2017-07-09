package it.uniroma1.neptis.planner.plans;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Calendar;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.planning.PlanningActivity;
import it.uniroma1.neptis.planner.planning.PlanningFragmentsInterface;

public class NewPlanFragment extends AbstractPlanFragment{

    private PlanningFragmentsInterface activity;
    
    public NewPlanFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        planString = getArguments().getString(PlanningActivity.EXTRA_MESSAGE);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_exit_f)
            activity.exitToMenu();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlanningFragmentsInterface) {
            activity = (PlanningFragmentsInterface) context;
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
        inflater.inflate(R.menu.save_plan_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Calendar calendar = Calendar.getInstance();
        DateFormat sdf = DateFormat.getDateInstance();
        //String ts = sdf.format(calendar.getTime());
        long ts = calendar.getTimeInMillis();
        String filename = plan.getName() + "_" + ts;
        FileOutputStream outputStream;
        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(planString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
        return true;
    }
}
