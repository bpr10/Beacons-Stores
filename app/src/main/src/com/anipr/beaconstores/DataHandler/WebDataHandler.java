package com.anipr.beaconstores.DataHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

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
//	public static String ConnectionString = "http://ehapi.cloudapp.net:9000/";
	public static String ConnectionString = "http://ehapi-cloudapp-net-8wdgc88jg5qe.runscope.net/";
	public WebDataHandler(Context context) {
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
								Toast.makeText(context, "Web data Refreshed",
										2000).show();
								for (int i = 0; i < dataArray.length(); i++) {
									JSONObject currentObj = dataArray
											.getJSONObject(i);
									dbHelper = DbHelper.getInstance(context);
									SQLiteDatabase dbWrite = dbHelper
											.getWritableDatabase();
									ContentValues cv = new ContentValues();
									cv.put(DbHelper.beaconMAC,
											currentObj.getString("becon_id"));
									cv.put(DbHelper.beaconStore,
											currentObj.getString("store_id"));
									cv.put(DbHelper.beaconDepartment,
											currentObj
													.getString("department_id"));
									dbWrite.insert(DbHelper.beaconsTable, "",
											cv);

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

	public void getOffers() {
		CustomParamRequest beaconsRequest = new CustomParamRequest(Method.GET,
				WebDataHandler.ConnectionString + "offer_list/", null, null,
				null, new Response.Listener<String>() {

					@Override
					public void onResponse(String arg0) {
						Log.d(tag, "resposne recieved :" + arg0);
						try {
							JSONObject resposne = new JSONObject(arg0);
							if (resposne.getString("code").equals("1")) {
								JSONArray dataArray = resposne
										.getJSONArray("data");
								Toast.makeText(context, "Web data Refreshed",
										2000).show();
								for (int i = 0; i < dataArray.length(); i++) {
									JSONObject currentObj = dataArray
											.getJSONObject(i);
									dbHelper = DbHelper.getInstance(context);
									SQLiteDatabase dbWrite = dbHelper
											.getWritableDatabase();
									ContentValues cv = new ContentValues();
									cv.put(DbHelper.offerCode,
											currentObj.getString("offer_code"));
									cv.put(DbHelper.storeCode,
											currentObj.getString("store_code"));

									cv.put(DbHelper.offerType, currentObj
											.getString("entry_exit_type"));
									cv.put(DbHelper.offerName,
											currentObj.getString("offername"));

									cv.put(DbHelper.minimumDuration, currentObj
											.getString("min_stay_time"));

									cv.put(DbHelper.offerDesc,
											currentObj.getString("description"));

									cv.put(DbHelper.offerMinMembership,
											currentObj.getString("membership"));
									cv.put(DbHelper.offerStartDate,
											currentObj.getString("start_time"));
									cv.put(DbHelper.offerEndDate,
											currentObj.getString("end_time"));
									dbWrite.insert(DbHelper.OFFERS_TABLE, null,
											cv);
									Log.d(tag, "Offers Inserted");
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
