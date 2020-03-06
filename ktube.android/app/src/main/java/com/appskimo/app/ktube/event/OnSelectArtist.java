package com.appskimo.app.ktube.event;

import com.appskimo.app.ktube.domain.Artist;

import lombok.Data;

@Data
public class OnSelectArtist {
    private Artist artist;

    public OnSelectArtist(Artist artist) {
        this.artist = artist;
    }
}
