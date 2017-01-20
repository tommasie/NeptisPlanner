package it.uniroma1.neptis.planner.services.tracking;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.List;
import java.util.Scanner;

/**
 * Created by thomas on 08/12/16.
 */

public abstract class AbstractTask extends AsyncTask<String,Void,JSONObject> {

    protected Context context;
    protected JSONObject serverProperties;
    protected List<ScanResult> scanResults;

    public AbstractTask(Context context, List<ScanResult> scanResults) {
        this.context = context;
        this.scanResults = scanResults;
        serverProperties = readPropertiesFile();
    }

    private JSONObject readPropertiesFile() {
        JSONObject out = null;
        try {
            StringBuilder properties = new StringBuilder();
            Scanner s = new Scanner(context.getAssets().open("server_properties.json"));
            while(s.hasNext()) {
                properties.append(s.next());
            }
            out = new JSONObject(properties.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return out;
    }

}
