package it.uniroma1.neptis.planner.test_planning;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Welcome;
import it.uniroma1.neptis.planner.planning.Best_Time_Plan;
import it.uniroma1.neptis.planner.planning.Your_Plan;

public class PlanningActivity extends AppCompatActivity implements PlanningFragments{

    public final static String EXTRA_MESSAGE = "key message";
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private ChoiceFragment choiceFragment;
    private BestTimeFragment bestTimeFragment;
    private Fragment newPlanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        choiceFragment = new ChoiceFragment();
        transaction.add(R.id.activity_planning,choiceFragment);
        transaction.commit();
    }

    @Override
    public void requestTime(Bundle bundle) {
        bestTimeFragment = new BestTimeFragment();
        bestTimeFragment.setArguments(bundle);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.activity_planning, bestTimeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void guide(View v) {
        choiceFragment.guide(v);
    }

    @Override
    public void computePlan(Bundle bundle) {
        newPlanFragment = new NewPlanFragment();
        newPlanFragment.setArguments(bundle);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.activity_planning, newPlanFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void exitToMenu() {
        Intent intent = new Intent(this, Welcome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }


}
