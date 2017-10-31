package it.uniroma1.neptis.planner.services.queue;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import it.uniroma1.neptis.planner.LoginActivity;
import it.uniroma1.neptis.planner.util.ConfigReader;

/**
 * Created by thomas on 24/01/17.
 */

public class ReportAsyncTask extends AsyncTask<String, String, Integer> {

    private String report_URL;
    private Context context;

    public ReportAsyncTask(Context context) {
        this.context = context;
        report_URL = ConfigReader.getConfigValue(context, "serverURL") + "/report_";
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream in;
        int code;
        String charset = "UTF-8";
        String reportType = params[0];
        String category = params[1];
        String attractionId = params[2];
        String sensedData = params[3];
        String token = params[4];
        try {
            URL url = new URL(report_URL + reportType);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", token);
            // set like post request
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Accept-Charset", charset);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            // JSON object to send
            JSONObject json = new JSONObject();
            String attraction;
            if(category.equals("city"))
                attraction = "attraction_c_id";
            else attraction = "attraction_m_id";
            json.put(attraction, attractionId);
            if(reportType.equals("rating"))
                json.put("rating", sensedData);
            else json.put("minutes", sensedData);

            // get current date
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
