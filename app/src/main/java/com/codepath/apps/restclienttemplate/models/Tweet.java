package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.codepath.apps.restclienttemplate.TimeFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId"))
public class Tweet {

    @ColumnInfo
    @PrimaryKey
    public long id;
    @ColumnInfo
    public String body;
    @ColumnInfo
    public String createdAt;
    @ColumnInfo
    public long userId;
    @ColumnInfo
    public int retweetCount;
    @ColumnInfo
    public int favoriteCount;
    @ColumnInfo
    public String mediaType;
    @ColumnInfo
    public String mediaUrl;
    @ColumnInfo
    public boolean retweeted;
    @ColumnInfo
    public boolean favorited;
    @Ignore
    public User user;



    // empty constructor needed by the Parceler library
    public Tweet() {

    }

    public Tweet(Tweet another) {
        this.id = another.id;
        this.body = another.body;
        this.createdAt = another.createdAt;
        this.userId = another.userId;
        this.retweetCount = another.retweetCount;
        this.favoriteCount = another.favoriteCount;
        this.mediaType = another.mediaType;
        this.mediaUrl = another.mediaUrl;
        this.retweeted = another.retweeted;
        this.favorited = another.favorited;
        this.user = new User(another.user);
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.id = jsonObject.getLong("id");
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.favorited = jsonObject.getBoolean("favorited");
        setMediaTypeAndURL(jsonObject, tweet);
        if (tweet.mediaUrl != null) {
            Log.d("Tweet", tweet.getFormattedTimestamp() + " " + tweet.mediaType + ": " + tweet.mediaUrl);
        }





        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
    public String getFormattedTimestamp() {
        return TimeFormatter.getTimeDifference(createdAt);
    }
    public static void setMediaTypeAndURL(JSONObject jsonObject, Tweet tweet) throws JSONException {

        JSONObject entitiesJsonObject = jsonObject.getJSONObject("entities");
        if (entitiesJsonObject.has("media")) {
            JSONArray mediaJsonArray = entitiesJsonObject.getJSONArray("media");
            JSONObject firstMedia = mediaJsonArray.getJSONObject(0);
            tweet.mediaUrl = firstMedia.getString("media_url_https");
            tweet.mediaType = firstMedia.getString("type");
        }

    }

}
