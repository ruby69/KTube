package com.appskimo.app.ktube.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(of = {"videoId", "title"})
@DatabaseTable(tableName = "FavoriteVideo")
public class VideoFavorite implements YoutubeVideo {
    private static final long serialVersionUID = -8613290018551157495L;
    public static final String FIELD_seq = "favoriteSeq";

    @DatabaseField(columnName = FIELD_seq, generatedId = true) protected Long favoriteSeq;
    @DatabaseField(index = true) private String videoId;
    @DatabaseField private String title;
    @DatabaseField private Date publishedAt;

    public VideoFavorite(YoutubeVideo video) {
        this.videoId = video.getVideoId();
        this.title = video.getTitle();
        this.publishedAt = video.getPublishedAt();
    }

    @Override
    public long getViewCount() {
        return 0L;
    }

    @Override
    public long getRankViewCount() {
        return 0L;
    }

    @Override
    public Long getSeq() {
        return favoriteSeq;
    }

    @Override
    public void setSeq(Long seq) {
        favoriteSeq = seq;
    }
}
