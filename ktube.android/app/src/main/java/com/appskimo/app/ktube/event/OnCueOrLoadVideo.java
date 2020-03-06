package com.appskimo.app.ktube.event;

import com.appskimo.app.ktube.domain.YoutubeVideo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnCueOrLoadVideo {
    private YoutubeVideo video;
    private From from;

    public OnCueOrLoadVideo(YoutubeVideo video, From from) {
        this.video = video;
        this.from = from;
    }

    public boolean fromActivity() {
        return from == From.ACTIVITY;
    }

    public boolean fromView() {
        return from == From.VIEW;
    }

    public enum From {
        ACTIVITY, VIEW
    }
}
