package com.appskimo.app.ktube.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnListedVideo {
    private Long seq;

    public OnListedVideo(Long seq) {
        this.seq = seq;
    }
}
