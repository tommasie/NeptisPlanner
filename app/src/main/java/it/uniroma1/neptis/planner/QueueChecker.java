package it.uniroma1.neptis.planner;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import it.uniroma1.neptis.planner.services.queue.QueueRecognitionService;
import it.uniroma1.neptis.planner.services.queue.ReportAsyncTask;

public class QueueChecker extends AppCompatActivity implements View.OnClickListener{

    private final static String report_URL = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/report_queue";
    private TextView museumName;
    private EditText time;
    private Button report;
    private Button noQueue;
    private String destinationId;
    private String destinationName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_checker);
        Intent startIntent = getIntent();
        destinationId = startIntent.getStringExtra("destination_id");
        destinationName = startIntent.getStringExtra("destination_name");
        museumName = (TextView)findViewById(R.id.text_museum_name);
        museumName.setText(destinationName);
        time = (EditText)findViewById(R.id.edit_queue_time);
        report = (Button)findViewById(R.id.btn_report);
        report.setOnClickListener(this);
        noQueue = (Button)findViewById(R.id.btn_no_queue);
        noQueue.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == report.getId()) {
            Intent i = new Intent(this, QueueRecognitionService.class);
            i.putExtra("destination_id",destinationId);
            startService(i);
        }
        else {
            new ReportAsyncTask(getApplicationContext()).execute(report_URL, destinationId, "1");
        }
    }
}
