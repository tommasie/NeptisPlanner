package it.uniroma1.neptis.planner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private static final Logger logger = LoggerFactory.getLogger(LoginActivity.class);

    public static final String EMAIL = "mail";
    public static final String UNAME = "name";

    public final static String ipvirt =  "neptistest.asuscomm.com";

    public final static String portvirt= "9070";

    private final static String apiURL = "http://"+LoginActivity.ipvirt+":"+portvirt+"/login_user"; //macch virtuale ifconfig

    private ProgressDialog progress;

    private EditText mEmailView;
    private EditText mPasswordView;
    private String mail_ui;
    private String username;
    private String password_ui;
    private CheckBox remember;
    private TextView newUserButton;
    private Button logButton;

    private FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;

    private CallbackManager mCallbackManager;
    private LoginButton loginButton;

    private static final int GOOGLE_SIGN_IN = 1;
    private static final int FACEBOOK_SIGN_IN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        logger.info("create");

        mEmailView = (EditText) findViewById(R.id.editText_mail);
        mPasswordView = (EditText) findViewById(R.id.password);

        remember = (CheckBox) findViewById(R.id.checkBox_rem_login);
        newUserButton = (TextView) findViewById(R.id.button_newuser);
        logButton = (Button)findViewById(R.id.email_sign_in_button);
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setMessage("Login..");

        //Retrieve previously used login credentials
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        String mymail = pref.getString("mail", null);
        String mypass = pref.getString("password", null);

        if(mymail != null && mypass != null) {
            mail_ui = mymail;
            password_ui = mypass;
            mEmailView.setText(mymail);
            //Execute the login on startup
            //new RESTLogin().execute(apiURL);
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    verifyCredentials();
                    return true;
                }
                return false;
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_key))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("", "facebook:onError", error);
                // ...
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(getApplicationContext(), "Google sign in failed, verify your credentials", Toast.LENGTH_LONG);
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }



    public void verifyCredentials() {
        mail_ui = mEmailView.getText().toString();
        password_ui = mPasswordView.getText().toString();

        if (mail_ui != null && !mail_ui.isEmpty()) {
            if (isEmailValid(mail_ui) && isPasswordValid(password_ui)) {
                String urlString = apiURL;
                new RESTLogin().execute(urlString);
            } else
                Toast.makeText(this, "Control your Credential", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, "Control your Credential", Toast.LENGTH_LONG).show();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;                       // DA CAMBIARE a 6.. 8
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Login failed", "Login failed");
    }

    private class RESTLogin extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String urlString = params[0]; // URL to call
            InputStream in;
            int code;
            String charset = "UTF-8";
            DataOutputStream outStream;
            // HTTP post
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // set like post request
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Object json to send
                JSONObject json = new JSONObject();
                json.put("mail", mail_ui);
                json.put("password", password_ui);

                outStream = new DataOutputStream(urlConnection.getOutputStream());
                String s = json.toString();
                byte[] data = s.getBytes("UTF-8");
                outStream.write(data);
                outStream.flush();
                outStream.close();
                //get the response
                in = urlConnection.getInputStream();
                code = urlConnection.getResponseCode();

            } catch (Exception e) {
                Log.d("RESTLogin", e.getMessage());
                //Can progress be called in doInBackground?
                progress.dismiss();
                return "";
            }

            if(code == -1){
                Toast.makeText(getApplicationContext(), "Login Error", Toast.LENGTH_LONG).show();
                progress.dismiss();
                return "";
            }
            else if (code == 200) {
                String response = "";
                username = "";
                // parser Json
                JsonReader reader;
                try {
                    reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    progress.dismiss();
                    return "";
                }

                try {
                    reader.beginObject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("response"))
                            response = reader.nextString();

                        else if (name.equals("user"))
                            username = reader.nextString();

                    }
                    reader.endObject();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // END Parser Json
                return response;
            }
            return String.valueOf(code);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.isEmpty()) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Error during login phase.\nThe server may be down", Toast.LENGTH_LONG).show();
                return;
            }

            else if (result.equals("ok")) {
                if(remember.isChecked()) {
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("mail", mail_ui);
                    editor.putString("password", password_ui);
                    //editor.commit()
                    //apply() is the same as commit, but asynchronous
                    editor.apply();
                }
                Intent intent = new Intent(getApplicationContext(), Home.class);
                intent.putExtra(EMAIL, mail_ui);
                intent.putExtra(UNAME, username);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(intent);
                progress.dismiss();
            }

            if(result.equals("500"))
                Toast.makeText(getApplicationContext(), "500: Internal server Error", Toast.LENGTH_LONG).show();

            else if(result.equals("204"))
                Toast.makeText(getApplicationContext(), "204: Your mail is not registered or your password is incorrect!", Toast.LENGTH_LONG).show();

            progress.dismiss();

        }

    }  // end RESTLogin
}
