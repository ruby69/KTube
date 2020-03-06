package com.appskimo.app.ktube.event;

import com.appskimo.app.ktube.domain.SupportLanguage;

import lombok.Data;

@Data
public class OnSelectLanguage {
    private SupportLanguage supportLanguage;

    public OnSelectLanguage(SupportLanguage supportLanguage) {
        this.supportLanguage = supportLanguage;
    }
}
