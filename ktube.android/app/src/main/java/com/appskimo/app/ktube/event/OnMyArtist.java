package com.appskimo.app.ktube.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnMyArtist {
    private Long seq;

    public OnMyArtist(Long seq) {
        this.seq = seq;
    }
}
