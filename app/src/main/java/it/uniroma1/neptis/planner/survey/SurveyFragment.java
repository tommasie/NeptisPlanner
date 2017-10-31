package it.uniroma1.neptis.planner.survey;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import it.uniroma1.neptis.planner.R;
import it.uniroma1.neptis.planner.custom.PlansListAdapter;
import it.uniroma1.neptis.planner.iface.MainInterface;
import it.uniroma1.neptis.planner.logging.LogEvent;
import it.uniroma1.neptis.planner.plans.PlansFragmentsInterface;

/**
 * Created by thomas on 09/10/17.
 */

public class SurveyFragment extends Fragment {

    private Logger eventLogger = LoggerFactory.getLogger("event_logger");
    private LogEvent logEvent;

    private MainInterface activity;
    private WebView webView;

    public SurveyFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.webView = new WebView(getContext());
        return this.webView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        System.out.println(Locale.getDefault().getLanguage());
        if(Locale.getDefault().getLanguage().equals("it"))
            //TODO show the italian suvey
            ;
        webView.loadUrl("https://docs.google.com/forms/u/0/d/e/1FAIpQLScngGutiZHLeSoX78iZrT6c546RBnIEEfJ5MGgA-7yeSjc0Pw/viewform?usp=sf_link");

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

}
