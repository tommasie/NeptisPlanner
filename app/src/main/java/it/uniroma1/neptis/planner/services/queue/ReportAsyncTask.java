package it.uniroma1.neptis.planner.services.queue;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import it.uniroma1.neptis.planner.LoginActivity;

/**
 * Created by thomas on 24/01/17.
 */

public class ReportAsyncTask extends AsyncTask<String, String, Integer> {

    private String report_URL = "http://" + LoginActivity.ipvirt + ":" + LoginActivity.portvirt + "/report_";
    private Context context;

    public ReportAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream in;
        int code;
        String charset = "UTF-8";
        String type = params[0];
        String category = params[1];
        String attractionId = params[2];
        String minutes = params[3];
        try {
            URL url = new URL(report_URL + type);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // set like post request
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Accept-Charset", charset);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            // JSON object to send
            JSONObject json = new JSONObject();
            json.put("category", category);
            json.put("attractionId", attractionId);
            json.put("minutes", minutes);

            // get current date
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
            String ts = ourJavaTimestampObject.toString().replace(' ','T');
            Log.d("timestamp: ",ts);
            json.put("data",ts);
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            String s = json.toString();
            byte[] data = s.getBytes("UTF-8");
            printout.write(data);
            printout.flush();
            printout.close();
            in = new BufferedInputStream(urlConnection.getInputStream());
            code = urlConnection.getResponseCode();
            Log.d("code report",code+"");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return code;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result== 204)
            Toast.makeText(context, "Report successful!\nThank you!", Toast.LENGTH_LONG).show();
        else Toast.makeText(context, result+"Error on reporting..", Toast.LENGTH_LONG).show();
    }
}
