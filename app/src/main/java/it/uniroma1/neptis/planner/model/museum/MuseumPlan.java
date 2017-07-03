package it.uniroma1.neptis.planner.model.museum;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.model.Attraction;
import it.uniroma1.neptis.planner.model.Plan;

/**
 * Created by thomas on 29/06/17.
 */

public class MuseumPlan extends Plan {

    private List<Area> areas;

    public MuseumPlan(String name) {
        super(name);
        this.areas = new ArrayList<>();
    }

    public void addArea(Area a) {
        this.areas.add(a);
    }

    public List<Area> getAreas() {
        return this.areas;
    }

    public List<Attraction> getAttractions() {
        List<Attraction>  l = new ArrayList<>();
        for(Area a : areas) {
            for (Attraction at : a.getAttractions())
                l.add(at);
        }
        return l;
    }


}
