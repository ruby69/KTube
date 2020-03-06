package com.appskimo.ktube.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Meta implements Serializable {
    private static final long serialVersionUID = -6089014773701882287L;

    @JsonIgnore private Long metaUid;
    private int androidVersion;
}
