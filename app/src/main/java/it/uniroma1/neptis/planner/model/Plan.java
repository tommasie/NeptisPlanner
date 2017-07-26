package it.uniroma1.neptis.planner.model;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.model.city.CityAttraction;

/**
 * Created by thomas on 19/06/17.
 */

public class Plan {

    //TODO filename?
    protected String name;
    private String type;
    protected ArrayList<Attraction> attractions;

    public Plan(String name, String type) {
        this.name = name;
        this.type = type;
        this.attractions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addAttraction(Attraction ca) {
        attractions.add(ca);
    }

    public ArrayList<Attraction> getAttractions() {
        return this.attractions;
    }
}
