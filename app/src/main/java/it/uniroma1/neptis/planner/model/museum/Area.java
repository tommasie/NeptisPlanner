package it.uniroma1.neptis.planner.model.museum;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.model.Attraction;

/**
 * Created by thomas on 30/06/17.
 */

public class Area {

    private String id;
    private String name;
    private List<Attraction> attractions;

    public Area(String id, String name) {
        this.id = id;
        this.name = name;
        attractions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Attraction> getAttractions() {
        return attractions;
    }

    public void addAttraction(Attraction a) {
        this.attractions.add(a);
    }
}
