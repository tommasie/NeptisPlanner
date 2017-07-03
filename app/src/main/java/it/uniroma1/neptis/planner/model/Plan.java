package it.uniroma1.neptis.planner.model;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.model.city.CityAttraction;

/**
 * Created by thomas on 19/06/17.
 */

public abstract class Plan {

    //TODO filename?
    protected String name;

    public Plan(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
