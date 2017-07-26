package it.uniroma1.neptis.planner.model.museum;

import java.io.Serializable;

import it.uniroma1.neptis.planner.model.Attraction;

/**
 * Created by thomas on 11/07/17.
 */

public class MuseumAttraction extends Attraction {

    private String area;

    public MuseumAttraction(String id, String name, byte rating, String area) {
        super(id, name, rating);
        this.area = area;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
