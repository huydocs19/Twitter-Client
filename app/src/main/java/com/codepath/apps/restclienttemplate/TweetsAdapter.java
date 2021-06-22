package com.codepath.apps.restclienttemplate;

import android.content.Context;


import android.media.MediaPlayer;
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

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    Context context;
    List<Tweet> tweets;
    TwitterClient client;


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
        VideoView mVideoView;
        View iView;
        TextView tvYouRetweeted;
        ImageView ivYouRetweeted;

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
                ivLike.setColorFilter(context.getResources().getColor(R.color.twitter_blue_fill_pressed));
            } else {
                ivLike.setColorFilter(context.getResources().getColor(R.color.medium_gray));
            }
            if (tweet.mediaType != null) {
                if (tweet.mediaType.equals("photo")) {
                    Glide.with(context).load(tweet.mediaUrl).into(ivTweetPhoto);
                    ivTweetPhoto.setVisibility(View.VISIBLE);
                    mVideoView.setVisibility(View.GONE);
                } else {
                    mVideoView.setVideoPath(tweet.mediaUrl);
                    mVideoView.setVisibility(View.VISIBLE);
                    ivTweetPhoto.setVisibility(View.GONE);
                    playVideo(mVideoView, iView);
                }
            } else {
                mVideoView.setVisibility(View.GONE);
                ivTweetPhoto.setVisibility(View.GONE);
            }


            ivReply.setOnClickListener(new View.OnClickListener() {
                boolean replySelected = false;
                @Override
                public void onClick(View v) {
                    replySelected = !replySelected;
                    if (replySelected) {
                        ivReply.setColorFilter(context.getResources().getColor(R.color.twitter_blue_fill_pressed));

                    } else {
                        ivReply.setColorFilter(context.getResources().getColor(R.color.medium_gray));
                    }

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
