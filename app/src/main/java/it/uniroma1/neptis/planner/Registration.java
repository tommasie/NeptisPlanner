package it.uniroma1.neptis.planner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Registration extends AppCompatActivity {
    private EditText name;
    private EditText surname;
    private EditText mail;
    private EditText password;
    private EditText conf_password;

    public final static String EXTRA_MESSAGE = "key message";
    private final static String url_reg = "http://"+LoginActivity.ipvirt+":"+LoginActivity.portvirt+"/user_registration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        name = (EditText) findViewById(R.id.name_reg);
        surname = (EditText) findViewById(R.id.surname);
        mail = (EditText) findViewById(R.id.mail_reg);
        password = (EditText) findViewById(R.id.pwd_reg);
        conf_password = (EditText) findViewById(R.id.conf_psw_reg);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public void confirm_reg(View v) throws IOException {
        String sname = name.getText().toString();
        String ssurname = surname.getText().toString();
        String smail = mail.getText().toString();
        String spassword = password.getText().toString();
        String sconf_password = conf_password.getText().toString();


        if (smail != null && !smail.isEmpty() && isEmailValid(smail)){
            if (sname != null) {
                if (ssurname != null) {
                    if (spassword != null && isPasswordValid(spassword)) {
                        if (sconf_password != null && sconf_password.equals(spassword)) {
                        //*** Make the POST Request ***;

                         new CallAPI().execute(url_reg, sname,ssurname, smail, spassword);

                         } else Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();

                    } else Toast.makeText(this, "Password not valid", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this, "Surname are required", Toast.LENGTH_SHORT).show();
            }else Toast.makeText(this, "Name are required", Toast.LENGTH_SHORT).show();



        }else Toast.makeText(this, "Mail not valid", Toast.LENGTH_SHORT).show();

    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }




    private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            InputStream in = null;
            int code = -1;
            String charset = "UTF-8";

            String urlString = params[0]; // URL to call
            String name = params[1];
            String surname = params[2];
            String mail = params[3];
            String pass = params[4];

            // HTTP Post
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Object json to send
                JSONObject json = new JSONObject();
                json.put("name", name );
                json.put("surname", surname);
                json.put("mail", mail);
                json.put("password", pass);

                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                printout.write(data);
                printout.flush();

                //printout.close();

                //get the response
                //in = urlConnection.getInputStream();
                code = urlConnection.getResponseCode();
                //Log.d("LOG","Status Code: "+code);

                //urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }

            return ""+code;
        }



        protected void onPostExecute(String result) {
            if(result.equals("204")) {
                Toast.makeText(getApplicationContext(), result+": Registration succeded!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), Welcome.class);

                ArrayList l = new ArrayList();

                l.add(mail.getText().toString());
                l.add(name.getText().toString());
                intent.putStringArrayListExtra(EXTRA_MESSAGE, l);
                startActivity(intent);

            }
            else if(result.equals("500")) {
                Toast.makeText(getApplicationContext(), result+": Registration failed! Verify your mail", Toast.LENGTH_LONG).show();
            }
            else{ Toast.makeText(getApplicationContext(), result+": Registration failed! Try later", Toast.LENGTH_LONG).show();}

      }

    }  // end CallAPI
    
}