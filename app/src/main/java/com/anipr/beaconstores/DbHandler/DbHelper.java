package com.anipr.beaconstores.DbHandler;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Techanipr on 1/5/2015.
 */
public class DbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "BeaconOffersDB";
    static final int DbVersion = 1;
    DatabaseErrorHandler errorHandler;
    private String tag ="DBhelper";
    private DbHelper dbHelper;
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DbVersion);
    }
    public DbHelper getInstance(Context context){
        if(dbHelper == null) {
            dbHelper = new DbHelper(context);
        }
        return dbHelper;
    }
    // DbTable name
    public static String beaconsTable = "BeaconsTable";

    //beacon table columns
    public static String beaconMAC = "BeaconMACAddress";
    public static String beaconMajorId = "BeaconMajorId";
    public static String beaconMinorId = "BeaconMinorId";
    public static String beaconAssignedToStore = "BeaconConnectedToStore";
    public static String beaconDepartment = "BeaconBelongsToDept";


    //Table Creation queries

    final String createBeaconsTable = "create table if not exists " + beaconsTable + " ( " + beaconMAC + " text primary key, " + beaconMajorId + " number, " + beaconMinorId + " number, " + beaconAssignedToStore + " text, " + beaconDepartment + " text); ";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createBeaconsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(tag,"Database Upgraded");
        db.execSQL("DROP TABLE IF EXISTS " + beaconsTable);
        onCreate(db);
    }
}
