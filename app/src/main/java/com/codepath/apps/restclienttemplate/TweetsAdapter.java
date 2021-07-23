package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    private static final int REQUEST_CODE = 21;
    Context context;
    List<Tweet> tweets;
    TwitterClient client;
    SimpleExoPlayer absPlayerInternal;



    // Pass in context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // For each row, inflate the layout
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get data at position
        Tweet tweet = tweets.get(position);

        // Bind tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvName;
        TextView tvRelativeTimestamp;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;
        ImageView ivReply;
        ImageView ivRetweet;
        ImageView ivLike;
        ImageView ivTweetPhoto;
        FrameLayout media;
        // VideoView mVideoView;
        View iView;
        TextView tvYouRetweeted;
        ImageView ivYouRetweeted;
        PlayerView mVideoView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivTweetPhoto = itemView.findViewById(R.id.ivTweetPhoto);
            media = itemView.findViewById(R.id.media);
            mVideoView = itemView.findViewById(R.id.vvTweetVideo);
            iView = itemView;
            tvYouRetweeted = itemView.findViewById(R.id.tvYouRetweeted);
            ivYouRetweeted = itemView.findViewById(R.id.ivYouRetweeted);

        }

        public void bind(Tweet tweet) {

            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvName.setText(tweet.user.name);
            tvRelativeTimestamp.setText(tweet.getFormattedTimestamp());
            tvFavoriteCount.setText(String.valueOf(tweet.favoriteCount));
            tvRetweetCount.setText(String.valueOf(tweet.retweetCount));
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);


            if (tweet.favorited) {
                ivLike.setColorFilter(context.getResources().getColor(R.color.twitter_blue_fill_pressed));
            } else {
                ivLike.setColorFilter(context.getResources().getColor(R.color.medium_gray));
            }
            if (tweet.retweeted) {
                ivRetweet.setColorFilter(context.getResources().getColor(R.color.twitter_blue_fill_pressed));
            } else {
                ivRetweet.setColorFilter(context.getResources().getColor(R.color.medium_gray));
            }
            if (tweet.mediaType != null) {
                if (tweet.mediaType.equals("photo")) {
                    Glide.with(context).load(tweet.mediaUrl).into(ivTweetPhoto);
                    ivTweetPhoto.setVisibility(View.VISIBLE);
                    mVideoView.setVisibility(View.GONE);
                } else {
                    /**
                    mVideoView.setVideoPath(tweet.mediaUrl);
                    mVideoView.setVisibility(View.VISIBLE);
                    ivTweetPhoto.setVisibility(View.GONE);
                    playVideo(mVideoView, iView);
                     */
                    mVideoView.setVisibility(View.VISIBLE);
                    ivTweetPhoto.setVisibility(View.GONE);
                    prepareToPlayVideo(tweet.mediaUrl, mVideoView);
                }
            } else {
                mVideoView.setVisibility(View.GONE);
                ivTweetPhoto.setVisibility(View.GONE);
            }
            tvName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Navigate to the compose Activity
                    Intent intent = new Intent(context, DetailedTweetActivity.class);
                    intent.putExtra("tweet", Parcels.wrap(tweet));
                    ((TimelineActivity) context).startActivityForResult(intent, REQUEST_CODE);
                }
            });
            tvScreenName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Navigate to the compose Activity
                    Intent intent = new Intent(context, DetailedTweetActivity.class);
                    intent.putExtra("tweet", Parcels.wrap(tweet));
                    ((TimelineActivity) context).startActivityForResult(intent, REQUEST_CODE);
                }
            });
            tvBody.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Navigate to the compose Activity
                    Intent intent = new Intent(context, DetailedTweetActivity.class);
                    intent.putExtra("tweet", Parcels.wrap(tweet));
                    ((TimelineActivity) context).startActivityForResult(intent, REQUEST_CODE);
                }
            });

            ivReply.setOnClickListener(new View.OnClickListener() {
                boolean replySelected = false;
                @Override
                public void onClick(View v) {
                    // Navigate to the compose Activity
                    Intent intent = new Intent(context, ReplyActivity.class);
                    intent.putExtra("username", tweet.user.screenName);
                    intent.putExtra("status_id", tweet.id);
                    intent.putExtra("avatar", tweet.user.profileImageUrl);
                    ((TimelineActivity) context).startActivityForResult(intent, REQUEST_CODE);
                }
            });
            ivRetweet.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("TweetsAdapter", "Click on retweet " + tweet.retweeted);
                    client = TwitterApp.getRestClient(context);
                    tweet.retweeted = !tweet.retweeted;
                    if (tweet.retweeted) {
                        client.retweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                ivRetweet.setColorFilter(context.getResources().getColor(R.color.twitter_blue_fill_pressed));
                                int newRetweetCount = tweet.retweetCount + 1;
                                tweet.retweetCount = newRetweetCount;
                                tvRetweetCount.setText(String.valueOf(newRetweetCount));

                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Toast.makeText(context, "Cannot retweet", Toast.LENGTH_SHORT);
                            }
                        });

                    } else {

                        client.unRetweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                ivRetweet.setColorFilter(context.getResources().getColor(R.color.medium_gray));
                                int newRetweetCount = tweet.retweetCount - 1;
                                tweet.retweetCount = newRetweetCount;
                                tvRetweetCount.setText(String.valueOf(newRetweetCount));
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Toast.makeText(context, "Cannot unretweet", Toast.LENGTH_SHORT);
                            }
                        });
                    }



                }
            });
            ivLike.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    tweet.favorited = !tweet.favorited;
                    client = TwitterApp.getRestClient(context);
                    if (tweet.favorited) {
                        client.favoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                ivLike.setColorFilter(context.getResources().getColor(R.color.twitter_blue_fill_pressed));
                                int newFavoriteCount = tweet.favoriteCount + 1;
                                tweet.favoriteCount = newFavoriteCount;
                                tvFavoriteCount.setText(String.valueOf(newFavoriteCount));
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Toast.makeText(context, "Cannot favorite this tweet", Toast.LENGTH_SHORT);
                            }
                        });
                    } else {

                        client.unFavoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                ivLike.setColorFilter(context.getResources().getColor(R.color.medium_gray));
                                int newFavoriteCount = tweet.favoriteCount - 1;
                                tweet.favoriteCount = newFavoriteCount;
                                tvFavoriteCount.setText(String.valueOf(newFavoriteCount));
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Toast.makeText(context, "Cannot favorite this tweet", Toast.LENGTH_SHORT);
                            }
                        });

                    }

                }
            });
        }
    }

    private void prepareToPlayVideo(String mediaUrl, PlayerView mVideoView) {
        try {



            // track selector is used to navigate between
            // video using a default seekbar.
            TrackSelector trackSelectorDef = new DefaultTrackSelector();


            // we are adding our track selector to exoplayer.
            absPlayerInternal = ExoPlayerFactory.newSimpleInstance(context, trackSelectorDef); //creating a player instance

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
        String userAgent = Util.getUserAgent(context, context.getString(appNameStringRes));
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,userAgent);

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


    private void playVideo(VideoView mVideoView, View iView) {

        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(mVideoView);

        mVideoView.setMediaController(mediaController);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = (Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        mediaController.setLayoutParams(lp);

        ((ViewGroup) mediaController.getParent()).removeView(mediaController);

        ((FrameLayout) iView.findViewById(R.id.tweetVideoWrapper)).addView(mediaController);
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                Log.i("TweetsAdapter", "Duration = " +
                        mVideoView.getDuration());
                mVideoView.start();

            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(context, "Error Playing Video", Toast.LENGTH_LONG);
                return false;
            }
        });
    }
}
