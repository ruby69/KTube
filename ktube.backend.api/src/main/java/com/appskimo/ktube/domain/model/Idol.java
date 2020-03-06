package com.appskimo.ktube.domain.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(of = {"idolUid", "searchName"})
@JsonInclude(Include.NON_NULL)
public class Idol implements Serializable {
    private static final long serialVersionUID = -5979113811686589143L;

    @JsonIgnore private Long idolUid;
    @JsonIgnore private String searchName;
    private int likeCount;
    private String tags;

    private String imageUrl;
    private String thumbUrl;
    private String name1;
    private String url1;
    private String summary1;
    private String name2;
    private String url2;
    private String summary2;

    private List<IdolImage> images;

    public Idol(Long idolUid, String searchName) {
        this.idolUid = idolUid;
        this.searchName = searchName;
    }

    public String getIdolKey() {
        return KeyEncryptor.getKey(KeyEncryptor.Target.IDOL, idolUid);
    }

    public void setIdolKey(String key) {
        Long uid = null;
        if (this.idolUid == null && (uid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, key)) != null) {
            this.idolUid = uid;
        }
    }
}
