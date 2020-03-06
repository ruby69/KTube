package com.appskimo.app.ktube.domain;

import java.io.Serializable;
import java.util.Date;

public interface YoutubeVideo extends Serializable {
    String FIELD_videoId = "videoId";

    String getVideoId();

    String getTitle();

    Date getPublishedAt();

    long getViewCount();

    long getRankViewCount();


    Long getSeq();
    void setSeq(Long seq);
}
