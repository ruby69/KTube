package com.appskimo.app.ktube.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnFavoriteVideo {
    private Long seq;

    public OnFavoriteVideo(Long seq) {
        this.seq = seq;
    }
}
