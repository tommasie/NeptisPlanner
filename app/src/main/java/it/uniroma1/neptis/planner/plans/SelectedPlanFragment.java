package it.uniroma1.neptis.planner.plans;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import it.uniroma1.neptis.planner.R;

public class SelectedPlanFragment extends AbstractPlanFragment {

    private String planFileName;

    private PlansFragmentsInterface activity;

    public SelectedPlanFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        planFileName = getArguments().getString(PlansListFragment.EXTRA_MESSAGE);

        FileInputStream fis = null;
        try {
            fis = getContext().openFileInput(planFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        planString = sb.toString();
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
