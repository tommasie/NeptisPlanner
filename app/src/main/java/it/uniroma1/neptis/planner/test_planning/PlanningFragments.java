package it.uniroma1.neptis.planner.test_planning;

import android.os.Bundle;

//Interface used byt PlanningActivity to communicate with its fragments
interface PlanningFragments {

    void requestTime(Bundle bundle);

    void computePlan(Bundle bundle);

    void exitToMenu();
}
