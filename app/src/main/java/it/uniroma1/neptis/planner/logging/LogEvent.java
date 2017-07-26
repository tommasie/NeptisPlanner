package it.uniroma1.neptis.planner.logging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thomas on 26/07/17.
 */

public class LogEvent {
    //The activity where the event was triggered
    String activity;
    //The action the user wanted to trigger
    String userAction;
    //The view used to trigger the action
    String viewId;

    long timestamp;

    public LogEvent(String activity, String userAction, String viewId, long timestamp) {
        this.activity = activity;
        this.userAction = userAction;
        this.viewId = viewId;
        this.timestamp = timestamp;
    }

    public String toJSONString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("activity", activity);
            obj.put("userAction", userAction);
            obj.put("viewId",viewId);
            obj.put("timestamp", timestamp);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return obj.toString();
    }
}
