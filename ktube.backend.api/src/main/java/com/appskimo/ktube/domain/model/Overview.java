package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Overview implements Serializable {
    private static final long serialVersionUID = 6758594946830334408L;

    private List<?> news;
    private List<?> millions;
    private List<?> videos;
    private List<?> fancams;
    private List<?> karaokes;
    private List<?> lyrics;
    private List<Idol> idols;
}
