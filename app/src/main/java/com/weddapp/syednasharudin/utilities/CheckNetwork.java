package com.weddapp.syednasharudin.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by syednasharudin on 11/4/14.
 */
public class CheckNetwork {

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {

            new AlertDialog.Builder(context)
                    .setTitle("No Internet Connection")
                    .setMessage("WeddApp could not connect to the internet, Please check your internet connection first.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .show();

            return false;
        } else
            return true;
    }

}
