package it.uniroma1.neptis.planner.planning;

import android.os.Bundle;

import java.util.List;
import java.util.Map;

//Interface used byt PlanningActivity to communicate with its fragments
public interface PlanningFragmentsInterface {

    void requestTime(Map<String,String> parameters);

    void computePlan(Bundle bundle, Map<String,String> map, Map<String,List<String>> map2);

    void exitToMenu();
}
