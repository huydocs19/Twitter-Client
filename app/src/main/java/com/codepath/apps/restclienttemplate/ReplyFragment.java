package com.codepath.apps.restclienttemplate;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

import static android.app.Activity.RESULT_OK;

public class ReplyFragment extends Fragment {
    private static final int MAX_TWEET_LENGTH = 280;
    private static final String TAG = "ReplyFragment";

    EditText etReply;
    Button btnReply;
    TextView tvWordCount;
    TextView tvCancel;
    TwitterClient client;
    public ReplyFragment() {

    }
    public static ReplyFragment newInstance(String screenName, long tweetId) {
        ReplyFragment replyFragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putLong("tweetId", tweetId);
        args.putString("screenName", screenName);
        replyFragment.setArguments(args);
        return replyFragment;
    }
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_reply, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        etReply = view.findViewById(R.id.etReply);
        btnReply = view.findViewById(R.id.btnReply);
        tvWordCount = view.findViewById(R.id.tvWordCount);
        tvCancel = view.findViewById(R.id.tvCancel);
        client = TwitterApp.getRestClient(getContext());

        etReply.requestFocus();
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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


        // Set click listener  bonutton
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tweetContent = etReply.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(getContext(), "Sorry, your reply cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getContext(), "Sorry, your reply is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getContext(), "Reply sent", Toast.LENGTH_LONG).show();
                String username = getArguments().getString("screenName", "");
                long statusID = getArguments().getLong("tweetId", 0);;
                Log.i(TAG, "ReplyFragment " + username + " " + statusID);
                String replyContent = "@" + username + " " + tweetContent;
                // Make an API call to Twitter to publish the tweet
                client.replyTweet(replyContent, statusID, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says " + tweet.body);
                            // Prepare data intent
                            Intent newData = new Intent();
                            // Pass relevant data back as a result
                            newData.putExtra("tweet", Parcels.wrap(tweet));
                            // Activity finished ok, return the data
                            getActivity().setResult(RESULT_OK, newData); // set result code and bundle data for response
                            getActivity().getSupportFragmentManager().beginTransaction().remove(ReplyFragment.this).commit();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });

            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().beginTransaction().remove(ReplyFragment.this).commit();
            }
        });
    }
}
