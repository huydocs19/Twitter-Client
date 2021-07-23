package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.codepath.apps.restclienttemplate.models.SampleModel;
import com.codepath.apps.restclienttemplate.models.SampleModelDao;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthLoginActionBarActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;

public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {

	SampleModelDao sampleModelDao;
	String TAG = "LoginActivity";
	TwitterClient client;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		final SampleModel sampleModel = new SampleModel();
		sampleModel.setName("CodePath");

		sampleModelDao = ((TwitterApp) getApplicationContext()).getMyDatabase().sampleModelDao();

		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				sampleModelDao.insertModel(sampleModel);
			}
		});
	}


	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	// OAuth authenticated successfully, launch primary authenticated activity
	// i.e Display application "homepage"
	@Override
	public void onLoginSuccess() {
		Log.d("LoginActivity", "login success");
		client = TwitterApp.getRestClient(this);
		client.getUserInfo(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Headers headers, JSON json) {
				String userImageUrl = "";
				JSONObject jsonObject = json.jsonObject;

				try {
					 if (jsonObject.has("profile_image_url_https") && ( jsonObject.getString("profile_image_url_https") != null
							|| !jsonObject.getString("profile_image_url_https").isEmpty())) {
						userImageUrl = json.jsonObject.getString("profile_image_url_https");
					} else if (jsonObject.has("profile_image_url") && ( jsonObject.getString("profile_image_url") != null
							|| !jsonObject.getString("profile_image_url").isEmpty())) {
						userImageUrl = json.jsonObject.getString("profile_image_url");
					} else {
						userImageUrl = "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png";
					}

					Log.d(TAG, userImageUrl);
				} catch (JSONException e) {
					Log.e(TAG, "Json exception", e);
				}
				SharedPreferences pref =
						PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
				SharedPreferences.Editor edit = pref.edit();
				edit.putString("userImageUrl", userImageUrl);
				edit.commit();
			}

			@Override
			public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
				Log.e(TAG, "onFailure " + response, throwable);
			}
		});
		Intent i = new Intent(this, TimelineActivity.class);
		startActivity(i);
	}

	// OAuth authentication flow failed, handle the error
	// i.e Display an error dialog or toast
	@Override
	public void onLoginFailure(Exception e) {
		e.printStackTrace();
	}

	// Click handler method for the button used to start OAuth flow
	// Uses the client to initiate OAuth authorization
	// This should be tied to a button used to login
	public void loginToRest(View view) {
		getClient().connect();
	}

}
