package it.uniroma1.neptis.planner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 30/06/17.
 */

public class Route {

    private List<RoutePoint> route;

    public Route() {
        route = new ArrayList<>();
    }

    public List<RoutePoint> getRoute() {
        return this.route;
    }
}
