package it.uniroma1.neptis.planner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class SurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);
        webView.loadUrl("https://docs.google.com/forms/u/0/d/e/1FAIpQLScngGutiZHLeSoX78iZrT6c546RBnIEEfJ5MGgA-7yeSjc0Pw/viewform?usp=sf_link");
    }
}
