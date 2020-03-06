package com.appskimo.app.ktube.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"title", "site"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioStation implements Serializable {
    private static final long serialVersionUID = 3765110555760404085L;

    private Long radioStationUid;
    private String title;
    private String site;
    private String stream;
    private String image;
    private Date registTime;

    private int index;
}
