package it.uniroma1.neptis.planner.services.tracking;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by thomas on 08/12/16.
 */

public class TrackTask extends AbstractTask {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(TrackTask.class);

    private List<String> roomList;

    public TrackTask(Context context, List<ScanResult> scanResults, List<String> roomList) {
        super(context,scanResults);
        this.roomList = roomList;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String room = params[0];
        String museum = params[1];
        URL url = null;
        String out = null;
        try {
            url = new URL(serverProperties.getString("ip_address") + "/track");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            JSONObject q = new JSONObject();
            q.put("group",museum);
            q.put("username","thomas");
            q.put("location",room);
            q.put("time", System.currentTimeMillis());

            JSONArray array = new JSONArray();
            for(ScanResult s : scanResults) {
                JSONObject obj = new JSONObject();
                obj.put("mac",s.BSSID);
                obj.put("rssi",s.level);
                array.put(obj);
            }
            q.put("wifi-fingerprint",array);
            Log.i("JSON",q.toString());
            bw.write(q.toString());
            bw.flush();
            bw.close();
            os.close();
            conn.connect();
            InputStream is = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s;
            out = "";
            while((s=br.readLine()) != null) {
                out+=s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject o = null;
        try {
            o = new JSONObject(out);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    protected void onPostExecute(JSONObject s) {
        super.onPostExecute(s);
        String out = s.toString();
        if(s != null) {
            try {
                if (s.getString("success").equals("true")) {
                    out = s.getString("location");
                    if(roomList.size() == 5)
                        roomList.remove(0);
                    roomList.add(out);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
