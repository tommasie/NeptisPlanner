/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.model.museum;

import it.uniroma1.neptis.planner.model.Attraction;

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
