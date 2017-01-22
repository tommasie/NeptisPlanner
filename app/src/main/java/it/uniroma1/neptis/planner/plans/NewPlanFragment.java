package it.uniroma1.neptis.planner.plans;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.planning.PlanningActivity;
import it.uniroma1.neptis.planner.planning.PlanningFragmentsInterface;

public class NewPlanFragment extends AbstractPlanFragment implements View.OnClickListener{

    private Button button;
    private PlanningFragmentsInterface activity;
    
    public NewPlanFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        plan = getArguments().getString(PlanningActivity.EXTRA_MESSAGE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title.setText("Your Plan");
        button = (Button)view.findViewById(R.id.button_exit_f);
        button.setOnClickListener(this);
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
}
