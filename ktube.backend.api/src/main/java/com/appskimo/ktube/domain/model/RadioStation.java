package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(of = {"radioStationUid", "title"})
@JsonInclude(Include.NON_NULL)
public class RadioStation implements Serializable {
    private static final long serialVersionUID = 4345838545689470283L;

    private Long radioStationUid;
    private String title;
    private String site;
    private String stream;
    private String image;
    private Date registTime;
}
