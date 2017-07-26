package it.uniroma1.neptis.planner.plans;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Settings;
import it.uniroma1.neptis.planner.report.Report;

public class PlansActivity extends AppCompatActivity implements PlansFragmentsInterface {

    public final static String EXTRA_MESSAGE = "key message";

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private Fragment plansFragment;
    private Fragment selectedPlanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        String filename = getIntent().getStringExtra("computed_plan_file");
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        plansFragment = new PlansListFragment();
        transaction.add(R.id.activity_plans, plansFragment);
        transaction.commit();
        if(filename != null) {
            selectedPlanFragment = new SelectedPlanFragment();
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_MESSAGE, filename);
            int attrsIndex = getIntent().getIntExtra("index",-1);
            bundle.putInt("index",attrsIndex);
            selectedPlanFragment.setArguments(bundle);
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.activity_plans, selectedPlanFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }

    @Override
    public void selectPlan(Bundle bundle) {
        transaction = fragmentManager.beginTransaction();
        selectedPlanFragment = new SelectedPlanFragment();
        bundle.putInt("index",-1);
        selectedPlanFragment.setArguments(bundle);
        transaction.replace(R.id.activity_plans, selectedPlanFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void attractionDetail(Bundle bundle) {
        transaction = fragmentManager.beginTransaction();
        Fragment fragment = new RateAttractionFragment();
        fragment.setArguments(bundle);
        transaction.replace(R.id.activity_plans, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.report) {
            Intent intent = new Intent(this, Report.class);
            startActivity(intent);
        }
        if (id == R.id.settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        fragmentManager.popBackStack();
        return true;
    }

    public void popBackStack() {
        fragmentManager.popBackStack();
    }
}
