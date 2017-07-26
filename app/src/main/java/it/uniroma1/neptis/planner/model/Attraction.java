package it.uniroma1.neptis.planner.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by thomas on 30/06/17.
 */

public class Attraction implements Serializable {

    protected String id;
    protected String name;
    protected byte rating;

    public Attraction(String id, String name, byte rating) {
        this.id = id;
        this.name = name;
        this.rating = rating;
    }

    public static Attraction parse(JSONObject attraction) {
        try {
            String id = attraction.getString("id");
            String name = attraction.getString("name");
            byte rating = (byte) attraction.getInt("rating");
            return new Attraction(id,name,rating);
        } catch (JSONException e) {
            return null;
        }

    }

    public JSONObject serialize() {
        JSONObject out = new JSONObject();
        try {
            out.put("id",id);
            out.put("name",name);
            out.put("rating",rating);
        } catch (JSONException e) {
            return null;
        }
        return out;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getRating() {
        return rating;
    }

    public void setRating(byte rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attraction that = (Attraction) o;

        if (!id.equals(that.id)) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
