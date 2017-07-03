package it.uniroma1.neptis.planner.planning;

import android.os.Bundle;

import java.util.List;
import java.util.Map;

//Interface used byt PlanningActivity to communicate with its fragments
public interface PlanningFragmentsInterface {

    void requestTime(Map<String,String> parameters);

    void computePlan(Map<String,String> params, Map<String,List<String>> visitPreferences);

    void exitToMenu();
}
