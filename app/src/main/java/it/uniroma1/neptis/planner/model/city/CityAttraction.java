package it.uniroma1.neptis.planner.model.city;

import java.io.Serializable;

import it.uniroma1.neptis.planner.model.Attraction;

/**
 * Created by thomas on 19/06/17.
 */

public class CityAttraction extends Attraction {

    private String latitude;
    private String longitude;
    private double radius;

    public CityAttraction(String id, String name, byte rating, String latitude, String longitude, double radius) {
        super(id, name, rating);
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
