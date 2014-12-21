package com.weddapp.syednasharudin.auth;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.weddapp.syednasharudin.dbase.User;
import com.weddapp.syednasharudin.dbase.UserDao;
import com.weddapp.syednasharudin.home.HomeActivity;
import com.weddapp.syednasharudin.home.R;
import com.weddapp.syednasharudin.json.JSONParser;
import com.weddapp.syednasharudin.utilities.CheckNetwork;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 0;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private SignInButton btnSignIn;
    private ProgressBar progressSignInBar;

    // JSON
    private JSONParser jsonParser;
    private JSONObject jsonObject;
    private String serverUrl;

    //Check Network
    CheckNetwork checkNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.activity_sign_in);

        checkNetwork = new CheckNetwork();
        mSignInClicked = false;
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        progressSignInBar = (ProgressBar) findViewById(R.id.progressSignInBar);
        jsonParser = new JSONParser();
        serverUrl = getResources().getString(R.string.server_url) + "register";

        progressSignInBar.setVisibility(View.GONE);

        UserDao userDao = new UserDao(SignInActivity.this);
        userDao.open();

        if(userDao.countUser() > 0) {

            //super.onStop();
            userDao.close();

            directToHomeIntent();

        }else {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();

            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                if(checkNetwork.isNetworkConnected(SignInActivity.this))
                    signInWithGplus();
                }
            });

        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        try {

            if (!mGoogleApiClient.isConnecting()) {
                mSignInClicked = true;
                resolveSignInError();
            }else{
                progressSignInBar.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        try{
            progressSignInBar.setVisibility(View.GONE);
            if (mConnectionResult.hasResolution()) {

                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);

            }
        } catch (Exception e) {
            mIntentInProgress = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {

        mSignInClicked = false;

        User user = getProfileInformation();

        if(user != null) {
            RegisterUser registerUser = new RegisterUser();
            registerUser.setUser(user);
            registerUser.execute();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = connectionResult;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private User getProfileInformation() {

        User user = null;

        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);

                user = new User();
                user.setEmail(Plus.AccountApi.getAccountName(mGoogleApiClient));
                user.setName(currentPerson.getDisplayName());

            } else {
                Toast.makeText(getApplicationContext(),
                        "WeddApp unable to retrieve your google plus information.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public void directToHomeIntent(){
        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        i.putExtra("firstTime", true);
        startActivity(i);
        this.finish();
    }


    class RegisterUser extends AsyncTask<String, String, String>{

        private User user;
        private boolean error;
        private String message;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        protected void onPreExecute(){
            super.onPreExecute();
            progressSignInBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", user.getEmail()));
            params.add(new BasicNameValuePair("name", user.getName()));
            params.add(new BasicNameValuePair("googleplus", "true"));

            jsonObject = jsonParser.makeHttpRequest(serverUrl,
                    "POST", params);

            Log.d("Create Response", jsonParser.toString());

            try {

                error = jsonObject.getBoolean("error");
                message = jsonObject.getString("message");

                if(!error) {
                    user.setServerId(jsonObject.getInt("id"));
                    user.setApiKey(jsonObject.getString("apiKey"));
                    user.setPassword("abc123");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url){
            progressSignInBar.setVisibility(View.GONE);

            if(error){
                new AlertDialog.Builder(SignInActivity.this)
                        .setTitle("WeddApp Registration")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .show();
            }else {

                user.setId(0);

                UserDao userDao = new UserDao(SignInActivity.this);
                userDao.open();
                userDao.createUser(user);
                userDao.close();

                directToHomeIntent();

                Toast.makeText(getApplicationContext(),
                        "Successfully Sign In WeddApp", Toast.LENGTH_LONG).show();
            }
        }
    }
}
