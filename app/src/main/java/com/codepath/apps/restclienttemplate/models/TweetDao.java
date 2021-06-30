package com.codepath.apps.restclienttemplate.models;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TweetDao {

    @Query("SELECT Tweet.body AS tweet_body, Tweet.createdAt as tweet_createdAt, Tweet.id AS tweet_id, Tweet.userId AS tweet_userId," +
            "Tweet.retweetCount AS tweet_retweetCount, Tweet.favoriteCount AS tweet_favoriteCount," +
            "Tweet.mediaType AS tweet_mediaType, Tweet.mediaUrl AS tweet_mediaUrl, Tweet.favorited AS tweet_favorited, " +
            "Tweet.retweeted AS tweet_retweeted," +
            "User.* FROM Tweet INNER JOIN User ON Tweet.userId = User.id " +
            "ORDER BY Tweet.id DESC LIMIT 100")
    List<TweetWithUser> recentItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(Tweet... tweets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);


}
