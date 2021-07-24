package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyActivity extends AppCompatActivity {

    public static final String TAG = "ReplyActivity";

    public static final int MAX_TWEET_LENGTH = 140;


    EditText etReply;
    Button btnReply;
    ImageView ivReplyAvatar;
    TwitterClient client;
    TextView tvCancel;
    TextView tvReplyTo;
    TextView tvWordCount;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        client = TwitterApp.getRestClient(this);


        etReply = findViewById(R.id.etReply);
        btnReply = findViewById(R.id.btnReply);
        tvCancel = findViewById(R.id.tvCancel);
        tvReplyTo = findViewById(R.id.tvReplyTo);
        ivReplyAvatar = findViewById(R.id.ivReplyAvatar);

        tvReplyTo.setText("Reply to @" + getIntent().getStringExtra("username"));
        Glide.with(this).load(getIntent().getStringExtra("avatar")).into(ivReplyAvatar);
        tvWordCount = findViewById(R.id.tvWordCount);

        etReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String tweetContent = s.toString();
                tvWordCount.setText(String.valueOf(280 - s.length()));
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    tvWordCount.setTextColor(Color.RED);
                }
            }
        });

        // Set click listener on button
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tweetContent = etReply.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your reply cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your reply is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(ReplyActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                String username = getIntent().getStringExtra("username");
                long statusID = getIntent().getLongExtra("status_id", 0);
                Log.i(TAG, "ReplyActivity " + username + " " + statusID);
                String replyContent = "@" + username + " " + tweetContent;
                // Make an API call to Twitter to publish the tweet
                client.replyTweet(replyContent, statusID, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to reply tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Replied tweet says " + tweet.body);
                            // Prepare data intent
                            Intent data = new Intent();
                            // Pass relevant data back as a result
                            data.putExtra("tweet", Parcels.wrap(tweet));
                            // Activity finished ok, return the data
                            setResult(RESULT_OK, data); // set result code and bundle data for response
                            finish(); // closes the activity, pass data to parent
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to reply tweet" + String.valueOf(statusCode), throwable);
                    }
                });

            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                finish();
            }
        });


    }
}