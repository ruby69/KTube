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
@ToString(of = {"lang", "name"})
@JsonInclude(Include.NON_NULL)
public class IdolInfo implements Serializable {
    private static final long serialVersionUID = 952075011730189704L;

    public static enum Lang {
        ar, bg, ca, hr, zh_Hans, zh_Hant, cs, da, nl, en, fil, fi, fr, de, el, he, hi, hu, id, it, ja, ko, lv, lt, no, pl, pt, ro, ru, sr, sk, sl, es, sv, th, tr, uk, vi;

        public static String resolveCode(String lang) {
            try {
                return valueOf(lang).name();
            } catch (Exception e) {
                return en.name();
            }
        }
    }

    @JsonIgnore private Long idolInfoUid;
    @JsonIgnore private Long idolUid;
    private Lang lang;
    private String name;
    private String url;
    private String summary;

    public String getIdolKey() {
        return KeyEncryptor.getKey(KeyEncryptor.Target.IDOL, idolUid);
    }

    public void setIdolKey(String key) {
        Long uid = null;
        if (this.idolUid == null && (uid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, key)) != null) {
            this.idolUid = uid;
        }
    }

    public String getIdolInfoKey() {
        return KeyEncryptor.getKey(KeyEncryptor.Target.IDOL, idolInfoUid);
    }

    public void setIdolInfoKey(String key) {
        Long uid = null;
        if (this.idolInfoUid == null && (uid = KeyEncryptor.getUid(KeyEncryptor.Target.IDOL, key)) != null) {
            this.idolInfoUid = uid;
        }
    }
}
