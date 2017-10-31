package it.uniroma1.neptis.planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.model.Attraction;

public class must_visit extends Activity {

    TextView title;
    ListView lv;
    SearchView sv;
    private List<Attraction> attractionsList;
    private String calling;

    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_must_visit);

        Intent intent = getIntent();
        attractionsList = (ArrayList<Attraction>) intent.getBundleExtra("list").getSerializable("list");
        calling = intent.getStringExtra("calling");

        title =  (TextView) findViewById(R.id.textView10);
        if(calling.equals("must"))
            title.setText("What you want to  visit");
        else  title.setText("What you don't want to  visit");

        lv=(ListView) findViewById(R.id.listView);
        sv=(SearchView) findViewById(R.id.searchView);

        List<String> adapt = new ArrayList<>();
        for(Attraction a : attractionsList)
            adapt.add(a.getName());

        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, adapt);
        lv.setAdapter(adapter);
        //lv.setOnItemClickListener();

        sv.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {

                adapter.getFilter().filter(text);

                return false;
            }
        });
    }

    public void done_must(View view) {
        ArrayList<Attraction> l = new ArrayList<>();
        SparseBooleanArray checked = lv.getCheckedItemPositions();

        for (int i = 0; i < lv.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                // Do something
                l.add(attractionsList.get(i));
            }
        }

        //Toast.makeText(this, l.toString(), Toast.LENGTH_LONG).show();

        Intent returnIntent = new Intent();
        //Intent returnIntent = getIntent();
        Bundle b = new Bundle();
        b.putSerializable("list", l);
        returnIntent.putExtra("list",b);
        //returnIntent.putStringArrayListExtra("result-"+calling, (ArrayList<String>) l);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();




    }
}