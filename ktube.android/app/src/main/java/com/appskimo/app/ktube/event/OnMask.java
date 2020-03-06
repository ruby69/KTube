package com.appskimo.app.ktube.event;

import lombok.Getter;

public class OnMask {
    @Getter private boolean enable;

    public OnMask(boolean enable) {
        this.enable = enable;
    }
}
