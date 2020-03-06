package com.appskimo.app.ktube.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"page"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -3746679396163423079L;

    private int page;
    private int scale;
    private int total;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<T> contents;
}
