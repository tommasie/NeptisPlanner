package it.uniroma1.neptis.planner.model.city;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Plan;

/**
 * Created by thomas on 29/06/17.
 */

public class CityPlan extends Plan {

    private List<CityAttraction> attractions;

    public CityPlan(String name) {
        super(name);
        this.attractions = new ArrayList<>();
    }


    public void addAttraction(CityAttraction ca) {
        attractions.add(ca);
    }

    public List<CityAttraction> getAttractions() {
        return this.attractions;
    }



}
