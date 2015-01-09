package com.anipr.beaconstores.dbhandler;

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
	private String tag = "DBhelper";
	private static DbHelper dbHelperSingletonInstance;

	private DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DbVersion);
	}

	public static DbHelper getInstance(Context context) {
		if (dbHelperSingletonInstance == null) {
			dbHelperSingletonInstance = new DbHelper(context);
		}
		return dbHelperSingletonInstance;
	}

	// DbTable name
	public static final String beaconsTable = "BeaconsTable";
	public static final String BEACONS_HISTORY_TABLE = "BeaconsHistoryTable";
	public static final String OFFERS_TABLE = "OffersTable";

	// beacon table columns
	public static final String beaconMAC = "MAC";
	public static final String beaconMajorId = "Major";
	public static final String beaconMinorId = "Minor";
	public static final String beaconStore = "StoreCode";
	public static final String beaconDepartment = "Department";

	// BeaconsHistoryTable columns
	public static final String BeaconEntryTableRowId = "_ID";
	public static final String BeaconEntryTime = "entry";
	public static final String BeaconExitTime = "exit";
	public static final String BeaconEntryNotificationID = "entryNotiicationID";
	public static final String BeaconExitNotificationID = "exitNotiicationID";
	public static final int BEACON_PRESENT = 1;
	public static final int BEACON_LEFT = 0;

	// OffersTable Columns
	public static final String offerCode = "_id";
	public static final String storeCode = "StoreCode";
	public static final String offerType = "OferType";
	public static final String offerName = "OfferName";
	public static final String offerDesc = "OfferDesc";
	public static final String offerStartDate = "startDate";
	public static final String offerEndDate = "endDate";
	public static final String offerMinMembership = "membership";
	public static final String minimumDuration = "timeperiod" ;
	public static final int OFFER_TYPE_ENTRY = 1;
	public static final int OFFER_TYPE_EXIT = 0 ;

	// Table Creation queries
	final String createOffersTable = "create table if not exists "
			+ OFFERS_TABLE + " ( " + offerCode + " text primary key, " + storeCode
			+ " text, " + offerType + " number, " + offerDesc + " text, "
			 + offerName + " text, "
			+ offerMinMembership + " integer, " + offerStartDate + " integer, " + minimumDuration + " integer, "
			+ offerEndDate + " integer); ";

	final String createBeaconsTable = "create table if not exists "
			+ beaconsTable + " ( " + beaconMAC + " text primary key, "
			+ beaconMajorId + " number, " + beaconMinorId + " number, "
			+ beaconStore + " text, " + beaconDepartment + " text); ";

	final String createBeaconsHIstoryTableSchema = "create table if not exists "
			+ BEACONS_HISTORY_TABLE
			+ " ( _ID INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ beaconMAC
			+ " text, "
			+ BeaconEntryTime
			+ " text, "
			+ BeaconExitTime
			+ " text, "+ BeaconEntryNotificationID
			+ " integer, " + BeaconExitNotificationID+ " integer); ";

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createBeaconsTable);
		db.execSQL(createBeaconsHIstoryTableSchema);
		db.execSQL(createOffersTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(tag, "Database Upgraded");
		db.execSQL("DROP TABLE IF EXISTS " + beaconsTable);
		onCreate(db);
	}
}
