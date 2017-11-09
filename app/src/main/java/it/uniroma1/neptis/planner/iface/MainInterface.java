package it.uniroma1.neptis.planner.iface;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

import it.uniroma1.neptis.planner.model.Attraction;

/**
 * Created by thomas on 09/10/17.
 */

public interface MainInterface {

    //Planning functions
    void requestTime(Map<String, String> parameters);
    void computePlan(Map<String, String> params, Map<String, List<Attraction>> visitPreferences);

    //Plans functions
    void selectPlan(Bundle bundle);
    void attractionDetail(Bundle bundle);
    void popBackStack();

    FirebaseUser getUser();
    Address getLocation();
}