/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.iface;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

import it.uniroma1.neptis.planner.model.Attraction;

public interface MainInterface {

    void mainMenu();
    //Planning functions
    void requestTime(Map<String, String> parameters);
    void computePlan(Map<String, String> params, Map<String, List<Attraction>> visitPreferences);

    //Plans functions
    void selectPlan(Bundle bundle);
    void setCurrentPlan(Bundle bundle);
    void attractionDetail(Bundle bundle);
    void popBackStack();

    FirebaseUser getUser();
    Address getLocation();
}
