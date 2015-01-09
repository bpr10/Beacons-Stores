package com.anipr.beaconstores;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends ActionBarActivity {
	private EditText userName, password;
	private Button loginButton;
	private String tag = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		getSupportActionBar().hide();
		userName = (EditText) findViewById(R.id.login_username_field);
		password = (EditText) findViewById(R.id.login_password_field);
		loginButton = (Button) findViewById(R.id.button1);
		loginButton.setOnClickListener(new OnClickListener() {

			
			@Override
			public void onClick(View v) {
				if(userName.getText().length()!=0){
					if(userName.getText().length()!=0){
						//Fire Request
						saveUserLoginDetails("User0ne");
						Intent i = new Intent(Login.this,MainActivity.class);
						startActivity(i);
						finish();
					}else{
						Log.d(tag, "Password Blank");
						password.setError("Please Enter your password");
					}
				}else{
					Log.d(tag, "UserName Blank");
					userName.setError("Username can't be blank");
				}	
			}
		});
	}

		void saveUserLoginDetails(String userName){
		SharedPreferences loginPreferences = getSharedPreferences(
				CommonConstants.LOGIN_SHARED_PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = loginPreferences.edit();
		editor.putString(CommonConstants.LOGIN_SHARED_PREF_NAME_LoggedUser,userName ) ;
		editor.commit();
		Log.i(tag, "User Login Details saved");
	}
}
