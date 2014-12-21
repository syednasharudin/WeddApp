package com.weddapp.syednasharudin.dbase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by syednasharudin on 11/2/14.
 */
public class DBHandler extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "user";
    public static final int DATABASE_VERSION = 1;

    /* user table */
    public static final String USER = "user";
    public static final String USER_ID = "user_id";
    public static final String USER_SERVER_ID = "user_server_id";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_NAME = "user_name";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_API_KEY = "user_api_key";
    public static final String DATABASE_CREATE_USER_TABLE = "create table "
            + USER + "("
            + USER_ID + " integer primary key, "
            + USER_SERVER_ID + " integer not null, "
            + USER_EMAIL + " text not null, "
            + USER_NAME + " text not null, "
            + USER_PASSWORD + " text not null, "
            + USER_API_KEY + " text not null "
            +");";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + USER);
    }
}
