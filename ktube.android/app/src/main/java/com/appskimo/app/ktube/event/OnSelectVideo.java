package com.appskimo.app.ktube.event;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.domain.YoutubeVideo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnSelectVideo {
    private YoutubeVideo video;
    private Constants.VideoDomain videoDomain;

    public OnSelectVideo(YoutubeVideo video, Constants.VideoDomain videoDomain) {
        this.video = video;
        this.videoDomain = videoDomain;
    }
}
