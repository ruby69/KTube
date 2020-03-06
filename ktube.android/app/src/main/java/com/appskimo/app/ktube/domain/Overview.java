package com.appskimo.app.ktube.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Overview implements Serializable {
    private static final long serialVersionUID = 7819314741025118537L;

    private ArrayList<Video> news;
    private ArrayList<Video> millions;
    private ArrayList<Video> videos;
    private ArrayList<Video> fancams;
    private ArrayList<Video> karaokes;
    private ArrayList<Video> lyrics;
    @JsonProperty("idols") private ArrayList<Artist> artists;

}
