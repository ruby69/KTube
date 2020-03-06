package com.appskimo.app.ktube.event;

import com.appskimo.app.ktube.domain.RadioStation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnSelectRadioStation {
    private RadioStation radioStation;

    public OnSelectRadioStation(RadioStation radioStation) {
        this.radioStation = radioStation;
    }
}
