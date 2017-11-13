package it.uniroma1.neptis.planner.plans;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

@Deprecated
public class PlansActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private Fragment plansFragment;
    private Fragment selectedPlanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
