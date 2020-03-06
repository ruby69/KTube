package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class Page implements Serializable {
    private static final long serialVersionUID = -6393774940845746189L;

    private int page = 1;
    private int scale = 15;
    private int total = 0;
    @JsonIgnore private int pageOffset = 2;
    private List<?> contents;
    private Map<String, Object> p = new HashMap<String, Object>();

    public int getBeginIndex(){
        return (page - 1) * scale;
    }

    public int getTotalPages(){
        if(total == scale) {
            return 1;
        } else if(total % scale == 0) {
            return total / scale;
        } else {
            return (total / scale) + 1;
        }
    }

    public void setPage(Integer page){
        this.page = page == null || page.intValue() < 1 ? 1 : page.intValue();
    }

    public void setTotal(int total) {
        this.total = total;
        int totalPages = getTotalPages();
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }
    }

    public int getStartPage() {
        int start = 0;
        int totalPages = getTotalPages();
        if(page <= pageOffset) {
            start = 1;
        } else if(page > (totalPages - pageOffset)) {
            start = totalPages - (pageOffset * 2);
        } else {
            start = page - pageOffset;
        }

        return start <= 1 ? 1 : start;
    }

    public int getEndPage() {
        int end = 0;
        int totalPages = getTotalPages();
        if(page <= pageOffset) {
            end = (pageOffset * 2) + 1;
        } else if(page > (totalPages - pageOffset)) {
            end = totalPages;
        } else {
            end = page + pageOffset;
        }
        return end >= totalPages ? totalPages : end;
    }

    public boolean isHasNext() {
        return getEndPage() < getTotalPages();
    }

    public boolean isHasPrevious() {
        return getStartPage() > 1;
    }

    public Page clear() {
        p.clear();
        return this;
    }

    @JsonIgnore
    public String getCacheKey() {
        return String.format("%s-%s-%s", getScale(), getPage(), p);
    }

    public Page param(String key, Object value) {
        p.put(key, value);
        return this;
    }
}
