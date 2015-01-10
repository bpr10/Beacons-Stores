package com.anipr.beaconstores;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AppController extends Application {
	public static final String TAG = AppController.class.getSimpleName();
	private RequestQueue mRequestQueue;
	public static String userCode, cookie;
	private static AppController mInstance;
	static void playSound(Context context, int resId) {
		MediaPlayer mp = MediaPlayer.create(context, resId);
		mp.start();
		mp.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});
	}
	public boolean userLoggedIn() {
		SharedPreferences loginPreferences = getSharedPreferences(
				CommonConstants.LOGIN_SHARED_PREF_NAME, Context.MODE_PRIVATE);
		if (loginPreferences.getString(
				CommonConstants.LOGIN_SHARED_PREF_NAME_LoggedUser,
				CommonConstants.NO_LOGGED_USER).equals(
				CommonConstants.NO_LOGGED_USER)) {
			return false;
		} else {
			return true;
		}

	}
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

}
