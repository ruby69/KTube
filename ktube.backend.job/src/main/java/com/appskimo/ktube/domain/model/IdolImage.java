package com.appskimo.ktube.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(of = {"url"})
@JsonInclude(Include.NON_NULL)
public class IdolImage implements Serializable {
    private static final long serialVersionUID = 952075011730189704L;

    private Long idolImageUid;
    @JsonIgnore private Long idolUid;
    private String sourceLink;
    private String url;
    private int width;
    private int height;
    private String thumbUrl;
    private int thumbWidth;
    private int thumbHeight;
    private boolean mainImage;

    public IdolImage(Long idolUid, String sourceLink, String url, int width, int height, String thumbUrl, int thumbWidth, int thumbHeight) {
        this.idolUid = idolUid;
        this.sourceLink = sourceLink;
        this.url = url;
        this.width = width;
        this.height = height;
        this.thumbUrl = thumbUrl;
        this.thumbWidth = thumbWidth;
        this.thumbHeight = thumbHeight;
    }

}
