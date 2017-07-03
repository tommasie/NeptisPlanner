package it.uniroma1.neptis.planner.model.city;

import it.uniroma1.neptis.planner.model.Attraction;

/**
 * Created by thomas on 19/06/17.
 */

public class CityAttraction extends Attraction {

    private String latitude;
    private String longitude;

    public CityAttraction(String id, String name, byte rating, String latitude, String longitude) {
        super(id, name, rating);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
