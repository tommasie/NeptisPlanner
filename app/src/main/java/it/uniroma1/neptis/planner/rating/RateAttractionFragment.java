/*
 * Copyright (c) 2017. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.rating;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.services.queue.ReportAsyncTask;

public class RateAttractionFragment extends Fragment implements View.OnClickListener{
    public final static String EXTRA_MESSAGE = "key message";

    private TextView attractionName;
    //private ImageView attractionPicture;
    private RatingBar ratingBar;
    private Button button;

    private String attractionId;
    private String type;

    private MainInterface activity;

    public RateAttractionFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rate_attraction, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attractionId = getArguments().getString("id");
        type = getArguments().getString("type");
        attractionName = (TextView)view.findViewById(R.id.attraction_rating_name);
        attractionName.setText(getArguments().getString("name"));
        //attractionPicture = (ImageView)view.findViewById(R.id.attraction_rating_image);
        //Rating bar can get the values {1.0, 2.0, 3.0} (floats)
        ratingBar = (RatingBar)view.findViewById(R.id.attraction_rating_bar);
        button = (Button)view.findViewById(R.id.attraction_rating_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainInterface) {
            activity = (MainInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.attraction_rating_button) {
            activity.getUser().getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                new RatingAsyncTask(getContext()).execute("rating",type, attractionId, String.valueOf((int)ratingBar.getRating()),idToken);
                            } else {
                                // Handle error -> task.getException();
                            }
                        }
                    });
        }
    }

    private class RatingAsyncTask extends ReportAsyncTask {

        public RatingAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            activity.popBackStack();
        }
    }
}
