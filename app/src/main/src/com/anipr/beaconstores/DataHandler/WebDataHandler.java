package com.anipr.beaconstores.DataHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.anipr.beaconstores.AppController;
import com.anipr.beaconstores.DbHandler.DbHelper;

/**
 * Created by Techanipr on 1/5/2015.
 */
public class WebDataHandler {
	private String tag = "WebDataHandler";
	DbHelper dbHelper;
	Context context;
	public static String ConnectionString = "http://192.168.1.30:8000/";

	WebDataHandler(Context context) {
		this.context = context;
	}

	public void getRegisteredBeacons() {
		CustomParamRequest beaconsRequest = new CustomParamRequest(Method.GET,
				WebDataHandler.ConnectionString + "beacon_mac/", null, null,
				null, new Response.Listener<String>() {

					@Override
					public void onResponse(String arg0) {
						Log.d(tag, "resposne recieved :" + arg0);
						try {
							JSONObject resposne = new JSONObject(arg0);
							if (resposne.getString("code").equals("1")) {
								JSONArray dataArray = resposne
										.getJSONArray("data");
								for (int i = 0; i < dataArray.length(); i++) {
									JSONObject currentObj = dataArray.getJSONObject(i);
									dbHelper = DbHelper.getInstance(context);
									SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
									ContentValues cv = new ContentValues();
									cv.put(DbHelper.beaconMAC,currentObj.getString("becon_id") );
									cv.put(DbHelper.beaconAssignedToStore, currentObj.getString("store_id"));
									cv.put(DbHelper.beaconDepartment, currentObj.getString("department_id"));
								}
							} else {

							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {

					}
				});
		AppController.getInstance().addToRequestQueue(beaconsRequest);

	}
}
