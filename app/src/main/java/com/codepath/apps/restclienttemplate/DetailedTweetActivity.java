package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class DetailedTweetActivity extends AppCompatActivity {
    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvName;
    TextView tvRetweetCount;
    TextView tvFavoriteCount;
    ImageView ivReply;
    ImageView ivRetweet;
    ImageView ivLike;
    ImageView ivTweetPhoto;
    FrameLayout media;
    // VideoView mVideoView;
    TextView tvWordCount;
    TextView tvTime;
    TextView tvDate;
    Button btnReply;
    TextView tvTweetReply;
    EditText etTweet2;
    PlayerView mVideoView;
    SimpleExoPlayer absPlayerInternal;
    RelativeLayout reply;
    ImageView ivClose;
    private static final int REQUEST_CODE = 21;
    TwitterClient client;
    InputMethodManager imm;

    public static final String TAG = "DetailedTweetActivity";

    public static final int MAX_TWEET_LENGTH = 140;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_tweet);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        // Remove default title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvBody = findViewById(R.id.tvBody);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvName = findViewById(R.id.tvName);
        tvRetweetCount = findViewById(R.id.tvRetweetCount);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        ivReply = findViewById(R.id.ivReply);
        ivRetweet = findViewById(R.id.ivRetweet);
        ivLike = findViewById(R.id.ivLike);
        ivTweetPhoto = findViewById(R.id.ivTweetPhoto);
        media = findViewById(R.id.media);
        mVideoView = findViewById(R.id.vvTweetVideo);
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        btnReply = findViewById(R.id.btnReply);
        tvTweetReply = findViewById(R.id.tvTweetReply);
        etTweet2 = findViewById(R.id.etReply);
        reply = findViewById(R.id.reply);
        client = TwitterApp.getRestClient(this);
        tvWordCount = findViewById(R.id.tvWordCount);
        ivClose = findViewById(R.id.ivClose);

        Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        bind(tweet);
    }

    public void bind(Tweet tweet) {

        tvBody.setText(tweet.body);
        tvScreenName.setText("@" + tweet.user.screenName);
        tvName.setText(tweet.user.name);

        tvFavoriteCount.setText(String.valueOf(tweet.favoriteCount) + " Likes");
        tvRetweetCount.setText(String.valueOf(tweet.retweetCount) + " Retweets");
        Glide.with(this).load(tweet.user.profileImageUrl).into(ivProfileImage);
        tvTime.setText(tweet.getTime());
        tvDate.setText(tweet.getDate());
        tvTweetReply.setText("Reply to " + tweet.user.name);


        tvTweetReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Begin the transaction
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                // Replace the contents of the container with the new fragment
                ft.replace(R.id.fragment_reply_placeholder, ReplyFragment.newInstance(tweet.user.screenName, tweet.id));
                // or ft.add(R.id.your_placeholder, new FooFragment());
                // Complete the changes added above
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (tweet.favorited) {
            ivLike.setColorFilter(getResources().getColor(R.color.twitter_blue_fill_pressed));
        } else {
            ivLike.setColorFilter(getResources().getColor(R.color.medium_gray));
        }
        if (tweet.retweeted) {
            ivRetweet.setColorFilter(getResources().getColor(R.color.twitter_blue_fill_pressed));
        } else {
            ivRetweet.setColorFilter(getResources().getColor(R.color.medium_gray));
        }
        if (tweet.mediaType != null) {
            if (tweet.mediaType.equals("photo")) {
                Glide.with(this).load(tweet.mediaUrl).into(ivTweetPhoto);
                ivTweetPhoto.setVisibility(View.VISIBLE);
                mVideoView.setVisibility(View.GONE);
            } else {
                mVideoView.setVisibility(View.VISIBLE);
                ivTweetPhoto.setVisibility(View.GONE);
                prepareToPlayVideo(tweet.mediaUrl, mVideoView);
            }
        } else {
            mVideoView.setVisibility(View.GONE);
            ivTweetPhoto.setVisibility(View.GONE);
        }


        ivReply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Navigate to the compose Activity
                Intent intent = new Intent(DetailedTweetActivity.this, ReplyActivity.class);
                intent.putExtra("username", tweet.user.screenName);
                intent.putExtra("status_id", tweet.id);
                intent.putExtra("avatar", tweet.user.profileImageUrl);
                DetailedTweetActivity.this.startActivityForResult(intent, REQUEST_CODE);

            }
        });
        ivRetweet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("TweetsAdapter", "Click on retweet " + tweet.retweeted);
                client = TwitterApp.getRestClient(DetailedTweetActivity.this);
                tweet.retweeted = !tweet.retweeted;
                if (tweet.retweeted) {
                    client.retweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            ivRetweet.setColorFilter(getResources().getColor(R.color.twitter_blue_fill_pressed));
                            int newRetweetCount = tweet.retweetCount + 1;
                            tweet.retweetCount = newRetweetCount;
                            tvRetweetCount.setText(String.valueOf(newRetweetCount) + "Retweets");

                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Toast.makeText(DetailedTweetActivity.this, "Cannot retweet", Toast.LENGTH_SHORT);
                        }
                    });

                } else {

                    client.unRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            ivRetweet.setColorFilter(getResources().getColor(R.color.medium_gray));
                            int newRetweetCount = tweet.retweetCount - 1;
                            tweet.retweetCount = newRetweetCount;
                            tvRetweetCount.setText(String.valueOf(newRetweetCount)  + "Retweets");
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Toast.makeText(DetailedTweetActivity.this, "Cannot unretweet", Toast.LENGTH_SHORT);
                        }
                    });
                }


            }
        });
        ivLike.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tweet.favorited = !tweet.favorited;
                client = TwitterApp.getRestClient(DetailedTweetActivity.this);
                if (tweet.favorited) {
                    client.favoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            ivLike.setColorFilter(getResources().getColor(R.color.twitter_blue_fill_pressed));
                            int newFavoriteCount = tweet.favoriteCount + 1;
                            tweet.favoriteCount = newFavoriteCount;
                            tvFavoriteCount.setText(String.valueOf(newFavoriteCount) + "Likes");
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Toast.makeText(DetailedTweetActivity.this, "Cannot favorite this tweet", Toast.LENGTH_SHORT);
                        }
                    });
                } else {

                    client.unFavoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            ivLike.setColorFilter(getResources().getColor(R.color.medium_gray));
                            int newFavoriteCount = tweet.favoriteCount - 1;
                            tweet.favoriteCount = newFavoriteCount;
                            tvFavoriteCount.setText(String.valueOf(newFavoriteCount) + "Likes");
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Toast.makeText(DetailedTweetActivity.this, "Cannot favorite this tweet", Toast.LENGTH_SHORT);
                        }
                    });

                }

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {

                // Get data from the intent (tweet)
                Tweet newTweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
                // Prepare data intent
                Intent newData = new Intent();
                // Pass relevant data back as a result
                newData.putExtra("tweet", Parcels.wrap(newTweet));
                // Activity finished ok, return the data
                setResult(RESULT_OK, newData); // set result code and bundle data for response



            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void prepareToPlayVideo(String mediaUrl, PlayerView mVideoView) {
        try {



            // track selector is used to navigate between
            // video using a default seekbar.
            TrackSelector trackSelectorDef = new DefaultTrackSelector();


            // we are adding our track selector to exoplayer.
            absPlayerInternal = ExoPlayerFactory.newSimpleInstance(this, trackSelectorDef); //creating a player instance

            // we are parsing a video url
            // and parsing its video uri.
            Uri uriOfContentUrl = Uri.parse(mediaUrl);


            MediaSource mediaSource = buildMediaSource(uriOfContentUrl);  // creating a media source

            // inside our exoplayer view
            // we are setting our player
            mVideoView.setPlayer(absPlayerInternal);

            // we are preparing our exoplayer
            // with media source.
            absPlayerInternal.prepare(mediaSource);

            // we are setting our exoplayer
            // when it is ready.
            //absPlayerInternal.setPlayWhenReady(true);

        } catch (Exception e) {
            // below line is used for
            // handling our errors.
            Log.e("TAG", "Error : " + e.toString());
        }
    }
    private MediaSource buildMediaSource(Uri uri) {
        int appNameStringRes = R.string.app_name;
        // we are creating a variable for datasource factory
        // and setting its user agent as 'exoplayer_view'
        String userAgent = Util.getUserAgent(DetailedTweetActivity.this, getString(appNameStringRes));
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(DetailedTweetActivity.this,userAgent);

        // MediaSource mediaSource = new ProgressiveMediaSource.Factory(defdataSourceFactory).createMediaSource(uriOfContentUrl);  // creating a media source
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

}


