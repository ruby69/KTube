package com.appskimo.ktube.domain.model;

import lombok.Getter;

public enum App {
    KTUBE("MetaKTube"),
    YTMUSIC("MetaYtMusic");

    @Getter private String table;

    App(String table){
        this.table = table;
    }
}
