package it.uniroma1.neptis.planner.planning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.Settings;
import it.uniroma1.neptis.planner.report.Report;

public class MyPlans extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "key message";

    private ListView listView;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plans);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.listView_myplans);
        ArrayList<String> filesList = new ArrayList<>();
        //Get the list of plans in the folder and display them in the ListView
        File fileDirectory = getFilesDir();
        File[] dirFiles = fileDirectory.listFiles();
        for (File f : dirFiles) {
            String fileName = f.getName();
            //Needed for Android Studio with Instant Run enabled
            //The IDE creates an empty file named "instant-run", don't include it in the list
            if(!fileName.equals("instant-run"))
                filesList.add(f.getName());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filesList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), Selected_Plan.class);
                intent.putExtra(EXTRA_MESSAGE, item);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }

        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(final AdapterView parent, View view, final int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                // DELETE FILES
                File dir = getFilesDir();
                final File file = new File(dir, item);

                AlertDialog alertDialog = new AlertDialog.Builder(MyPlans.this).create();
                alertDialog.setTitle("Delete");
                alertDialog.setMessage("Are you sure you want to remove it?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                boolean deleted = file.delete();
                                adapter.remove(item);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.report) {
            Intent intent = new Intent(this, Report.class);
            startActivity(intent);
        }
        if (id == R.id.myplans) {
            Intent intent = new Intent(this, MyPlans.class);
            startActivity(intent);
        }
        if (id == R.id.settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }


}

