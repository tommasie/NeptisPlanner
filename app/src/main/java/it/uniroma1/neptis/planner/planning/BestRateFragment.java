package it.uniroma1.neptis.planner.planning;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BestRateFragment extends BestTimeFragment {

    public static final int ACTIVITY_2 = 1002;

    private EditText ratingView;

    private List<String> ratingList;

    public BestRateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ratingList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_best_rate, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title.setText("Best Rate Planning");
        ratingView = (EditText)view.findViewById(R.id.autoCompleteTextView_rate_f);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                //String result=data.getStringExtra("rating");
                ratingList = (ArrayList) data.getStringArrayListExtra("rating2");
                ratingView.setText("[Rate!]");
                Toast.makeText(getContext(), "rating: " + ratingList.toString(), Toast.LENGTH_LONG).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ratingView.setEnabled(true);
    }
}

//TODO
/*
protected void onPostExecute(String result) {

                if (result.equals("ok")) {


                multi_must.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        // TODO Auto-generated method stub
                        multi_must.setEnabled(false);
                        Intent i = new Intent(Best_Rate_Plan.this, must_visit.class);
                        i.putStringArrayListExtra(EXTRA_MESSAGE, (ArrayList<String>) mlist);
                        i.putExtra("calling","must");
                        // must_visit.title_t10.setText("What you don't want to  visit");
                        startActivityForResult(i, 1);
                        return false;
                    }
                });



                multi_exclude.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        // TODO Auto-generated method stub
                         multi_exclude.setEnabled(false);
                        Intent i = new Intent(Best_Rate_Plan.this, must_visit.class);
                        i.putStringArrayListExtra(EXTRA_MESSAGE, (ArrayList<String>) mlist);
                        i.putExtra("calling","exclude");
                        startActivityForResult(i, 2);
                        return false;
                    }
                });



                autocomplete.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        // TODO Auto-generated method stub
                        autocomplete.setEnabled(false);
                        Intent i = new Intent(Best_Rate_Plan.this, rating_visit.class);
                        i.putStringArrayListExtra(EXTRA_MESSAGE, (ArrayList<String>) mlist);
                        // must_visit.title_t10.setText("What you don't want to  visit");
                        startActivityForResult(i, 3);
                        return false;

                    }
                });


                progress.dismiss();
                } else
                    Toast.makeText(getApplicationContext(), result + " There is an error", Toast.LENGTH_LONG).show();
            }


            protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                //String result=data.getStringExtra("result");
                lmust = (ArrayList) data.getStringArrayListExtra("result-must");
                multi_must.setText(lmust.toString());
                //Toast.makeText(getApplicationContext(), l.toString(), Toast.LENGTH_LONG).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                lexclude = (ArrayList) data.getStringArrayListExtra("result-exclude");
                multi_exclude.setText(lexclude.toString());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else if (requestCode == 3) {
        if(resultCode == Activity.RESULT_OK){
            //String result=data.getStringExtra("rating");
            ratingList = (ArrayList) data.getStringArrayListExtra("rating2");

            autocomplete.setText("[Rate!]");


            Toast.makeText(getApplicationContext(),"rating: "+ratingList.toString(), Toast.LENGTH_LONG).show();
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        multi_must.setEnabled(true);
        multi_exclude.setEnabled(true);
        autocomplete.setEnabled(true);
    }
 */
