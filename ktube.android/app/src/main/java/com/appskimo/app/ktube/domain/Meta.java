package com.appskimo.app.ktube.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta implements Serializable {
    private static final long serialVersionUID = 6000477617613276161L;
    private long androidVersion;
}
