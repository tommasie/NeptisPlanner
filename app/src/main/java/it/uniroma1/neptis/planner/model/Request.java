package it.uniroma1.neptis.planner.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 09/10/17.
 */

public class Request {

    private Map<String,String> planningParameters;
    private List<Attraction> mustVisit;
    private List<Attraction> excludeVisit;
    private String plan;

    public Request() {
        planningParameters = new HashMap<>();
        mustVisit = new ArrayList<>();
        excludeVisit = new ArrayList<>();
        plan = "";
    }

    public void addRequestParams(Map<String, String> parameters) {
        this.planningParameters.putAll(parameters);
    }

    public Map<String, String> getRequestParameters() {
        return this.planningParameters;
    }

    public List<Attraction> getMustVisit() {
        return mustVisit;
    }

    public void setMustVisit(List<Attraction> mustVisit) {
        this.mustVisit = mustVisit;
    }

    public List<Attraction> getExcludeVisit() {
        return excludeVisit;
    }

    public void setExcludeVisit(List<Attraction> excludeVisit) {
        this.excludeVisit = excludeVisit;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}
