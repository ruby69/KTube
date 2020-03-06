package com.appskimo.app.ktube.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"videoId", "title"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Video implements YoutubeVideo {
    private static final long serialVersionUID = 6090558319443863279L;

    private String videoId;
    private String title;
    private Date publishedAt;
    private long viewCount;
    private long rankViewCount;

    @Override
    public Long getSeq() {
        return 0L;
    }

    @Override
    public void setSeq(Long seq) {
    }
}
