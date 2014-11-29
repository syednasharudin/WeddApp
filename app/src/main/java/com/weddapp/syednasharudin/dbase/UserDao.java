package com.weddapp.syednasharudin.dbase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by syednasharudin on 11/2/14.
 */
public class UserDao {

    private SQLiteDatabase sqLiteDatabase;
    private DBHandler dbHandler;
    private String[] allColumns = { dbHandler.USER_ID, dbHandler.USER_EMAIL, dbHandler.USER_NAME,
                                    dbHandler.USER_PASSWORD, dbHandler.USER_API_KEY };

    public UserDao(Context context){
        dbHandler = new DBHandler(context);
    }

    public void open(){
        sqLiteDatabase = dbHandler.getReadableDatabase();
    }

    public void close(){
        sqLiteDatabase.close();
    }

    public User createUser(User user){
        ContentValues values = new ContentValues();
        values.put(dbHandler.USER_ID, user.getId());
        values.put(dbHandler.USER_EMAIL, user.getEmail());
        values.put(dbHandler.USER_NAME, user.getName());
        values.put(dbHandler.USER_PASSWORD, user.getPassword());
        values.put(dbHandler.USER_API_KEY, user.getApiKey());
        long insertId = sqLiteDatabase.insert(dbHandler.USER, null,
                values);
        Cursor cursor = sqLiteDatabase.query(dbHandler.USER,
                allColumns, dbHandler.USER_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        User createUser = cursorToUser(cursor);
        cursor.close();

        return createUser;
    }

    public void deleteUser(User user){
        int id = user.getId();
        sqLiteDatabase.delete(dbHandler.USER, dbHandler.USER_ID
                + " = " + id, null);
    }

    public List<User> getAllBmies() {
        List<User> users = new ArrayList<User>();

        Cursor cursor = sqLiteDatabase.query(dbHandler.USER,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = cursorToUser(cursor);
            users.add(user);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return users;
    }

    public User getBmi(int id){
        Cursor cursor =  sqLiteDatabase.query(dbHandler.USER, allColumns ,
                dbHandler.USER_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        User user = cursorToUser(cursor);
        return user;
    }

    public int countUser(){
        int numRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, dbHandler.USER);
        return numRows;
    }

    private User cursorToUser(Cursor cursor) {

        User user = new User();
        user.setId(cursor.getInt(0));
        user.setEmail(cursor.getString(1));
        user.setName(cursor.getString(2));
        user.setPassword(cursor.getString(3));
        user.setApiKey(cursor.getString(4));

        return user;
    }

}
